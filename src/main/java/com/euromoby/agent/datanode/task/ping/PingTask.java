package com.euromoby.agent.datanode.task.ping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Constants;
import com.euromoby.agent.datanode.core.storage.FileStorage;
import com.euromoby.agent.http.HttpClientProvider;
import com.euromoby.agent.model.PingRequest;
import com.euromoby.agent.model.PingResponse;
import com.google.gson.Gson;

@Component
public class PingTask implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(PingTask.class);
	private static final Gson gson = new Gson();

	@Autowired
	private HttpClientProvider httpClientProvider;
	
	@Autowired
	private FileStorage fileStorage;

	@Value("${masternode.url}")
	private String masternodeUrl;

	private String pingUrl;

	@Override
	public void afterPropertiesSet() throws Exception {
		pingUrl = masternodeUrl + Constants.MASTER_URL_PING;
	}

	@Scheduled(fixedDelayString = "${ping.delay}")
	public void ping() {

		try {
			PingRequest pingRequest = createPingRequest();
			log.info("Ping {} ", pingUrl);
			PingResponse pingResponse = sendPing(pingRequest);
			log.info("Ping response: " + pingResponse.getUpdateTime());
		} catch (Exception e) {
			log.warn("Ping failed");
			if (log.isDebugEnabled()) {
				log.debug("Ping failed", e);
			}
		}

	}

	private PingResponse sendPing(PingRequest pingRequest) throws IOException {

		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder();

		StringEntity requestEntity = new StringEntity(gson.toJson(pingRequest), ContentType.APPLICATION_JSON);
		HttpUriRequest request = RequestBuilder.post(pingUrl).setConfig(requestConfigBuilder.build()).setEntity(requestEntity).build();

		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new IOException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			String json = EntityUtils.toString(entity, Consts.UTF_8);
			EntityUtils.consumeQuietly(entity);
			return gson.fromJson(json, PingResponse.class);
		} finally {
			response.close();
		}
	}

	private PingRequest createPingRequest() throws IOException {
		PingRequest request = new PingRequest();
		request.setCurrentTime(System.currentTimeMillis());

		long freeSpace = Files.getFileStore(Paths.get(fileStorage.getDataHome())).getUsableSpace();
		request.setFreeSpace(freeSpace);

		return request;
	}

}
