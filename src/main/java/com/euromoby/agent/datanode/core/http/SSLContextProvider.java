package com.euromoby.agent.datanode.core.http;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SSLContextProvider implements InitializingBean {

	private SSLContext sslContext;
	
	@Value("${keystore.password}")
	private String keystorePassword;

	@Override
	public void afterPropertiesSet() throws Exception {
		initSSLContext();
	}

	protected void initSSLContext() throws Exception {
		InputStream keystoreInputStream = getKeystoreInputStream();
		try {
			KeyStore keystore = KeyStore.getInstance("JKS");
			keystore.load(keystoreInputStream, keystorePassword.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keystore, keystorePassword.toCharArray());

			TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), new TrustManager[] { tm }, null);

		} finally {
			IOUtils.closeQuietly(keystoreInputStream);
		}
	}

	private InputStream getKeystoreInputStream() throws Exception {
		ClassPathResource cpr = new ClassPathResource("agent.keystore");
		return cpr.getInputStream();
	}

	public SSLEngine newServerSSLEngine() {
		if (sslContext == null) {
			throw new IllegalStateException("SSLContext is not initialized");
		}
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		return sslEngine;
	}

	public SSLContext getSSLContext() {
		return sslContext;
	}
}