package com.softech.test.core.lab;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.softech.test.core.util.TestRun;

public class TimeoutCapabilities {

	public static DesiredCapabilities setMobileTimeouts(DesiredCapabilities capabilities) {
		String defaultTimeout = System.getenv("DEFAULT_APPIUM_TIMEOUTS");
		if (TestRun.isAndroid()) {
			// app wait activity
			capabilities.setCapability("appWaitDuration", defaultTimeout);
			capabilities.setCapability("deviceReadyTimeout", defaultTimeout);
		} else {
			if (TestRun.isXCUITest()) {
				capabilities.setCapability("useNewWDA", false);
				capabilities.setCapability("wdaLaunchTimeout", defaultTimeout);
				capabilities.setCapability("wdaConnectionTimeout", defaultTimeout);
				capabilities.setCapability("commandTimeouts", defaultTimeout);
				capabilities.setCapability("wdaStartupRetries", 3);
				capabilities.setCapability("shouldUseSingletonTestManager", false);
			} else {
				capabilities.setCapability("launchTimeout", Integer.parseInt(defaultTimeout));
			}
		}
		return capabilities;
	}
	
}
