package com.euromoby.agent.datanode.core.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import com.euromoby.agent.datanode.rest.RestException;


public class HttpResponseProvider {
	
	private HttpRequest request;
	
	public HttpResponseProvider(HttpRequest request) {
		this.request = request;
	}

	public FullHttpResponse createHttpResponse() {
		return createHttpResponse(HttpResponseStatus.OK);
	}

	public FullHttpResponse createHttpResponse(HttpResponseStatus status) {
		return createHttpResponse(status, Unpooled.EMPTY_BUFFER);
	}
	
	public FullHttpResponse createHttpResponse(HttpResponseStatus status, ByteBuf buf) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

		// Decide whether to close the connection or not.
		boolean close = request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE, true) 
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
		}
		return response;
	}

	public FullHttpResponse createRedirectResponse(String location) {
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.FOUND);
		response.headers().add(HttpHeaders.Names.LOCATION, location);
		return response;
	}

    public FullHttpResponse createNotModifiedResponse() {
        FullHttpResponse response = createHttpResponse(HttpResponseStatus.NOT_MODIFIED);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");        
        HttpUtils.setDateHeader(response);
        return response;
    }	

    public FullHttpResponse createUnauthorizedResponse(String realm) {
        FullHttpResponse response = createHttpResponse(HttpResponseStatus.UNAUTHORIZED);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().set(HttpHeaders.Names.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
        return response;
    }    
    
	public FullHttpResponse errorResponse(RestException e) {
		FullHttpResponse response = createHttpResponse(e.getStatus(), HttpUtils.fromString(e.getMessage()));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}	

	public void writeResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
		Cookie cookie = null;
		String value = request.headers().get(HttpHeaders.Names.COOKIE);
		if (value != null) {
			cookie = ClientCookieDecoder.LAX.decode(value);
		}
		if (cookie != null) {
			response.headers().add(HttpHeaders.Names.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));
		}
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);

		// Close the connection after the write operation is done if necessary.
		if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}	
	
}
