package com.softech.test.core.driver;

import org.openqa.selenium.remote.RemoteWebDriver;

import com.softech.test.core.lab.ActiveBrowserManager;
import com.softech.test.core.lab.ActiveDeviceManager;
import com.softech.test.core.lab.BrowserNodeManager;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.lab.ProxyUseLogger;
import com.softech.test.core.lab.SauceNodeManager;
import com.softech.test.core.lab.TestDeviceInfo;
import com.softech.test.core.props.BrowserType;
import com.softech.test.core.proxy.ProxyFactory;
import com.softech.test.core.sauce.SauceTunnelsManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.TestRun;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class DriverManager {

    private static ThreadLocal<AppiumDriver<MobileElement>> appiumDriver = new ThreadLocal<AppiumDriver<MobileElement>>();
    private static ThreadLocal<AndroidDriver<MobileElement>> androidDriver = new ThreadLocal<AndroidDriver<MobileElement>>();
    private static ThreadLocal<IOSDriver<MobileElement>> iOSDriver = new ThreadLocal<IOSDriver<MobileElement>>();
    private static ThreadLocal<RemoteWebDriver> webDriver = new ThreadLocal<RemoteWebDriver>();
    
    /**********************************************************************************************
     * Sets the Remote WebDriver instance for the running session.
     * 
     * @param driver - {@link RemoteWebDriver} - The instance of the driver as created in the DriverFactory() class.
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
    public static synchronized void setWebDriver(RemoteWebDriver driver) {
        webDriver.set(driver);
    }
    
    /**********************************************************************************************
     * Gets the Remote WebDriver instance for the running session.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return driver - The instance of the driver as created in the DriverFactory() class.
     ***********************************************************************************************/
    public static RemoteWebDriver getWebDriver() {
        return webDriver.get();
    }
    
    /**********************************************************************************************
     * Sets the Appium Driver instance for the running session.
     * 
     * @param driver - {@link AppiumDriver} - The instance of the driver as created in the DriverFactory() class.
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
    public static synchronized void setAppiumDriver(AppiumDriver<MobileElement> driver) {
        appiumDriver.set(driver);
    }
    
    /**********************************************************************************************
     * Gets the Appium Driver instance for the running session.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return driver - The instance of the driver as created in the DriverFactory() class.
     ***********************************************************************************************/
    public static AppiumDriver<MobileElement> getAppiumDriver() {
        return appiumDriver.get();
    }
    
    /**********************************************************************************************
     * Sets the Android Driver instance for the running session.
     * 
     * @param driver - {@link AndroidDriver} - The instance of the driver as created in the DriverFactory() class.
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
    public static synchronized void setAndroidDriver(AndroidDriver<MobileElement> driver) {
        androidDriver.set(driver);
    }
    
    /**********************************************************************************************
     * Gets the Android Driver instance for the running session.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return driver - The instance of the driver as created in the DriverFactory() class.
     ***********************************************************************************************/
    public static AndroidDriver<MobileElement> getAndroidDriver() {
        return androidDriver.get();
    }
    
    /**********************************************************************************************
     * Sets the IOS Driver instance for the running session.
     * 
     * @param driver - {@link IOSDriver} - The instance of the driver as created in the DriverFactory() class.
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
    public static synchronized void setIOSDriver(IOSDriver<MobileElement> driver) {
        iOSDriver.set(driver);
    }
    
    /**********************************************************************************************
     * Gets the IOS Driver instance for the running session.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return driver - The instance of the driver as created in the DriverFactory() class.
     ***********************************************************************************************/
    public static IOSDriver<MobileElement> getIOSDriver() {
        return iOSDriver.get();
    }
    
    /**********************************************************************************************
     * Stops the webdriver session.
     * 
     * @author Brandon Clark created June 23, 2016
     * @version 1.0 June 23, 2016
     ***********************************************************************************************/
    public static void stopWebDriver() {
    	String browserAddress = null;
    	BrowserType browserType = null;
    	if (GridManager.isEC2Agent()) {
    		browserAddress = ActiveBrowserManager.getActiveBrowserAddress();
    		browserType = ActiveBrowserManager.getActiveBrowserType();
    	}
    	
    	// quit the driver
    	if (DriverManager.getWebDriver() != null) {
    		if (DriverManager.getWebDriver().getSessionId() != null) {
    			try {
    	            DriverManager.getWebDriver().quit();
    	        } catch (Exception e) {
    	            Logger.logConsoleMessage("Driver failed to quit gracefully.");
    	            e.printStackTrace();
    	        }
    		}
    	}
    	
    	// stop the browser proxy
    	if (ProxyFactory.isProxyStarted()) {
    		ProxyFactory.stopProxyServer();
    	}
    	
    	// stop the sauce tunnel
    	if (TestRun.isSauceRun()) {
    		SauceTunnelsManager.closeSauceTunnel();
    	}
    	
    	// set the browser node no longer in use
    	if (TestRun.isLabRun() && GridManager.isQALabHub()) {
    		BrowserNodeManager.removeBrowserNodeTypeInUse(browserAddress, browserType);
    		BrowserNodeManager.setBrowserNodeTypeUseDuration(browserAddress, browserType, 0L);
    	}
    	
    	// set the sauce node no longer in use
    	if (TestRun.isSauceRun() && GridManager.isEC2Agent()) {
    		SauceNodeManager.setSauceNodeInUse(browserAddress, false);
    		SauceNodeManager.setSauceNodeUseDuration(browserAddress, 0L);
    		SauceNodeManager.setSauceNodeTunnelId(browserAddress, "notunnel");
    	}
    }
    
    /**********************************************************************************************
     * Stops the appium driver session and releases the device back to the active device farm.
     * 
     * @author Brandon Clark created June 23, 2016
     * @version 1.0 June 23, 2016
     ***********************************************************************************************/
    public static void stopAppiumDriver() {
    	String labDeviceMachineIP = null;
    	
    	if (TestRun.isLabRun() && GridManager.isQALabHub()) {
    		labDeviceMachineIP = LabDeviceManager.getDeviceMachineIPAddress(ActiveDeviceManager.getActiveDevice());
    		
    		// remove the active driver from the list
    		ActiveDeviceManager.removeActiveDeviceID(ActiveDeviceManager.getActiveDevice());
    		
    		// turn off the android display
    		if (TestRun.isAndroid() && ActiveDeviceManager.getActiveDevice() != null) {
    			LabDeviceManager.turnOffDisplay(labDeviceMachineIP);
    		}
    	}
    	
    	// stop the device proxy
    	if (ProxyFactory.isProxyStarted()) {
			ProxyFactory.stopProxyServer();
			if (GridManager.isEC2Agent()) {
				ProxyUseLogger.removeUseLog(TestDeviceInfo.getMobileOS(), TestDeviceInfo.getDeviceProxyPort().toString());
			}
		}
		
    	// terminate the driver session
    	if (DriverManager.getAppiumDriver() != null) {
    		if (DriverManager.getAppiumDriver().getSessionId() != null) {
    			try {
    	    		DriverManager.getAppiumDriver().quit();
    	    	} catch (Exception e) {
    	    		Logger.logConsoleMessage("Driver failed to quit gracefully.");
    	    		e.printStackTrace();
    	    	}
    		}
    	}
    	
    	// set the device no longer in use
    	if (TestRun.isLabRun() && GridManager.isQALabHub()) {
    		LabDeviceManager.setDeviceInUse(ActiveDeviceManager.getActiveDevice(), false);
    		LabDeviceManager.setDeviceUseDuration(ActiveDeviceManager.getActiveDevice(), 0L);
    	}
    }
    
}
