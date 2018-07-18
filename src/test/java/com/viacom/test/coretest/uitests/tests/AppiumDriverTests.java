package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.TestRun;
import com.viacom.test.coretest.common.BaseTest;
import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class AppiumDriverTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps = null;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
    }
	
    @Test(groups = { GroupProps.FULL, GroupProps.IOS })
    @Features(GroupProps.IOS)
	public void labiOSNativePhoneTest() {
    	Logger.logMessage("Start an iOS native instance on a phone.");
    	DriverFactory.initiateAppiumDriver(MobileOS.IOS, DeviceCategory.PHONE, defaultCaps.mobileNative(MobileOS.IOS));
		
		Logger.logMessage("Verify the driver started and is not null.");
		Assert.assertNotNull(DriverManager.getAppiumDriver());
		Assert.assertNotNull(DriverManager.getIOSDriver());
		
		Logger.logMessage("Verify it's running iOS phone device.");
		String machineIP = GridManager.getRunningSessionIP();
		String deviceID = LabDeviceManager.getDeviceID(machineIP, MobileOS.IOS);
		Assert.assertTrue(LabDeviceManager.isDeviceInUse(deviceID));
		Assert.assertEquals(LabDeviceManager.getDeviceCategory(machineIP, MobileOS.IOS), DeviceCategory.PHONE.value());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
		
		Logger.logMessage("Verify the asset was released.");
		Assert.assertFalse(LabDeviceManager.isDeviceInUse(deviceID));
	}
    
    @Test(groups = { GroupProps.FULL, GroupProps.IOS })
    @Features(GroupProps.IOS)
	public void labiOSNativeTabletTest() {
    	Logger.logMessage("Start an iOS native instance on a tablet.");
		DriverFactory.initiateAppiumDriver(MobileOS.IOS, DeviceCategory.TABLET, defaultCaps.mobileNative(MobileOS.IOS));
		
		Logger.logMessage("Verify the driver started and is not null.");
		Assert.assertNotNull(DriverManager.getAppiumDriver());
		Assert.assertNotNull(DriverManager.getIOSDriver());
		
		Logger.logMessage("Verify it's running a iOS tablet device.");
		String machineIP = GridManager.getRunningSessionIP();
		String deviceID = LabDeviceManager.getDeviceID(machineIP, MobileOS.IOS);
		Assert.assertTrue(LabDeviceManager.isDeviceInUse(deviceID));
		Assert.assertEquals(LabDeviceManager.getDeviceCategory(machineIP, MobileOS.IOS), DeviceCategory.TABLET.value());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
		
		Logger.logMessage("Verify the asset was released.");
		Assert.assertFalse(LabDeviceManager.isDeviceInUse(deviceID));
	}
    
    @Test(groups = { GroupProps.FULL, GroupProps.ANDROID })
    @Features(GroupProps.ANDROID)
	public void labAndroidNativePhoneTest() {
    	Logger.logMessage("Start an Android native instance on a phone.");
		DriverFactory.initiateAppiumDriver(MobileOS.ANDROID, DeviceCategory.PHONE, defaultCaps.mobileNative(MobileOS.ANDROID));
		
		Logger.logMessage("Verify the driver started and is not null.");
		Assert.assertNotNull(DriverManager.getAppiumDriver());
		Assert.assertNotNull(DriverManager.getAndroidDriver());
		
		Logger.logMessage("Verify it's running an Android phone device.");
		String machineIP = GridManager.getRunningSessionIP();
		String deviceID = LabDeviceManager.getDeviceID(machineIP, MobileOS.ANDROID);
		Assert.assertTrue(LabDeviceManager.isDeviceInUse(deviceID));
		Assert.assertEquals(LabDeviceManager.getDeviceCategory(machineIP, MobileOS.ANDROID), DeviceCategory.PHONE.value());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
		
		Logger.logMessage("Verify the asset was released.");
		Assert.assertFalse(LabDeviceManager.isDeviceInUse(deviceID));
	}
    
    @Test(groups = { GroupProps.FULL, GroupProps.ANDROID })
    @Features(GroupProps.ANDROID)
	public void labAndroidNativeTabletTest() {
    	Logger.logMessage("Start an Android native instance on a tablet.");
		DriverFactory.initiateAppiumDriver(MobileOS.ANDROID, DeviceCategory.TABLET, defaultCaps.mobileNative(MobileOS.ANDROID));
		
		Logger.logMessage("Verify the driver started and is not null.");
		Assert.assertNotNull(DriverManager.getAppiumDriver());
		Assert.assertNotNull(DriverManager.getAndroidDriver());
		
		Logger.logMessage("Verify it's running an Android tablet device.");
		String machineIP = GridManager.getRunningSessionIP();
		String deviceID = LabDeviceManager.getDeviceID(machineIP, MobileOS.ANDROID);
		Assert.assertTrue(LabDeviceManager.isDeviceInUse(deviceID));
		Assert.assertEquals(LabDeviceManager.getDeviceCategory(machineIP, MobileOS.ANDROID), DeviceCategory.TABLET.value());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
		
		Logger.logMessage("Verify the asset was released.");
		Assert.assertFalse(LabDeviceManager.isDeviceInUse(deviceID));
	}
    
    @Test(groups = { GroupProps.FULL, GroupProps.ANDROID })
    @Features(GroupProps.ANDROID)
	public void labAndroidNativeSelendroidTest() {
    	Logger.logMessage("Start an Android native instance for selendroid.");
    	DesiredCapabilities capabilities = defaultCaps.mobileNative(MobileOS.ANDROID);
    	capabilities.setCapability("automationName", "Selendroid");
		DriverFactory.initiateAppiumDriver(MobileOS.ANDROID, DeviceCategory.PHONE, capabilities);
		
		Logger.logMessage("Verify the selendroid driver started and is selendroid.");
		Assert.assertNotNull(DriverManager.getAppiumDriver());
		Assert.assertNotNull(DriverManager.getAndroidDriver());
		Assert.assertTrue(TestRun.isSelendroid());
		
		Logger.logMessage("Stop the driver instance.");
		DriverManager.stopAppiumDriver();
	}
    
}
