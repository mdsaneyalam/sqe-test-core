package com.softech.test.core.emerging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.littleshoot.proxy.HttpFiltersSource;

import com.softech.test.core.lab.ActiveDeviceManager;
import com.softech.test.core.lab.AvailableDevicePoller;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.proxy.ProxyManager;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.TestRun;

import net.lightbody.bmp.BrowserMobProxyServer;

public class EmergingDriverFactory {

	private static List<String> allDevices = new ArrayList<String>();
	private static ThreadLocal<EmergingOS> activeEmergingOS = new ThreadLocal<EmergingOS>();
    private static ThreadLocal<String> activeEmergingOSVersion = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceID = new ThreadLocal<String>();
    private static ThreadLocal<String> activeHarmonyDeviceID = new ThreadLocal<String>();
    private static ThreadLocal<String> activeHarmonyHubIP = new ThreadLocal<String>();
    private static ThreadLocal<String> activeMachineIP = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceIP = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceUsername = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDevicePassword = new ThreadLocal<String>();
    private static ThreadLocal<String> activeAppID = new ThreadLocal<String>();
    private static ThreadLocal<String> activeLaunchActivity = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceProxyPort = new ThreadLocal<String>();
    private static ThreadLocal<String> activeAppPackageLocation = new ThreadLocal<String>();
    private static ThreadLocal<String> activeADBPath = new ThreadLocal<String>();
    
