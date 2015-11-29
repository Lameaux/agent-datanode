package com.euromoby.agent.datanode.rest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RestServer implements InitializingBean, DisposableBean {

	private static final Logger log = LoggerFactory.getLogger(RestServer.class);	
	
	@Autowired
	private RestServerInitializer initializer;
	
	@Value("${rest.port}")
	private int restPort;

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel serverChannel;

	@Override
	public void afterPropertiesSet() {
		try {

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(initializer);

			serverChannel = b.bind(restPort).sync().channel();

			log.info("RestServer started on port {}", restPort);
		} catch (Exception e) {
			log.info("Unable to start RestServer on port {}", restPort);
			destroy();
		}
	}

	@Override
	public void destroy() {

		try {
			if (serverChannel != null) {
				serverChannel.close().sync();
			}
		} catch (Exception e) {

		} finally {
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
				bossGroup = null;
			}
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
				workerGroup = null;
			}
		}

	}

}
