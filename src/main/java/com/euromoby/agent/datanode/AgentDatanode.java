package com.euromoby.agent.datanode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class AgentDatanode {

	private static final Logger log = LoggerFactory.getLogger(AgentDatanode.class);
	private static final long SLEEP_TIME = 10000L;

	public static final void main(String args[]) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring-context.xml");
		try {
			log.info("Started");
			while (!Thread.currentThread().isInterrupted()) {
				Thread.sleep(SLEEP_TIME);
			}
		} catch (InterruptedException e) {

		} finally {
			context.close();
			log.info("Stopped");
		}
	}

}
