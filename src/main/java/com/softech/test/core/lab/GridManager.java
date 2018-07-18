package com.softech.test.core.lab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.remote.SessionId;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

public class GridManager {

	private static Integer localAndroidHubPort = null;
	private static Integer localiOSHubPort = null;
	private static Integer localWebHubPort = null;
	
	private static ThreadLocal<String> overrideHubIP = new ThreadLocal<String>() {
    	protected String initialValue() {
    		return null;
    	}
    };
	
	/**********************************************************************************************
     * Gets the grid lab hub ip.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return String - The lab hub ip address.
     ***********************************************************************************************/
	public static synchronized String getGridHubIP() {
		if (overrideHubIP.get() != null) {
			return overrideHubIP.get();
		}
		
		if (GridManager.isQALabHub()) {
			if (TestRun.isMobile()) { // lab mobile
				return LabDeviceManager.getNodeGridIP(TestDeviceInfo.getTetheredMachineIP(), TestRun.getMobileOS());
			} else { // lab web
				return BrowserNodeManager.getBrowserNodeGridIP(ActiveBrowserManager.getActiveBrowserAddress());
			}
		} else { // local
			return Constants.LOCALHOST;
		}
	}
    
	/**********************************************************************************************
     * Gets the appropriate running hub port of the test execution, i.e. port 4445 if the execution is
     * iOS vs 4446 if the execution is for iOS. Optionally the user can set their own desired local port
     * of execution.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.1 December 1, 2016
     * @return String - The hub port.
     ***********************************************************************************************/
	public static String getGridHubPort(MobileOS mobileOS) {
		String hubPort = null;
		if (isQALabHub()) { // lab run
			if (mobileOS != null) {
				if (mobileOS.equals(MobileOS.ANDROID_SIM) || mobileOS.equals(MobileOS.IOS_SIM)) {
					if (mobileOS.equals(MobileOS.ANDROID_SIM)) {
						hubPort = Constants.LAB_ANDROID_SIM_HUB_PORT;
					} else if (mobileOS.equals(MobileOS.IOS_SIM)) {
						hubPort = Constants.LAB_IOS_SIM_HUB_PORT;
					}
				} else {
					if (mobileOS.equals(MobileOS.ANDROID)) {
						hubPort = Constants.LAB_ANDROID_HUB_PORT;
					} else if (mobileOS.equals(MobileOS.IOS)) {
						hubPort = Constants.LAB_IOS_HUB_PORT;
					}
				}
			} else {
				hubPort = Constants.LAB_WEB_HUB_PORT;
			}
		} else { // local run
		    if (mobileOS != null) {
		    	if (mobileOS.equals(MobileOS.ANDROID)) {
		    		if (localAndroidHubPort != null) {
		    			hubPort = localAndroidHubPort.toString();
		    		} else {
		    			hubPort = Constants.LAB_ANDROID_HUB_PORT;
		    		}
		    	} else if (mobileOS.equals(MobileOS.IOS)) {
		    		if (localiOSHubPort != null) {
		    			hubPort = localiOSHubPort.toString();
		    		} else {
		    			hubPort = Constants.LAB_IOS_HUB_PORT;
		    		}
		    	}
		    } else {
		    	if (localWebHubPort != null) {
		    		hubPort = localWebHubPort.toString();
		    	} else {
		    		hubPort = Constants.LAB_WEB_HUB_PORT;
		    	}
		    }
		}
		
		return hubPort;
	}
	
	public static String getLabNodePort(MobileOS mobileOS) {
		if (mobileOS == null) {
			return System.getenv("GRID_DESKTOP_WEB_HUB_PORT");
		}
		return mobileOS.equals(MobileOS.IOS) ? System.getenv("GRID_IOS_NATIVE_HUB_PORT") 
			: System.getenv("GRID_ANDROID_NATIVE_HUB_PORT");
	}
	
