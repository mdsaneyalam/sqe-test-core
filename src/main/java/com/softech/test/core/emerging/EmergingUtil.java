package com.softech.test.core.emerging;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.DependencyManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

public class EmergingUtil {

	public static ThreadLocal<Integer> timeout = new ThreadLocal<Integer>();
	
	public EmergingUtil() {
    	
    }

	public static String getHarmonyCLIPath() {
    	String harmonyPath = null;
    	if (GridManager.isEC2Agent()) {
			harmonyPath = System.getenv("HARMONY_CLI_PATH");
		} else {
			String harmonyJSName = "harmonyHubCLI.js";
			String harmonyZipName = "harmonyhubcli.zip";
			try {
				DependencyManager.setDependencyLoc("harmonyhubcli/" + harmonyJSName);
				if (!DependencyManager.dependencyExists()) {
					DependencyManager.downloadDependency(harmonyZipName);
					DependencyManager.unzipDependency(harmonyZipName);
					DependencyManager.setDependencyExec("harmonyhubcli");
				}
				
				harmonyPath = DependencyManager.getDependencyLoc();
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to get dependency '" + harmonyPath + "'.");
				e.printStackTrace();
			}
		}
    	
    	return harmonyPath;
    }
    
    public static String getNodePath() {
    	String nodeDir = "/usr/local/Cellar/node";
    	String nodeVersion = CommandExecutor.execCommand("ls " + nodeDir, null, null).trim();
        String nodePath = null;
    	if (StringUtils.isEmpty(nodeVersion)) {
            Logger.logConsoleMessage("Could not find node in directory: '" + nodeDir + "'. Was npm installed via brew?");
        } else {
            nodePath = nodeDir + "/" + nodeVersion + "/bin/node";
        	
        }
    	return nodePath;
    }
    
    public static String getImageMagickPath() {
    	String dir = "/usr/local/Cellar/imagemagick";
    	CommandExecutor.setTargetGatewayIP(getTargetGateway());
    	String version = CommandExecutor.execCommand("ls " + dir, null, null).trim();
        String path = null;
    	if (StringUtils.isEmpty(version)) {
            Logger.logConsoleMessage("Could not find imagemagick in directory: '" + dir + "'. Was imagemagick installed via brew?");
        } else {
            path = dir + "/" + version + "/bin/magick";
        	
        }
    	return path;
    }
    
    public static String getIDeviceScreenshotPath(String machineIP) {
    	String libiDir = "/usr/local/Cellar/libimobiledevice";
    	String libiVersion = CommandExecutor.execCommand("ls " + libiDir, machineIP, null).trim();
        String libiPath = null;
    	if (StringUtils.isEmpty(libiVersion)) {
            Logger.logConsoleMessage("Could not find libimobile in directory: " + libiDir);
        } else {
            libiPath = libiDir + "/" + libiVersion + "/bin/idevicescreenshot";
        	
        }
    	return libiPath;
    }
    
    public static String getIDeviceInstallerPath(String machineIP) {
    	String libiDir = "/usr/local/Cellar/ideviceinstaller";
    	String libiVersion = CommandExecutor.execCommand("ls " + libiDir, machineIP, null).trim();
        String libiPath = null;
    	if (StringUtils.isEmpty(libiVersion)) {
            Logger.logConsoleMessage("Could not find ideviceinstaller in directory: " + libiDir);
        } else {
            libiPath = libiDir + "/" + libiVersion + "/bin/ideviceinstaller";
        	
        }
    	return libiPath;
    }
    
    public static String getiOSDeployPath() {
    	return "/usr/local/lib/node_modules/ios-deploy/build/Release/ios-deploy";
    }
    
    public static String getTesseractPath() {
    	CommandExecutor.setTargetGatewayIP(getTargetGateway());
    	String tesseractDir = "/usr/local/Cellar/Tesseract";
    	String tesseractVersion = CommandExecutor.execCommand("ls " + tesseractDir, null, null).trim();
    	
    	String tesseractPath = null;
    	if (StringUtils.isEmpty(tesseractVersion)) {
            Logger.logConsoleMessage("Could not find tesseract in directory '" + tesseractDir + "'. Did you first run "
            	+ "'brew install tesseract'?");
        } else {
            tesseractPath = tesseractDir + "/" + tesseractVersion + "/bin/tesseract";
        	
        }
    	return tesseractPath;
    }
    
    public static void cleanScreenImageDir() {
    	File screenshotDir = new File(System.getProperty("user.dir") + "/test-output/screenshots");
    	if (screenshotDir.exists()) {
    		try {
    			Logger.logConsoleMessage("Emptying all images from the screenshot directory.");
				FileUtils.cleanDirectory(screenshotDir);
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to clean screenshot directory.");
				e.printStackTrace();
			}
    	}
    }
    
    public static void setTimeout(Integer timeoutInSec) {
    	timeout.set(timeoutInSec);
    }
    
    public static void commandSender(String[] command) {
    	String success = "executed successfully";
    	Integer maxIter = 5;
    	if (TestRun.isRoku()) {
    		maxIter = 2;
    	}
    	Integer iter = 0;
    	
    	for (int i = 0; i <= maxIter; i++) {
    		if (iter.equals(maxIter)) {
    			throw new RuntimeException("Emerging interaction with command '" + command + "' failed to execute after multiple attempts.");
    		}
    		
    		String result = null;
    		try {
    			CommandExecutor.setTargetGatewayIP(getTargetGateway());
    			if (EmergingDriverManager.getTargetGateway() == null) { // local run
    				String commandStr = String.join(" ", command);
    				result = CommandExecutor.execMultiCommand(commandStr, getTimeout());
    			} else { // lab run
    				result = CommandExecutor.execCommand(command, null, getTimeout());
    			}
    			
    			if (result != null) {
    				if (TestRun.isAppleTV()) {
    					if (result.contains(success)) {
            				break;
                		}
        			} else if (TestRun.isRoku()) {
        				if (!result.toLowerCase().contains("failed to")) {
        					break;
        				}
        			} else if (TestRun.isFireTV()) {
        				Logger.logConsoleMessage("DEBUG - RESULT: " + result);
        				break;
        			}
    			}
    		} catch (Exception e) {
    			SleepUtils.sleep(1000);
    		}
    		iter++;
    	}
    }
    
    public static GatewayIP getTargetGateway() {
    	GatewayIP gatewayIP = null;
    	if (GridManager.isEC2Agent()) {
    		gatewayIP = EmergingDriverManager.getTargetGateway();
		}
    	return gatewayIP;
    }
    
    private static Integer getTimeout() {
    	return timeout.get();
    }
    
}
