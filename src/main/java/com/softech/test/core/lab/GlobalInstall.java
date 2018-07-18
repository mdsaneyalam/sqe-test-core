package com.softech.test.core.lab;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.props.MQEDriverCaps;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.JenkinsAPIUtil;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

public class GlobalInstall {

	private static final String NO_FILE_TXT = "no such file or directory";
	
	private static String suiteAppFileName = null;
	private static ThreadLocal<String> testAppFileName = new ThreadLocal<String>();
	private static Boolean downloadAppPerTest = false;
	private static Map<String, String> installedMap = Collections.synchronizedMap(new HashMap<String, String>());
	private static String appPackageID = null;
	private static MobileOS mobileOS = null;
	
	private static ThreadLocal<String> ignoredAppVersion = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> ignoreAppVersion = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
    private static ThreadLocal<String> installOutput = new ThreadLocal<String>();
    
    private static Boolean appResigned = false;
    
	public static void downloadAppPackage(String appPackageUrl) {
		// name the package
		MobileOS mobileOS = MobileOS.getEnumByString(System.getProperty("system.test.mobileos"));
		String appExt = mobileOS.equals(MobileOS.ANDROID) ? Constants.APK_EXT : Constants.IPA_EXT;
		
		String packageName = JenkinsAPIUtil.getRunningJobName() + "_" + JenkinsAPIUtil.getRunningBuildId() + "_" 
				+ new SimpleDateFormat(Constants.PACKAGE_DATE_FORMAT).format(new Date()) + RandomData.getCharacterString(10) + appExt;
		
		if (downloadAppPerTest) {
			testAppFileName.set(packageName);
		} else {
			suiteAppFileName = packageName;
		}
		
		// download the app on the core machine
		Integer maxDownloadAtt = 2;
		Boolean downloadSuccess = false;
		Integer downloadIter = 0;
		
		String result = null;
		while (!downloadSuccess && downloadIter < maxDownloadAtt) {
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				if (GridManager.isLabGatewayOnline(gateway)) {
					// download the app package to the gateway machines
					try {
						Logger.logConsoleMessage("Downloading app package from '" + appPackageUrl + "'" + " on '" + gateway 
						+ "' with filename '" + getAppFile() + "'.");
						CommandExecutor.setTargetGatewayIP(gateway);
				        LabDeviceManager.downloadAppPackage(null, appPackageUrl, getAppFile());
					} catch (Exception e) {
						Logger.logConsoleMessage("App package timed out during download.");
					}
				}
			}
			
			// check the file size of the downloaded package to ensure it's valid
			List<Boolean> existChecks = new ArrayList<Boolean>();
			existChecks.clear();
			Boolean existCheck = false;
			List<Boolean> sizeChecks = new ArrayList<Boolean>();
			sizeChecks.clear();
			Boolean sizeCheck = false;
			List<Boolean> securityPolicyChecks = new ArrayList<Boolean>();
			securityPolicyChecks.clear();
			Boolean securityPolicyCheck = false;
			
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				if (GridManager.isLabGatewayOnline(gateway)) {
					CommandExecutor.setTargetGatewayIP(gateway);
					result = CommandExecutor.execCommand("ls -a " + Constants.HUB_APP_PACKAGE_DIR + getAppFile(), null, null);
					if (!result.toLowerCase().contains(NO_FILE_TXT)) {
						existChecks.add(true);
					} else {
						existChecks.add(false);
					}
					
					if (!existChecks.contains(false)) {
						CommandExecutor.setTargetGatewayIP(gateway);
				    	result = CommandExecutor.execCommand("wc -c " + Constants.HUB_APP_PACKAGE_DIR + getAppFile(), null, null);
				    	Integer size = Integer.parseInt(result.replace("\n", "").trim().split(" ")[0]);
					    Logger.logConsoleMessage("Size of app package to install: " + size);
					    if (size > Constants.INVALID_APP_FILE_SIZE) {
					    	Logger.logConsoleMessage("Size of app package validated.");
					    	sizeChecks.add(true);
					    } else {
					    	Logger.logConsoleMessage("The size of the app package does not meet the minimum install requirements.");
					    	sizeChecks.add(false);
					    }
				    }
				}
			}
			
