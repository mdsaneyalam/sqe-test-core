package com.softech.test.core.lab;

import java.util.HashSet;
import java.util.Set;

public class ActiveDeviceManager {
	
	private static Set<String> activeDevices = new HashSet<String>();
	private static Set<String> inactiveDevices = new HashSet<String>();
	private static ThreadLocal<String> activeDevice = new ThreadLocal<String>();
	
	public synchronized static void addActiveDevice(String deviceID) {
		activeDevices.add(deviceID);
	}
   
	public synchronized static void removeActiveDeviceID(String deviceID) {
		activeDevices.remove(deviceID);
	}
	
	public synchronized static Set<String> getActiveDevices() {
		return activeDevices;
	}
	
	public synchronized static void setActiveDevice(String deviceID) {
		activeDevice.set(deviceID);
	}
	
	public synchronized static String getActiveDevice() {
		return activeDevice.get();
	}
	
	public synchronized static void addInactiveDevice(String deviceID) {
		inactiveDevices.add(deviceID);
	}
	
	public synchronized static Set<String> getInactiveDevices() {
		return inactiveDevices;
	}
	
}
