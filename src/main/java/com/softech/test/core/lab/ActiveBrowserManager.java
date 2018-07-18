package com.softech.test.core.lab;

import java.util.HashSet;
import java.util.Set;

import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.util.TestRun;

public class ActiveBrowserManager {
	
	private static ThreadLocal<String> activeBrowserNode = new ThreadLocal<String>();
	private static ThreadLocal<DesktopOSType> activeDesktopOSType = new ThreadLocal<DesktopOSType>();
	private static ThreadLocal<String> activeDesktopOSVersion = new ThreadLocal<String>();
	private static ThreadLocal<BrowserType> activeBrowserType = new ThreadLocal<BrowserType>();
	private static ThreadLocal<String> activeBrowserVersion = new ThreadLocal<String>();
	private static ThreadLocal<String> activeProxyHost = new ThreadLocal<String>();
	private static ThreadLocal<Integer> activeProxyPort = new ThreadLocal<Integer>();
	private static ThreadLocal<String> agentLocation = new ThreadLocal<String>();
	private static Set<String> inactiveBrowserNodes = new HashSet<String>();
	
	public synchronized static void setActiveBrowserNode(String machineIP, DesktopOSType desktopOSType, BrowserType browserType, String browserVersion) {
		activeDesktopOSType.set(desktopOSType);
		if (TestRun.isSauceRun()) {
			activeDesktopOSVersion.set(desktopOSType.value());
		} else {
			activeDesktopOSVersion.set(BrowserNodeManager.getBrowserNodeOSVersion(machineIP));
			agentLocation.set(BrowserNodeManager.getBrowserNodeLocation(machineIP));
		}
		activeBrowserNode.set(machineIP);
		activeBrowserType.set(browserType);
		if (browserVersion == null) {
			activeBrowserVersion.set(BrowserNodeManager.getBrowserNodeTypeVersion(machineIP, browserType));
		} else {
			activeBrowserVersion.set(browserVersion);
		}
	}
	
	public synchronized static void setActiveBrowserNodeProxy(String proxyHost, Integer proxyPort) {
		activeProxyHost.set(proxyHost);
		activeProxyPort.set(proxyPort);
	}
	
	public static DesktopOSType getActiveDesktopOSType() {
		return activeDesktopOSType.get();
	}
	
	public static String getActiveDesktopOSVersion() {
		return activeDesktopOSVersion.get();
	}
	
	public static String getActiveBrowserAddress() {
		return activeBrowserNode.get();
	}
	
	public static BrowserType getActiveBrowserType() {
		return activeBrowserType.get();
	}
	
	public static String getActiveBrowserVersion() {
		return activeBrowserVersion.get();
	}
	
	public static String getActiveBrowserProxyHost() {
		return activeProxyHost.get();
	}
	
	public static Integer getActiveBrowserProxyPort() {
		return activeProxyPort.get();
	}
	
	public static String getAgentLocation() {
		return agentLocation.get();
	}
	
	public synchronized static void addInactiveBrowserNode(String machineIP) {
		inactiveBrowserNodes.add(machineIP);
	}
	
	public synchronized static Set<String> getInactiveBrowserNodes() {
		return inactiveBrowserNodes;
	}
}
