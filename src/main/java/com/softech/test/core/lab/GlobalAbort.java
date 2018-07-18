package com.softech.test.core.lab;

import com.softech.test.core.util.Logger;

public class GlobalAbort {

	private static final String GLOBAL_ABORT_MSG = "GLOBAL ABORT HAS BEEN TRIGGERED. This test "
			+ "is being terminated for expediancy. Please check loggging above and diagnose the root cause of the issue, and "
			+ "if the problem persists, please contact an MQE admin as soon as possible!";

	public static void terminateTestSuite(String message) {
		Logger.logConsoleMessage(message);
		Logger.logConsoleMessage(GLOBAL_ABORT_MSG);
		System.exit(0);
	}
}
