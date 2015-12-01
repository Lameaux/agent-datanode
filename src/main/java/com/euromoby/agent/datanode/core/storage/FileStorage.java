package com.euromoby.agent.datanode.core.storage;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileStorage {

	private static final Logger log = LoggerFactory.getLogger(FileStorage.class);		
	
	public static String getUserHome() {
		return System.getProperty("user.home");
	}
	
	public void storeFile(String uploadTicket, String fileName, File file) {
		
		log.info("File uploaded. Ticket {}, fileName {} ", uploadTicket, fileName);
		
//		File targetFile = new File(new File(config.getAgentFilesPath()), location);
//		File parentDir = targetFile.getParentFile();
//		if (!parentDir.exists() && !parentDir.mkdirs()) {
//			throw new RestException("Unable to store file");
//		}
//
//		FileUtils.copyFile(tempUploadedFile, targetFile);		
	}
	
}