    private static ThreadLocal<String> specificTargetDeviceID = new ThreadLocal<String>() {
    	protected String initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<List<HttpFiltersSource>> filters = new ThreadLocal<List<HttpFiltersSource>>() {
    	protected List<HttpFiltersSource> initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<Integer> localDebugProxyPort = new ThreadLocal<Integer>() {
    	protected Integer initialValue() {
    		return null;
    	}
    };
    
    // TODO - some redundant code between specific device/lab device/debug. Clean up as time allows
    public static void initiateEmergingDriver(EmergingOS emergingOS, String pathOrUrlToApp) {
		activeEmergingOS.set(emergingOS);
		TestRun.setEmergingOS(emergingOS);
		
		if (GridManager.isQALabHub() && specificTargetDeviceID.get() != null) { // SPECIFIC DEVICE REQUESTED
			activeDeviceID.set(specificTargetDeviceID.get());
			// get the device info
			HashMap<String, String> deviceInfo = getEmergingDeviceInfo();
			
			activeHarmonyDeviceID.set(deviceInfo.get("harmony_device_id"));
			activeEmergingOSVersion.set(deviceInfo.get("device_os_version"));
			activeHarmonyHubIP.set(LabDeviceManager.getHarmonyHubIPAddress(activeEmergingOS.get()));
    	    
			// apple tv specific
			if (TestRun.isAppleTV()) {
				activeMachineIP.set(deviceInfo.get("machine_ip"));
				activeDeviceProxyPort.set(deviceInfo.get("device_proxy_port"));
			}
			
			// roku specific
			if (TestRun.isRoku()) {
				activeDeviceIP.set(deviceInfo.get("device_ip"));
				activeDeviceUsername.set(deviceInfo.get("device_username"));
				activeDevicePassword.set(deviceInfo.get("device_password"));
			}
			
			// fire tv specific
			if (TestRun.isFireTV()) {
				activeADBPath.set(Constants.ADB_PATH);
			}
			
			activeAppPackageLocation.set(pathOrUrlToApp);
			EmergingDriverManager.setEmergingOS(activeEmergingOS.get());
			EmergingDriverManager.setDeviceId(activeDeviceID.get());
			EmergingDriverManager.setHarmonyDeviceId(activeHarmonyDeviceID.get());
			EmergingDriverManager.setHarmonyHubHubIP(activeHarmonyHubIP.get());
			if (activeEmergingOS.get().equals(EmergingOS.ROKU)) {
				EmergingDriverManager.setDeviceIP(activeDeviceIP.get());
				EmergingDriverManager.setDeviceUsername(activeDeviceUsername.get());
				EmergingDriverManager.setDevicePassword(activeDevicePassword.get());
			}
			EmergingDriverManager.setMachineIP(null);
			
			while (LabDeviceManager.isDeviceInUse(activeEmergingOS.get(), activeDeviceID.get())) {
			    AvailableDevicePoller.pollFarmForDevice(activeEmergingOS.get(), activeDeviceID.get(), true);
			}
	    	
			// install/launch
			if (activeEmergingOS.get().equals(EmergingOS.APPLE_TV)) {

			} else if (activeEmergingOS.get().equals(EmergingOS.ROKU)) {
				EmergingInstall.downloadAppPackage(activeEmergingOS.get(), pathOrUrlToApp);
				activeAppPackageLocation.set(EmergingInstall.getHubAppFilePath());
				launchRokuApp();
			}
		} else if (GridManager.isQALabHub()) {
    		Boolean sessionSuccess = false;
    		
    		for (int farmIter = 0; farmIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; farmIter++) {
    			Boolean deviceAvailable = false;
    			while (!deviceAvailable) {
    				synchronized (EmergingDriverFactory.class) {
    					List<String> currDeviceFarm = new ArrayList<String>();
						if (allDevices.isEmpty()) {
							currDeviceFarm = LabDeviceManager.getAllDeviceIDs(activeEmergingOS.get());
						} else {
							currDeviceFarm = allDevices;
						}
						
						for (String deviceID : currDeviceFarm) {
							activeDeviceID.set(deviceID);
        					
							// get the device info
							HashMap<String, String> deviceInfo = getEmergingDeviceInfo();
							
							// set the active device info
        					activeHarmonyDeviceID.set(deviceInfo.get("harmony_device_id"));
        					activeEmergingOSVersion.set(deviceInfo.get("device_os_version"));
            				activeHarmonyHubIP.set(LabDeviceManager.getHarmonyHubIPAddress(activeEmergingOS.get()));
        	        	    
        					// apple tv specific
        					if (TestRun.isAppleTV()) {
        						activeMachineIP.set(deviceInfo.get("machine_ip"));
                				activeDeviceProxyPort.set(deviceInfo.get("device_proxy_port"));
            				}
        					
        					// roku specific
        					if (TestRun.isRoku()) {
        						activeDeviceIP.set(deviceInfo.get("device_ip"));
        						activeDeviceUsername.set(deviceInfo.get("device_username"));
        						activeDevicePassword.set(deviceInfo.get("device_password"));
        					}
        					
        					// fire tv specific
        					if (TestRun.isFireTV()) {
        						activeADBPath.set(Constants.ADB_PATH);
        					}
        					
        	        	    // is the device active/enabled on the farm
        					Boolean deviceActive = deviceInfo.get("device_status").equals("active");
        					
        					// is the device currently not in use
        					Boolean inUse = Boolean.parseBoolean(deviceInfo.get("device_in_use"));
        					
        					if (deviceActive && !inUse) {
        						// DEVICE AVAILABLE FOR TEST
                				ActiveDeviceManager.setActiveDevice(activeDeviceID.get());
                        	    ActiveDeviceManager.addActiveDevice(activeDeviceID.get());
                        	    LabDeviceManager.setDeviceInUse(activeEmergingOS.get(), activeDeviceID.get(), true);
                        	    deviceAvailable = true;
                        	    break;
        					}
						}
    				}
    				
    				if (!deviceAvailable) {
    					AvailableDevicePoller.pollFarmForDevice(emergingOS, activeDeviceID.get(), false);
    				}
    			}
    			
        		for (int deviceIter = 0; deviceIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; deviceIter++) {
        			// attempt to start a session
        			try {
        				// set and start the proxy for the device prior to the app startup
        				if (activeDeviceProxyPort.get() != null) {
        					ProxyManager.enableMITM();
            				ProxyManager.setProxyServer(Integer.parseInt(activeDeviceProxyPort.get()));
            				ProxyManager.startProxyServer();
                			
                			// set any proxy rewrites prior to session startup
                			if (filters.get() != null) {
                				for (HttpFiltersSource filter : filters.get()) {
                					ProxyManager.getProxyServer().addHttpFilterFactory(filter);
                    			}
                			}
        				}
        				
            			// set the driver instance details
        				EmergingDriverManager.setEmergingOS(activeEmergingOS.get());
        				EmergingDriverManager.setHarmonyDeviceId(activeHarmonyDeviceID.get());
        				EmergingDriverManager.setHarmonyHubHubIP(activeHarmonyHubIP.get());
        				if (TestRun.isAppleTV()) {
        					EmergingDriverManager.setDeviceId(activeDeviceID.get());
        					EmergingDriverManager.setMachineIP(activeMachineIP.get());
        				} else if (TestRun.isRoku()) {
        					EmergingDriverManager.setDeviceIP(activeDeviceIP.get());
        					EmergingDriverManager.setDeviceUsername(activeDeviceUsername.get());
        					EmergingDriverManager.setDevicePassword(activeDevicePassword.get());
        					EmergingDriverManager.setTargetGateway(LabDeviceManager.getDeviceGatewayIP(
        							activeEmergingOS.get(), activeDeviceID.get()));
        				} else if (TestRun.isFireTV()) {
        					EmergingDriverManager.setDeviceId(activeDeviceID.get());
        					EmergingDriverManager.setADBPath(activeADBPath.get());
        					EmergingDriverManager.setAppId(activeAppID.get());
        					EmergingDriverManager.setLaunchActivity(activeLaunchActivity.get());
        				}
        				
        				// install/launch app
        				EmergingInstall.downloadAppPackage(activeEmergingOS.get(), pathOrUrlToApp);
    					activeAppPackageLocation.set(EmergingInstall.getHubAppFilePath());
        				if (TestRun.isRoku()) {
        					launchRokuApp();
        				} else if (TestRun.isAppleTV()) {
        					launchAppleTVApp();
        				} else if (TestRun.isFireTV()) {
        					launchFireTVApp();
        				}
        				
        				Logger.logMessage("Session successfully created for an " + activeEmergingOS.get().toString() 
                        		+ " device with device id '" + activeHarmonyDeviceID.get() + "'.");
                        
                        sessionSuccess = true;
                        break;
                    } catch (Exception e) {
                    	try {
							Thread.sleep(Constants.DRIVER_RECYLE_TIMEOUT_MS);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
                    	
                    	if (deviceIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
                    		// unhealthy device, remove from the active lab device list
                    		Logger.logMessage("A session failed to be spun up for emerging device type '" + activeEmergingOS.get().value() + "' on device with id '" 
                    				+ activeHarmonyDeviceID.get() + "'. This device has been removed from the active device farm.");
                    		ActiveDeviceManager.removeActiveDeviceID(activeDeviceID.get());
                    		LabDeviceManager.setDeviceInUse(activeEmergingOS.get(), activeHarmonyDeviceID.get(), false);
                            try { ProxyManager.stopProxyServer(); } catch (Exception e2) { /* ignore */ }
                    	} else {
                    		Logger.logMessage("Failed to start Emerging session on attempt '" + deviceIter 
                    			+ "' for device '" + activeHarmonyDeviceID.get() + "'. Retrying...");
                    		Logger.logMessage(e.getMessage());
                    		e.printStackTrace();
                    		
                    		// kill the proxy server
                    		try { ProxyManager.stopProxyServer(); } catch (Exception e2) { /* ignore */ }
                    	}
                    }
        		}
    			
        		if (sessionSuccess) {
    				break;
    			}
        		
        		if (farmIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
        			Logger.logConsoleMessage("FAILED TO START SESSION AFTER MULTIPLE ATTEMPTS ON MULTIPLE DEVICES!");
        		} else {
        			Logger.logConsoleMessage("Failed to start session on lab device with id '" + activeHarmonyDeviceID.get() + "' after multiple "
        				+ "attempts. Retrying on a different device...");
        		} 
    		}
    	} else { // LOCAL TEST RUN (DEBUG)
    		try {
    			if (localDebugProxyPort.get() != null) {
    				// set and start the proxy for the device prior to the app startup
    				ProxyManager.enableMITM();
    				ProxyManager.setProxyServer(localDebugProxyPort.get());
    				ProxyManager.startProxyServer();
    			}
    		} catch (Exception e) {
    			
    		}
    		try {
				if (filters.get() != null) {
					HashMap<Integer, BrowserMobProxyServer> integerBrowserMobProxyServerHashMap = ProxyManager.getAllProxyServers();

					for (Integer integer : integerBrowserMobProxyServerHashMap.keySet()) {
						for (HttpFiltersSource filter : filters.get()) {
							integerBrowserMobProxyServerHashMap.get(integer).addHttpFilterFactory(filter);
						}
					}
				}

				// set the app instance
				activeAppPackageLocation.set(pathOrUrlToApp);
				EmergingDriverManager.setEmergingOS(activeEmergingOS.get());
				EmergingDriverManager.setDeviceId(activeDeviceID.get());
				EmergingDriverManager.setHarmonyDeviceId(activeHarmonyDeviceID.get());
				EmergingDriverManager.setHarmonyHubHubIP(activeHarmonyHubIP.get());
				if (TestRun.isRoku()) {
					EmergingDriverManager.setDeviceIP(activeDeviceIP.get());
					EmergingDriverManager.setDeviceUsername(activeDeviceUsername.get());
					EmergingDriverManager.setDevicePassword(activeDevicePassword.get());
				} else if (TestRun.isFireTV()) {
					EmergingDriverManager.setDeviceId(activeDeviceID.get());
					EmergingDriverManager.setADBPath(activeADBPath.get());
					EmergingDriverManager.setAppId(activeAppID.get());
					EmergingDriverManager.setLaunchActivity(activeLaunchActivity.get());
				}
				EmergingDriverManager.setMachineIP(null);
				
				// install/launch
				if (TestRun.isAppleTV()) {
					launchAppleTVApp();
				} else if (TestRun.isRoku()) {
					launchRokuApp();
				} else if (TestRun.isFireTV()) {
					launchFireTVApp();
				}
            } catch (Exception e) {
            	Logger.logConsoleMessage("Failed to spin up a local Emerging session.");
            	e.printStackTrace();
            } 
    	}
    }
	
	public static void setRewritesAtStartup(List<HttpFiltersSource> rewriteFilters) {
    	filters.set(rewriteFilters);
    }
    
    public static void setLocalDebugProxyPort(Integer debugProxyPort) {
    	localDebugProxyPort.set(debugProxyPort);
    }
    
    public static void setLocalAppleTVDeviceDetails(String deviceID, String harmonyDeviceID, String harmonyHubIP) {
    	activeDeviceID.set(deviceID);
    	activeHarmonyDeviceID.set(harmonyDeviceID);
    	activeHarmonyHubIP.set(harmonyHubIP);
    }
    
    public static void setLocalRokuDeviceDetails(String deviceIP, String username, String password, String harmonyDeviceID, String harmonyHubIP) {
    	activeDeviceIP.set(deviceIP);
    	activeDeviceUsername.set(username);
    	activeDevicePassword.set(password);
    	activeHarmonyDeviceID.set(harmonyDeviceID);
    	activeHarmonyHubIP.set(harmonyHubIP);
    }
    
    public static void setLocalFireTVDeviceDetails(String deviceID, String harmonyDeviceID, String harmonyHubIP, String pathToADB) {
    	activeDeviceID.set(deviceID);
    	activeHarmonyDeviceID.set(harmonyDeviceID);
    	activeHarmonyHubIP.set(harmonyHubIP);
    	activeADBPath.set(pathToADB);
    }
    
    public static void setFireTVAppPackageDetails(String appID, String launchActivity) {
    	activeAppID.set(appID);
    	activeLaunchActivity.set(launchActivity);
    }
    
    public static void setTargetDevice(String deviceID) {
    	specificTargetDeviceID.set(deviceID);
    }
    
    private static void launchRokuApp() {
    	// delete the sideloaded app
    	Logger.logMessage("Removing any previously sideloaded test apps.");
    	String[] removeCommand = {"curl", "-u", activeDeviceUsername.get() + ":" + activeDevicePassword.get(), "-v", "-F", "mysubmit=Delete"
    	, "-F", "'archive= ' " + "'http://" + activeDeviceIP.get() + "/plugin_install'", "--digest"};
    	EmergingUtil.commandSender(removeCommand);

    	// install and launch the app
    	Logger.logMessage("Installing and launching app package.");
    	String[] installCommand = {"curl", "-u", activeDeviceUsername.get() + ":" + activeDevicePassword.get(),
    	"-v", "-F", "mysubmit=Install", "-F", "archive=@" + activeAppPackageLocation.get()
    	+ " http://" + activeDeviceIP.get() + "/plugin_install", "--digest"};
    	EmergingUtil.commandSender(installCommand);
    }
    
    private static void launchAppleTVApp() {
    	// install and launch the app
    	Logger.logMessage("Uninstalling/Installing and launching app package.");
    	String command = EmergingUtil.getiOSDeployPath() + " -i " + activeDeviceID.get() + " -b " + activeAppPackageLocation.get() + " -L -r";
    	if (GridManager.isQALabHub()) {
    		CommandExecutor.execCommand(command, activeMachineIP.get(), null);
    	} else {
    		CommandExecutor.execMultiCommand(command, null);
    	}
    }
    
    private static void launchFireTVApp() {
    	String adb = EmergingDriverManager.getADBPath();
    	
    	// uninstall any previously installed app
    	Logger.logMessage("Removing any previously installed app package.");
    	String[] removeCommand = {adb, "-s", activeDeviceID.get(), "uninstall", activeAppID.get()};
    	EmergingUtil.commandSender(removeCommand);
		
		// install the app
    	Logger.logMessage("Installing the app package.");
    	EmergingUtil.setTimeout(60); // TODO - hardwire the fire tvs via usb to drastically improve this time
    	String[] installCommand = {adb, "install", activeAppPackageLocation.get()};
    	EmergingUtil.commandSender(installCommand);
    	
    	// launch the app
    	Logger.logMessage("Launching the app package.");
    	String[] launchCommand = {adb, "shell", "am", "start", "-n", activeAppID.get() + "/" + activeLaunchActivity.get()};
    	EmergingUtil.commandSender(launchCommand);
    }
    
    private static HashMap<String, String> getEmergingDeviceInfo() {
    	if (TestRun.isRoku()) {
    		return LabDeviceManager.getRokuDeviceInfo(activeDeviceID.get());
    	} else if (TestRun.isAppleTV()) {
    		return LabDeviceManager.getAppleTVDeviceInfo(activeDeviceID.get());
    	} else if (TestRun.isFireTV()) {
    		return LabDeviceManager.getFireTVDeviceInfo(activeDeviceID.get());
    	}
    	
    	return null;
    }
    
}
