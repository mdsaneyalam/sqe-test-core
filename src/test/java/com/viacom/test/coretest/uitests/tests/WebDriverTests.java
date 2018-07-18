package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.util.Logger;
import com.viacom.test.coretest.common.BaseTest;
import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class WebDriverTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps = null;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
    }
	
    @Test(groups = { GroupProps.FULL, GroupProps.WEB })
    @Features(GroupProps.WEB)
	public void labStartDriverTest() {
    	Logger.logMessage("Start a webdriver instance on the lab.");
		DriverFactory.initiateWebDriver(defaultCaps.desktopWeb());
		
		Logger.logMessage("Verify the driver started and is not null.");
		Assert.assertNotNull(DriverManager.getWebDriver());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopWebDriver();
	}
    
}
