package com.zhiyi.im.mockclient;

public class TestingPerfMain {

	private static final Integer TESTING_CLIENTS_NUM = 2;
	
	public static void main(String[] args) {
		LoadRunner loadRunner = new LoadRunner(TESTING_CLIENTS_NUM);
		
		// test idle clients
//		loadRunner.testIdleClientsPerf();
		
		// test active clients
		loadRunner.testActiveClientsPerf();
	}

}
