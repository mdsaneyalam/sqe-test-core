package com.softech.test.core.lab;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;

public class LabDeviceManager {

	public static HashMap<String, String> getIOSDeviceInfo(String deviceID) {
		return LabDatabaseFactory.getIOSDeviceInfo(deviceID);
	}

	public static HashMap<String, String> getAndroidDeviceInfo(MobileOS mobileOS, String deviceID) {
		return LabDatabaseFactory.getAndroidDeviceInfo(mobileOS, deviceID);
	}

	public static HashMap<String, String> getRokuDeviceInfo(String deviceID) {
		return LabDatabaseFactory.getRokuDeviceInfo(deviceID);
	}

	public static HashMap<String, String> getAppleTVDeviceInfo(String deviceID) {
		return LabDatabaseFactory.getAppleTVDeviceInfo(deviceID);
	}

	public static HashMap<String, String> getFireTVDeviceInfo(String deviceID) {
		return LabDatabaseFactory.getFireTVDeviceInfo(deviceID);
	}

	public static String getNodeGridIP(String machineAddress, MobileOS mobileOS) {
		String result = LabDatabaseFactory.getResults("select grid_ip from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + machineAddress + "'").get(0);

		return result;
	}

	public static GatewayIP getMachineGatewayIP(String machineIP) {
		String gatewayIP = LabDatabaseFactory
				.getResults("select gateway_ip from agentmachines where machine_ip = '" + machineIP + "'").get(0);
		return GatewayIP.getEnumByString(gatewayIP);
	}

	public static GatewayIP getDeviceGatewayIP(EmergingOS emergingOS, String deviceID) {
		String gatewayIP = LabDatabaseFactory.getResults("select gateway_ip from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where device_id = '" + deviceID + "'").get(0);
		return GatewayIP.getEnumByString(gatewayIP);
	}

	/**********************************************************************************************
	 * Gets the device ID of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The device ID of the currently running device.
	 ***********************************************************************************************/
	public static String getDeviceID(String sessionIPAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_id from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the device name of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The device name of the currently running device.
	 ***********************************************************************************************/
	public static String getDeviceName(String sessionIPAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_name from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	/**********************************************************************************************
	 * (IOS ONLY) Gets the device code of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @author Brandon Clark created February 18, 2016
	 * @version 1.0 February 18, 2016
	 * @return String - The device code of the currently running device.
	 ***********************************************************************************************/
	public static String getDeviceCode(String sessionIPAddress) {
		return LabDatabaseFactory.getResults("select device_code from " + LabDatabaseFactory.getOSTable(MobileOS.IOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the device OS version of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The device OS version of the currently running device.
	 ***********************************************************************************************/
	public static String getDeviceOSVersion(String sessionIPAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_os_version from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	public static String getDeviceOSVersion(String deviceID, EmergingOS emergingOS) {
		return LabDatabaseFactory.getResults("select device_os_version from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where device_id = '" + deviceID + "'").get(0);
	}

	public static String getHarmonyDeviceId(String deviceId, EmergingOS emergingOS) {
		return LabDatabaseFactory.getResults("select harmony_device_id from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where device_id = '" + deviceId + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the device category (Phone or Tablet) of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created June 22, 2016
	 * @version 1.0 June 22, 2016
	 * @return String - The device category (Phone or Tablet) of the currently
	 *         running device.
	 ***********************************************************************************************/
	public static String getDeviceCategory(String sessionIPAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_category from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the device proxy port of a currently running lab device.
	 * 
	 * @param sessionIPAddress
	 *            - {@link String} - The active session IP address of the webdriver
	 *            session.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The proxy port of the currently running device.
	 ***********************************************************************************************/
	public static String getDeviceProxyPort(String sessionIPAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_proxy_port from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	public static String getDeviceProxyPort(String sessionIPAddress, EmergingOS emergingOS) {
		return LabDatabaseFactory.getResults("select device_proxy_port from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where machine_ip = '" + sessionIPAddress + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets all the active device proxy ports for all lab devices belonging to a
	 * particular mobile OS. Generally used to start all the proxy servers for all
	 * the devices on suite start.
	 * 
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 March 9, 2016
	 * @return List<String> - The device proxy ports of all devices belonging to the
	 *         target mobile OS on the lab.
	 ***********************************************************************************************/
	public static List<String> getAllDeviceProxyPorts(MobileOS mobileOS) {
		String osTable = LabDatabaseFactory.getOSTable(mobileOS);
		String query = "select " + osTable + ".device_proxy_port from " + osTable + ", agentmachines where " + osTable
				+ ".machine_ip = agentmachines.machine_ip " + "and agentmachines.machine_status = 'active' and "
				+ osTable + ".device_status = 'active'";
		return LabDatabaseFactory.getResults(query);
	}

	/**********************************************************************************************
	 * NOTE - ADMIN ONLY! NOT TO BE USED AT THE PROJECT LEVEL!!!
	 * 
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created April 1, 2016
	 * @version 1.0 April 1, 2016
	 ***********************************************************************************************/
	public static void killDeviceProxyPort(Integer port) {
		CommandExecutor.setEC2CommandHop(false);
		if (OSDetector.isWindows()) {
			if (GridManager.isEC2Agent()) {
				CommandExecutor.setEC2CommandHop(false);
			}
			String pid = CommandExecutor.execCommand("cmd /c netstat -a -n -o | findstr :" + port.toString(), null,
					null);
			pid = pid.trim().split("\\r?\\n")[0];
			pid = pid.substring(pid.length() - 4, pid.length());
			if (GridManager.isEC2Agent()) {
				CommandExecutor.setEC2CommandHop(false);
			}
			CommandExecutor.execCommand("cmd /c taskkill /pid " + pid + " /F ", null, null);
		} else {
			CommandExecutor.execMultiCommand("kill -15 $( lsof -i:" + port.toString() + " -t )", null);
		}
	}

	/**********************************************************************************************
	 * Gets all the active device ids for all lab devices belonging to a particular
	 * mobile OS.
	 * 
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created June 17, 2016
	 * @version 1.0 June 17, 2016
	 * @return List<String> - The device ids of all devices belonging to the target
	 *         mobile OS on the lab.
	 ***********************************************************************************************/
	public static List<String> getAllDeviceIDs(MobileOS mobileOS) {
		String osTable = LabDatabaseFactory.getOSTable(mobileOS);
		String query = "select " + osTable + ".device_id from " + osTable + ", agentmachines where " + osTable
				+ ".machine_ip = agentmachines.machine_ip " + "and agentmachines.machine_status = 'active' and "
				+ osTable + ".device_status = 'active'";
		return LabDatabaseFactory.getResults(query);
	}

	public static List<String> getAllUnusedDeviceIDs(MobileOS mobileOS, DeviceCategory deviceCategory) {
		String osTable = LabDatabaseFactory.getOSTable(mobileOS);
		String underMaintenanceQuery = " and " + osTable + ".device_under_maintenance = false";
		String proxyBindQuery = " and " + osTable + ".proxy_port_bound_time = 0";
		if (DriverFactory.isMaintenanceCheck()) {
			underMaintenanceQuery = "";
			proxyBindQuery = "";
		}

		String query = "select " + osTable + ".device_id from " + osTable + ", agentmachines where " + osTable
				+ ".machine_ip = agentmachines.machine_ip " + "and agentmachines.machine_status = 'active' and "
				+ osTable + ".device_status = 'active' " + "and " + osTable + ".device_in_use = false and " + osTable
				+ ".unhealthy_during_test = false " + "and " + osTable + ".device_category = '" + deviceCategory.value()
				+ "'" + underMaintenanceQuery + proxyBindQuery;
		return LabDatabaseFactory.getResults(query);
	}

	public static List<String> getAllDeviceIDs(EmergingOS emergingOS) {
		String osTable = LabDatabaseFactory.getOSTable(emergingOS);
		String query = "select device_id from " + osTable + " where device_status = 'active'";
		return LabDatabaseFactory.getResults(query);
	}

	public static List<String> getAllInactiveDeviceIDs(EmergingOS emergingOS) {
		String osTable = LabDatabaseFactory.getOSTable(emergingOS);
		String query = "select device_id from " + osTable + " where device_status = 'inactive'";
		return LabDatabaseFactory.getResults(query);
	}

	public static List<String> getAllInactiveDeviceIDs(MobileOS mobileOS) {
		String osTable = LabDatabaseFactory.getOSTable(mobileOS);
		String query = "select device_id from " + osTable + " where device_status = 'inactive'";
		return LabDatabaseFactory.getResults(query);
	}

	/**********************************************************************************************
	 * Gets all the active lab agent machine IP's. Generally used to do global
	 * activities on suite start like installing an app on every device on every
	 * agent on the lab.
	 * 
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 March 15, 2016
	 * @return List<String> - The ip's of every active agent machine on the lab.
	 ***********************************************************************************************/
	public static List<String> getAllMachineIPAddresses() {
		String query = "select machine_ip from agentmachines where machine_status = 'active'";
		return LabDatabaseFactory.getResults(query);
	}

	/**********************************************************************************************
	 * Gets all the inactive lab agent machine IP's. NOTE - ADMIN USE ONLY AND NOT
	 * TO BE USED AT THE PROJECT LEVEL!!!
	 * 
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 March 15, 2016
	 * @return List<String> - The ip's of every inactive agent machine on the lab.
	 ***********************************************************************************************/
	public static List<String> getAllInactiveMachineIPAddresses() {
		String query = "select machine_ip from agentmachines where machine_status = 'inactive'";
		return LabDatabaseFactory.getResults(query);
	}

	/**********************************************************************************************
	 * Gets the machine ip address that a device belongs to.
	 * 
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The ip of the lab machine the device is tethered to.
	 ***********************************************************************************************/
	public static String getDeviceMachineIPAddress(String deviceId) {
		return LabDatabaseFactory.getResults("select machine_ip from "
				+ LabDatabaseFactory.getOSTable(getDeviceOS(deviceId)) + " where device_id = '" + deviceId + "'")
				.get(0);
	}

	public static String getDeviceMachineIPAddress(EmergingOS emergingOS, String deviceId) {
		return LabDatabaseFactory.getResults("select machine_ip from " + LabDatabaseFactory.getOSTable(emergingOS)
				+ " where device_id = '" + deviceId + "'").get(0);
	}

	public static String getDeviceIPAddress(EmergingOS emergingOS, String deviceId) {
		return LabDatabaseFactory.getResults("select device_ip from " + LabDatabaseFactory.getOSTable(emergingOS)
				+ " where device_id = '" + deviceId + "'").get(0);
	}

	public static String getDeviceUsername(EmergingOS emergingOS, String deviceId) {
		return LabDatabaseFactory.getResults("select device_username from " + LabDatabaseFactory.getOSTable(emergingOS)
				+ " where device_id = '" + deviceId + "'").get(0);
	}

	public static String getDevicePassword(EmergingOS emergingOS, String deviceId) {
		return LabDatabaseFactory.getResults("select device_password from " + LabDatabaseFactory.getOSTable(emergingOS)
				+ " where device_id = '" + deviceId + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the machine name that a device belongs to.
	 * 
	 * @author Brandon Clark created April 13, 2016
	 * @version 1.0 April 13, 2016
	 * @return String - The machine name of the lab machine the device is tethered
	 *         to.
	 ***********************************************************************************************/
	public static String getDeviceMachineName(String deviceId) {
		String osTable = LabDatabaseFactory.getOSTable(getDeviceOS(deviceId));
		String query = "select agentmachines.machine_name from agentmachines, " + osTable + " where " + osTable
				+ ".machine_ip = agentmachines.machine_ip and " + osTable + ".device_id = '" + deviceId + "'";
		return LabDatabaseFactory.getResults(query).get(0);
	}

	public static String getHarmonyHubIPAddress(EmergingOS emergingOS) {
		String query = "select hub_ip from harmonyhubs where hub_os_type = '" + emergingOS.value() + "'";
		return LabDatabaseFactory.getResults(query).get(0);
	}

	/**********************************************************************************************
	 * Gets the machine name.
	 * 
	 * @author Brandon Clark created November 12, 2016
	 * @version 1.0 November 12, 2016
	 * @return String - The machine name of the lab machine.
	 ***********************************************************************************************/
	public static String getMachineName(String machineIP) {
		return LabDatabaseFactory
				.getResults("select machine_name from agentmachines where machine_ip = '" + machineIP + "'").get(0);
	}

	public static String getMachineIP(String machineName) {
		return LabDatabaseFactory
				.getResults("select machine_ip from agentmachines where machine_name = '" + machineName + "'").get(0);
	}

	/**********************************************************************************************
	 * Gets the device mobile os that a device belongs to.
	 * 
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return String - The os type of the device.
	 ***********************************************************************************************/
	public static MobileOS getDeviceOS(String deviceId) {
		MobileOS mobileOS = null;
		if (!LabDatabaseFactory.getResults("select * from iosdevices where device_id = '" + deviceId + "'").isEmpty()) {
			mobileOS = MobileOS.IOS;
		} else if (!LabDatabaseFactory.getResults("select * from androiddevices where device_id = '" + deviceId + "'")
				.isEmpty()) {
			mobileOS = MobileOS.ANDROID;
		} else if (!LabDatabaseFactory.getResults("select * from iossimulators where device_id = '" + deviceId + "'")
				.isEmpty()) {
			mobileOS = MobileOS.IOS_SIM;
		} else if (!LabDatabaseFactory
				.getResults("select * from androidsimulators where device_id = '" + deviceId + "'").isEmpty()) {
			mobileOS = MobileOS.ANDROID_SIM;
		}
		return mobileOS;
	}

	/**********************************************************************************************
	 * ADMINISTRATOR ONLY!!! Reboots a device on the lab.
	 * 
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to restart.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 April 27, 2016
	 ***********************************************************************************************/
	public static void rebootDevice(String deviceID) {
		Logger.logConsoleMessage("Rebooting device with id '" + deviceID + "'.");
		MobileOS mobileOS = getDeviceOS(deviceID);
		if (mobileOS.equals(MobileOS.ANDROID)) {
			CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " reboot",
					getDeviceMachineIPAddress(deviceID), null);
		} else {
			CommandExecutor.execCommand(
					getIDeviceDiagnosticsPath(getDeviceMachineIPAddress(deviceID)) + " -u " + deviceID + " restart",
					getDeviceMachineIPAddress(deviceID), null);
		}
	}

	public static void cleanAndroidTmpDir(String machineIP) {
		String deviceID = getDeviceID(machineIP, MobileOS.ANDROID);
		String command = Constants.ADB_PATH + " -s " + deviceID + " shell rm -rf /data/local/tmp/*";
		CommandExecutor.execCommand(command, machineIP, 60);
	}
	
	public static String getDeviceBatteryLevel(String deviceID) {
		MobileOS mobileOS = getDeviceOS(deviceID);
		if (mobileOS.equals(MobileOS.ANDROID)) {
			String output = CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " shell dumpsys battery",
					getDeviceMachineIPAddress(deviceID), null);
			return output.split("level:\\s+")[1].split("\\r?\\n")[0];
		} else {
			String output = CommandExecutor.execCommand(
					getLibimobileDeviceBinPath(getDeviceMachineIPAddress(deviceID)) + "/ideviceinfo" + " -u " + deviceID + " -q com.apple.mobile.battery",
					getDeviceMachineIPAddress(deviceID), null);
			return output.split("BatteryCurrentCapacity:\\s+")[1].split("\\r?\\n")[0];
		}
	}

	/**********************************************************************************************
	 * Uninstalls an existing app package from a device on the lab.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to uninstall
	 *            the app from.
	 * @param appPackageID
	 *            - {@link String} - The app package ID of the app to be
	 *            uninstalled.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 April 27, 2016
	 ***********************************************************************************************/
	public static void uninstallApp(String ipAddress, MobileOS mobileOS, String deviceID, String appPackageID) {
		Logger.logConsoleMessage(
				"Uninstalling app '" + appPackageID + "' on device '" + deviceID + "' " + "on '" + ipAddress + "'.");
		String command;
		if (mobileOS.equals(MobileOS.IOS)) {
			command = getIDeviceInstallerPath(ipAddress) + " -u " + deviceID + " -U " + appPackageID;
		} else {
			command = Constants.ADB_PATH + " -s " + deviceID + " uninstall " + appPackageID;
		}
		CommandExecutor.execCommand(command, ipAddress, 60);
	}

	/**********************************************************************************************
	 * Installs an app package on a device on the lab.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to uninstall
	 *            the app from.
	 * @param appPackagePath
	 *            - {@link String} - The path to the app package of the app to be
	 *            installed.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 ***********************************************************************************************/
	public static String installApp(String ipAddress, MobileOS mobileOS, String deviceID, String appPackagePath) {
		Logger.logConsoleMessage(
				"Installing app '" + appPackagePath + "' on device '" + deviceID + "' " + "on '" + ipAddress + "'.");
		String command;
		if (mobileOS.equals(MobileOS.IOS)) {
			command = getIDeviceInstallerPath(ipAddress) + " -u " + deviceID + " -i " + appPackagePath;
		} else {
			command = Constants.ADB_PATH + " -s " + deviceID + " install " + appPackagePath;
		}
		return CommandExecutor.execCommand(command, ipAddress, 120);
	}

	/**********************************************************************************************
	 * Checks if an app is already installed or not on a device on the lab.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to uninstall
	 *            the app from.
	 * @param appPackageID
	 *            - {@link String} - The app package ID of the app to be
	 *            uninstalled.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 * @return Boolean - true if the app is installed else false.
	 ***********************************************************************************************/
	public static Boolean isAppInstalled(String ipAddress, MobileOS mobileOS, String deviceID, String appPackageID) {
		String command;
		String splitter;
		if (mobileOS.equals(MobileOS.IOS)) {
			command = getIDeviceInstallerPath(ipAddress) + " -u " + deviceID + " -l | grep '" + appPackageID + "'";
			splitter = "\\r?\\n";
		} else {
			command = Constants.ADB_PATH + " -s " + deviceID + " shell 'pm list packages' | grep '" + appPackageID
					+ "'";
			splitter = "package:";
		}
		Boolean appInstalled = false;
		String result = CommandExecutor.execCommand(command, ipAddress, null);

		if (result != null) {
			List<String> appPackageParts = Arrays.asList(result.split(splitter));
			List<String> appPackages = new ArrayList<String>();

			// construct the list of packages
			for (String appPackagePart : appPackageParts) {
				if (appPackagePart.contains("com.")) {
					if (mobileOS.equals(MobileOS.IOS)) {
						appPackagePart = appPackagePart.split(",")[0];
					}
					appPackages.add(appPackagePart);
				}
			}

			// check that the app package entry exists and is an exact match
			for (String appPackage : appPackages) {
				if (appPackage.trim().equals(appPackageID)) {
					appInstalled = true;
				}
			}
		}

		Logger.logConsoleMessage("Is app '" + appPackageID + "' installed on device '" + deviceID + "' on '" + ipAddress
				+ ": " + appInstalled);
		return appInstalled;
	}

	/**********************************************************************************************
	 * Checks if a device on the lab is connected.
	 * 
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to uninstall
	 *            the app from.
	 * @author Brandon Clark created September 12, 2016
	 * @version 1.0 September 12, 2016
	 * @return Boolean - true if the device is reachable else false.
	 ***********************************************************************************************/
	public static Boolean isDeviceConnected(String deviceID) {
		String ipAddress = getDeviceMachineIPAddress(deviceID);
		MobileOS mobileOS = getDeviceOS(deviceID);
		String command;
		if (mobileOS.equals(MobileOS.IOS)) {
			command = getIDeviceInfoPath(ipAddress) + " -u " + deviceID;
		} else {
			command = Constants.ADB_PATH + " -s " + deviceID + " get-state";
		}
		Boolean deviceConnected = true;
		String result = CommandExecutor.execCommand(command, ipAddress, null);

		if (mobileOS.equals(MobileOS.IOS)) {
			if (result.toLowerCase().contains("error") || result.toLowerCase().contains("no device found")) {
				deviceConnected = false;
			}
		} else {
			if (result.contains("unknown")) {
				deviceConnected = false;
			}
		}

		Logger.logConsoleMessage("Is device '" + deviceID + "' on '" + ipAddress
				+ " connected and reachable for testing: " + deviceConnected);
		return deviceConnected;
	}

	/**********************************************************************************************
	 * Gets the installed version/build number of the app package.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device to get the
	 *            app version from.
	 * @param appPackageID
	 *            - {@link String} - The app package ID of the app to be queried.
	 * @author Brandon Clark created April 27, 2016
	 * @version 1.0 April 27, 2016
	 * @return String - The build number/version of the app package.
	 ***********************************************************************************************/
	public static String getAppPackageVersion(String ipAddress, MobileOS mobileOS, String appPackageID) {
		String command;
		String result;
		String deviceID = getDeviceID(ipAddress, mobileOS);
		if (mobileOS.equals(MobileOS.IOS)) {
			command = getIDeviceInstallerPath(ipAddress) + " -u " + deviceID + " -l | grep " + appPackageID;
			result = CommandExecutor.execCommand(command, ipAddress, null).split(", ")[1].trim().replace("\"", "");
		} else {
			command = Constants.ADB_PATH + " -s " + deviceID + " shell dumpsys package " + appPackageID
					+ " | grep versionName";
			result = CommandExecutor.execCommand(command, ipAddress, null).split("=")[1].trim();
		}

		return result;
	}

	/**********************************************************************************************
	 * ANDROID ONLY (for now) turns off the display on a device.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine
	 *            the android device is on.
	 * @author Brandon Clark created May 15, 2016
	 * @version 1.0 May 15, 2016
	 ***********************************************************************************************/
	public static void turnOffDisplay(String ipAddress) {
		String deviceID = getDeviceID(ipAddress, MobileOS.ANDROID);
		String screenState = CommandExecutor.execCommand(
				Constants.ADB_PATH + " -s " + deviceID + " shell dumpsys display | grep mScreenState", ipAddress, null);
		if (screenState.contains("ON")) {
			Logger.logConsoleMessage(
					"Turning off Android device display on device '" + deviceID + "' " + "on '" + ipAddress + "'.");
			CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " shell input keyevent KEYCODE_POWER",
					ipAddress, null);
		} else {
			Logger.logConsoleMessage("Android device display on device '" + deviceID + "' " + "on '" + ipAddress
					+ "' is already turned off.");
		}
	}

	/**********************************************************************************************
	 * ANDROID ONLY (for now) turns off all the android device displays on the
	 * device farm. Generally used in suite teardown for power consumption savings.
	 * 
	 * @author Brandon Clark created May 15, 2016
	 * @version 1.0 May 15, 2016
	 ***********************************************************************************************/
	public static void turnOffAllDisplays() {
		List<Thread> displayThreads = new ArrayList<Thread>();
		for (final String ipAddress : LabDeviceManager.getAllMachineIPAddresses()) {
			displayThreads.add(new Thread() {
				public void run() {
					if (hasOSNode(ipAddress, MobileOS.ANDROID)) {
						turnOffDisplay(ipAddress);
					}
				}
			});
		}
		for (Thread thread : displayThreads) {
			thread.start();
		}
		for (Thread thread : displayThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Logger.logConsoleMessage("Failed to turn off all Android displays.");
				e.printStackTrace();
			}
		}
	}

	/**********************************************************************************************
	 * ANDROID ONLY Unlocks the device.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine
	 *            the android device is on.
	 * @author Brandon Clark created October 28, 2016
	 * @version 1.0 October 28, 2016
	 ***********************************************************************************************/
	public static void unlockDevice(String ipAddress) {
		String deviceID = getDeviceID(ipAddress, MobileOS.ANDROID);
		String unlockStrategy = LabDatabaseFactory.getResults("select unlock_strategy from "
				+ LabDatabaseFactory.getOSTable(MobileOS.ANDROID) + " where machine_ip = '" + ipAddress + "'").get(0);

		Logger.logConsoleMessage(
				"Setting device '" + deviceID + "' orientation to portrait on machine '" + ipAddress + "'.");
		CommandExecutor.execCommand(
				Constants.ADB_PATH + " -s " + deviceID + " shell settings put system accelerometer_rotation 0",
				ipAddress, null);
		CommandExecutor.execCommand(
				Constants.ADB_PATH + " -s " + deviceID + " shell settings put system user_rotation 0", ipAddress, null);
		CommandExecutor.execCommand(
				Constants.ADB_PATH + " -s " + deviceID + " shell settings put system accelerometer_rotation 1",
				ipAddress, null);

		Logger.logConsoleMessage("Unlocking android device '" + deviceID + "' on machine '" + ipAddress + "'.");
		String screenState = CommandExecutor.execCommand(
				Constants.ADB_PATH + " -s " + deviceID + " shell dumpsys display | grep mScreenState", ipAddress, null);
		if (screenState.contains("ON")) {
			CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " shell input keyevent KEYCODE_POWER",
					ipAddress, null);
		}
		CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " shell input keyevent KEYCODE_POWER",
				ipAddress, null);

		String[] unlockSteps = unlockStrategy.split("::");
		for (String unlockStep : unlockSteps) {
			CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " " + unlockStep, ipAddress, null);
		}
	}

	/**********************************************************************************************
	 * ANDROID ONLY - Unlocks all android devices.
	 * 
	 * @author Brandon Clark created October 28, 2016
	 * @version 1.0 October 28, 2016
	 ***********************************************************************************************/
	public static void unlockAllDevices() {
		List<Thread> displayThreads = new ArrayList<Thread>();
		for (final String ipAddress : LabDeviceManager.getAllMachineIPAddresses()) {
			displayThreads.add(new Thread() {
				public void run() {
					if (hasOSNode(ipAddress, MobileOS.ANDROID)) {
						unlockDevice(ipAddress);
					}
				}
			});
		}
		for (Thread thread : displayThreads) {
			thread.start();
		}
		for (Thread thread : displayThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Logger.logConsoleMessage("Failed to turn off all Android displays.");
				e.printStackTrace();
			}
		}
	}

	/**********************************************************************************************
	 * ANDROID ONLY - Clears application cache on android device without requiring
	 * an app reset.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine
	 *            the android device is on.
	 * @param appPackage
	 *            - {@link String} - The app package you wish to clear cache.
	 * @author Brandon Clark created June 1, 2016
	 * @version 1.0 June 1, 2016
	 ***********************************************************************************************/
	public static void clearAppCache(String ipAddress, String appPackage) {
		String deviceID = getDeviceID(ipAddress, MobileOS.ANDROID);
		Logger.logConsoleMessage(
				"Clearing android app cache and data for device id '" + deviceID + "' on machine '" + ipAddress + "'.");
		CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " shell pm clear " + appPackage, ipAddress,
				null);
	}

	public static void clearADBLogs() {
		String activeMachineIP = TestDeviceInfo.getTetheredMachineIP();
		String deviceID = TestDeviceInfo.getDeviceID();
		if (activeMachineIP != null) {
			CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " logcat -c", activeMachineIP, null);
		}
	}

	public static String getADBLogs() {
		String activeMachineIP = TestDeviceInfo.getTetheredMachineIP();
		String deviceID = TestDeviceInfo.getDeviceID();
		if (activeMachineIP != null) {
			return CommandExecutor.execCommand(Constants.ADB_PATH + " -s " + deviceID + " logcat -d", activeMachineIP,
					null);
		}

		return null;
	}

	/**********************************************************************************************
	 * Downloads an app package from the web and stores it on a machine on the lab.
	 * 
	 * @param ipAddress
	 *            - {@link String} - The ip address of the desired remote machine.
	 * @param appURL
	 *            - {@link String} - The url of the app package to be downloaded.
	 * @param fileName
	 *            - {@link String} - The fileName to save the downloaded package as.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.1 April 26, 2016
	 ***********************************************************************************************/
	public static void downloadAppPackage(String ipAddress, String appURL, String fileName) {
		String appPath = Constants.HUB_APP_PACKAGE_DIR + fileName;

		String basicAuth = "";
		if (appURL.contains(System.getenv("HUDSON_HOST"))) {
			basicAuth = "-u " + System.getenv("HUDSON_USERNAME") + ":" + System.getenv("HUDSON_PASSWORD") + " ";
		}

		String command = "curl --connect-timeout 30 --max-time 179 " + basicAuth + "-o " + appPath + " -L " + appURL;
		Logger.logConsoleMessage(CommandExecutor.execCommand(command, ipAddress, 180)); // large timeout for super large
																						// packages
	}

	/**********************************************************************************************
	 * Copies an already downloaded app package from one the core machine to a node
	 * machine on the lab.
	 * 
	 * NOTE - uses the pre-set getAppPackagePath() method to retrieve the file path
	 * to copy. NOTE - uses the pre-set getAppPackageDirectory() method to retrieve
	 * the directory path to copy to.
	 * 
	 * @param user
	 *            - {@link String} - The scp user string of the desired remote
	 *            machine. In format "admin@ipaddress".
	 * @author Brandon Clark created February 12, 2016
	 * @version 1.0 February 12, 2016
	 ***********************************************************************************************/
	public static void copyAppPackage(String user, String fromFilePath) {
		Logger.logConsoleMessage("Copying app package from '" + fromFilePath + "'" + " for user '" + user + "'.");
		FileDeployer.deployFileFromGatewayToNode(LabDeviceManager.getMachineGatewayIP(user), user,
				new File(fromFilePath));
	}

	/**********************************************************************************************
	 * Determines if the device is active or not. Typically used during maintenance
	 * to check and see if a device is unhealthy and should be activated/reactivated
	 * automatically by the maintenance scripts.
	 * 
	 * @author Brandon Clark created March 17, 2016
	 * @version 1.0 March 17, 2016
	 * @return Boolean - The active/inactive status of the device.
	 ***********************************************************************************************/
	public static Boolean isDeviceActive(String deviceId) {
		String status = LabDatabaseFactory.getResults("select device_status from "
				+ LabDatabaseFactory.getOSTable(getDeviceOS(deviceId)) + " where device_id = '" + deviceId + "'")
				.get(0);
		if (status.equals(Constants.ACTIVE)) {
			return true;
		}
		return false;
	}

	public static Boolean isDeviceActive(EmergingOS emergingOS, String deviceId) {
		String status = LabDatabaseFactory.getResults("select device_status from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where device_id = '" + deviceId + "'").get(0);
		if (status.equals(Constants.ACTIVE)) {
			return true;
		}
		return false;
	}

	/**********************************************************************************************
	 * Determines if the device is enabled or not. Typically used during maintenance
	 * to check and see if a device is manually set to disabled status meaning the
	 * maintenance should NOT try to automatically activate or deactivate it.
	 * 
	 * @author Brandon Clark created March 17, 2016
	 * @version 1.0 March 17, 2016
	 * @return Boolean - The enabled/disabled status of the device.
	 ***********************************************************************************************/
	public static Boolean isDeviceEnabled(String deviceId) {
		String status = LabDatabaseFactory.getResults("select device_status from "
				+ LabDatabaseFactory.getOSTable(getDeviceOS(deviceId)) + " where device_id = '" + deviceId + "'")
				.get(0);
		if (status.equals(Constants.DISABLED)) {
			return false;
		}
		return true;
	}

	/**********************************************************************************************
	 * Determines if the machine node is active or not.
	 * 
	 * @author Brandon Clark created March 17, 2016
	 * @version 1.0 March 17, 2016
	 * @return Boolean - The active/inactive status of the machine node.
	 ***********************************************************************************************/
	public static Boolean isMachineNodeActive(String machineIP) {
		String status = LabDatabaseFactory
				.getResults("select machine_status from agentmachines where machine_ip = '" + machineIP + "'").get(0);
		if (status.equals(Constants.ACTIVE)) {
			return true;
		}
		return false;
	}

	/**********************************************************************************************
	 * Disables an OS level node. NOTE - ADMIN USE ONLY AND NOT TO BE USED AT THE
	 * PROJECT LEVEL!!!
	 * 
	 * @param machineAddress
	 *            - {@link MobileOS} - The machine ip address.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created March 16, 2016
	 * @version 1.0 March 16, 2016
	 ***********************************************************************************************/
	public static void deactivateOSNode(String machineAddress, MobileOS mobileOS) {
		if (isMachineNodeActive(machineAddress)) {
			if (isOSNodeActive(machineAddress, mobileOS) && !isOSNodeDisabled(machineAddress, mobileOS)) {
				LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS)
						+ " set device_status = 'inactive' where machine_ip = '" + machineAddress + "'");
			}
		}
	}

	/**********************************************************************************************
	 * Enables an OS level node. NOTE - ADMIN USE ONLY AND NOT TO BE USED AT THE
	 * PROJECT LEVEL!!!
	 * 
	 * @param machineAddress
	 *            - {@link MobileOS} - The machine ip address.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created March 16, 2016
	 * @version 1.0 March 16, 2016
	 ***********************************************************************************************/
	public static void activateOSNode(String machineAddress, MobileOS mobileOS) {
		if (isMachineNodeActive(machineAddress)) {
			if (!isOSNodeActive(machineAddress, mobileOS) && !isOSNodeDisabled(machineAddress, mobileOS)) {
				LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS)
						+ " set device_status = 'active' where machine_ip = '" + machineAddress + "'");
			}
		}
	}

	public static void setOSNodeInactiveReason(String machineAddress, MobileOS mobileOS, String inactiveReason) {
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set inactive_reason = '"
				+ inactiveReason + "' where machine_ip = '" + machineAddress + "'");
	}

