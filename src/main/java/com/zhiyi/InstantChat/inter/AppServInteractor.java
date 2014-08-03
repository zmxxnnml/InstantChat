package com.zhiyi.InstantChat.inter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

/*
 * The class to talk with APP server.
 * 
 * TODO: Should have some strategy to authenticate instant chat server with app server.
 */
public class AppServInteractor {
	
	private static final String APP_BASE_URL = ""; // TODO: fill it later.

	private static final String APP_AUTH_URI = ""; // TODO: fill it later.
	
	// TODO: make the strategy to auth app client.
	public Boolean authenticateAppClient(Long uid, String deviceId, String secToken) {
		String authUrl = APP_BASE_URL + APP_AUTH_URI;
		HttpPost httpPost = new HttpPost(authUrl);
		
		List<NameValuePair> postData = new ArrayList<NameValuePair>();
		postData.add(new BasicNameValuePair("uid", uid.toString()));
		postData.add(new BasicNameValuePair("device_id", deviceId));
		postData.add(new BasicNameValuePair("sec_token", secToken));
		
		CloseableHttpResponse resp = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(postData));
			CloseableHttpClient httpClient = HttpClientPool.getInstance();
			resp = httpClient.execute(httpPost);
			
			int code = resp.getStatusLine().getStatusCode();
			if (code != 200) {
				return false;
			}
			
			// TODO: Get the resp and return result.
			
			return true;
		} catch (UnsupportedEncodingException e) {
			// TODO: log exception
		} catch (ClientProtocolException e) {
			// TODO: log exception
		} catch (IOException e) {
			// TODO: log exception
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					// TODO: log exception
				}
			}
		}

		return false;
	}
}