	/**********************************************************************************************
     * Gets the running session address of the webdriver/appium execution from the grid in the format of
     * "http://address:port".
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.1 March 17, 2016
     * @return String - The session address of the running session.
     ***********************************************************************************************/
    public static synchronized String getRunningSessionIP() {
    	String sessionIP = null;
    	String responseBody = null;
    	try {
    		SessionId sessionID = TestRun.isMobile() ? DriverManager.getAppiumDriver().getSessionId() 
        			: DriverManager.getWebDriver().getSessionId();
        	MobileOS mobileOS = null;
        	if (TestRun.isMobile()) {
        		mobileOS = TestRun.getMobileOS();
        	}
    		String testSessionApi = "http://" + getGridHubIP() + ":" + getGridHubPort(mobileOS) 
					+ "/grid/api/testsession?session=" + sessionID.toString();
    		responseBody = queryWebdriverHub(testSessionApi);
			JSONParser parser = new JSONParser();
		    JSONObject object = (JSONObject) parser.parse(responseBody);
		    sessionIP = object.get("proxyId").toString();
		    sessionIP = sessionIP.split("http://")[1];
		    sessionIP = sessionIP.split(":")[0];
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to get the node IP address of the running session. Was the session not "
				+ "started properly, or has the session terminated already?");
		}
		
