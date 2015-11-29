package com.euromoby.agent.datanode.rest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.euromoby.agent.datanode.core.http.ReadWriteTimeoutHandler;
import com.euromoby.agent.datanode.core.http.SSLContextProvider;

@Component
public class RestServerInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SSLContextProvider sslContextProvider;	
	private final RestMapper restMapper;

	@Value("${rest.timeout}")
	private int restTimeout;
	
	@Autowired
	public RestServerInitializer(SSLContextProvider sslContextProvider, RestMapper restMapper) {
		this.sslContextProvider = sslContextProvider;
		this.restMapper = restMapper;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		
		//p.addLast("ssl", new SslHandler(sslContextProvider.newServerSSLEngine()));		
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new HttpResponseEncoder());

		p.addLast("idle", new IdleStateHandler(0, 0, restTimeout));
		p.addLast("timeout", new ReadWriteTimeoutHandler());		
		
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("rest", new RestServerHandler(restMapper));
	}
}