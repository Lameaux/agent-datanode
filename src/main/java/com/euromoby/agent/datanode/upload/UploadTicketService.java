package com.euromoby.agent.datanode.upload;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.euromoby.agent.model.UploadTicket;

@Component
public class UploadTicketService {

	public UploadTicket generateTicket() {
		UploadTicket ticket = new UploadTicket();
		ticket.setId(UUID.randomUUID().toString());
		return ticket;
	}
	
}
