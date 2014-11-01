package com.zhiyi.InstantChat.trans;

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
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhiyi.InstantChat.config.InstantChatConfig;
import com.zhiyi.InstantChat.trans.exception.DeviceNotExistingException;
import com.zhiyi.InstantChat.trans.exception.InternalException;
import com.zhiyi.InstantChat.trans.exception.InvalidSecTokenException;
import com.zhiyi.InstantChat.trans.exception.UserNotExistingException;

/*
 * The class to talk with application server.
 */
public class DefaultApplicationServerTransporter implements ApplicationServerTransporter {
	
	private static final String APP_AUTH_URI = ""; // TODO: fill it later.
	
	private static final Logger logger = Logger.getLogger(DefaultApplicationServerTransporter.class);
	
	private static class AppServInteractorHolder {
		public static final DefaultApplicationServerTransporter instance= new DefaultApplicationServerTransporter();
	}
	
	public static DefaultApplicationServerTransporter getInstance() {
		return AppServInteractorHolder.instance;
	}
	
	private DefaultApplicationServerTransporter() {}
	
	public void authenticateAppClient(Long uid, String deviceId, String secToken)
			throws InternalException, DeviceNotExistingException,
			UserNotExistingException, InvalidSecTokenException {
		String authUrl = InstantChatConfig.getInstance().getApplicationServerUrl() + APP_AUTH_URI;
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
				logger.error("Application server return http error code: " + code);
				throw new InternalException();
			}

	        HttpEntity entity = resp.getEntity();
	        String respJsonData = EntityUtils.toString(entity);
	        JSONObject json = JSON.parseObject(respJsonData);
	        
			Integer authRetCode = json.getInteger("code");
			if (authRetCode == TransporterErrorCode.USER_NOT_EXISTING.toInteger()) {
				throw new UserNotExistingException();
			}
			if (authRetCode == TransporterErrorCode.USER_NOT_EXISTING.toInteger()) {
				throw new DeviceNotExistingException();
			}
			if (authRetCode == TransporterErrorCode.INVALID_SEC_TOKEN.toInteger()) {
				throw new InvalidSecTokenException();
			}
	        
	        EntityUtils.consume(entity);
		} catch (UnsupportedEncodingException e) {
			logger.error("authorize client failed!", e);
			throw new InternalException();
		} catch (ClientProtocolException e) {
			logger.error("authorize client failed!", e);
			throw new InternalException();
		} catch (IOException e) {
			logger.error("authorize client failed!", e);
			throw new InternalException();
		} finally {
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					logger.error("authorize client failed!", e);
					throw new InternalException();
				}
			}
		}
	}

	@Override
	public void sendNotificationToClient(Long uid, String deviceId) {
		// TODO Auto-generated method stub
		
	}
	
	
}
