package com.softech.test.core.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AndroidMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.util.ExceptionUtils;
import org.littleshoot.proxy.HttpFiltersSource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import com.softech.test.core.driver.ancillary.UIPostSessionHandler;
import com.softech.test.core.lab.ActiveBrowserManager;
import com.softech.test.core.lab.ActiveDeviceManager;
import com.softech.test.core.lab.AvailableDevicePoller;
import com.softech.test.core.lab.BrowserNodeManager;
import com.softech.test.core.lab.EC2Manager;
import com.softech.test.core.lab.GlobalAbort;
import com.softech.test.core.lab.GlobalInstall;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.lab.LabDeviceUSBManager;
import com.softech.test.core.lab.ProxyUseLogger;
import com.softech.test.core.lab.SauceNodeManager;
import com.softech.test.core.lab.TestDeviceInfo;
import com.softech.test.core.lab.TimeoutCapabilities;
import com.softech.test.core.props.AgentLocationType;
import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.ChromeExtensions;
import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.props.DeviceCategory;
import com.softech.test.core.props.MQEDriverCaps;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.proxy.ProxyFactory;
import com.softech.test.core.proxy.ProxyManager;
import com.softech.test.core.proxy.ProxyRESTManager;
import com.softech.test.core.sauce.SauceCredentialManager;
import com.softech.test.core.sauce.SauceTunnelsManager;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

public class DriverFactory {

	private static final Integer DESKTOP_PROXY_START_RANGE = 10300;
	private static final Integer DESKTOP_PROXY_END_RANGE = 11000;
	
	// DRIVERS
	private static ThreadLocal<RemoteWebDriver> webDriver = new ThreadLocal<RemoteWebDriver>();
    private static ThreadLocal<AppiumDriver<MobileElement>> appiumDriver = new ThreadLocal<AppiumDriver<MobileElement>>();
    private static ThreadLocal<AndroidDriver<MobileElement>> androidDriver = new ThreadLocal<AndroidDriver<MobileElement>>();
    private static ThreadLocal<IOSDriver<MobileElement>> iOSDriver = new ThreadLocal<IOSDriver<MobileElement>>();
    
