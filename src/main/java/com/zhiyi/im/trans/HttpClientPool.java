package com.zhiyi.im.trans;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/*
 *  Singleton Pool of http clients.
 */
public class HttpClientPool {
	private static final int MAX_CONNECT_COUNT = 200;
	
	private static final int MAX_CONNECT_COUNT_PER_ROUTE = 200;
	
	private static CloseableHttpClient httpClient;
	
	public static CloseableHttpClient getInstance() {
		if (null == httpClient) {
			synchronized(HttpClientPool.class) {
				if (null == httpClient) {
					PoolingHttpClientConnectionManager cm =
							new PoolingHttpClientConnectionManager();
					// Increase max total connection to 200
					cm.setMaxTotal(MAX_CONNECT_COUNT);
					// Increase default max connection per route to 200
					cm.setDefaultMaxPerRoute(MAX_CONNECT_COUNT_PER_ROUTE);

					httpClient = HttpClients.custom().setConnectionManager(cm).build();
				}
			}
		}
		
		return httpClient;
	}
}