package com.softech.test.core.lab;

import java.util.List;
import java.util.Random;

import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.softech.test.core.props.AgentLocationType;
import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.DesktopOSType;

public class BrowserNodeManager {

	public static List<String> getAllBrowserNodeAddresses() {
		String query = "select machine_ip from availablebrowsernodes";
    	return LabDatabaseFactory.getResults(query);
	}
	
	public static List<String> getAllBrowserNodeAddresses(DesktopOSType desktopType) {
		String query = "select availablebrowsernodes.machine_ip from availablebrowsernodes, agentmachines where agentmachines.machine_ip "
			+ "= availablebrowsernodes.machine_ip and agentmachines.machine_os = '" + desktopType.value() + "'";
    	return LabDatabaseFactory.getResults(query);
	}
	
	public static String getAvailableBrowserNodeAddress(AgentLocationType agentLocation, DesktopOSType desktopType, BrowserType browserType) {
		String ec2OnlineQuery = desktopType.equals(DesktopOSType.MQE_WINDOWS) ? " and agentmachines.ec2_machine_status = 'online'" : "";
		String agentLocationQ = "";
		if (agentLocation == null) {
			agentLocationQ = "and (agentmachines.location = '" + AgentLocationType.NEW_YORK_1515.value() + "' OR agentmachines.location = '" + AgentLocationType.EC2_EAST_1.value() + "')";
		}
		if (agentLocation != null) {
			agentLocationQ = " and agentmachines.location = '" + agentLocation.value() + "'";
		}
		String baseQuery = "select availablebrowsernodes.machine_ip from availablebrowsernodes, agentmachines where agentmachines.machine_ip "
			+ "= availablebrowsernodes.machine_ip and agentmachines.machine_os = '" + desktopType.value() + "' and availablebrowsernodes.has_" 
			+ getBrowserNodeTypeString(browserType) + " = true and availablebrowsernodes." + getBrowserNodeTypeString(browserType) + "_status = 'active' and availablebrowsernodes." 
			+ getBrowserNodeTypeString(browserType) + "_unhealthy_during_test = false and availablebrowsernodes.node_restarting = false and availablebrowsernodes.current_running_browsers "
			+ "< availablebrowsernodes.max_concurrent_browsers and availablebrowsernodes.running_" + getBrowserNodeTypeString(browserType) + "_sessions < availablebrowsernodes.max_" 
			+ getBrowserNodeTypeString(browserType) + "_sessions and agentmachines.machine_status = 'active'" + agentLocationQ;
		List<String> allFreeNodes = null;
		allFreeNodes = LabDatabaseFactory.getResults(baseQuery + ec2OnlineQuery);
		if (allFreeNodes.isEmpty()) {
			allFreeNodes = LabDatabaseFactory.getResults(baseQuery);
		}

    	try {
    		// result found on query
    		return allFreeNodes.get(new Random().nextInt(allFreeNodes.size()));
    	} catch (Exception e) {
    		return null;
    	}
	}
	
	public static String getAvailableBrowserNodeAddress(String machineAddress, DesktopOSType desktopType, BrowserType browserType) {
		
		String query = "select availablebrowsernodes.machine_ip from availablebrowsernodes, agentmachines where agentmachines.machine_ip "
			+ "= availablebrowsernodes.machine_ip and agentmachines.machine_os = '" + desktopType.value() + "' and availablebrowsernodes.has_" 
			+ getBrowserNodeTypeString(browserType) + " = true and availablebrowsernodes." + getBrowserNodeTypeString(browserType) + "_status = 'active' and availablebrowsernodes." 
			+ getBrowserNodeTypeString(browserType) + "_unhealthy_during_test = false and availablebrowsernodes.node_restarting = false and availablebrowsernodes.current_running_browsers "
			+ "< availablebrowsernodes.max_concurrent_browsers and availablebrowsernodes.running_" + getBrowserNodeTypeString(browserType) + "_sessions < availablebrowsernodes.max_" 
			+ getBrowserNodeTypeString(browserType) + "_sessions and availablebrowsernodes.machine_ip = '" + machineAddress + "'"
			+ " and agentmachines.machine_status = 'active'";
		String node = null;
		try {
    		node = LabDatabaseFactory.getResults(query).get(0);
    	} catch (Exception e) {
    		// ignore
    	}
		return node;
	}
	
