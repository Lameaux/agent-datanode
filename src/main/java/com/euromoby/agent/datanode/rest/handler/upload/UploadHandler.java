package com.euromoby.agent.datanode.rest.handler.upload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Constants;
import com.euromoby.agent.datanode.core.http.HttpResponseProvider;
import com.euromoby.agent.datanode.core.storage.FileStorage;
import com.euromoby.agent.datanode.rest.RestException;
import com.euromoby.agent.datanode.rest.handler.RestHandlerBase;
import com.euromoby.agent.model.DatanodeFile;
import com.google.gson.Gson;


@Component
public class UploadHandler extends RestHandlerBase {

	private static final Gson gson = new Gson();	
	
	public static final String URL = Constants.NODE_URL_UPLOAD;
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/([0-9a-zA-Z]+)");

	private static final String REQUEST_INPUT_FILE = "file";

	@Autowired
	private FileStorage fileStorage;
	
	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}	

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws Exception {

		URI uri = new URI(request.getUri());

		Matcher m = URL_PATTERN.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		String fileId = m.group(1);
		
		File tempUploadedFile = uploadFiles.get(REQUEST_INPUT_FILE);

		if (tempUploadedFile == null) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_FILE);
		}

		fileStorage.storeFile(fileId, tempUploadedFile);

		DatanodeFile datanodeFile = new DatanodeFile();
		datanodeFile.setId(fileId);
		datanodeFile.setSize(tempUploadedFile.length());

		String jsonResponse = gson.toJson(datanodeFile);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");		
		
		return response;
	}


}