		return sessionIP;
    }
    
    public static synchronized Integer getRunningSessionCount(MobileOS mobileOS, String gridIPAddress) {
    	Integer sessionCount = null;
    	CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
    	String responseBody = CommandExecutor.execCommand("curl -i -H \"Accept: application/json\" -H \"Content-Type"
    			+ ": application/json\" -X GET http://" + gridIPAddress + ":" + getGridHubPort(mobileOS) + "/wd/hub/sessions", null, null);
        try {
    		sessionCount = StringUtils.countMatches(responseBody, "ext. key");
    		Logger.logConsoleMessage("Count of currently running sessions: " + sessionCount);
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to get the count of active sessions.");
			e.printStackTrace();
		}
		
		return sessionCount;
    }
    
    public static Boolean isGridOnline(MobileOS mobileOS, String gridIPAddress) {
    	String response = null;
    	
        try {
        	response = CommandExecutor.execCommand("curl --connect-timeout 10 -i -H \"Accept: application/json\" -H \"Content-Type"
        			+ ": application/json\" -X GET http://" + gridIPAddress + ":" + getGridHubPort(mobileOS) + "/wd/hub/sessions", null, null);
		} catch (Exception e) {
			// could not connect, grid is not online
		}
		
        if (response == null) {
        	return false;
        } else if (!response.contains("Session")) {
        	return false;
        } else {
        	return true;
        }
    }
    
    public static synchronized Boolean isLabNodeRegistered(String nodeIP, MobileOS mobileOS) {
    	CommandExecutor.setEC2CommandHop(false);
    	CommandExecutor.setTargetGatewayIP(LabDeviceManager.getMachineGatewayIP(nodeIP));
    	String responseBody = CommandExecutor.execCommand("curl -i -H \"Accept: application/json\" -H \"Content-Type"
    		+ ": application/json\" -X GET http://" + getGridHubIP() + ":" + getGridHubPort(mobileOS) + "/grid/api/proxy?id=http://" + nodeIP + ":" 
    		+ getLabNodePort(mobileOS), null, null);
        
		return responseBody.toLowerCase().contains("proxy found");
    }
    
    public static Boolean isQALabHub() {
    	String ec2Subnet = getEC2Subnet();
    	if (ec2Subnet == null) {
    		return false;
    	}
    	
    	String hostName = getHostName();
    	if (hostName.contains(Constants.GRID_HUB_MACHINE_NAME) || hostName.contains(Constants.GRID_HUB_EC2_MACHINE_NAME)
    			|| hostName.contains(ec2Subnet)) {
    		return true;
    	}
    	return false;
    }
    
    public static Boolean isEC2Agent() {
    	String ec2Subnet = getEC2Subnet();
    	if (ec2Subnet == null) {
    		return false;
    	}
    	
    	String hostName = getHostName();
    	if (hostName.contains(Constants.GRID_HUB_EC2_MACHINE_NAME) || hostName.contains(ec2Subnet)) {
    		return true;
    	}
    	return false;
    }
    
    public static void setLocalAndroidHubPort(Integer port) {
    	localAndroidHubPort = port;
    }
    
    public static void setLocaliOSHubPort(Integer port) {
    	localiOSHubPort = port;
    }
    
    public static void setLocalWebHubPort(Integer port) {
    	localWebHubPort = port;
    }
    
    /**********************************************************************************************
     * NOTE - ADMIN USE ONLY! NOT TO BE USED AT THE PROJECT LEVEL!!!
     * Resets a remote mobile node on the lab
     * 
     * @author Brandon Clark created February 17, 2017
     * @version 1.0 February 17, 2017
     ***********************************************************************************************/
	public static void resetRemoteMobileNode(MobileOS mobileOS, String machineIP) {
		stopRemoteMobileNode(mobileOS, machineIP);
		startRemoteMobileNode(mobileOS, machineIP);
	}
	
	/**********************************************************************************************
     * NOTE - ADMIN USE ONLY! NOT TO BE USED AT THE PROJECT LEVEL!!!
     * Starts a remote mobile node on the lab
     * 
     * @author Brandon Clark created February 17, 2017
     * @version 1.0 February 17, 2017
     ***********************************************************************************************/
	public static void startRemoteMobileNode(MobileOS mobileOS, String machineIP) {
		Logger.logConsoleMessage("Starting remote mobile '" + mobileOS.value() + "' on machine '" + machineIP + "'.");
		String startNodeScriptPath = null;
		if (mobileOS.equals(MobileOS.ANDROID)) {
			startNodeScriptPath = Constants.ANDROID_START_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.ANDROID_SIM)) {
			startNodeScriptPath = Constants.ANDROID_SIM_START_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.IOS)) {
			startNodeScriptPath = Constants.IOS_START_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.IOS_SIM)) {
			startNodeScriptPath = Constants.IOS_SIM_START_REMOTE_NODE_PATH;
		}
			
		try { Thread.sleep(Constants.DRIVER_RECYLE_TIMEOUT_MS); } catch (InterruptedException e) {}
		CommandExecutor.execCommand("open " + startNodeScriptPath, machineIP, null);
		if (mobileOS.equals(MobileOS.IOS)) {
			if (LabDeviceManager.isDeviceConnected(LabDeviceManager.getDeviceID(machineIP, mobileOS)) 
					&& !GridManager.isiOSWebDebugProxyRunning(machineIP)) {
				SleepUtils.sleep(1000);
				CommandExecutor.execCommand("open " + Constants.IOS_START_WEB_DEBUGGER_PATH, machineIP, null);
			}
		}
		
		// wait for the node to register
		SleepUtils.sleep(10000);
		Integer nodeRegisterPoll = 0;
		while (!isLabNodeRegistered(machineIP, mobileOS) && nodeRegisterPoll <= 30) {
			if (nodeRegisterPoll == 30) {
				Logger.logConsoleMessage("Node did not register with the grid on '" + machineIP + "'.");
			}
			SleepUtils.sleep(1000);
			nodeRegisterPoll++;
		}
	}
	
	/**********************************************************************************************
     * NOTE - ADMIN USE ONLY! NOT TO BE USED AT THE PROJECT LEVEL!!!
     * Terminates a remote mobile node on the lab
     * 
     * @author Brandon Clark created February 17, 2017
     * @version 1.0 February 17, 2017
     ***********************************************************************************************/
	public static void stopRemoteMobileNode(MobileOS mobileOS, String machineIP) {
		Logger.logConsoleMessage("Terminating remote mobile '" + mobileOS.value() + "' on machine '" + machineIP + "'.");
		String killNodeScriptPath = null;
		if (mobileOS.equals(MobileOS.ANDROID)) {
			killNodeScriptPath = Constants.ANDROID_KILL_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.ANDROID_SIM)) {
			killNodeScriptPath = Constants.ANDROID_SIM_KILL_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.IOS)) {
			killNodeScriptPath = Constants.IOS_KILL_REMOTE_NODE_PATH;
		} else if (mobileOS.equals(MobileOS.IOS_SIM)) {
			killNodeScriptPath = Constants.IOS_SIM_KILL_REMOTE_NODE_PATH;
		}
			
		CommandExecutor.execCommand("bash " + killNodeScriptPath, machineIP, null);
		try { Thread.sleep(Constants.DRIVER_RECYLE_TIMEOUT_MS); } catch (InterruptedException e) {}
		if (mobileOS.equals(MobileOS.IOS)) {
			CommandExecutor.execCommand("'pkill ios_webkit_debug_proxy'", machineIP, null);
			SleepUtils.sleep(1000);
			CommandExecutor.execCommand("open " + Constants.IOS_STOP_WEB_DEBUGGER_PATH, machineIP, null);
		}
	}
	
	public static void overrideHubIP(String hubIPToOverride) {
		overrideHubIP.set(hubIPToOverride);
	}
	
	public static Boolean isiOSWebDebugProxyRunning(String machineIP) {
		for (int i = 0; i <= 2; i++) {
			CommandExecutor.setTargetGatewayIP(LabDeviceManager.getMachineGatewayIP(machineIP));
			String response = CommandExecutor.execCommand("curl " + machineIP + ":27753", null, null);
	   	 	if (response.contains("Inspectable pages")) {
	   	 		return true;
	   	 	}
	   	 	SleepUtils.sleep(1000);
		}
		return false;
	}
	
	public static List<GatewayIP> getOnlineLabGatewayIPs() {
		String[] allGateways = System.getenv("GATEWAY_IPS").split(",");
		List<GatewayIP> gateways = new ArrayList<GatewayIP>();
		
		for (String gateway : allGateways) {
			gateways.add(GatewayIP.getEnumByString(gateway.trim()));
		}
		return gateways;
	}
	
	public static Boolean isLabGatewayOnline(GatewayIP gateway) {
		for (int i = 0; i <= 2; i++) {
			CommandExecutor.setTargetGatewayIP(gateway);
			String response = CommandExecutor.execCommand("DATE", null, null);
			if (response.contains("EDT") || response.contains("EST") || response.contains("CUT")) {
	   	 		return true;
	   	 	}
	   	 	SleepUtils.sleep(1000);
		}
		Logger.logConsoleMessage("Lab Gateway '" + gateway + "' is NOT online. No test traffic should "
				+ "be distributed to this gateway.");
		return false;
	}
	
    private static String queryWebdriverHub(String url) {
    	String responseBody = null;
    	try {
    		URL jsonURL= new URL(url);
		    HttpURLConnection request = (HttpURLConnection) jsonURL.openConnection();
			request.setRequestMethod("GET");
			request.connect();
				
		    InputStream input = request.getInputStream();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
		    }
		    reader.close();
			input.close();
				
		    responseBody = sb.toString();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to query webdriver hub.");
			e.printStackTrace();
		}
		
		return responseBody;
    }
    
    private static String getHostName() {
    	String hostName = "";
    	try {
    		if (OSDetector.isLinux()) {
    			hostName = CommandExecutor.execMultiCommand("hostname", null);
    		} else {
    			hostName = InetAddress.getLocalHost().getHostName();
    		}
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to get hostname.");
			e.printStackTrace();
		}
    	
    	return hostName;
    }
    
    private static String getEC2Subnet() {
    	return System.getenv("EC2_SUBNET");
    }
    
}
