package com.zhiyi.InstantChat.inter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhiyi.InstantChat.inter.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.inter.exception.InternalException;
import com.zhiyi.InstantChat.inter.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.inter.exception.UserNotExistingException;

/*
 * The class to talk with APP server.
 * 
 * TODO: Should have some strategy to authenticate instant chat server with app server.
 */
public class DefaultAppServInteractor implements AppServInteractor {
	
	private static final String APP_BASE_URL = ""; // TODO: fill it later.

	private static final String APP_AUTH_URI = ""; // TODO: fill it later.
	
	private static class AppServInteractorHolder {
		public static final DefaultAppServInteractor instance= new DefaultAppServInteractor();
	}
	
	public static DefaultAppServInteractor getInstance() {
		return AppServInteractorHolder.instance;
	}
	
	private DefaultAppServInteractor() {}
	
	// TODO: make the strategy to auth app client.
	public void authenticateAppClient(Long uid, String deviceId, String secToken)
			throws InternalException, DeviceNotExistingException,
			UserNotExistingException, InvalidSecTokenException {
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
			if (code != HttpStatus.SC_OK) {
				// TODO: log exception
				throw new InternalException();
			}

	        HttpEntity entity = resp.getEntity();
	        String respJsonData = EntityUtils.toString(entity);
	        JSONObject json = JSON.parseObject(respJsonData);
	        
			Integer authRetCode = json.getInteger("code");
			if (authRetCode == InterErrorCode.USER_NOT_EXISTING.toInteger()) {
				throw new UserNotExistingException();
			}
			if (authRetCode == InterErrorCode.USER_NOT_EXISTING.toInteger()) {
				throw new DeviceNotExistingException();
			}
			if (authRetCode == InterErrorCode.INVALID_SEC_TOKEN.toInteger()) {
				throw new InvalidSecTokenException();
			}
	        
	        EntityUtils.consume(entity);
		} catch (UnsupportedEncodingException e) {
			// TODO: log exception
			throw new InternalException();
		} catch (ClientProtocolException e) {
			// TODO: log exception
			throw new InternalException();
		} catch (IOException e) {
			// TODO: log exception
			throw new InternalException();
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					// TODO: log exception
					throw new InternalException();
				}
			}
		}
	}
}
