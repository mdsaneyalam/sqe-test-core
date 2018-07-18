package com.softech.test.core.lab;

import java.util.ArrayList;
import java.util.List;

import com.softech.test.core.util.Logger;

public class SystemProxyManager {
	
	private static ThreadLocal<Boolean> proxyInitiated = new ThreadLocal<Boolean>() {
	    protected Boolean initialValue() {
	    	return false;
	    }
	};
	
	public static void startMacProxy(String machineAddress, String proxyHost, Integer proxyPort) {
		List<Integer> exitCodes = new ArrayList<Integer>();
		for (int i = 1; i < 3; i++) {
			Logger.logConsoleMessage("Setting system proxy on '" + machineAddress + "' to '" + proxyHost + ":" + proxyPort + "'.");
			CommandExecutor.execCommand("sudo networksetup -setwebproxy Ethernet " + proxyHost + " " + proxyPort, machineAddress, null);
			exitCodes.add(CommandExecutor.getExitCode());
	        CommandExecutor.execCommand("sudo networksetup -setwebproxystate Ethernet on", machineAddress, null);
	        exitCodes.add(CommandExecutor.getExitCode());
	        CommandExecutor.execCommand("sudo networksetup -setsecurewebproxy Ethernet " + proxyHost + " " + proxyPort, machineAddress, null);
	        exitCodes.add(CommandExecutor.getExitCode());
	        CommandExecutor.execCommand("sudo networksetup -setsecurewebproxystate Ethernet on", machineAddress, null);
	        exitCodes.add(CommandExecutor.getExitCode());
	        
	        if (!exitCodes.contains(1)) {
	        	proxyInitiated.set(true);
	        	break;
	        }
	        
	        Logger.logConsoleMessage("Mac system proxy not initiated on attempt '" + i + "'.");
		}
	}
	
	public static void stopMacProxy(String machineAddress, String proxyHost, Integer proxyPort) {
		CommandExecutor.execCommand("sudo networksetup -setwebproxystate Ethernet off", machineAddress, null);
        CommandExecutor.execCommand("sudo networksetup -setsecurewebproxystate Ethernet off", machineAddress, null);
	}
	
	public static Boolean isProxyInitiated() {
		return proxyInitiated.get();
	}
	
}
