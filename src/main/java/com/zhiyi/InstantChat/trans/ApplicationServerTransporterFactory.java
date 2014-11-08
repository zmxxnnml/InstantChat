package com.zhiyi.InstantChat.trans;

import org.apache.log4j.Logger;

public class ApplicationServerTransporterFactory {

	private static final Logger logger = 
			Logger.getLogger(ApplicationServerTransporterFactory.class);
	
	public enum ApplicationServerType {
		REAL(1),
		DEBUG(2);
		
		ApplicationServerType(Integer type) {
			this.type = type;
		}
		
		public Integer getInteger() {
			return type;
		}
		
		public static ApplicationServerType convertToEnum(Integer val) {
			switch (val) {
			case 1:
				return REAL;
			case 2:
				return DEBUG;
			default:
				return DEBUG;
			}
		}

		 Integer type;
	}
	
	public static ApplicationServerTransporter getTransporter(ApplicationServerType type) {
		
		if (type.equals(ApplicationServerType.REAL)) {
			return DefaultApplicationServerTransporter.getInstance();
		} else if (type.equals(ApplicationServerType.DEBUG)) {
			return DebugApplicationServerTransporter.getInstance();
		}
		
		logger.error("Invalid application server transporter type: " + type);
		return null;
	}
	
}