    // ACTIVE DEVICE
    private static Set<String> failedInstallDevices = new HashSet<String>();
    private static ThreadLocal<MobileOS> activeMobileOS = new ThreadLocal<MobileOS>();
    private static ThreadLocal<BrowserType> activeBrowserType = new ThreadLocal<BrowserType>();
    private static ThreadLocal<DesktopOSType> activeDesktopOSType = new ThreadLocal<DesktopOSType>();
    private static ThreadLocal<String> activeMobileOSVersion = new ThreadLocal<String>();
    private static ThreadLocal<DeviceCategory> activeDeviceCategory = new ThreadLocal<DeviceCategory>();
    private static ThreadLocal<String> activeDeviceID = new ThreadLocal<String>();
    private static ThreadLocal<String> activeMachineIP = new ThreadLocal<String>();
    private static ThreadLocal<String> activeMachineAgentLocation = new ThreadLocal<String>();
    private static ThreadLocal<String> activeMachineName = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceName = new ThreadLocal<String>();
    private static ThreadLocal<String> activeDeviceProxyPort = new ThreadLocal<String>();
    private static ThreadLocal<String> activeAppPackage = new ThreadLocal<String>();
    private static ThreadLocal<Boolean> resetDeviceNode = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return true;
    	}
    };
    
    private static ThreadLocal<List<HttpFiltersSource>> filters = new ThreadLocal<List<HttpFiltersSource>>() {
    	protected List<HttpFiltersSource> initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<List<String>> requestFilters = new ThreadLocal<List<String>>() {
    	protected List<String> initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<List<String>> responseFilters = new ThreadLocal<List<String>>() {
    	protected List<String> initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<Boolean> checkProxyLogsOnStartup = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
    private static ThreadLocal<Boolean> labAppInstallOnStartup = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    private static ThreadLocal<Boolean> installSuccess = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return true;
    	}
    };
    private static ThreadLocal<Boolean> installOnTest = new ThreadLocal<Boolean>();
    private static ThreadLocal<String> installAppPackageID = new ThreadLocal<String>();
    
    private static ThreadLocal<Integer> localDebugProxyPort = new ThreadLocal<Integer>() {
    	protected Integer initialValue() {
    		return null;
    	}
    };
    
    private static ThreadLocal<Boolean> ec2NodeOnline = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return true;
    	}
    };
    
    private static ThreadLocal<Boolean> maintenanceCheck = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
    private static ThreadLocal<Boolean> proxySpinupFailure = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
    
    /**********************************************************************************************
     * Initiates a new instance of Remote WebDriver.
     * 
     * @param capabilities - {@link DesiredCapabilities} - The capability object of the desired webdriver session.
     * @author Brandon Clark created February 1, 2016
     * @version 1.3 June 6, 2016
     ***********************************************************************************************/
    public static void initiateWebDriver(DesiredCapabilities capabilities) {
    	TestRun.setMobile(false);
    	prepDelay();
    	
    	// get the hub url and set the appropriate runtime environment (Sauce, Browserstack, QA Lab)
    	if (GridManager.isQALabHub()) {
    		Boolean sessionSuccess = false;
    		
    		// set os/desktop type and determine if sauce or lab
    		Boolean isSauce = true;
    		TestRun.setSauceRun(true);
    		Boolean isLab = false;
    		activeBrowserType.set(BrowserType.getEnumByString(capabilities.getBrowserName().toString()));
    		activeDesktopOSType.set(DesktopOSType.getEnumByString(capabilities.getCapability("platform").toString()));
    		if (activeDesktopOSType.get().equals(DesktopOSType.MQE_MAC) 
    			|| activeDesktopOSType.get().equals(DesktopOSType.MQE_WINDOWS)) {
    			isLab = true;
    			isSauce = false;
    			TestRun.setSauceRun(false);
    		}
    		
    		// check if user indicated specific geographic location
    		Object agentLocationCap = capabilities.getCapability(MQEDriverCaps.MQE_INTL_LOCATION.value());
    		AgentLocationType agentLocation = null;
    		if (agentLocationCap != null) {
    			agentLocation = AgentLocationType.getEnumByString(agentLocationCap.toString());
    		}
    		
    		for (int farmIter = 0; farmIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; farmIter++) {
    			if (capabilities.getCapability(Constants.MACHINE_NAME) == null 
    					|| capabilities.getCapability(Constants.MACHINE_NAME) == "") {
    				 // no specific node identified by the user grab one from the farm or from sauce
    				Boolean freeNodeAvailable = false;
    				while (!freeNodeAvailable) {
    					synchronized (DriverFactory.class) {
    						if (isSauce) {
    							// query for available sauce node and set
    							activeMachineIP.set(SauceNodeManager.getAvailableSauceNodeId());
    							
    						} else { // mqe lab
    							// set the active machine info
    							activeMachineIP.set(BrowserNodeManager.getAvailableBrowserNodeAddress(agentLocation, activeDesktopOSType.get(), activeBrowserType.get()));
    						}
    						
    						if (isSauce && activeMachineIP.get() != null) {
    							SauceNodeManager.setSauceNodeInUse(activeMachineIP.get(), true);
    							SauceNodeManager.setSauceNodeUseDuration(activeMachineIP.get(), System.currentTimeMillis());
    							ActiveBrowserManager.setActiveBrowserNode(activeMachineIP.get(), activeDesktopOSType.get(), activeBrowserType.get(), 
    									capabilities.getVersion());
    							freeNodeAvailable = true;
    							break;
    						}
    						
    						if (isLab && activeMachineIP.get() != null) {
    							// secondary warm check on browser node available
    							try { Thread.sleep(500); } catch (InterruptedException e) { }
    							activeMachineIP.set(BrowserNodeManager.getAvailableBrowserNodeAddress(activeMachineIP.get(), activeDesktopOSType.get(), activeBrowserType.get()));
    							
    							if (activeMachineIP.get() != null) {
    								// BROWSER NODE AVAILABLE FOR TEST
    								activeMachineAgentLocation.set(BrowserNodeManager.getBrowserNodeLocation(activeMachineIP.get()));
    	                        	capabilities = BrowserNodeManager.setTargetNodeCapability(capabilities, activeMachineIP.get(), activeMachineAgentLocation.get(), activeDesktopOSType.get(), activeBrowserType.get());
    	                            ActiveBrowserManager.setActiveBrowserNode(activeMachineIP.get(), activeDesktopOSType.get(), activeBrowserType.get(), null);
    	                            BrowserNodeManager.addBrowserNodeTypeInUse(activeMachineIP.get(), activeBrowserType.get());
    	                            BrowserNodeManager.setBrowserNodeTypeUseDuration(activeMachineIP.get(), activeBrowserType.get(), System.currentTimeMillis());
    	                            freeNodeAvailable = true;
    	                            break;
    							}
            				}
        				}
    					
    					if (!freeNodeAvailable) {
        					AvailableDevicePoller.pollFarmForBrowser(activeMachineIP.get(), activeBrowserType.get(), false);
        	        	}
    				}
    				
    				// ec2 online if necessary 
                    if (activeDesktopOSType.get().equals(DesktopOSType.MQE_WINDOWS)) {
                    	if (EC2Manager.isEC2Machine(activeMachineIP.get())) {
                    		if (!EC2Manager.isEC2MachineOnline(activeMachineIP.get())) {
                    			String instanceID = EC2Manager.getEC2MachineInstanceId(activeMachineIP.get());
                    			EC2Manager.startEC2Instance(instanceID);
                    			ec2NodeOnline.set(EC2Manager.waitForEC2InstanceOnline(instanceID));
                    			
                    			if (ec2NodeOnline.get()) {
                    				EC2Manager.setEC2MachineOnline(activeMachineIP.get());
                    			}
                    		}
                    		
                    		if (!maintenanceCheck.get()) {
                    			EC2Manager.setEC2MachineOnlineStartTime(activeMachineIP.get(), System.currentTimeMillis());
                    		}
                    	}
                    }
            	} else { // specific node identified
            		activeMachineName.set(capabilities.getCapability(Constants.MACHINE_NAME).toString());
        			String machineAddress = LabDeviceManager.getMachineIP(activeMachineName.get());
        			
        			Boolean specificNodeAvailable = false;
					while (!specificNodeAvailable) {
						synchronized(DriverFactory.class) {
							activeMachineIP.set(BrowserNodeManager.getAvailableBrowserNodeAddress(machineAddress, activeDesktopOSType.get(), activeBrowserType.get()));
							if (activeMachineIP.get() != null) {
								specificNodeAvailable = true;
								activeMachineAgentLocation.set(BrowserNodeManager.getBrowserNodeLocation(activeMachineIP.get()));
								capabilities = BrowserNodeManager.setTargetNodeCapability(capabilities, activeMachineIP.get(), activeMachineAgentLocation.get(), activeDesktopOSType.get(), activeBrowserType.get());
			    	        	ActiveBrowserManager.setActiveBrowserNode(activeMachineIP.get(), activeDesktopOSType.get(), activeBrowserType.get(), null);
			            	    BrowserNodeManager.addBrowserNodeTypeInUse(activeMachineIP.get(), activeBrowserType.get());
								BrowserNodeManager.setBrowserNodeTypeUseDuration(activeMachineIP.get(), activeBrowserType.get(), System.currentTimeMillis());
								break;
							}
						}
						
						if (!specificNodeAvailable) {
							AvailableDevicePoller.pollFarmForBrowser(activeMachineName.get(), activeBrowserType.get(), true);
						}
					}
            	}
    			
    			Exception initiationException = null;
        		for (int machineIter = 0; machineIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; machineIter++) {
        			// attempt to start a session
        			try {
        				// set the proxy type as indicated by the user
        				ProxyFactory.setProxyType(capabilities);
        				
        				// get an unused proxy port
        				activeDeviceProxyPort.set(ProxyFactory.getUnusedProxyPort(DESKTOP_PROXY_START_RANGE, 
        						DESKTOP_PROXY_END_RANGE));
        				
        				// get the proxy host
        				String proxyHost = BrowserNodeManager.getBrowserNodeProxyHost(activeMachineIP.get());
        		        
        				// assign the proxy to the browser
        				if (isLab) {
        					capabilities = ProxyFactory.assignProxyToBrowser(capabilities, activeBrowserType.get(), activeMachineIP.get(), 
        							proxyHost, activeDeviceProxyPort.get());
                			ActiveBrowserManager.setActiveBrowserNodeProxy(proxyHost, Integer.parseInt(activeDeviceProxyPort.get()));
        				}
            			
        				// enable mitm for bmp (non rest)
        	            if (!isSauce) {
        					ProxyFactory.enableMITM(capabilities);
        				}
        	            
        	            // start the proxy
        				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            			ProxyFactory.startProxyServer(null, activeDeviceProxyPort.get());
            			
            			// if sauce, start the tunnel
            			Boolean sauceConnected = true;
            			if (isSauce && ProxyFactory.isBMP()) {
            				SauceTunnelsManager.downloadSC();
            				String tunnelId = RandomData.getCharacterString(40);
            				sauceConnected = SauceTunnelsManager.startSauceTunnel(tunnelId, activeDeviceProxyPort.get());
            				if (sauceConnected) {
            					SauceNodeManager.setSauceNodeTunnelId(activeMachineIP.get(), tunnelId);
            					capabilities.setCapability("tunnelIdentifier", SauceTunnelsManager.getSauceTunnelId());
            				} else {
            					Logger.logConsoleMessage("Failed to spinup sauce node/tunnel after multiple attempts!");
            					SauceNodeManager.setSauceNodeInUse(activeMachineIP.get(), false);
								SauceNodeManager.setSauceNodeUseDuration(activeMachineIP.get(), 0L);
            					throw new RuntimeException("Failed to spinup sauce node/tunnel after multiple attempts!");
            				}
            			}
            			
            			// set any proxy rewrites prior to session startup
            			ProxyFactory.setRewritesPriorToSessionStart(filters.get(), requestFilters.get(), responseFilters.get());
            			
            			// if the ec2 node failed to startup, throw an exception and give it another try
            			if (!ec2NodeOnline.get()) {
            				Logger.logConsoleMessage("Failed to spinup ec2 node.");
            				throw new RuntimeException("Failed to spinup ec2 node!");
            			}
            			
            			// if ie ignore zoom setting
            			if (activeBrowserType.get().equals(BrowserType.IEXPLORE)) {
            				capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
            			}
            			
            			// set page load strategy to none
            			capabilities.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, "none");
            			
            			// if chrome, enable some additional settings as needed
            			if (activeBrowserType.get().equals(BrowserType.CHROME)) {
            				String[] chromeSwitches = {"--always-authorize-plugins","--allow-outdated-plugins"};
            				capabilities.setCapability("chrome.switches", Arrays.asList(chromeSwitches));
            				
            				// install extensions if indicated
            				capabilities = ChromeExtensions.applyChromeExtension(capabilities, activeDesktopOSType.get());
            			}
            			
        				// start the session
            			DriverManager.setWebDriver(new RemoteWebDriver(new URL(getHubAddress()), capabilities));
            			
            			// TODO - open a test page and validate the presence of a https element otherwise don't release the test session
            			
            			// command to verify driver is up and responding
            			DriverManager.getWebDriver().getWindowHandle();
            			
            			// if ie set the zoom setting to 100%
            			try {
            				if (activeBrowserType.get().equals(BrowserType.IEXPLORE)) {
                				DriverManager.getWebDriver().findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, "0"));
                			}
            			} catch (Exception e) {
            				Logger.logConsoleMessage("Failed to set IE Zoom setting to 100%.");
            			}
            			
            			// set the browser node in use
            			Logger.logMessage("Webdriver session successfully created on '" + activeMachineName.get() + "'" 
                        		+ " for desktop os '" + activeDesktopOSType.get().value() + "' for browser '" + activeBrowserType.get().value() 
                        		+ "' on '" + activeMachineIP.get() + "'.");
                        sessionSuccess = true;
                        break;
                    } catch (Exception e) {
                    	initiationException = e;
                    	e.printStackTrace();
                    	
                    	// if a maintenance check is being performed do not execute retry protocol
                        if (maintenanceCheck.get()) {
                        	break;
                        }
                        
                    	// stop the driver (if necessary)
                    	try {
                    		if (DriverManager.getWebDriver() != null) {
                        		if (DriverManager.getWebDriver().getSessionId() != null) {
                        			DriverManager.getWebDriver().quit();
                        		}
                        	}
                    	} catch (Exception e2) {
                    		Logger.logConsoleMessage("Failed to terminate driver on driver spinup failure.");
                    	}
                    	
                    	try {
							Thread.sleep(Constants.DRIVER_RECYLE_TIMEOUT_MS);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
                    	
                    	if (machineIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
                    		// unhealthy machine, remove from the active lab machine list
                    		Logger.logMessage("A session failed to be spun up on lab machine '" + activeMachineName.get() 
                    				+ "' with session ip '" + activeMachineIP.get() + "' for browser '" + activeBrowserType.get().value() 
                    				+ "'. This machine has been removed from the active machine farm.");
                    		try { ProxyFactory.stopProxyServer(); } catch (Exception e2) { /* ignore */ }
                    		BrowserNodeManager.removeBrowserNodeTypeInUse(activeMachineIP.get(), activeBrowserType.get());
    						BrowserNodeManager.setBrowserNodeTypeUseDuration(activeMachineIP.get(), activeBrowserType.get(), 0L);
            	        	BrowserNodeManager.setBrowserNodeTypeHealth(activeMachineIP.get(), activeBrowserType.get(), true);
                    	} else {
                    		Logger.logMessage("Failed to start Webdriver session on attempt '" + machineIter 
                    			+ "' for machine '" + activeMachineName.get() + "' with ip '" + activeMachineIP.get() + "' for browser '" 
                    			+ activeBrowserType.get().value() + "'. Retrying...");
                    		
                    		try { ProxyFactory.stopProxyServer(); } catch (Exception e2) { /* ignore */ }
                    		try { Thread.sleep(Constants.DRIVER_NODE_RECYLE_PAUSE_MS); } catch (InterruptedException e2) { }
                    	
                    		// reset the browser node
                    		try {
                    			if (BrowserNodeManager.getRunningBrowserNodeTypeCount(activeMachineIP.get(), activeBrowserType.get()) <= 1) {
                        			if (activeBrowserType.get().equals(BrowserType.CHROME)) {
                            			BrowserNodeManager.resetChrome(activeMachineIP.get(), activeDesktopOSType.get());
                            		} else if (activeBrowserType.get().equals(BrowserType.IEXPLORE)) {
                            			BrowserNodeManager.resetIExplore(activeMachineIP.get());
                            		} else if (activeBrowserType.get().equals(BrowserType.FIREFOX)) {
                            			BrowserNodeManager.resetFirefox(activeMachineIP.get(), activeDesktopOSType.get());
                            		} else if (activeBrowserType.get().equals(BrowserType.SAFARI)) {
                            			BrowserNodeManager.resetSafari(activeMachineIP.get());
                            		} else if (activeBrowserType.get().equals(BrowserType.EDGE)) {
                            			BrowserNodeManager.resetEdge(activeMachineIP.get());
                            		}
                        		}
                    		} catch (Exception e2) {
                    			Logger.logConsoleMessage("Failed to reset browser on session spin up failure.");
                    		}
                    	}
                    }
        		}
    			
        		if (sessionSuccess || maintenanceCheck.get()) {
    				break;
    			}
        		
        		if (farmIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
        			Logger.logConsoleMessage("FAILED TO START WEBDRIVER SESSION AFTER MULTIPLE ATTEMPTS ON MULTIPLE MACHINES!");
        			if (initiationException != null) {
        				initiationException.printStackTrace();
        			}
        		} else {
        			Logger.logConsoleMessage("Failed to start Webdriver session on lab machine '" + activeMachineName.get() + "' after multiple "
        				+ "attempts. Retrying on a different machine...");
        		} 
    		}
    	} else { // LOCAL TEST RUN OR CLOUD BASED WEB EXECUTION (sauce)
    		try {
    			TestRun.setMobile(false);
    			
    			// set the debug proxy port
    			activeDeviceProxyPort.set(localDebugProxyPort.get().toString());
    			
    			if (!TestRun.isSauceRun()) {
    				// set the proxy capability
    				Proxy seleniumProxy = new Proxy();
    	            String serverAddress = "localhost:" + activeDeviceProxyPort.get();
    	            seleniumProxy.setHttpProxy(serverAddress).setSslProxy(serverAddress).setFtpProxy(serverAddress);
    	            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
    			}
    			
    			// start the proxy
    			ProxyFactory.setProxyType(capabilities);
    			ProxyFactory.enableMITM(capabilities);
    			ProxyFactory.startProxyServer(null, activeDeviceProxyPort.get());
    			ProxyFactory.setRewritesPriorToSessionStart(filters.get(), requestFilters.get(), responseFilters.get());
				
				// if sauce, start the tunnel
    			Boolean sauceConnected = true;
    			if (TestRun.isSauceRun()) {
    				SauceTunnelsManager.downloadSC();
    				String tunnelId = RandomData.getCharacterString(40);
    				sauceConnected = SauceTunnelsManager.startSauceTunnel(tunnelId, activeDeviceProxyPort.get());
    				if (sauceConnected) {
    					capabilities.setCapability("tunnelIdentifier", SauceTunnelsManager.getSauceTunnelId());
    				} else {
    					Logger.logConsoleMessage("Failed to spinup sauce node/tunnel after multiple attempts!");
    					throw new RuntimeException("Failed to spinup sauce node/tunnel after multiple attempts!");
    				}
    			}
				
    			// set the driver instance
		        webDriver.set(new RemoteWebDriver(new URL(getHubAddress()), capabilities));
		        DriverManager.setWebDriver(webDriver.get()); 
            } catch (Exception e) {
            	Logger.logConsoleMessage("Failed to spin up a Webdriver session.");
            	e.printStackTrace();
            } 
    	}
    }
    
    /**********************************************************************************************
     * Initiates a new instance of Appium driver and the corresponding iOSDriver or AndroidDriver
     * 
     * @param mobileOS - {@link MobileOS} - The device mobile OS (iOS or Android).
     * @param deviceCategory - {@link DeviceCategory} - The device category to run against (Phone or Tablet).
     * @param capabilities - {@link DesiredCapabilities} - The capability object of the desired appium session.
     * @author Brandon Clark created February 1, 2016
     * @version 1.4 November 28, 2016
     ***********************************************************************************************/
    public static void initiateAppiumDriver(MobileOS mobileOS, DeviceCategory deviceCategory, DesiredCapabilities capabilities) {
    	// set the mobile os and the device category
    	TestRun.setMobile(true);
    	TestRun.setSauceRun(false);
    	ProxyFactory.setProxyType(capabilities);
    	prepDelay();
    	
    	// set if it's mobile web or not
    	if (capabilities.getBrowserName().equals("Safari") || capabilities.getBrowserName().equals("Chrome")) {
    		TestRun.setMobileWebRun(true);
    	}
    	
    	if (mobileOS != null && deviceCategory != null) {
    	    TestRun.setMobileOS(mobileOS);
    	    activeMobileOS.set(mobileOS);
    	    TestRun.setDeviceCategory(deviceCategory);
    	    activeDeviceCategory.set(deviceCategory);
    	    
    	    // indicate if the test is selendroid
    	    if (capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME) != null) {
    	    	if (capabilities.getCapability(MobileCapabilityType.AUTOMATION_NAME) == "Selendroid") {
    	    		TestRun.setSelendroidRun(true);
    	    	}
    	    }
    	}
    	
    	// set the ios app id as it gets stripped out on a session failure and we need to reapply it
    	String iosAppId = null;
    	if (TestRun.isIos() && !TestRun.isMobileWeb()) {
    		iosAppId = capabilities.getCapability(MobileCapabilityType.APP).toString();
    	}
    	
    	// get the hub url and set the appropriate runtime environment (Sauce, Browserstack, QA Lab)
    	if (GridManager.isQALabHub()) {
    		Boolean sessionSuccess = false;
    		Exception initiationException = null;
    		for (int farmIter = 0; farmIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; farmIter++) {
    			// check if the user explicitly indicated a target device
        		if (capabilities.getCapability(Constants.DEVICE_ID) == null 
    					|| capabilities.getCapability(Constants.DEVICE_ID) == "") {
        			// no device specified, grab a free device from the farm
        			
        			List<String> allUserExcludedDevices = new ArrayList<String>(Arrays.asList(""));
    				
    				// exclude any versions the user didn't specify. TODO want to tear all this out and have
    				// everything be a custom MQE capability that gets passed instead of custom methods within the driver
    				String[] targetVersions = null;
    				if (capabilities.getCapability("setTargetDeviceVersions") != null) {
    					targetVersions = capabilities.getCapability("setTargetDeviceVersions").toString()
        						.replaceAll(" ", "").split(",");
    					
    					if (targetVersions != null) {
    						for (String deviceID : LabDeviceManager.getAllDeviceIDs(mobileOS)) {
    							Boolean targetDeviceOS = false;
    							String deviceOS = LabDeviceManager.getDeviceOSVersion(LabDeviceManager
                						.getDeviceMachineIPAddress(deviceID), mobileOS);
    							for (String targetVersion : targetVersions) {
    								if (deviceOS.contains(targetVersion)) {
    									targetDeviceOS = true;
    									break;
    								}
    							}
    							if (!targetDeviceOS) {
    								allUserExcludedDevices.add(deviceID);
    							}
    						}
        				}
    				}
    				
    				// add any devices the user explicitLy indicated they wanted to exclude
    				if (capabilities.getCapability("excludedDevices") != null) {
    					List<String> explicitExcludedDevices = new ArrayList<String>(Arrays.asList(capabilities.getCapability("excludedDevices")
                			    .toString().split(",")));
    					
    					for (String explicitDevice : explicitExcludedDevices) {
    						allUserExcludedDevices.add(explicitDevice);
    					}
    				}
    				
    				// if ios mobile web, exclude any non ios 10 devices as we only support 10 and higher currently
    				if (TestRun.isMobileWeb() && TestRun.isIos()) {
    					for (String deviceID : LabDeviceManager.getAllDeviceIDs(mobileOS)) {
    						String osVersion = LabDeviceManager.getDeviceOSVersion(LabDeviceManager
        							.getDeviceMachineIPAddress(deviceID), mobileOS);
    						if (!osVersion.contains("10.")) {
    							allUserExcludedDevices.add(deviceID);
    						}
    					}
    				}
    				
    				Boolean deviceAvailable = false;
    				while (!deviceAvailable) {
    					List<String> availableDeviceFarm = null;
    					synchronized (DriverFactory.class) {
    						availableDeviceFarm = LabDeviceManager.getAllUnusedDeviceIDs(mobileOS, deviceCategory);
    					
    						if (!availableDeviceFarm.isEmpty()) {
        						for (String deviceID : availableDeviceFarm) {
        							// get the device info
        							activeDeviceID.set(deviceID);
        							HashMap<String, String> deviceInfo = TestRun.isIos() ? LabDeviceManager.getIOSDeviceInfo(activeDeviceID.get()) 
        								: LabDeviceManager.getAndroidDeviceInfo(activeMobileOS.get(), activeDeviceID.get());
                					// set the active device info
                					activeMachineIP.set(deviceInfo.get("machine_ip"));
                					activeMobileOSVersion.set(deviceInfo.get("device_os_version"));
                					activeDeviceName.set(deviceInfo.get("device_name"));
            	        	    	activeDeviceProxyPort.set(deviceInfo.get("device_proxy_port"));
            	        	    	activeMachineName.set(LabDeviceManager.getDeviceMachineName(activeDeviceID.get()));
            	        	    	
            	        	    	// set the app package (if testing a native app)
            	        	    	if ((activeMobileOS.get().equals(MobileOS.IOS) || activeMobileOS.get().equals(MobileOS.IOS_SIM)) && !TestRun.isMobileWeb()) {
            	        	    		activeAppPackage.set(capabilities.getCapability(MobileCapabilityType.APP).toString());
            	        	    	} else if ((activeMobileOS.get().equals(MobileOS.ANDROID) || activeMobileOS.get().equals(MobileOS.ANDROID_SIM)) && !TestRun.isMobileWeb()) {
            	        	    		if (capabilities.getCapability(AndroidMobileCapabilityType.APP_PACKAGE) != null) {
            	        	    			activeAppPackage.set(capabilities.getCapability(AndroidMobileCapabilityType.APP_PACKAGE).toString());
            	        	    		}
            	        	    	}
            	        	    	
            	        	    	// user exclude device
            	        	    	Boolean excluded = allUserExcludedDevices.contains(activeDeviceID.get());
            	        	    	
            	        	    	// secondary warm check on device in use
            	        	    	SleepUtils.sleep(RandomData.getInteger(250, 1000));
            	        	    	Boolean warmDeviceInUse = LabDeviceManager.isDeviceInUse(activeDeviceID.get());
            	        	    	
            	        	    	// proxy check
            	        	    	Boolean proxyInUse = ProxyFactory.isProxyPortInUse(Integer.parseInt(activeDeviceProxyPort.get()));
            	        	    	
            	        	    	// is the device ready
            	        	    	Boolean deviceReady = false;
            	        	    	if (!warmDeviceInUse && !excluded && !proxyInUse) {
            	        	    		deviceReady = true;
            	        	    	}
            	        	    	
            	        	    	// DEVICE IS A MATCH!
            	        	    	if (deviceReady) {
            	        	    		// DEVICE AVAILABLE FOR TEST
            	        	    		LabDeviceManager.setDeviceInUse(activeDeviceID.get(), true);
                	        	    	LabDeviceManager.setDeviceUseDuration(activeDeviceID.get(), System.currentTimeMillis());
                	        	    	ActiveDeviceManager.setActiveDevice(activeDeviceID.get());
                	        	    	ActiveDeviceManager.addActiveDevice(activeDeviceID.get());
                	        	    	capabilities.setCapability(MobileCapabilityType.VERSION, activeDeviceID.get());
                	        	    	deviceAvailable = true;
                	        	    	TestDeviceInfo.initDeviceInfo(activeMobileOS.get(), activeDeviceCategory.get(), activeMachineName.get(), deviceInfo);
                	        	    	break;
            	        	    	}
            					}
        					}
    					}
    					
    					if (!deviceAvailable) {
    						AvailableDevicePoller.pollFarmForDevice(activeMobileOS.get(), activeDeviceID.get(), false);
    	        		}	
    				}
            } else {
            	// specific device identified by the user!
            	activeDeviceID.set(capabilities.getCapability(Constants.DEVICE_ID).toString());
        		HashMap<String, String> deviceInfo = TestRun.isIos() ? LabDeviceManager.getIOSDeviceInfo(activeDeviceID.get()) 
        			: LabDeviceManager.getAndroidDeviceInfo(activeMobileOS.get(), activeDeviceID.get());
				activeMachineIP.set(deviceInfo.get("machine_ip"));
				activeDeviceName.set(deviceInfo.get("device_name"));
        	    activeDeviceProxyPort.set(deviceInfo.get("device_proxy_port"));
        	    if (!TestRun.isMobileWeb()) {
        	    	if (activeMobileOS.get().equals(MobileOS.IOS)) {
            	    	activeAppPackage.set(capabilities.getCapability(MobileCapabilityType.APP).toString());
            	    } else {
            	    	if (capabilities.getCapability(AndroidMobileCapabilityType.APP_PACKAGE) != null) {
            	    		activeAppPackage.set(capabilities.getCapability(AndroidMobileCapabilityType.APP_PACKAGE).toString());
            	    	}
            	    }
        	    }
        	    activeMachineName.set(LabDeviceManager.getDeviceMachineName(activeDeviceID.get()));
        	    TestDeviceInfo.initDeviceInfo(activeMobileOS.get(), activeDeviceCategory.get(), activeMachineName.get(), deviceInfo);
    	    	
        	    // check if the requested device is in use and poll until available
        	    while (LabDeviceManager.isDeviceInUse(activeDeviceID.get())) {
        	    	SleepUtils.sleep(RandomData.getInteger(250, 1000));
        	    	Boolean inUse = LabDeviceManager.isDeviceInUse(activeDeviceID.get());
        	    	
        	    	if (inUse) {
        	    		AvailableDevicePoller.pollFarmForDevice(activeMobileOS.get(), activeDeviceID.get(), true);
        	    	}
        	    }
					
        	    LabDeviceManager.setDeviceInUse(activeDeviceID.get(), true);
        	    LabDeviceManager.setDeviceUseDuration(activeDeviceID.get(), System.currentTimeMillis());
        		ActiveDeviceManager.setActiveDevice(activeDeviceID.get());
        	    ActiveDeviceManager.addActiveDevice(activeDeviceID.get());
            	capabilities.setCapability(MobileCapabilityType.VERSION, activeDeviceID.get());
            }
    		
            for (int deviceIter = 0; deviceIter <= Constants.DRIVER_MAX_SESSION_ATTEMPTS; deviceIter++) {
            	// set timeout overrides
            	capabilities = TimeoutCapabilities.setMobileTimeouts(capabilities);
            	
        		// attempt to start a session
        		try {
        			// iOS 10/11 handling
            		if (activeMobileOS.get().equals(MobileOS.IOS) || activeMobileOS.get().equals(MobileOS.IOS_SIM)) {
            			String osVersion = LabDeviceManager.getDeviceOSVersion(activeMachineIP.get(), MobileOS.IOS);
            			if (osVersion.contains("10.") || osVersion.contains("11.")) {
            				TestRun.setXCUITest(true);
            				capabilities.setCapability(MobileCapabilityType.AUTOMATION_NAME, "XCUITest");
            				if (Boolean.parseBoolean(System.getenv("IOS_XCODE_USE_CORP_ACCOUNT"))) {
            					capabilities.setCapability("xcodeOrgId", System.getenv("IOS_XCODE_ORG_ID"));
            					capabilities.setCapability("xcodeSigningId", System.getenv("IOS_XCODE_SIGNING_ID"));
            				}
            				
    	        	    	if (!TestRun.isSimulator()) {
    	        	    		if (!TestRun.isMobileWeb()) {
    	    						capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
    	    					}
    	        	    		capabilities.setCapability("bundleId", iosAppId);
    	        	    		capabilities.setCapability(MobileCapabilityType.APP, "");
    	        	    	}
    	        	    } else {
    	        	    	TestRun.setXCUITest(false);
    	        	    }
            		}
            		
        			// clean the logs
        			LabDeviceManager.cleanAppiumLog(activeMobileOS.get(), activeMachineIP.get());
        			if (mobileOS == MobileOS.ANDROID) {
        			    LabDeviceManager.clearADBLogs();
        			}
        				
        			// ensure the device's usb hub is active
        			Integer usbHub = TestRun.isAndroid() ? 1 : 2;
        			LabDeviceUSBManager.enableUSBPort(activeMachineIP.get(), usbHub);
        			
        			// wake up the android device
        			if (mobileOS == MobileOS.ANDROID) {
        				LabDeviceManager.unlockDevice(activeMachineIP.get());
        			}
        				
        			// add the proxy to the log of in use by job
        			if (!maintenanceCheck.get()) {
        				ProxyUseLogger.initUseLog(activeMobileOS.get(), activeDeviceProxyPort.get());
        			}
        			
        			// set and start the proxy for the device prior to the app startup
        			proxySpinupFailure.set(false);
        			ProxyFactory.enableMITM(capabilities);
        			ProxyFactory.startProxyServer(null, activeDeviceProxyPort.get());
        			if (!ProxyFactory.isProxyStarted()) {
        				proxySpinupFailure.set(true);
        				throw new RuntimeException("The proxy was not started on '" + activeDeviceProxyPort.get() + "'.");
        			}
        			
            		// set any proxy rewrites prior to session startup
        			ProxyFactory.setRewritesPriorToSessionStart(filters.get(), requestFilters.get(), responseFilters.get());
            			
        			// install the app as the user has indicated
        			if (labAppInstallOnStartup.get()) {
        				installSuccess.set(GlobalInstall.installApp(capabilities, installOnTest.get(), activeDeviceID.get(), 
        					installAppPackageID.get()));
        			}
        				
        			// fail if the install failed (abort if too many failed installs)
        			if (labAppInstallOnStartup.get() && !installSuccess.get()) {
        				failedInstallDevices.add(activeDeviceID.get());
        				if (failedInstallDevices.size() >= Integer.parseInt(System.getenv("MAX_DEVICE_INSTALL_FAILS")) 
        						&& !maintenanceCheck.get()) {
        					GlobalAbort.terminateTestSuite("TOO MANY FAILED APP INSTALLS!");
        				}
        				throw new RuntimeException("App failed to install on device '" + activeDeviceID.get() + "'");
        			}
        				
        			// selendroid app install handling
        			if (TestRun.isSelendroid()) {
        				capabilities.setCapability(MobileCapabilityType.APP, GlobalInstall.getNodeAppFile());
        			}
        				
            		// start the session
            		if (capabilities.getCapability(MobileCapabilityType.VERSION) == null) {
            			Logger.logConsoleMessage("Attempting to start a session without a device id identified, "
            			    + "either explicitly by the user or via the virtual device farm. The grid will "
            			    + "attempt to find any matching device but there's no guarantee it will meet "
            			    + "the requested runtime requirements.");
            		}
            		
            		initAppiumDrivers(getHubAddress(), capabilities);
            		
            		// check app network connectivity (if the user indicated)
            		if (checkProxyLogsOnStartup.get()) {
            			new FluentWait<WebDriver>(DriverManager.getAppiumDriver()).withTimeout(Constants.PROXY_LOG_TIMEOUT_S, TimeUnit.SECONDS)
                	        .withMessage("Proxy connection not established on device '" + activeDeviceID.get() + "' with proxy port '" 
            				    + activeDeviceProxyPort.get() + "' on machine '" + activeMachineIP.get() + "'.")
                	        .pollingEvery(Constants.PROXY_LOG_POLLING_MS, TimeUnit.MILLISECONDS).until(new ExpectedCondition<Boolean>() {
                	        	@Override public Boolean apply(WebDriver input) {
                	        		if (ProxyFactory.isBMP()) {
                	        			return ProxyManager.getLogEntries().size() > 0;
                	        		} else {
                	        			return ProxyRESTManager.getLogEntries().size() > 0;
                	        		}
                	        	}
                	    });
            		}
            			
            		// check for android appium settings crash post startup
            		if (TestRun.isAndroid()) {
            			UIPostSessionHandler.handleAndroidAppiumSettingsCrashOnStart();
            		}
            		
            		// check for ios allow alerts and auto accept or accept per capability indicator
            		if (TestRun.isIos()) {
            			UIPostSessionHandler.handleiOSAlertsOnStart(capabilities);
            		}
            		
            		Logger.logMessage("Appium session successfully created for an " + activeMobileOS.get().toString() 
                        + " " + activeDeviceCategory.get().toString() + " device with device id '" + activeDeviceID.get() 
                        + "' on '" + activeMachineIP.get() + "' with proxy port '" + activeDeviceProxyPort.get() + ".");
            		
                    sessionSuccess = true;
                    break;
        		} catch (Exception e) {
        			initiationException = e;
        			Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
        			
        			// if a maintenance check is being performed do not execute retry protocol, and throw 
        			// the exception from the driver spinup
                    if (maintenanceCheck.get()) {
                    	throw new RuntimeException(e);
                    }
                    
        			// stop the driver (if necessary)
                    try {
                    	if (DriverManager.getAppiumDriver() != null) {
                        	if (DriverManager.getAppiumDriver().getSessionId() != null) {
                        		DriverManager.getAppiumDriver().quit();
                        	}
                        }
                    } catch (Exception e2) {
                    	Logger.logConsoleMessage("Failed to terminate driver.");
                    	e2.printStackTrace();
                    }
                    	
                    if (ProxyFactory.isProxyStarted()) {
                    	try { ProxyFactory.stopProxyServer(); } catch (Exception e2) { /* ignore */ }
                    }
                	
                    try {
						Thread.sleep(Constants.DRIVER_RECYLE_TIMEOUT_MS);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
                    	
                    if (deviceIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
                    	// unhealthy device, remove from the active lab device list
                    	Logger.logMessage("A session failed to be spun up on lab machine '" + activeMachineName.get() 
                    		+ "' with session ip '" + activeMachineIP.get() + "' for device '" + activeDeviceName.get() 
                    		+ "' with id '" + activeDeviceID.get() + "'. This device has been removed from "
                    		+ "the active device farm.");
                    	ActiveDeviceManager.removeActiveDeviceID(activeDeviceID.get());
                    	LabDeviceManager.setDeviceHealthDuringTest(activeDeviceID.get(), true);
                    	LabDeviceManager.setDeviceInUse(activeDeviceID.get(), false);
                        LabDeviceManager.setDeviceUseDuration(activeDeviceID.get(), 0L);
                    } else {
                    	Logger.logMessage("Failed to start Appium session on attempt '" + deviceIter 
                    		+ "' for device '" + activeDeviceID.get() + "' on machine '" + activeMachineIP.get() + "'. Resetting remote "
                    		+ "node and retrying...");
                    	
                    	// kill the problematic machine node and reboot device
                    	if (resetDeviceNode.get()) {
                    		try {
                    			// reboot the device if not an install issue, and it wasn't a proxy spin up issue
                    			// and the reboot device setting is enabled.
                    			if (!proxySpinupFailure.get() && installSuccess.get()
                    					&& Boolean.parseBoolean(System.getenv("LAB_REBOOT_DEVICES"))) {
                    				LabDeviceManager.rebootDevice(activeDeviceID.get());
                    				SleepUtils.sleep(90000);
                    			}
                    		} catch (Exception e2) {
                    			Logger.logConsoleMessage("Failed to restart device.");
                        		e2.printStackTrace();
                    		}
                    		
                    		try {
                    			GridManager.resetRemoteMobileNode(activeMobileOS.get(), activeMachineIP.get());
                        	} catch (Exception e2) {
                        		Logger.logConsoleMessage("Failed to reset remote node.");
                        		e2.printStackTrace();
                        	}
                    	} else {
                    		SleepUtils.sleep(Constants.DRIVER_NODE_RECYLE_PAUSE_MS);
                    	}
                    }
                }
        	}
    			
        	if (sessionSuccess || maintenanceCheck.get()) {
    			break;
    		}
        		
        	if (farmIter == Constants.DRIVER_MAX_SESSION_ATTEMPTS) {
        		Logger.logConsoleMessage("FAILED TO START APPIUM SESSION AFTER MULTIPLE ATTEMPTS ON MULTIPLE DEVICES!");
        		if (initiationException != null) {
        			initiationException.printStackTrace();
        		}
        	} else {
        		Logger.logConsoleMessage("Failed to start Appium session on lab device with id '" + activeDeviceID.get() + "' after multiple "
        			+ "attempts. Retrying on a different device...");
        	} 
    	}
    } else { // LOCAL TEST RUN (DEBUG)
    	try {
    		if (localDebugProxyPort.get() != null) {
    			// set and start the proxy for the device prior to the app startup
    			ProxyFactory.enableMITM(capabilities);
    			ProxyFactory.startProxyServer(null, localDebugProxyPort.get().toString());
    			ProxyFactory.setRewritesPriorToSessionStart(filters.get(), requestFilters.get(), responseFilters.get());
    		}
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Proxy failed to start!");
    		e.printStackTrace();
    	}
    		
    	try {
			// ios 10 handling
			if (capabilities.getCapability("automationName") != null) {
				if (capabilities.getCapability("automationName").equals("XCUITest")) {
					TestRun.setXCUITest(true);
					if (!TestRun.isMobileWeb()) {
						capabilities.setCapability(CapabilityType.BROWSER_NAME, "");
					}
					capabilities.setCapability("bundleId", capabilities.getCapability("app"));
    	    		capabilities.setCapability("app", "");
				} else {
					TestRun.setXCUITest(false);
				}
			}
				
			initAppiumDrivers(getHubAddress(), capabilities);
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to spin up a local Appium session.");
            e.printStackTrace();
        } 
    	} // TODO - margins off...
    }
    
    /**********************************************************************************************
     * Enables the 'proxy log entries > 0' check performed at driver spinup to help ensure the user gets a device
     * with a valid internet/proxy connection prior to test start. NOTE - some of our test rig internal apps don't
     * make any calls on app open so this additional proxy check is disabled by default.
     * 
     * @author Brandon Clark created July 17, 2016
     * @version 1.0 July 17, 2016
     ***********************************************************************************************/
    public static void enableProxyCheckAtAppiumStartup() {
    	checkProxyLogsOnStartup.set(true);
    }
    
    /**********************************************************************************************
     * Applies a list of HttpFiltersSource Proxy rewrites to the proxy session BEFORE the driver is initiated, allowing
     * for traffic capture/manipulation of actions on application startup.
     * 
     * @param rewriteFilters - {@link List<HttpFiltersSource} - A list of HttpFiltersSource BrowserMobProxy rewrite objects
     * @author Brandon Clark created July 17, 2016
     * @version 1.0 July 17, 2016
     ***********************************************************************************************/
    public static void setRewritesAtStartup(List<HttpFiltersSource> rewriteFilters) {
    	filters.set(rewriteFilters);
    }
    
    public static void setRequestRewritesAtStartup(List<String> filters) {
    	requestFilters.set(filters);
    }
    
    public static void setResponseRewritesAtStartup(List<String> filters) {
    	responseFilters.set(filters);
    }
    
    public static void enableLabAppInstall(Boolean installOnTest, String appPackageID) {
    	labAppInstallOnStartup.set(true);
    	DriverFactory.installOnTest.set(installOnTest);
    	installAppPackageID.set(appPackageID);
    }
    
    public static void setLocalDebugProxyPort(Integer debugProxyPort) {
    	localDebugProxyPort.set(debugProxyPort);
    }
    
    /**********************************************************************************************
     * ADMINISTRATOR USE ONLY!!! NOTE TO BE USED AT THE PROJECT LEVEL!
     * 
     * @author Brandon Clark created July 17, 2016
     * @version 1.0 July 17, 2016
     ***********************************************************************************************/
    public static void enableMaintenanceCheck() {
    	maintenanceCheck.set(true);
    }
    
    public static Boolean isMaintenanceCheck() {
    	return maintenanceCheck.get();
    }
    
    private static String getHubAddress() {
    	String hubUrl = null;
    	
    	// is sauce
    	try {
    		if (TestRun.isSauceRun()) {
    			hubUrl = "http://" + SauceCredentialManager.getSauceUsername() + ":" + SauceCredentialManager.getSauceKey() + "@ondemand.saucelabs.com:80/wd/hub";
    			TestRun.setLabRun(false);
    		}
    	} catch (NullPointerException e) {
    		TestRun.setSauceRun(false);
    	}
    	
    	// is MQE lab run OR local run
    	if (!TestRun.isSauceRun()) {
    		String host = null;
    		String port = null;
    		if (GridManager.isQALabHub()) {
    			if (TestRun.isMobile()) { // lab mobile
    				host = LabDeviceManager.getNodeGridIP(activeMachineIP.get(), activeMobileOS.get());
    				port = GridManager.getGridHubPort(activeMobileOS.get());
    			} else { // lab web
    				host = BrowserNodeManager.getBrowserNodeGridIP(activeMachineIP.get());
    				port = GridManager.getGridHubPort(null);
    			}
    		} else { // local
    			host = Constants.LOCALHOST;
    			if (TestRun.isMobile()) {
    				port = GridManager.getGridHubPort(activeMobileOS.get());
    			} else {
    				port = GridManager.getGridHubPort(null);
    			}
    		}
    		hubUrl = "http://" + host + ":" + port + "/wd/hub";
    		TestRun.setLabRun(true);
    	}
    	
    	return hubUrl;
    }
    
    @SuppressWarnings("unchecked")
	private static void initAppiumDrivers(String hubUrl, DesiredCapabilities capabilities) throws Exception {
    	if (TestRun.isAndroid() || TestRun.isAndroidSim()) {
    		if (TestRun.isSelendroid()) {
    			appiumDriver.set(new SelendroidDriver(new URL(hubUrl), capabilities));
    		} else {
    			appiumDriver.set(new AndroidDriver<MobileElement>(new URL(hubUrl), capabilities));
    		}
    		DriverManager.setAppiumDriver(appiumDriver.get());
            androidDriver.set((AndroidDriver<MobileElement>) appiumDriver.get());
            DriverManager.setAndroidDriver(androidDriver.get());
        } else {
            appiumDriver.set(new IOSDriver<MobileElement>(new URL(hubUrl), capabilities));
            DriverManager.setAppiumDriver(appiumDriver.get());
            iOSDriver.set((IOSDriver<MobileElement>) appiumDriver.get());
            DriverManager.setIOSDriver(iOSDriver.get());
        }
    }
    
    private static void prepDelay() {
    	SleepUtils.sleep(RandomData.getInteger(250, 1000));
    	if (TestRun.isMobile()) {
    		if (!AvailableDevicePoller.isDevicePriority() && AvailableDevicePoller.getCurrentPriorityPosition() != 1) {
    			SleepUtils.sleep(11000); // helps ensure tests from other jobs don't hog the devices
    		}
    	}
    } 
    
}
