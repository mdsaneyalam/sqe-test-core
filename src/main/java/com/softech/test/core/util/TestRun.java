package com.softech.test.core.util;

import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.MobileOS;

public class TestRun {

	private static ThreadLocal<MobileOS> mobileOS = new ThreadLocal<MobileOS>();
	private static ThreadLocal<EmergingOS> emergingOS = new ThreadLocal<EmergingOS>();
    private static ThreadLocal<DeviceCategory> deviceCategory = new ThreadLocal<DeviceCategory>();
    private static ThreadLocal<Boolean> mobile = new ThreadLocal<Boolean>();
    private static ThreadLocal<Boolean> sauceRun = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<Boolean> labRun = new ThreadLocal<Boolean>();
    private static ThreadLocal<Boolean> iosXCUITestRun = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<Boolean> selendroidRun = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<Boolean> simulatorRun = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<Boolean> mobileWebRun = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<BrowserType> browserType = new ThreadLocal<BrowserType>();
    
    public static void setMobile(Boolean isMobile) {
        TestRun.mobile.set(isMobile);
    }
    
    public static void setSauceRun(Boolean isSauceRun) {
        TestRun.sauceRun.set(isSauceRun);
    }
    
    public static void setLabRun(Boolean isLabRun) {
        TestRun.labRun.set(isLabRun);
    }
    
    public static void setMobileOS(MobileOS mobileOS) {
        TestRun.mobileOS.set(mobileOS);
    }
    
    public static MobileOS getMobileOS() {
        return TestRun.mobileOS.get();
    }
    
    public static void setDeviceCategory(DeviceCategory deviceCategory) {
        TestRun.deviceCategory.set(deviceCategory);
    }
    
    public static DeviceCategory getDeviceCategory() {
        return deviceCategory.get();
    }
    
    public static void setSelendroidRun(Boolean isSelendroid) {
    	selendroidRun.set(isSelendroid);
    }
    
    public static Boolean isIos() {
    	return getMobileOS().equals(MobileOS.IOS);
    }
    
    public static Boolean isIosSim() {
    	return getMobileOS().equals(MobileOS.IOS_SIM);
    }
    
    public static void setXCUITest(Boolean isXCUITest) {
    	iosXCUITestRun.set(isXCUITest);
    }
    
    public static Boolean isXCUITest() {
    	return iosXCUITestRun.get();
    }
    
    public static void setSimulatorRun(Boolean isSimulatorRun) {
    	simulatorRun.set(isSimulatorRun);
    }
    
    public static Boolean isSimulator() {
    	return simulatorRun.get();
    }
    
    public static void setMobileWebRun(Boolean isMobileWebRun) {
    	mobileWebRun.set(isMobileWebRun);
    }
    
    public static Boolean isMobileWeb() {
    	return mobileWebRun.get();
    }
    
    public static Boolean isAndroid() {
    	return getMobileOS().equals(MobileOS.ANDROID);
    }
    
    public static Boolean isAndroidSim() {
    	return getMobileOS().equals(MobileOS.ANDROID_SIM);
    }
    
    public static Boolean isPhone() {
    	return getDeviceCategory().equals(DeviceCategory.PHONE);
    }
    
    public static Boolean isTablet() {
    	return getDeviceCategory().equals(DeviceCategory.TABLET);
    }
    
    public static Boolean isMobile() {
    	return mobile.get();
    }
    
    public static Boolean isSauceRun() {
    	return sauceRun.get();
    }
    
    public static Boolean isLabRun() {
    	return labRun.get();
    }
    
    public static Boolean isSelendroid() {
    	return selendroidRun.get();
    }
    
    public static void setBrowserType(BrowserType browserType) {
    	TestRun.browserType.set(browserType);
    }
    
    public static BrowserType getBrowserType() {
    	return browserType.get();
    }
    
    public static void setEmergingOS(EmergingOS emergingOS) {
        TestRun.emergingOS.set(emergingOS);
    }
    
    public static EmergingOS getEmergingOS() {
        return TestRun.emergingOS.get();
    }
    
    public static Boolean isAppleTV() {
        return emergingOS.get().equals(EmergingOS.APPLE_TV);
    }
    
    public static Boolean isFireTV() {
        return emergingOS.get().equals(EmergingOS.FIRE_TV);
    }
    
    public static Boolean isRoku() {
        return emergingOS.get().equals(EmergingOS.ROKU);
    }
    
}