	public static String getOSNodeInactiveReason(String machineAddress, MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select inactive_reason from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where machine_ip = '" + machineAddress + "'").get(0);
	}

	public static void deactivateEmergingDevice(EmergingOS emergingOS, String deviceID) {
		if (isDeviceActive(emergingOS, deviceID)) {
			LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(emergingOS)
					+ " set device_status = 'inactive' where device_id = '" + deviceID + "'");
		}
	}

	public static void activateEmergingDevice(EmergingOS emergingOS, String deviceID) {
		if (!isDeviceActive(emergingOS, deviceID)) {
			LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(emergingOS)
					+ " set device_status = 'active' where device_id = '" + deviceID + "'");
		}
	}

	/**********************************************************************************************
	 * Activates all inactive Roku devices. NOTE - ADMIN USE ONLY AND NOT TO BE USED
	 * AT THE PROJECT LEVEL!!!
	 * 
	 * @author Brandon Clark created November 12, 2016
	 * @version 1.0 February 12, 2017
	 ***********************************************************************************************/
	public static void activateAllRokuDevices() {
		LabDatabaseFactory
				.getResults("update rokudevices set device_status = 'active' where device_status = 'inactive'");
	}

	/**********************************************************************************************
	 * Disables a machine level node. NOTE - ADMIN USE ONLY AND NOT TO BE USED AT
	 * THE PROJECT LEVEL!!!
	 * 
	 * @param machineAddress
	 *            - {@link String} - The machine ip address.
	 * @author Brandon Clark created August 21, 2016
	 * @version 1.0 August 21, 2016
	 ***********************************************************************************************/
	public static void deactivateMachineNode(String machineAddress) {
		if (isMachineNodeActive(machineAddress)) {
			LabDatabaseFactory.getResults(
					"update agentmachines set machine_status = 'inactive' where machine_ip = '" + machineAddress + "'");
		}
	}

	/**********************************************************************************************
	 * Enables a machine level node. NOTE - ADMIN USE ONLY AND NOT TO BE USED AT THE
	 * PROJECT LEVEL!!!
	 * 
	 * @param machineAddress
	 *            - {@link String} - The machine ip address.
	 * @author Brandon Clark created August 18, 2016
	 * @version 1.0 August 18, 2016
	 ***********************************************************************************************/
	public static void activateMachineNode(String machineAddress) {
		if (!isMachineNodeActive(machineAddress)) {
			LabDatabaseFactory.getResults(
					"update agentmachines set machine_status = 'active' where machine_ip = '" + machineAddress + "'");
		}
	}

	/**********************************************************************************************
	 * Determines if a machine node has the relevant OS node/device attached. NOTE -
	 * ADMIN USE ONLY AND NOT TO BE USED AT THE PROJECT LEVEL!!!
	 * 
	 * @param machineAddress
	 *            - {@link MobileOS} - The machine ip address.
	 * @param mobileOS
	 *            - {@link MobileOS} - The device mobile OS (iOS or Android).
	 * @author Brandon Clark created April 13, 2016
	 * @version 1.0 April 13, 2016
	 * @return Boolean - The true/false value indicating if the machine node has the
	 *         relevant OS/device type.
	 ***********************************************************************************************/
	public static Boolean hasOSNode(String machineAddress, MobileOS mobileOS) {
		List<String> results = LabDatabaseFactory.getResults("select device_id from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineAddress + "'");
		if (!results.isEmpty()) {
			return true;
		}
		return false;
	}

	/**********************************************************************************************
	 * ANDROID ONLY Gets the CPU utilization from a device.
	 * 
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device.
	 * @author Brandon Clark created September 12, 2016
	 * @version 1.0 September 12, 2016
	 * @return Integer - The CPU utilization.
	 ***********************************************************************************************/
	public static Integer getDeviceCPU(String deviceID) {
		String ipAddress = getDeviceMachineIPAddress(deviceID);
		Integer cpu = 0;
		Pattern cpuPattern = Pattern.compile("(\\d*)\\% TOTAL.*");
		Logger.logConsoleMessage("Getting CPU from device '" + deviceID + "' on machine '" + ipAddress + "'.");
		String cpuOutput = CommandExecutor.execCommand(Constants.ADB_PATH + " shell dumpsys cpuinfo", ipAddress, null);
		Matcher matcher = cpuPattern.matcher(cpuOutput);
		if (matcher.find()) {
			String cpuValue = matcher.group(1);
			cpu = Integer.parseInt(cpuValue);
		} else {
			Logger.logConsoleMessage(
					"Failed to retrieve CPU results from device '" + deviceID + "' on machine '" + ipAddress + "'.");
		}
		return cpu;
	}

	/**********************************************************************************************
	 * ANDROID ONLY Gets the memory utilization from a device.
	 * 
	 * @param deviceID
	 *            - {@link String} - The device ID of the target device.
	 * @param appPackageID
	 *            - {@link String} - The target app package id.
	 * @author Brandon Clark created September 12, 2016
	 * @version 1.0 September 12, 2016
	 * @return Integer - The Memory utilization.
	 ***********************************************************************************************/
	public static Integer getDeviceMemory(String deviceID, String appPackageID) {
		String ipAddress = getDeviceMachineIPAddress(deviceID);
		Integer memory = 0;
		Pattern memoryPattern = Pattern.compile(".*TOTAL[ ]*(\\d*)");
		Logger.logConsoleMessage("Getting memory from device '" + deviceID + "' on machine '" + ipAddress + "'.");
		String memoryOutput = CommandExecutor.execCommand(
				String.format(Constants.ADB_PATH + " shell dumpsys meminfo '%s'", appPackageID), ipAddress, null);
		Matcher matcher = memoryPattern.matcher(memoryOutput);
		if (matcher.find()) {
			String memoryValue = matcher.group(1);
			memory = Integer.parseInt(memoryValue);
		} else {
			Logger.logConsoleMessage(
					"Failed to retrieve memory results from device '" + deviceID + "' on machine '" + ipAddress + "'.");
		}
		return memory;
	}

	public static Boolean isDeviceInUse(String deviceId) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		String status = LabDatabaseFactory.getResults("select device_in_use from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}

	public static synchronized Boolean isDeviceInUse(EmergingOS emergingOS, String deviceId) {
		String status = LabDatabaseFactory.getResults("select device_in_use from "
				+ LabDatabaseFactory.getOSTable(emergingOS) + " where device_id = '" + deviceId + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}

	public static synchronized void setDeviceInUse(String deviceId, Boolean inUse) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set device_in_use = "
				+ inUse.toString() + " where machine_ip = '" + machineIP + "'");
	}

	public static synchronized void setDeviceUseDuration(String deviceId, Long useDuration) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		LabDatabaseFactory
				.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set device_in_use_duration = "
						+ useDuration.toString() + " where machine_ip = '" + machineIP + "'");
	}

	public static Long getDeviceUseDuration(String deviceId) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		String duration = LabDatabaseFactory.getResults("select device_in_use_duration from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		return Long.parseLong(duration);
	}

	public static void setDeviceInUse(EmergingOS emergingOS, String deviceId, Boolean inUse) {
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(emergingOS) + " set device_in_use = "
				+ inUse.toString() + " where device_id = '" + deviceId + "'");
	}

	public static Boolean isDeviceUnhealthyDuringTest(String deviceId) {
		String status = LabDatabaseFactory.getResults("select unhealthy_during_test from "
				+ LabDatabaseFactory.getOSTable(getDeviceOS(deviceId)) + " where device_id = '" + deviceId + "'")
				.get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}

	public static void setDeviceHealthDuringTest(String deviceId, Boolean unhealthy) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		LabDatabaseFactory.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " set unhealthy_during_test = " + unhealthy.toString() + " where machine_ip = '" + machineIP + "'");
	}

	public static List<String> getUnhealthyDuringTestDevices(MobileOS mobileOS) {
		return LabDatabaseFactory.getResults("select device_id from " + LabDatabaseFactory.getOSTable(mobileOS)
				+ " where unhealthy_during_test = true");
	}

	public static synchronized void setDeviceUnderMaintenance(String deviceId, Boolean underMaintenance) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		LabDatabaseFactory
				.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set device_under_maintenance = "
						+ underMaintenance.toString().toLowerCase() + " where machine_ip = '" + machineIP + "'");
	}

	public static Boolean isDeviceUnderMaintenance(String deviceId) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		String status = LabDatabaseFactory.getResults("select device_under_maintenance from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}

	public static synchronized void setProxyPortBoundTime(String deviceId, Long timeOfBindDetection) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		LabDatabaseFactory
				.getResults("update " + LabDatabaseFactory.getOSTable(mobileOS) + " set proxy_port_bound_time = "
						+ timeOfBindDetection.toString() + " where machine_ip = '" + machineIP + "'");
	}

	public static Long getProxyPortBoundTime(String deviceId) {
		MobileOS mobileOS = LabDeviceManager.getDeviceOS(deviceId);
		String machineIP = LabDeviceManager.getDeviceMachineIPAddress(deviceId);
		String duration = LabDatabaseFactory.getResults("select proxy_port_bound_time from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		return Long.parseLong(duration);
	}

	public static Boolean isJMeterInUse() {
		List<String> results = LabDatabaseFactory
				.getResults("select jmeter_node_in_use from jmeternodes where jmeter_node_in_use = true");
		if (results.isEmpty()) {
			return false;
		}
		return true;
	}

	public static void setJMeterInUse(Boolean inUse) {
		LabDatabaseFactory.getResults("update jmeternodes set jmeter_node_in_use = " + inUse.toString());
	}

	public static DesktopOSType getMachineOSType(String machineIP) {
		return DesktopOSType.getEnumByString(LabDatabaseFactory
				.getResults("select machine_os from agentmachines where " + "machine_ip = '" + machineIP + "'").get(0));
	}

	public static File getAppiumLog(MobileOS mobileOS, String machineIP) {
		// copy the log file on the local machine
		String logFilePath = mobileOS.equals(MobileOS.ANDROID) ? Constants.NODE_APPIUM_LOG_ANDROID
				: Constants.NODE_APPIUM_LOG_IOS;
		File newLogFile = new File(logFilePath.replace("appiumlog_", "appiumlog_" + machineIP + "_"));
		CommandExecutor.execCommand("cp " + logFilePath + " " + newLogFile.getAbsolutePath(), machineIP, null);
		GatewayIP gatewayIP = LabDeviceManager.getMachineGatewayIP(machineIP);
		FileDeployer.pullFileFromNodeToGateway(gatewayIP, machineIP, newLogFile, Constants.HUB_APPIUM_LOG_DIR);

		String adminLogFile = Constants.HUB_APPIUM_LOG_DIR + "/" + newLogFile.getName();
		if (GridManager.isEC2Agent()) {
			CommandExecutor.setEC2SCPHop(false);
			FileDeployer.pullFileFromGatewayToAgent(gatewayIP, new File(adminLogFile), GlobalReportDir.getReportDir());
			String scpEC2LogFile = GlobalReportDir.getReportDir() + File.separator + newLogFile.getName();
			return new File(scpEC2LogFile);
		}
		return new File(adminLogFile);
	}

	public static void cleanAppiumLog(MobileOS mobileOS, String machineIP) {
		String logFilePath = mobileOS.equals(MobileOS.ANDROID) ? Constants.NODE_APPIUM_LOG_ANDROID
				: Constants.NODE_APPIUM_LOG_IOS;
		String newLogFilePath = logFilePath.replace("appiumlog_", "appiumlog_" + machineIP + "_");
		CommandExecutor.execCommand("'echo -n \"\" > " + logFilePath + "'", machineIP, null);
		CommandExecutor.execCommand("'echo -n \"\" > " + newLogFilePath + "'", machineIP, null);
	}

	private static Boolean isOSNodeActive(String machineIP, MobileOS mobileOS) {
		String result = LabDatabaseFactory.getResults("select device_status from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		if (result.equals(Constants.ACTIVE)) {
			return true;
		}
		return false;
	}

	private static Boolean isOSNodeDisabled(String machineIP, MobileOS mobileOS) {
		String result = LabDatabaseFactory.getResults("select device_status from "
				+ LabDatabaseFactory.getOSTable(mobileOS) + " where machine_ip = '" + machineIP + "'").get(0);
		if (result.equals(Constants.DISABLED)) {
			return true;
		}
		return false;
	}

	public static String getIDeviceInstallerPath(String machineIP) {
		String ideviceInstallerDir = "/usr/local/Cellar/ideviceinstaller";
		String ideviceInstallerVersion = CommandExecutor.execCommand("ls " + ideviceInstallerDir, machineIP, null)
				.trim();
		String ideviceInstallerPath = null;
		if (StringUtils.isEmpty(ideviceInstallerVersion)) {
			Logger.logConsoleMessage("Could not find ideviceinstaller in directory: " + ideviceInstallerDir);
		} else {
			ideviceInstallerPath = ideviceInstallerDir + "/" + ideviceInstallerVersion + "/bin/ideviceinstaller";

		}
		return ideviceInstallerPath;
	}

	private static String getIDeviceDiagnosticsPath(String machineIP) {
		return getLibimobileDeviceBinPath(machineIP) + "idevicediagnostics";
	}

	private static String getIDeviceInfoPath(String machineIP) {
		return getLibimobileDeviceBinPath(machineIP) + "ideviceinfo";
	}

	public static String getLibimobileDeviceBinPath(String machineIP) {
		String dir = "/usr/local/Cellar/libimobiledevice";
		String version = CommandExecutor.execCommand("ls " + dir, machineIP, null).trim();
		String path = null;
		if (StringUtils.isEmpty(version)) {
			Logger.logConsoleMessage("Could not find libimobiledevice in directory: " + dir);
		} else {
			path = dir + "/" + version + "/bin/";

		}
		return path;
	}

}
