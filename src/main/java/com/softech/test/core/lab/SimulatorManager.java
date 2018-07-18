package com.softech.test.core.lab;

import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

public class SimulatorManager {

	private static final String ANDROID_SIM_ID = "emulator-5554";
	
	public static void startSimulator(MobileOS mobileOS, String machineIP) {
		Logger.logMessage("Starting '" + mobileOS.value() + "' simulator on '" + machineIP + "'.");
		String simStartPath = mobileOS.equals(MobileOS.ANDROID_SIM) ? Constants.ANDROID_SIM_START_PATH : Constants.IOS_SIM_START_PATH;
		CommandExecutor.execCommand("open " + simStartPath, machineIP, null);
	}
	
	public static void stopSimulator(MobileOS mobileOS, String machineIP) {
		Logger.logMessage("Stopping '" + mobileOS.value() + "' simulator on '" + machineIP + "'.");
		String simStopPath = mobileOS.equals(MobileOS.ANDROID_SIM) ? Constants.ANDROID_SIM_STOP_PATH : Constants.IOS_SIM_STOP_PATH;
		CommandExecutor.execCommand("open " + simStopPath, machineIP, null);
	}
	
	public static Boolean waitForSimulatorOnline(MobileOS mobileOS, String machineIP) {
		Boolean simOnline = false;
		
		if (mobileOS.equals(MobileOS.IOS_SIM)) {
			// TODO - come up with a better dynamic check for the ios device ready
			// the booted state now is indicated immediately as opposed to when the sim is ready
			// something like xcrun simctl list | grep 'Booted' but appropriate
			try { Thread.sleep(30000); } catch (InterruptedException e) { }
			return true;
		}
		
		for (int i = 1; i < Integer.parseInt(System.getenv("LAB_SIM_ONLINE_POLL_MAX")); i++) {
			Logger.logConsoleMessage("Checking simulator instance online status for '" + machineIP + "' attempt '" + i + "'.");
			String command = "";
			if (mobileOS.equals(MobileOS.ANDROID_SIM)) {
				command = Constants.ADB_PATH + " -s " + ANDROID_SIM_ID + " shell 'getprop sys.boot_completed'";
			}
			
			String status = CommandExecutor.execCommand(command, machineIP, null);
			
			if (status.contains("1")) {
				simOnline = true;
				try { Thread.sleep(10000); } catch (InterruptedException e) { }
				Logger.logConsoleMessage("Simulator is online and ready for tests on '" + machineIP + "'.");
				break;
			}
			try { Thread.sleep(5000); } catch (InterruptedException e) { }
		}
		
		return simOnline;
	}
	
	public static Boolean isSimulatorOnline(MobileOS mobileOS, String machineAddress) {
		String result = LabDatabaseFactory.getResults("select simulator_online from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (result == null) {
			return false;
		} else if (result.contains("t")) {
			return true;
		}
		return false;
	}
	
	public static void setSimulatorOnline(MobileOS mobileOS, String machineAddress) {
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set simulator_online = true "
				+ "where machine_ip = '" + machineAddress + "'");
	}
	
	public static  void setSimulatorOffline(MobileOS mobileOS, String machineAddress) {
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set simulator_online = false "
				+ "where machine_ip = '" + machineAddress + "'");
	}
	
	public static void setSimulatorOnlineStartTime(MobileOS mobileOS, String machineAddress, Long startTime) {
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set simulator_in_use_duration = " + startTime.toString()
		    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized Long getSimulatorOnlineStartTime(MobileOS mobileOS, String machineAddress) {
		String result = LabDatabaseFactory.getResults("select simulator_in_use_duration from " + LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" 
	        + machineAddress + "'").get(0);
		return Long.parseLong(result);
	}
	
	public static void terminateAssistantD(String machineAddress) {
		CommandExecutor.execCommand("pkill -9 assistantd", machineAddress, null);
	}
    
}
