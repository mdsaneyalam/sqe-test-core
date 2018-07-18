package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.proxy.ProxyManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.SleepUtils;
import com.viacom.test.coretest.common.BaseTest;

import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import net.lightbody.bmp.core.har.HarEntry;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class ProxyTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
    }
	
    @Test(groups = { GroupProps.FULL, GroupProps.IOS })
    @Features(GroupProps.IOS)
	public void labNativeProxyLogHasEntriesTest() {
    	Logger.logMessage("Start an iOS native instance on a phone.");
    	DriverFactory.initiateAppiumDriver(MobileOS.IOS, DeviceCategory.PHONE, defaultCaps.mobileNative(MobileOS.IOS));
		
    	Logger.logMessage("Verify proxy is not null and has entries.");
		Assert.assertNotNull(ProxyManager.getProxyServer());
		List<HarEntry> harEntries = ProxyManager.getLogEntries();
		Assert.assertTrue(harEntries.size() > 0);
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
	}
    
    @Test(groups = { GroupProps.FULL, GroupProps.WEB })
    @Features(GroupProps.WEB)
	public void labWebProxyLogHasEntriesTest() {
    	Logger.logMessage("Start a webdriver instance on the lab.");
		DriverFactory.initiateWebDriver(defaultCaps.desktopWeb());
		
		Logger.logMessage("Open a web page.");
		DriverManager.getWebDriver().get("http://www.seleniumhq.org");
		SleepUtils.sleep(1000);
		
		Logger.logMessage("Verify proxy is not null and has entries.");
		Assert.assertNotNull(ProxyManager.getProxyServer());
		List<HarEntry> harEntries = ProxyManager.getLogEntries();
		Assert.assertTrue(harEntries.size() > 0);
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopWebDriver();
	}
    
}