			if (!existChecks.contains(false)) {
				existCheck = true;
			}
			
			if (!sizeChecks.contains(false)) {
				sizeCheck = true;
			}
			
			// update android security policy for apks
			String postSignedFileName = null;
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				if (GridManager.isLabGatewayOnline(gateway)) {
					if (existCheck && sizeCheck && mobileOS.equals(MobileOS.ANDROID) && !downloadAppPerTest) {
						postSignedFileName = APKSecuritySigner.updateAPKSecuritySettings(gateway, Constants.HUB_APP_PACKAGE_DIR + suiteAppFileName);
					}
				}
			}
			
			// check if the sec updates for android were successful
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				if (GridManager.isLabGatewayOnline(gateway)) {
					if (existCheck && sizeCheck && mobileOS.equals(MobileOS.ANDROID) && !downloadAppPerTest) {
						CommandExecutor.setTargetGatewayIP(gateway);
			    		result = CommandExecutor.execCommand("ls -a " + Constants.HUB_APP_PACKAGE_DIR + postSignedFileName, null, null);
						if (!result.toLowerCase().contains(NO_FILE_TXT)) {
							Logger.logConsoleMessage("The android apk security policy for ssl was successfully updated.");
							securityPolicyChecks.add(true);
						} else {
							Logger.logConsoleMessage("The android apk security policy for ssl was NOT successfully updated!");
							securityPolicyChecks.add(false);
						}
					}
				}
			}
			
			if (mobileOS.equals(MobileOS.IOS)) { // ignore apk security check
				securityPolicyCheck = true;
			} else if (mobileOS.equals(MobileOS.ANDROID) && !downloadAppPerTest) { // android and not download per test
				if (securityPolicyChecks.contains(false)) {
					securityPolicyCheck = false;
				} else {
					securityPolicyCheck = true;
					suiteAppFileName = postSignedFileName;
				}
			} else { // android and download per test so apk security updates not being applied
				securityPolicyCheck = true;
			}
			
			if (existCheck && sizeCheck && securityPolicyCheck) {
		    	downloadSuccess = true;
		    } else {
		    	Logger.logConsoleMessage("App package failed to download successfully on attempt '" 
		            + downloadIter + "'. Retrying...");
		    }
		    
			downloadIter++;
		}
		
		if (!downloadSuccess && !downloadAppPerTest) {
			GlobalAbort.terminateTestSuite("The app package did not download successfully after '" 
		        + maxDownloadAtt + "' attempts.");
		} else if (!downloadSuccess && downloadAppPerTest) {
			throw new RuntimeException("The app package at '" + appPackageUrl + "' did not download successfully.");
		} else {
			Logger.logConsoleMessage("App package at '" + appPackageUrl + "' downloaded successfully.");
		}
	}
	
	/**********************************************************************************************
     * Installs an app on a target device.
     * 
     * @param installOnTest - {@link Boolean} - Set to true if you want the app to be installed at the beginning of EVERY test execution. 
     * Otherwise, the script will install the app the first time the test runs on the device in the suite.
     * @param appPackageID - {@link String} - The app package id of the app to install.
     * @param appPackageUrl - {@link String} - The url of the application to download.
     * @author Brandon Clark created August 1, 2016
     * @version 1.0 August 1, 2016
     * @return Boolean - the success or failure of the install attempt.
     ***********************************************************************************************/
	public static Boolean installApp(DesiredCapabilities capabilities, Boolean installOnTest, String deviceID, String appPackageID) {
		if (getAppFile() == null) {
			String errorMsg = "The app file path is null. Did you call GlobalInstall.downloadAppPackage "
				+ "prior to test startup?";
			Logger.logMessage(errorMsg);
			throw new RuntimeException(errorMsg);
		}
		
		Boolean installSuccess = false;
		Integer installIter = 0;
		
    	GlobalInstall.mobileOS = TestDeviceInfo.getMobileOS();
        GlobalInstall.appPackageID = appPackageID;
		
		// check if the device is reachable on the lab
		if (LabDeviceManager.isDeviceConnected(deviceID)) {
			while (!installSuccess && installIter != 2) {
				// check if the app is installed
				String machineIP = TestDeviceInfo.getTetheredMachineIP();
				Boolean appInstalled = LabDeviceManager.isAppInstalled(machineIP, mobileOS, deviceID, appPackageID);
				
				String appVersionNumber = null;
				if (ignoreAppVersion.get() && appInstalled) {
					appVersionNumber = LabDeviceManager.getAppPackageVersion(machineIP, mobileOS, appPackageID);
					if (appVersionNumber.equals(ignoredAppVersion.get())) {
						Logger.logMessage("The installed app package version of '" + appVersionNumber 
							+ "' matches the supplied ignored app version of '" + ignoredAppVersion.get() + "'. The app package will not be uninstalled/re-installed.");
					} else {
						Logger.logConsoleMessage("The app package '" + appPackageID + "' is installed, but the version "
							+ "number '" + appVersionNumber + "' does not meet the required version '" 
							+ ignoredAppVersion.get() + "'. The app will be uninstalled/re-installed.");
					}
				}
				
				if (TestRun.isSelendroid()) { // SELENDROID
					if (!alreadyInstalled(deviceID, appPackageID)) {
						// push the app to the target machine
						LabDeviceManager.copyAppPackage(machineIP, Constants.HUB_APP_PACKAGE_DIR + getAppFile());
						if (appInstalled) {
							LabDeviceManager.uninstallApp(machineIP, mobileOS, deviceID, appPackageID);
						}
						
						installedMap.put(deviceID, appPackageID);
						installSuccess = true;
						break;
					} else {
						installSuccess = true;
						break;
					}
				} else { // APPIUM
					// successful install
					if (appInstalled) {
						if (ignoreAppVersion.get() && ignoredAppVersion.get().equals(appVersionNumber)) {
							installSuccess = true;
							break;
						} else {
							if (!ignoreAppVersion.get() && alreadyInstalled(deviceID, appPackageID) && !installOnTest) {
								installSuccess = true;
								break;
							}
						}
					}
					
					// uninstall the app
					Boolean appUninstalled = false;
					if (appInstalled) {
						if (ignoreAppVersion.get() && !ignoredAppVersion.get().equals(appVersionNumber)) {
							LabDeviceManager.uninstallApp(machineIP, mobileOS, deviceID, appPackageID);
							appUninstalled = true;
						} else {
							if (installOnTest || (!installOnTest && !alreadyInstalled(deviceID, appPackageID))) {
								LabDeviceManager.uninstallApp(machineIP, mobileOS, deviceID, appPackageID);
								appUninstalled = true;
							}
						}
					}
					
					// install the app
					if (!appInstalled || appUninstalled) {
						if (TestRun.isIos() && capabilities.getCapability(MQEDriverCaps.MQE_IOS_RESIGN_PROVISION.value()) != null) {
							synchronized (GlobalInstall.class) {
								if (appResigned) {
									Logger.logConsoleMessage("App has previously been code resigned");
								} else {
									File mobileProvision = new File(capabilities.getCapability(MQEDriverCaps.MQE_IOS_RESIGN_PROVISION.value()).toString());
									if (mobileProvision.exists()) {
										// copy the provision file to the gateway (NOTE - 01 gateway currently handles all re-signs)
										FileDeployer.deployFileToGatewayFromEC2Agent(GatewayIP.LAB_01, mobileProvision);
									    
										String bundleId = null;
										if (capabilities.getCapability(MQEDriverCaps.MQE_IOS_RESIGN_BUNDLE_ID.value()) != null) {
											bundleId = capabilities.getCapability(MQEDriverCaps.MQE_IOS_RESIGN_BUNDLE_ID.value()).toString();
										}
										
										// code resign the app
										File appPackage = new File(Constants.HUB_APP_PACKAGE_DIR + getAppFile());
										File gatewayProvision = new File(Constants.HUB_APP_PACKAGE_DIR + mobileProvision.getName());
										String resignedPath = IOSCodeResigner.resignApp(GatewayIP.LAB_01, appPackage, gatewayProvision, bundleId);
										File resignedFile = new File(resignedPath);
										File agentFile = new File(FileDeployer.pullFileFromGatewayToAgent(GatewayIP.LAB_01, resignedFile, GlobalReportDir.getReportDir()));
										FileDeployer.deployFileToGatewayFromEC2Agent(GatewayIP.LAB_02, agentFile);
										suiteAppFileName = new File(resignedPath).getName();
										appResigned = true;
									}
								}
							}
						}
						
						String hubFile = Constants.HUB_APP_PACKAGE_DIR + getAppFile();
						String nodeFile = Constants.NODE_APP_PACKAGE_DIR + getAppFile();
						LabDeviceManager.copyAppPackage(machineIP, hubFile);
						
						if (TestRun.isAndroid()) {
							// safe uninstall in the event the uninstall failed
							LabDeviceManager.uninstallApp(machineIP, mobileOS, deviceID, appPackageID);
							SleepUtils.sleep(1000);
						}
						
						installOutput.set(LabDeviceManager.installApp(machineIP, mobileOS, deviceID, nodeFile));
						
						installSuccess = LabDeviceManager.isAppInstalled(machineIP, mobileOS, deviceID, appPackageID);
						if (!installSuccess) {
							Logger.logConsoleMessage("App was not installed successfully on attempt '" + installIter + "' for device '" 
									+ deviceID + "'.");
							Logger.logToSysFile(installOutput.get());
						} else {
							installedMap.put(deviceID, appPackageID);
						}
					}
				}
				
				installIter++;
			}
		} else {
			Logger.logConsoleMessage("Device '" + deviceID + "' cannot be reached on the lab.");
		}
		
		return installSuccess;
    }
	
	public static String getNodeAppFile() {
		return Constants.NODE_APP_PACKAGE_DIR + getAppFile();
	}
	
	public static Boolean uninstallApp() {
		// get the current running info
		String sessionIP = GridManager.getRunningSessionIP();
		String deviceID = LabDeviceManager.getDeviceID(sessionIP, mobileOS);
		
		// stop the current session
		DriverManager.stopAppiumDriver();
		
		// uninstall the app package from the current device
		LabDeviceManager.uninstallApp(sessionIP, mobileOS, deviceID, appPackageID);
		
		return LabDeviceManager.isAppInstalled(sessionIP, mobileOS, deviceID, appPackageID);
	}
	
	public static void setDownloadAppPerTest() {
		downloadAppPerTest = true;
	}
	
	public static void setIgnoreByAppVersion(String versionNumber) {
		Logger.logConsoleMessage("Setting global install to ignore uninstall/re-install of the app if "
				+ "the app package is already installed and matches the requisite version number.");
		ignoreAppVersion.set(true);
		ignoredAppVersion.set(versionNumber);
	}
	
	public static String getAppFile() {
		return downloadAppPerTest.equals(true) ? testAppFileName.get() : suiteAppFileName;
	}
	
	public static Map<String, String> getInstalledMap() {
		return installedMap;
	}
	
	public static String getInstallOutput() {
		return installOutput.get();
	}
	
	@SuppressWarnings("rawtypes")
	private static synchronized Boolean alreadyInstalled(String deviceId, String packageId) {
		Boolean alreadyInstalled = false;
		Iterator iterator = installedMap.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry pair = (Map.Entry) iterator.next();
	        if (pair.getKey().equals(deviceId) && pair.getValue().equals(packageId)) {
	        	alreadyInstalled = true;
	        	break;
	        }
	        iterator.remove();
	    }
	    
	    return alreadyInstalled;
	}

}
