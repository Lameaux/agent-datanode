package com.euromoby.agent.datanode.core.storage;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileStorage {

	private static final String AGENT_DATA = "agent" + File.separator + "data";
	
	private static final Logger log = LoggerFactory.getLogger(FileStorage.class);		
	
	private final String dataHome;
	
	public FileStorage() {
		dataHome = getUserHome() + File.separator + AGENT_DATA;
		File dataHomeDir = new File(dataHome);
		if (!dataHomeDir.exists()) {
			dataHomeDir.mkdirs();
		}
	}
	
	public String getDataHome() {
		return dataHome;
	}
	
	private static String getUserHome() {
		return System.getProperty("user.home");
	}
	
	public void storeFile(String fileId, File tempFile) throws IOException {
		File targetFile = new File(new File(dataHome), fileId);
		FileUtils.copyFile(tempFile, targetFile);		
		log.debug("File uploaded. fileId {} ", fileId);
	}

	public File getFile(String fileId) {
		return new File(new File(dataHome), fileId);
	}	
	
}
