package com.euromoby.agent.datanode.core.http;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.euromoby.agent.datanode.core.utils.Strings;

@Component
public class HttpClientProvider implements InitializingBean, DisposableBean {

	private CloseableHttpClient httpClient;
	private SSLContextProvider sslContextProvider;

	@Value("${http.useragent}")
	private String httpUserAgent;

	@Value("${http.proxy.host}")
	private String httpProxyHost;
	@Value("${http.proxy.port}")
	private int httpProxyPort;

	@Value("${http.client.timeout}")
	private int httpClientTimeout;

	@Autowired
	public HttpClientProvider(SSLContextProvider sslContextProvider) {
		this.sslContextProvider = sslContextProvider;

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.httpClient = createHttpClient();
	}

	protected CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create().setSslcontext(sslContextProvider.getSSLContext()).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setUserAgent(httpUserAgent).build();
	}

	public RequestConfig.Builder createRequestConfigBuilder() {

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
		requestConfigBuilder.setSocketTimeout(httpClientTimeout);
		requestConfigBuilder.setConnectTimeout(httpClientTimeout);

		if (!Strings.nullOrEmpty(httpProxyHost)) {
			requestConfigBuilder.setProxy(new HttpHost(httpProxyHost, httpProxyPort));
		}
		return requestConfigBuilder;
	}

	public CloseableHttpResponse executeRequest(HttpUriRequest httpRequest) throws ClientProtocolException, IOException {
		return httpClient.execute(httpRequest);
	}

	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(httpClient);
	}

}
