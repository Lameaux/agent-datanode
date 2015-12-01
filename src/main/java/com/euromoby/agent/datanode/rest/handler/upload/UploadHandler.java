package com.euromoby.agent.datanode.rest.handler.upload;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.datanode.core.http.HttpResponseProvider;
import com.euromoby.agent.datanode.core.http.HttpUtils;
import com.euromoby.agent.datanode.core.storage.FileStorage;
import com.euromoby.agent.datanode.rest.RestException;
import com.euromoby.agent.datanode.rest.handler.RestHandlerBase;
import com.euromoby.agent.datanode.upload.UploadTicketService;
import com.euromoby.agent.utils.Lists;


@Component
public class UploadHandler extends RestHandlerBase {

	public static final String URL = "/upload";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/([0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12})");

	private static final String REQUEST_INPUT_FILENAME = "filename";
	private static final String REQUEST_INPUT_FILE = "file";

	@Autowired
	private FileStorage fileStorage;
	
	@Autowired
	private UploadTicketService uploadTicketService;
	
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
		String uploadTicketId = m.group(1);
		
		if (!uploadTicketService.validateTicketId(uploadTicketId)) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");			
		}
		
		String fileName = Lists.getFirst(postParameters.get(REQUEST_INPUT_FILENAME));
		File tempUploadedFile = uploadFiles.get(REQUEST_INPUT_FILE);

		if (tempUploadedFile == null) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_FILE);
		}

		fileStorage.storeFile(uploadTicketId, fileName, tempUploadedFile);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}


}
