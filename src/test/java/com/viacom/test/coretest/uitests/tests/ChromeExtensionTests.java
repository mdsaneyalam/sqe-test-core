package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.props.ChromeExtensions;
import com.softech.test.core.props.MQEDriverCaps;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.SleepUtils;
import com.viacom.test.coretest.common.BaseTest;
import com.viacom.test.coretest.common.util.props.IProps.ConfigProps;
import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class ChromeExtensionTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps = null;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
    }
	
    @Test(groups = { GroupProps.FULL, GroupProps.WEB })
    @Features(GroupProps.WEB)
	public void labAdBlockerTest() {
    	Logger.logMessage("Start a webdriver instance on the lab with the ad guard extension installed.");
    	DesiredCapabilities capabilities = defaultCaps.desktopWeb();
    	capabilities.setCapability(MQEDriverCaps.MQE_CHROME_EXTENSION.value(),
    			ChromeExtensions.ADGUARD_AD_BLOCKER.value());
		DriverFactory.initiateWebDriver(capabilities);
		
		Logger.logMessage("Verify the ad blocker is installed in the browser.");
		waitForExtensionTitle("Thank you for installing Adguard!");
		
		Logger.logMessage("Open a web page and ensure the browser still works as expected after ad block install.");
		DriverManager.getWebDriver().get("http://www.seleniumhq.org");
		new WebDriverWait(DriverManager.getWebDriver(), ConfigProps.MAX_WAIT_TIME)
			.until(ExpectedConditions.titleContains("Selenium"));
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopWebDriver();
	}
    
    private void waitForExtensionTitle(String extensionTitle) {
    	Boolean pluginInstalled = false;
    	Integer iter = 0;
    	while (!pluginInstalled) {
    		for (String handle : DriverManager.getWebDriver().getWindowHandles()) {
    			DriverManager.getWebDriver().switchTo().window(handle);
    			String windowTitle = DriverManager.getWebDriver().getTitle();
    			if (windowTitle.contains(extensionTitle)) { 
    				pluginInstalled = true; 
    				break; 
    			}
    		}

    		if (iter > 10) { 
    			Assert.fail("Extension with title '" + extensionTitle + "' did not install in a timely fashion!"); 
    		}

    		SleepUtils.sleep(1000);
    		iter++;
    	}
    }
    
}
