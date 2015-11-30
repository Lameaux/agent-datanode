package com.euromoby.agent.datanode.rest.handler.upload_request;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.datanode.core.http.HttpResponseProvider;
import com.euromoby.agent.datanode.rest.handler.RestHandlerBase;
import com.euromoby.agent.datanode.upload.UploadTicketService;
import com.euromoby.agent.model.UploadTicket;
import com.google.gson.Gson;

@Component
public class UploadRequestHandler extends RestHandlerBase {

	public static final String URL = "/upload/ticket";	

	private static final Gson gson = new Gson();
	
	@Autowired
	private UploadTicketService uploadTicketService; 
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		
		UploadTicket uploadTicket = uploadTicketService.generateTicket();
		
		String jsonResponse = gson.toJson(uploadTicket);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);		
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");		
		return response;
	}
}
