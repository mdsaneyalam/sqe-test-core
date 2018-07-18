package com.viacom.test.coretest.uitests.support;

import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.props.MobileOS;
import com.viacom.test.coretest.common.util.props.IProps.ConfigProps;

import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class DefaultCapabilityFactory {

	public DesiredCapabilities mobileNative(MobileOS mobileOS) {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // retrieve options from config file
        String platform = "MAC";
        String appPackageID = "";
        String androidLaunchActivity = "";
        
        // set desired capabilities
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, mobileOS.value());
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "null"); //required but not used
        capabilities.setCapability(CapabilityType.PLATFORM, platform);
        capabilities.setCapability(CapabilityType.BROWSER_NAME, mobileOS.value());
        String serverCommandTimeout = ConfigProps.SERVER_COMMAND_TIMEOUT;
        capabilities.setCapability(MobileCapabilityType.NEW_COMMAND_TIMEOUT, serverCommandTimeout);
        capabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, "true");
        
        // set extra capabilities needed for Android
        if (mobileOS.equals(MobileOS.ANDROID)) {
        	appPackageID = ConfigProps.MQE_HEALTH_TEST_ANDROID_APP_PACKAGE;
            androidLaunchActivity = ConfigProps.MQE_HEALTH_TEST_ANDROID_LAUNCH_ACTIVITY;
            capabilities.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, appPackageID);
            capabilities.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, androidLaunchActivity);
        }
        
        // set extra capabilities for iOS
        if (mobileOS.equals(MobileOS.IOS)) {
        	appPackageID = ConfigProps.MQE_HEALTH_TEST_IOS_BUNDLE_ID;
        	capabilities.setCapability(MobileCapabilityType.APP, appPackageID);
        	capabilities.setCapability("autoAcceptAlerts", ConfigProps.IOS_AUTO_ACCEPT_ALERTS);
            capabilities.setCapability("screenshotWaitTimeout", ConfigProps.IOS_SCREENSHOT_WAIT);
        }
        
        return capabilities;
    }
	
	public DesiredCapabilities desktopWeb() {
    	DesiredCapabilities capabilities = new DesiredCapabilities();
        
        // set the browser
        capabilities.setBrowserName(BrowserType.CHROME.value());

        // set the os/platform
        capabilities.setCapability("platform", DesktopOSType.MQE_MAC.value());
        
        // set screenshot capability
        capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);

        // initiate the driver
        com.softech.test.core.util.TestRun.setSauceRun(false);
        com.softech.test.core.util.TestRun.setLabRun(true);
        
        return capabilities;
    }
    
}