	public static synchronized Boolean isBrowserNodeRestarting(String machineAddress) {
		String status = LabDatabaseFactory.getResults("select node_restarting from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}
	
	public static synchronized void setBrowserNodeRestarting(String machineAddress, Boolean restarting) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set node_restarting = " + restarting.toString()
		    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized Boolean hasBrowserNodeType(String machineAddress, BrowserType browserType) {
		String status = LabDatabaseFactory.getResults("select has_" + getBrowserNodeTypeString(browserType) + " from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}
	
	public static synchronized Boolean isBrowserNodeTypeActive(String machineAddress, BrowserType browserType) {
		String status = LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_status from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (status == null) {
			return false;
		} else if (status.equals("active")) {
			return true;
		}
		return false;
	}
	
	public static synchronized Boolean isBrowserNodeTypeEnabled(String machineAddress, BrowserType browserType) {
		String status = LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_status from availablebrowsernodes where machine_ip = '" + machineAddress + "'").get(0);
		
		if (status == null) {
			return false;
		} else if (status.equals("disabled")) {
			return false;
		}
		return true;
	}
	
	public static synchronized void setBrowserNodeConcurrentCount(String machineAddress, Integer count) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set current_running_browsers = " + count.toString()
    		+ " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized void setBrowserNodeTypeSessionCount(String machineAddress, BrowserType browserType, Integer count) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set running_" + getBrowserNodeTypeString(browserType) + "_sessions = " + count.toString()
    		+ " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized Boolean isBrowserNodeAtMax(String machineAddress) {
		String max = LabDatabaseFactory.getResults("select max_concurrent_browsers from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		String current = LabDatabaseFactory.getResults("select current_running_browsers from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		if (max == null || current == null) {
			return false;
		} else if (Integer.parseInt(max) == Integer.parseInt(current)) {
			return true;
		}
		return false;
	}
	
	public static synchronized Boolean isBrowserNodeTypeAtMax(String machineAddress, BrowserType browserType) {
		String max = LabDatabaseFactory.getResults("select max_" + getBrowserNodeTypeString(browserType) + "_sessions from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		String current = LabDatabaseFactory.getResults("select running_" + getBrowserNodeTypeString(browserType) + "_sessions from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		if (max == null || current == null) {
			return false;
		} else if (Integer.parseInt(max) == Integer.parseInt(current)) {
			return true;
		}
		return false;
	}
	
	public static synchronized Boolean isBrowserNodeTypeInUse(String machineAddress, BrowserType browserType) {
		String result = LabDatabaseFactory.getResults("select running_" + getBrowserNodeTypeString(browserType) + "_sessions from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		if (result == null) {
			return false;
		} else if (Integer.parseInt(result) > 0) {
			return true;
		}
		return false;
	}
	
	public static synchronized Boolean isBrowserNodeTypeHealthy(String machineAddress, BrowserType browserType) {
		String result = LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_unhealthy_during_test from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		if (result.contains("t")) {
			return false;
		}
		return true;
	}
	
	public static synchronized void addBrowserNodeTypeInUse(String machineAddress, BrowserType browserType) {
		Integer count = getRunningBrowserCount(machineAddress) + 1;
		LabDatabaseFactory.getResults("update availablebrowsernodes set current_running_browsers = " + count.toString()
		    + " where machine_ip = '" + machineAddress + "'");
		
		count = getRunningBrowserNodeTypeCount(machineAddress, browserType) + 1;
		LabDatabaseFactory.getResults("update availablebrowsernodes set running_" + getBrowserNodeTypeString(browserType) + "_sessions = " + count.toString()
	    	+ " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized void removeBrowserNodeTypeInUse(String machineAddress, BrowserType browserType) {
		Integer count = getRunningBrowserCount(machineAddress) - 1;
		if (count <= 0) {
			count = 0;
		}
		LabDatabaseFactory.getResults("update availablebrowsernodes set current_running_browsers = " + count.toString()
		    + " where machine_ip = '" + machineAddress + "'");
		
		count = getRunningBrowserNodeTypeCount(machineAddress, browserType) - 1;
		if (count <= 0) {
			count = 0;
		}
		LabDatabaseFactory.getResults("update availablebrowsernodes set running_" + getBrowserNodeTypeString(browserType) + "_sessions = " + count.toString()
	    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized void setBrowserNodeTypeUseDuration(String machineAddress, BrowserType browserType, Long useDuration) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set " + getBrowserNodeTypeString(browserType) + "_duration = " + useDuration.toString()
		    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized void setBrowserNodeTypeHealth(String machineAddress, BrowserType browserType, Boolean unhealthy) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set " + getBrowserNodeTypeString(browserType) + "_unhealthy_during_test = " + unhealthy.toString()
		    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized Integer getRunningBrowserCount(String machineAddress) {
		String count = LabDatabaseFactory.getResults("select current_running_browsers from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		return Integer.parseInt(count);
	}
	
	public static synchronized Integer getRunningBrowserNodeTypeCount(String machineAddress, BrowserType browserType) {
		String count = LabDatabaseFactory.getResults("select running_" + getBrowserNodeTypeString(browserType) + "_sessions from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		return Integer.parseInt(count);
	}
	
	public static synchronized String getBrowserNodeOSVersion(String machineAddress) {
		return LabDatabaseFactory.getResults("select machine_os_version from agentmachines where machine_ip = '" 
			+ machineAddress + "'").get(0);
	}
	
	public static String getBrowserNodeTypeVersion(String machineAddress, BrowserType browserType) {
		return LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_version from availablebrowsernodes where machine_ip = '" 
			+ machineAddress + "'").get(0);
	}
	
	public static String getBrowserNodeProxyHost(String machineAddress) {
		return LabDatabaseFactory.getResults("select proxy_host from availablebrowsernodes where machine_ip = '" 
			+ machineAddress + "'").get(0);
	}
	
	public static synchronized Integer getBrowserNodeTypeProxyPort(String machineAddress, BrowserType browserType) {
		return Integer.parseInt(LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_proxy_port from availablebrowsernodes where machine_ip = '" 
			+ machineAddress + "'").get(0));
	}
	
	public static synchronized void deactivateBrowserNodeType(String machineAddress, BrowserType browserType) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set " + getBrowserNodeTypeString(browserType) + "_status = 'inactive' where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized void activateBrowserNodeType(String machineAddress, BrowserType browserType) {
		LabDatabaseFactory.getResults("update availablebrowsernodes set " + getBrowserNodeTypeString(browserType) + "_status = 'active' where machine_ip = '" + machineAddress + "'");
	}
	
	public static Long getBrowserNodeTypeDuration(String machineAddress, BrowserType browserType) {
		String duration = LabDatabaseFactory.getResults("select " + getBrowserNodeTypeString(browserType) + "_duration from availablebrowsernodes where machine_ip = '" 
	        + machineAddress + "'").get(0);
		return Long.parseLong(duration);
	}
	
	public static synchronized String getBrowserNodeGridIP(String machineAddress) {
		String result = LabDatabaseFactory.getResults("select grid_ip from availablebrowsernodes"
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		
		return result;
	}
	
	public static String getBrowserNodeLocation(String machineAddress) {
		String query = "select location from agentmachines where machine_ip = '" + machineAddress + "'";
		return LabDatabaseFactory.getResults(query).get(0);
	}
	
	public static synchronized DesiredCapabilities setTargetNodeCapability(DesiredCapabilities capabilities, String machineAddress, String agentLocation, DesktopOSType desktopOSType, BrowserType browserType) {
		capabilities.setVersion("");
		
		// TODO - clean all this up and simply leverage the applicationName cap for all agents
		AgentLocationType agentLocationType = AgentLocationType.getEnumByString(agentLocation);
		String nodeID = null;
		if ((agentLocationType.equals(AgentLocationType.NEW_YORK_1515) || agentLocationType.equals(AgentLocationType.HAUPPAUGE_NEW_YORK)) 
				&& desktopOSType.equals(DesktopOSType.MQE_MAC)) {
			String machineName = LabDeviceManager.getMachineName(machineAddress);
			nodeID = machineName.replaceAll("_", "-") + ".local";
		} else if ((agentLocationType.equals(AgentLocationType.NEW_YORK_1515) || agentLocationType.equals(AgentLocationType.HAUPPAUGE_NEW_YORK)) 
				&& desktopOSType.equals(DesktopOSType.MQE_WINDOWS)) {
			nodeID = machineAddress;
		}
		else if (agentLocationType.equals(AgentLocationType.EC2_EAST_1)) {
			nodeID = machineAddress;
		} else if (agentLocationType.equals(AgentLocationType.BERLIN) || agentLocationType.equals(AgentLocationType.WARSAW) 
				|| agentLocationType.equals(AgentLocationType.MILAN)) {
			nodeID = LabDeviceManager.getMachineName(machineAddress);
		}
		
		if (browserType.equals(BrowserType.FIREFOX)) {
			capabilities.setCapability(CapabilityType.VERSION, "");
			capabilities.setCapability("applicationName", nodeID);
		} else {
			capabilities.setCapability(CapabilityType.VERSION, nodeID);
		}
		
		return capabilities;
	}
	
	public static synchronized Boolean isEC2Box(String machineAddress) {
		return LabDeviceManager.getMachineName(machineAddress).toLowerCase().contains("ec2");
	}
	
	public static void resetIExplore(String machineAddress) {
		CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM iexplore.exe /F", machineAddress, null);
		CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM IEDriverServer.exe /F", machineAddress, null);
	}
	
	public static void resetEdge(String machineAddress) {
		CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM MicrosoftEdge.exe /F", machineAddress, null);
		CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM MicrosoftWebDriver.exe /F", machineAddress, null);
	}
	
	public static void resetFirefox(String machineAddress, DesktopOSType desktopOSType) {
		if (desktopOSType.equals(DesktopOSType.MQE_MAC)) {
			CommandExecutor.execCommand("'pkill firefox'", machineAddress, null);
			CommandExecutor.execCommand("'pkill geckodriver'", machineAddress, null);
		} else {
			CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM firefox.exe /F", machineAddress, null);
			CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM geckodriver.exe /F", machineAddress, null);
		}
	}
	
	public static void resetChrome(String machineAddress, DesktopOSType desktopOSType) {
		if (desktopOSType.equals(DesktopOSType.MQE_MAC)) {
			CommandExecutor.execCommand("'pkill \"Google Chrome\"'", machineAddress, null);
			CommandExecutor.execCommand("'pkill chromedriver'", machineAddress, null);
		} else {
			CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM chrome.exe /F", machineAddress, null);
			CommandExecutor.execCommand("/cygdrive/c/Windows/System32/taskkill.exe /IM chromedriver.exe /F", machineAddress, null);
		}
	}
	
	public static void resetSafari(String machineAddress) {
		CommandExecutor.execCommand("'pkill Safari'", machineAddress, null);
		CommandExecutor.execCommand("'pkill safaridriver'", machineAddress, null);
	}
	
	private static String getBrowserNodeTypeString(BrowserType browserType) {
		if (browserType.equals(BrowserType.FIREFOX)) {
			return "firefox";
		} else if (browserType.equals(BrowserType.CHROME)) {
			return "chrome";
		} else if (browserType.equals(BrowserType.SAFARI)) {
			return "safari";
		} else if (browserType.equals(BrowserType.IEXPLORE)) {
			return "iexplore";
		} else {
			return "edge";
		}
	}
    
}
