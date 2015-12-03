package com.euromoby.agent.datanode.rest.handler.get;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Constants;
import com.euromoby.agent.datanode.core.http.FileResponse;
import com.euromoby.agent.datanode.core.http.HttpResponseProvider;
import com.euromoby.agent.datanode.core.http.HttpUtils;
import com.euromoby.agent.datanode.core.storage.FileStorage;
import com.euromoby.agent.datanode.core.storage.MimeHelper;
import com.euromoby.agent.datanode.rest.RestException;
import com.euromoby.agent.datanode.rest.handler.RestHandlerBase;

@Component
public class GetHandler extends RestHandlerBase {

	public static final String URL = Constants.NODE_URL_GET;
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/([0-9a-zA-Z]+)");

	
	@Autowired
	private FileStorage fileStorage;
	@Autowired
	private MimeHelper mimeHelper;

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public boolean isChunkedResponse() {
		return true;
	}
	
	@Override
	public void doGetChunked(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {
		URI uri = new URI(request.getUri());

		Matcher m = URL_PATTERN.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		String fileId = m.group(1);

		try {
			fileId = URLDecoder.decode(fileId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RestException("Invalid request");
		}		
		
		File targetFile = fileStorage.getFile(fileId);
		if (targetFile == null || !targetFile.exists()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
			FullHttpResponse response = httpResponseProvider.createNotModifiedResponse();
			httpResponseProvider.writeResponse(ctx, response);
        	return;			
		}
		
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		fileResponse.send(ctx, targetFile);
	}

}

