package com.softech.test.core.lab;

import java.util.HashMap;

import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.MobileOS;

public class TestDeviceInfo {

	private static ThreadLocal<MobileOS> mobileOS = new ThreadLocal<MobileOS>();
	private static ThreadLocal<String> machineIP = new ThreadLocal<String>();
	private static ThreadLocal<String> machineName = new ThreadLocal<String>();
    private static ThreadLocal<DeviceCategory> deviceCategory = new ThreadLocal<DeviceCategory>();
    private static ThreadLocal<Integer> deviceProxyPort = new ThreadLocal<Integer>();
    private static ThreadLocal<String> deviceCode = new ThreadLocal<String>();
    private static ThreadLocal<String> deviceID = new ThreadLocal<String>();
    private static ThreadLocal<String> deviceName = new ThreadLocal<String>();
    private static ThreadLocal<String> deviceOSVersion = new ThreadLocal<String>();
    
    public static void initDeviceInfo(MobileOS mobileOS, DeviceCategory deviceCategory, String tetheredMachineName, 
    		HashMap<String, String> deviceInfo) {
        TestDeviceInfo.mobileOS.set(mobileOS);
        TestDeviceInfo.deviceCategory.set(deviceCategory);
        machineIP.set(deviceInfo.get("machine_ip"));
        machineName.set(tetheredMachineName);
        deviceProxyPort.set(Integer.parseInt(deviceInfo.get("device_proxy_port")));
        deviceCode.set(deviceInfo.get("device_code"));
        deviceID.set(deviceInfo.get("device_id"));
        deviceName.set(deviceInfo.get("device_name"));
        deviceOSVersion.set(deviceInfo.get("device_os_version"));
    }
    
    public static MobileOS getMobileOS() {
        return mobileOS.get();
    }
    
    public static DeviceCategory getDeviceCategory() {
        return deviceCategory.get();
    }
    
    public static String getTetheredMachineIP() {
        return machineIP.get();
    }
    
    public static String getTetheredMachineName() {
        return machineName.get();
    }
    
    public static Integer getDeviceProxyPort() {
        return deviceProxyPort.get();
    }
    
    public static String getDeviceCode() {
        return deviceCode.get();
    }
    
    public static String getDeviceID() {
        return deviceID.get();
    }
    
    public static String getDeviceName() {
        return deviceName.get();
    }
    
    public static String getDeviceOSVersion() {
        return deviceOSVersion.get();
    }
    
}
