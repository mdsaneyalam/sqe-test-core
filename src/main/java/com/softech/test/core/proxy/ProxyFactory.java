package com.softech.test.core.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.littleshoot.proxy.HttpFiltersSource;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.softech.test.core.lab.ActiveBrowserManager;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.SystemProxyManager;
import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.MQEDriverCaps;
import com.softech.test.core.props.ProxyType;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;

public class ProxyFactory {

	public static final String APPLE_UPDATE_1 = "mesu.apple.com";
	public static final String APPLE_UPDATE_2 = "appldnld.apple.com";
	public static final Integer PROXY_TIMEOUT = 60;
	
	private static ThreadLocal<ProxyType> proxyType = new ThreadLocal<ProxyType>();
	
	public static void setProxyType(DesiredCapabilities capabilities) {
		if (capabilities.getCapability(MQEDriverCaps.MQE_PROXY_TYPE.value()) == null) {
			proxyType.set(ProxyType.BMP);
		} else {
			proxyType.set(ProxyType.getEnumByString(capabilities
					.getCapability(MQEDriverCaps.MQE_PROXY_TYPE.value()).toString()));
		}
	}
	
	public static ProxyType getProxyType() {
		return proxyType.get();
    }
	
	public static Boolean isBMP() {
		return proxyType.get().equals(ProxyType.BMP);
	}
	
	public static Boolean isBMPRest() {
		return proxyType.get().equals(ProxyType.BMP_REST);
	}
	
	public static void enableMITM(DesiredCapabilities capabilities) {
		if (isBMP()) {
			if (capabilities.getCapability(MQEDriverCaps.MQE_NO_MITM.value()) == null 
					|| capabilities.getCapability(MQEDriverCaps.MQE_NO_MITM.value()).equals(false)) {
				ProxyManager.enableMITM();
			} else {
				Logger.logConsoleMessage("Disabling MITM SSL proxying as requested by user capabilities.");
			}
		} else {
			// rest to disable mitm as it is enabled by default
		}
	}
	
	public static void startProxyServer(String machineIP, String proxyPort) {
		if (isBMP()) {
			ProxyManager.setProxyServer(Integer.parseInt(proxyPort));
			ProxyManager.startProxyServer();
		} else {
			ProxyRESTManager.setProxyServerInstance(machineIP, Integer.parseInt(proxyPort));
			ProxyRESTManager.startProxyServer();
			ProxyRESTManager.startProxyInstance();
		}
	}
	
	public static void stopProxyServer() {
		if (isBMP()) {
			ProxyManager.stopProxyServer();
		} else {
			if (ProxyRESTManager.isProxyInstanceStarted()) {
				ProxyRESTManager.stopProxyInstance();
			}
			
			if (ProxyRESTManager.isProxyServerStarted() && GridManager.isEC2Agent()) {
				ProxyRESTManager.stopProxyServer();
			}
		}
	}
	
	public static synchronized Boolean isProxyPortInUse(Integer port) {
    	Boolean result;
    	ServerSocket socket = null;
        try {
        	socket = new ServerSocket(port);
            result = false;
        } catch(Exception e) {
        	result = true;
        }
        
        if (socket != null) {
        	try {
				socket.close();
			} catch (IOException e) {
				Logger.logConsoleMessage("Failed to close socket after port in use check on port '" + port + "'.");
				e.printStackTrace();
			}
        }

        return(result);
    }
	
	public static synchronized String getUnusedProxyPort(Integer startRange, Integer endRange) {
		Integer availableProxyPort = null;
		
		Integer iterLoopMax = endRange - startRange;
		try {
			for (int i = 0; i <= iterLoopMax; i++) {
				Integer port = RandomData.getInteger(startRange, endRange);
				if (!isProxyPortInUse(port)) {
					availableProxyPort = port;
					break;
				}
			}
			
			if (availableProxyPort == null) {
				Logger.logConsoleMessage("Failed to get an available proxy port.");
			}
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to generate proxy ports/tunnel identifiers.");
            e.printStackTrace();
        }
		
		return String.valueOf(availableProxyPort);
	}
	
	public static DesiredCapabilities assignProxyToBrowser(DesiredCapabilities capabilities, BrowserType browserType, String browserMachineIP, String proxyHost, String proxyPort) {
		// assign the proxy in the browser
		String serverAddress = proxyHost + ":" + proxyPort;
        if (browserType.equals(BrowserType.SAFARI)) {
			SystemProxyManager.startMacProxy(browserMachineIP, proxyHost, Integer.parseInt(proxyPort));
			if (!SystemProxyManager.isProxyInitiated()) {
				throw new RuntimeException("Failed to initiate system proxy on '" + browserMachineIP + "'");
			}
        } else if (browserType.equals(BrowserType.CHROME) || browserType.equals(BrowserType.IEXPLORE)) {
        	// standard proxy
			Proxy seleniumProxy = new Proxy();
			seleniumProxy.setHttpProxy(serverAddress).setSslProxy(serverAddress)
                .setFtpProxy(serverAddress);
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
		} else if (browserType.equals(BrowserType.EDGE)) {
			// TODO need to get proxy working
			// maybe system proxy?
		} else if (browserType.equals(BrowserType.FIREFOX)) {
			JsonObject proxy = new JsonParser().parse("{proxy:{" +
			        "proxyType:" + "manual" + "," +
			        "httpProxy:\"" + proxyHost + "\"," +
			        "httpProxyPort:" + proxyPort + "," +
			        "sslProxy:\"" + proxyHost + "\"," +
			        "sslProxyPort:" + proxyPort + "," +
			        "ftpProxy:\"" + proxyHost + "\"," +
			        "ftpProxyPort:" + proxyPort + "," +
			        "socksProxy:\"" + proxyHost + "\"," +
			        "socksProxyPort:" + proxyPort +
			        "}}").getAsJsonObject();
			
			capabilities.setCapability("requiredCapabilities", proxy);
			capabilities.setCapability("acceptInsecureCerts", true);
		}
        ActiveBrowserManager.setActiveBrowserNodeProxy(proxyHost, Integer.parseInt(proxyPort));
	
        return capabilities;
	}
	
	public static void setRewritesPriorToSessionStart(List<HttpFiltersSource> filters, List<String> bmpRequestFilters, List<String> bmpResponseFilters) {
		if (isBMP()) {
			if (filters != null) {
				for (HttpFiltersSource filter : filters) {
					ProxyManager.getProxyServer().addHttpFilterFactory(filter);
    			}
			}
		} else {
			if (bmpRequestFilters != null) {
				for (String requestFilter : bmpRequestFilters) {
					ProxyRESTManager.applyRequestFilters(requestFilter);
				}
			}
			
			if (bmpResponseFilters != null) {
				for (String responseFilter : bmpResponseFilters) {
					ProxyRESTManager.applyResponseFilters(responseFilter);
				}
			}
		}
	}
	
	public static Boolean isProxyStarted() {
		if (isBMP()) {
			if (ProxyManager.getProxyServer() != null) {
				if (ProxyManager.getProxyServer().isStarted()) {
					return true;
				}
			}
		} else {
			return ProxyRESTManager.isProxyInstanceStarted();
		}
		return false;
	}
	
}
