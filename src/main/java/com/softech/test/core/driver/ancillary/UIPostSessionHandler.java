package com.softech.test.core.driver.ancillary;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.props.MQEDriverCaps;
import com.softech.test.core.util.Logger;

import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;

public class UIPostSessionHandler {
	
  public static void handleiOSAlertsOnStart(DesiredCapabilities capabilities) {
	  if (capabilities.getCapability(MQEDriverCaps.MQE_IOS_NATIVE_AUTO_ACCEPT_ALERTS.value()) == null || 
			  capabilities.getCapability(MQEDriverCaps.MQE_IOS_NATIVE_AUTO_ACCEPT_ALERTS.value()).equals(true)) {
		  if (DriverManager.getAppiumDriver() != null) {
			  MobileElement allowButton = null;
			  try {
				  allowButton = DriverManager.getAppiumDriver().findElement(MobileBy.AccessibilityId("Allow"));
			  } catch (Exception e) {
				  // allow button not present
			  }
			  
			  if (allowButton != null) {
				  try {
					  Logger.logMessage("An 'Allow' alert is present on session spinup. This is being automatically accepted. "
					  		+ "To ignore the alert and handle at the project level, pass "
					  		+ "the '" + MQEDriverCaps.MQE_IOS_NATIVE_AUTO_ACCEPT_ALERTS.value() 
					  		+ "' capability and set the value to false.");
					  allowButton.click();
				  } catch (Exception e) {
					  Logger.logConsoleMessage("Failed to automatically dismiss the 'Allow' button on session startup.");
				  }
			  }
		  }
	  }
  }
  
  public static void handleAndroidAppiumSettingsCrashOnStart() {
	  if (DriverManager.getAppiumDriver() != null) {
		  MobileElement appiumCrashTxt = null;
		  try {
			  appiumCrashTxt = DriverManager.getAppiumDriver().findElement(MobileBy
					  .AndroidUIAutomator("textContains(\"Appium Settings has stopped\")"));
		  } catch (Exception e) {
			  // crash text not present
		  }
		  
		  if (appiumCrashTxt != null) {
			  try {
				  Logger.logMessage("Appium settings app crashed on startup. Attempting to dismiss the alert and continue the test.");
				  DriverManager.getAppiumDriver().findElement(MobileBy
						  .AndroidUIAutomator("text(\"OK\")")).click();
			  } catch (Exception e) {
				  Logger.logConsoleMessage("Failed to dismiss appium settings crash alert.");
			  }
		  }
	  }
  }
  
}
