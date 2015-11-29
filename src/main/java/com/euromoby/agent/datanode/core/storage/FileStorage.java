package com.euromoby.agent.datanode.core.storage;

import org.springframework.stereotype.Component;

@Component
public class FileStorage {

	public static String getUserHome() {
		return System.getProperty("user.home");
	}
	
}
