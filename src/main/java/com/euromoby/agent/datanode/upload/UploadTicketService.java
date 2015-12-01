package com.euromoby.agent.datanode.upload;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.euromoby.agent.model.UploadTicket;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class UploadTicketService {

	private final Cache<String, UploadTicket> tickets;
	
	public UploadTicketService() {
		tickets = CacheBuilder.newBuilder()
				.maximumSize(10000)
			    .expireAfterWrite(30, TimeUnit.MINUTES).build();		
	}
	
	public boolean validateTicketId(String ticketId) {
		return tickets.getIfPresent(ticketId) != null;
	}
	
	public UploadTicket generateTicket() {
		UploadTicket ticket = new UploadTicket();
		ticket.setId(UUID.randomUUID().toString());
		tickets.put(ticket.getId(), ticket);
		return ticket;
	}
	
}
