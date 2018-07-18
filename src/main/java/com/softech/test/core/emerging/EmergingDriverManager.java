package com.softech.test.core.emerging;

import com.softech.test.core.lab.ActiveDeviceManager;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.proxy.ProxyManager;

public class EmergingDriverManager {

	private static ThreadLocal<EmergingOS> emergingOS = new ThreadLocal<EmergingOS>();
	private static ThreadLocal<String> emergingAppID = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingDeviceID = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingMachineIP = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingDeviceIP = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingDeviceUsername = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingDevicePassword = new ThreadLocal<String>();
	private static ThreadLocal<String> emergingHarmonyDeviceID = new ThreadLocal<String>();
	private static ThreadLocal<String> harmonyHubIP = new ThreadLocal<String>();
	private static ThreadLocal<String> launchActivity = new ThreadLocal<String>();
	private static ThreadLocal<String> pathToADB = new ThreadLocal<String>();
	private static ThreadLocal<GatewayIP> targetGateway = new ThreadLocal<GatewayIP>();
    
	public static void setEmergingOS(EmergingOS emergingOS) {
        EmergingDriverManager.emergingOS.set(emergingOS);
    }
    
	public static EmergingOS getEmergingOS() {
        return emergingOS.get();
    }
	
    public static void setDeviceId(String deviceId) {
        EmergingDriverManager.emergingDeviceID.set(deviceId);
    }
    
    public static String getDeviceId() {
        return emergingDeviceID.get();
    }
    
    public static void setAppId(String appId) {
        EmergingDriverManager.emergingAppID.set(appId);
    }
    
    public static String getAppId() {
        return emergingAppID.get();
    }
    
    public static void setMachineIP(String machineIP) {
        EmergingDriverManager.emergingMachineIP.set(machineIP);
    }
    
    public static String getMachineIP() {
        return emergingMachineIP.get();
    }
    
    public static void setDeviceUsername(String username) {
        EmergingDriverManager.emergingDeviceUsername.set(username);
    }
    
    public static String getDeviceUsername() {
        return emergingDeviceUsername.get();
    }
    
    public static void setDevicePassword(String password) {
        EmergingDriverManager.emergingDevicePassword.set(password);
    }
    
    public static String getDevicePassword() {
        return emergingDevicePassword.get();
    }
    
    public static void setDeviceIP(String deviceIP) {
        EmergingDriverManager.emergingDeviceIP.set(deviceIP);
    }
    
    public static String getDeviceIP() {
        return emergingDeviceIP.get();
    }
    
    public static void setHarmonyDeviceId(String harmonyDeviceId) {
        EmergingDriverManager.emergingHarmonyDeviceID.set(harmonyDeviceId);
    }
    
    public static String getHarmonyDeviceId() {
        return emergingHarmonyDeviceID.get();
    }
    
    public static void setHarmonyHubHubIP(String ipAddress) {
        harmonyHubIP.set(ipAddress);
    }
    
    public static String getHubHarmonyIP() {
        return harmonyHubIP.get();
    }
    
    public static void setLaunchActivity(String activityName) {
    	launchActivity.set(activityName);
    }
    
    public static String getLaunchActivity() {
    	return launchActivity.get();
    }
    
    public static void setADBPath(String adbPath) {
    	pathToADB.set(adbPath);
    }
    
    public static String getADBPath() {
    	return pathToADB.get();
    }
    
    public static void setTargetGateway(GatewayIP gatewayIP) {
    	targetGateway.set(gatewayIP);
    }
    
    public static GatewayIP getTargetGateway() {
    	return targetGateway.get();
    }
    
    public static void stopDriver() {
    	if (GridManager.isQALabHub()) {
    		// remove the active driver from the list
    		ActiveDeviceManager.removeActiveDeviceID(ActiveDeviceManager.getActiveDevice());
    		
    		// set the device no longer in use
    		LabDeviceManager.setDeviceInUse(getEmergingOS(), ActiveDeviceManager.getActiveDevice(), false);
    	}
    	
    	// stop the device proxy
		if (ProxyManager.getProxyServer() != null) {
			ProxyManager.stopProxyServer();
		}
    }
	
}
