package com.zhiyi.InstantChat.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.zhiyi.InstantChat.trans.ApplicationServerTransporterFactory.ApplicationServerType;

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
	
	private static final String MAX_LOGIC_THREAD_NUM_KEY = "inst.maxLogicThreadNum";
	
	private static final String APPLICATION_SERVER_TYPE_KEY = "inst.applicationServerType";
	
	// Mongodb server address.
	private String mongoDbAddr = "localhost";
	
	// Mongodb port.
	private Integer mongoDbPort = 27017;
	
	// Netty server port.
	private Integer serverPort = 23104;
	
	// Shut down the connection if the connection can't authorize
	// after ${connectionUnauthorizedDeadline}
	private Integer connectionUnauthorizedDeadline = 30;  // seconds
	
	// Shut down the connection if the server don't receive any packet
	// from the connection after ${connectionUnactiveDeadline}
	private Integer connectionUnactiveDeadline = 30;  // seconds
	
	// The application server base url.
	private String applicationServerUrl = "";
	
	// The time interval of checking client health.
	private Integer sessionScanInterval = 60;
	
	// Number of threads used to handle all requests.
	private Integer maxLogicThreadNum = 50;
	
	// The type{DEBUG/REAL} of application server which our server connects to.
	private ApplicationServerType applicationServerType = ApplicationServerType.DEBUG;
	
	private InstantChatConfig() {
		reloadConfig();
	}
	
	private static class ClientMgrHolder {
		public static final InstantChatConfig instance= new InstantChatConfig();
	}
	
	public void preload() {
		// do nothing.
		// Just for avoid lazy loading configuration.
	}
	
	public static InstantChatConfig getInstance() {
		return ClientMgrHolder.instance;
	}

	private void reloadConfig() {
		String deploy = "dev";
		
		Properties deployProperties = new Properties();
		InputStream inputStream1 = this.getClass().getClassLoader().getResourceAsStream(
				"com/zhiyi/InstantChat/config/deploy.properties");
		if (inputStream1 != null) {
			try {
				deployProperties.load(inputStream1);
				String deployVal = deployProperties.getProperty(DEPLOY_KEY);
				if (deployVal != null) {
					deploy = deployVal;
				}
			} catch (IOException e1) {
				logger.error("Load deploy properties failed!", e1);
			}
		}
		
		String globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-dev.properties";
		if (deploy == "prod") {
			globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-prod.properties";
		} else if (deploy == "local") {
			globalConfigPath = "com/zhiyi/InstantChat/config/IntantChat-local.properties";
		}
		
		Properties instantChatProperties = new Properties();
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(globalConfigPath);
		if (inputStream != null) {
			try {
				instantChatProperties.load(inputStream);
				String serverPortVal = instantChatProperties
						.getProperty(SERVER_PORT_KEY);
				if (serverPortVal != null) {
					setServerPort(Integer.parseInt(serverPortVal));
				}

				String mongoDbAddrVal = instantChatProperties
						.getProperty(MONGODB_ADDR_KEY);
				if (mongoDbAddrVal != null) {
					setMongoDbAddr(mongoDbAddrVal);
				}

				String mongoDbPortVal = instantChatProperties
						.getProperty(MONGODB_PORT_KEY);
				if (mongoDbPortVal != null) {
					setMongoDbPort(Integer.parseInt(mongoDbPortVal));
				}

				String connectionUnauthorizedDeadlineVal = instantChatProperties
						.getProperty(CONN_UNAUTHORIZED_DEADLINE_KEY);
				if (connectionUnauthorizedDeadlineVal != null) {
					setConnectionUnauthorizedDeadline(Integer
							.parseInt(connectionUnauthorizedDeadlineVal));
				}

				String connectionUnactiveDeadlineVal = instantChatProperties
						.getProperty(CONN_UNACTIVE__DEADLINE_KEY);
				if (connectionUnactiveDeadlineVal != null) {
					setConnectionUnactiveDeadline(Integer
							.parseInt(connectionUnactiveDeadlineVal));
				}

				String applicationServerUrlVal = instantChatProperties
						.getProperty(APPLICATION_SERVER_URL_KEY);
				if (applicationServerUrlVal != null) {
					setApplicationServerUrl(applicationServerUrlVal);
				}

				String sessionScanIntervalVal = instantChatProperties
						.getProperty(SESSION_SCAN_INTERVAL_KEY);
				if (sessionScanIntervalVal != null) {
					setSessionScanInterval(Integer
							.parseInt(sessionScanIntervalVal));
				}
				
				String maxLogicThreadNumVal =
						instantChatProperties.getProperty(MAX_LOGIC_THREAD_NUM_KEY);
				if (maxLogicThreadNumVal != null) {
					setMaxLogicThreadNum(Integer.parseInt(maxLogicThreadNumVal));
				}
				
				String applicationServerTypeVal =
						instantChatProperties.getProperty(APPLICATION_SERVER_TYPE_KEY);
				if (applicationServerTypeVal != null) {
					Integer typeVal = Integer.parseInt(applicationServerTypeVal);
					applicationServerType = ApplicationServerType.convertToEnum(typeVal);
				}
			} catch (IOException e) {
				logger.error("Load global properties failed!", e);
			}
		}

	}

	public String getMongoDbAddr() {
		return mongoDbAddr;
	}

	public void setMongoDbAddr(String mongoDbAddr) {
		this.mongoDbAddr = mongoDbAddr;
	}

	public Integer getMongoDbPort() {
		return mongoDbPort;
	}

	public void setMongoDbPort(Integer mongoDbPort) {
		this.mongoDbPort = mongoDbPort;
	}

	public Integer getServerPort() {
		return serverPort;
	}

	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}

	public Integer getConnectionUnauthorizedDeadline() {
		return connectionUnauthorizedDeadline;
	}

	public void setConnectionUnauthorizedDeadline(
			Integer connectionUnauthorizedDeadline) {
		this.connectionUnauthorizedDeadline = connectionUnauthorizedDeadline;
	}

	public Integer getConnectionUnactiveDeadline() {
		return connectionUnactiveDeadline;
	}

	public void setConnectionUnactiveDeadline(Integer connectionUnactiveDeadline) {
		this.connectionUnactiveDeadline = connectionUnactiveDeadline;
	}

	public String getApplicationServerUrl() {
		return applicationServerUrl;
	}

	public void setApplicationServerUrl(String applicationServerUrl) {
		this.applicationServerUrl = applicationServerUrl;
	}

	public Integer getSessionScanInterval() {
		return sessionScanInterval;
	}

	public void setSessionScanInterval(Integer sessionScanInterval) {
		this.sessionScanInterval = sessionScanInterval;
	}

	public Integer getMaxLogicThreadNum() {
		return maxLogicThreadNum;
	}

	public void setMaxLogicThreadNum(Integer maxLogicThreadNum) {
		this.maxLogicThreadNum = maxLogicThreadNum;
	}

	public ApplicationServerType getApplicationServerType() {
		return applicationServerType;
	}

	public void setApplicationServerType(ApplicationServerType applicationServerType) {
		this.applicationServerType = applicationServerType;
	}
	
}
