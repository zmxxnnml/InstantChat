package com.zhiyi.InstantChat.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

// singleton and lazy load configuration.
public class InstantChatConfig {
	
	private static final Logger logger = Logger.getLogger(InstantChatConfig.class);

	private static final String DEPLOY_KEY = "inst.deploy";
	
	private static final String SERVER_PORT_KEY = "inst.serverPort";
	
	private static final String MONGODB_ADDR_KEY = "inst.mongoDbAddr";
	
	private static final String MONGODB_PORT_KEY = "inst.mongoDbPort";
	
	private static final String CONN_UNAUTHORIZED_DEADLINE_KEY = "inst.connectionUnauthorizedDeadline";
	
	private static final String CONN_UNACTIVE__DEADLINE_KEY = "inst.connectionUnactiveDeadline";
	
	private static final String APPLICATION_SERVER_URL_KEY = "inst.applicationServerUrl";
	
	private static final String SESSION_SCAN_INTERVAL_KEY = "inst.sessionScanInterval";
	
	// Mongodb server address.
	private String mongoDbAddr = "localhost";
	
	// Mongodb port.
	private Integer mongoDbPort = 27017;
	
	// Server port.
	private Integer serverPort = 23104;
	
	// Shut down the connection if the connection can't authorize
	// after ${connectionUnauthorizedDeadline}
	private Integer connectionUnauthorizedDeadline = 30;  // seconds
	
	// Shut down the connection if the server don't receive any packet
	// from the connection after ${connectionUnactiveDeadline}
	private Integer connectionUnactiveDeadline = 30;  // seconds
	
	private String applicationServerUrl = "";
	
	private Integer sessionScanInterval = 60;
	
	private InstantChatConfig() {
		reloadConfig();
	}
	
	private static class ClientMgrHolder {
		public static final InstantChatConfig instance= new InstantChatConfig();
	}
	
	public static InstantChatConfig getInstance() {
		return ClientMgrHolder.instance;
	}

	private void reloadConfig() {
		String deploy = "dev";
		
		Properties deployProperties = new Properties();
		InputStream inputStream1 = this.getClass().getClassLoader().getResourceAsStream(
				"com/zhiyi/InstantChat/config/deploy.properties");
		try {
			deployProperties.load(inputStream1);
			String deployVal = deployProperties.getProperty(DEPLOY_KEY);
			if (deployVal != null) {
				deploy = deployVal;
			}
		} catch (IOException e1) {
			logger.error("Load deploy properties failed!", e1);
		}
		
		String globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-dev.properties";
		if (deploy == "prod") {
			globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-prod.properties";
		} else if (deploy == "local") {
			globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-local.properties";
		}
		
		Properties instantChatProperties = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(globalConfigPath);
		try {
			instantChatProperties.load(inputStream);
			String serverPortVal = instantChatProperties.getProperty(SERVER_PORT_KEY);
			if (serverPortVal != null) {
				serverPort = Integer.parseInt(serverPortVal);
			}

			String mongoDbAddrVal = instantChatProperties
					.getProperty(MONGODB_ADDR_KEY);
			if (mongoDbAddrVal != null) {
				mongoDbAddr = mongoDbAddrVal;
			}

			String mongoDbPortVal = instantChatProperties
					.getProperty(MONGODB_PORT_KEY);
			if (mongoDbPortVal != null) {
				mongoDbPort = Integer.parseInt(mongoDbPortVal);
			}

			String connectionUnauthorizedDeadlineVal = instantChatProperties
					.getProperty(CONN_UNAUTHORIZED_DEADLINE_KEY);
			if (connectionUnauthorizedDeadlineVal != null) {
				connectionUnauthorizedDeadline = Integer
						.parseInt(connectionUnauthorizedDeadlineVal);
			}

			String connectionUnactiveDeadlineVal = instantChatProperties
					.getProperty(CONN_UNACTIVE__DEADLINE_KEY);
			if (connectionUnactiveDeadlineVal != null) {
				connectionUnactiveDeadline = Integer
						.parseInt(connectionUnactiveDeadlineVal);
			}

			String applicationServerUrlVal = instantChatProperties
					.getProperty(APPLICATION_SERVER_URL_KEY);
			if (applicationServerUrlVal != null) {
				applicationServerUrl = applicationServerUrlVal;
			}
			
			String sessionScanIntervalVal = instantChatProperties.getProperty(SESSION_SCAN_INTERVAL_KEY);
			if (sessionScanIntervalVal != null) {
				sessionScanInterval = Integer.parseInt(sessionScanIntervalVal);
			}
		} catch (IOException e) {
			logger.error("Load global properties failed!", e);
		}

	}

	public String getMongoDbAddr() {
		return mongoDbAddr;
	}
	
	public Integer getServerPort() {
		return serverPort;
	}
	
	public Integer getMongoDbPort() {
		return mongoDbPort;
	}
	
	public Integer getConnectionUnauthorizedDeadline() {
		return connectionUnauthorizedDeadline;
	}
	
	public Integer getConnectionUnactiveDeadline() {
		return connectionUnactiveDeadline;
	}
	
	public String getApplicationServerUrl() {
		return applicationServerUrl;
	}
	
	public Integer getSessionScanInterval() {
		return sessionScanInterval;
	}
	
}
