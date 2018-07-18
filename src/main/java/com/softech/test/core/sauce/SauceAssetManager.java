package com.softech.test.core.sauce;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;
import com.softech.test.core.util.RandomData;

public class SauceAssetManager {
	
	private static ThreadLocal<String> screenRecordingPath = new ThreadLocal<String>();
	private static ThreadLocal<String> logPath = new ThreadLocal<String>();
	private static ThreadLocal<Boolean> screenRecordingSuccess = new ThreadLocal<Boolean>();
	private static ThreadLocal<Boolean> logSuccess = new ThreadLocal<Boolean>();
	
	public static void setScreenRecordingPath(String recordingPath) {
		screenRecordingPath.set(recordingPath);
	}
	
	public static String getScreenRecordingPath() {
		return screenRecordingPath.get();
	}
	
	public static void setLogPath(String logPath) {
		SauceAssetManager.logPath.set(logPath);
	}
	
	public static String getLogPath() {
		return logPath.get();
	}
	
	public static Boolean isRecordingSuccess() {
		return screenRecordingSuccess.get();
	}
	
	public static Boolean isLogSuccess() {
		return logSuccess.get();
	}
	
	/**********************************************************************************************
     * Gets the video screen recording of the sauce session and returns an .mp4 file.
     * 
     * @param sessionID - {@link String} - The webdriver sessionId of the test execution.
     * @param fileName - {@link String} - The absolute file path/name of the file to save the .mp4 to. NOTE - this
     * will most likely be your project screenshots directory in test-output.
     * @author Brandon Clark created May 15, 2016
     * @version 1.0.0 May 15, 2016
     ***********************************************************************************************/
	public static void getScreenRecording(String sessionID, String fileName) {
		String fileLoc = fileName.replace("/", File.separator);
		
		if (GridManager.isEC2Agent()) {
			fileLoc = GlobalReportDir.getReportDir() + File.separator + RandomData.getCharacterString(40) + "video.mp4";
		}
		setScreenRecordingPath(fileLoc);
		
		Logger.logConsoleMessage("Getting screen recording video from sauce labs for session id '" + sessionID + "'.");
		String videoUrl = "https://saucelabs.com/rest/v1/" + SauceCredentialManager.getSauceUsername() + "/jobs/" + sessionID + "/assets/video" + Constants.FLV_EXT;
		
		// wait for the file to be pulled from sauce labs
		String errorMsg = "Failed to copy screen recording video from sauce with session id '" + sessionID + "'.";
		Boolean success = false;
		try {
			Thread.sleep(20000); // TODO - no nice dynamic wait to determine when sauce has generated the video. Sauce is going away so not overly concerned for now
			String curlLoc = "curl";
			if (OSDetector.isWindows()) {
				curlLoc = "C:\\cygwin\\bin\\curl.exe"; // TODO - dynamically config
			}
			String command = curlLoc + " --connect-timeout 30 --max-time 120 " + getBasicAuthString() + "-o " + fileLoc.replace(Constants.MP4_EXT, Constants.FLV_EXT) + " -L " + videoUrl;
	    	if (GridManager.isEC2Agent()) {
	    		CommandExecutor.setEC2CommandHop(false);
	    	}
			CommandExecutor.execCommand(command, null, 30);
			File file = new File(fileLoc.replace(Constants.MP4_EXT, Constants.FLV_EXT));
			
			if (file.exists()) {
				if (file.length() > Constants.INVALID_FILE_SIZE) {
				    success = true;
				}
			}
			
			if (!success) {
				Logger.logConsoleMessage(errorMsg);
			}
		} catch (Exception e) {
			Logger.logConsoleMessage(errorMsg);
		}
		
		if (success) {
			// convert the .flv to mp4
			String ffmpegLoc = null;
			if (OSDetector.isWindows()) {
				ffmpegLoc = "C:\\ffmpeg\\bin\\ffmpeg.exe"; // TODO - dynamically config this
			} else {
				String ffmpegVersion = CommandExecutor.execCommand("ls " + Constants.FFMPEG_DIR, null, null).trim();
		        if (StringUtils.isEmpty(ffmpegVersion)) {
		            Logger.logConsoleMessage("Could not find ffmpeg in directory: " + Constants.FFMPEG_DIR);
		        } else if (OSDetector.isLinux()) {
		        	ffmpegLoc = "ffmpeg";
		        } else {
		            ffmpegLoc = Constants.FFMPEG_DIR + "/" + ffmpegVersion + "/bin/ffmpeg";
		        }
			}
	    	
			if (GridManager.isEC2Agent()) {
	    		CommandExecutor.setEC2CommandHop(false);
	    	}
			CommandExecutor.execCommand(ffmpegLoc + " -i " + fileLoc.replace(Constants.MP4_EXT, Constants.FLV_EXT) + " " + fileLoc, null, null);
			screenRecordingSuccess.set(true);
		}
		
	}
	
	/**********************************************************************************************
     * Gets the selenium logs of the sauce session and returns a log file.
     * 
     * @param sessionID - {@link String} - The webdriver sessionId of the test execution.
     * @param fileName - {@link String} - The absolute file path/name of the file to save the .log to. NOTE - this
     * will most likely be your project screenshots directory in test-output.
     * @author Brandon Clark created May 15, 2016
     * @version 1.0.0 May 15, 2016
     ***********************************************************************************************/
	public static void getSeleniumLog(String sessionID, String fileName) {
		Logger.logConsoleMessage("Getting selenium log from sauce labs for session id '" + sessionID + "'.");
		String fileLoc = fileName.replace("/", File.separator);
		if (GridManager.isEC2Agent()) {
			fileLoc = GlobalReportDir.getReportDir() + File.separator + RandomData.getCharacterString(40)
			+ "selenium-server.log";
		}
		setLogPath(fileLoc);
		
		String logUrl = "https://saucelabs.com/rest/" + SauceCredentialManager.getSauceUsername() + "/jobs/" + sessionID + "/results/selenium-server.log";
		
		// wait for the file to be pulled from sauce labs
    	ExpectedCondition<Boolean> waitCondition = new ExpectedCondition<Boolean>() {
    		@Override
    		public Boolean apply(WebDriver input) {
    			Boolean success = false;
    			String curlLoc = "curl";
    			if (OSDetector.isWindows()) {
    				curlLoc = "C:\\cygwin\\bin\\curl.exe"; // TODO - dynamically config
    			}
    			String command = curlLoc + " --connect-timeout 30 --max-time 120 " + getBasicAuthString() + "-o " + getLogPath() + " -L " + logUrl;
    	    	if (GridManager.isEC2Agent()) {
    	    		CommandExecutor.setEC2CommandHop(false);
    	    	}
    			CommandExecutor.execCommand(command, null, 30);
    			File file = new File(getLogPath());
    			
    			if (file.exists()) {
    				if (file.length() > Constants.INVALID_FILE_SIZE) {
    				    success = true;
    				}
    			}
    			return success;
    		}
    	};
    		
    	try {
    		new FluentWait<WebDriver>(DriverManager.getWebDriver()).withTimeout(10, TimeUnit.SECONDS)
	            .pollingEvery(500, TimeUnit.MILLISECONDS).until(waitCondition);
    		logSuccess.set(true);
    	} catch (TimeoutException e) {
    		Logger.logConsoleMessage("Failed to copy log file from sauce with session id '" + sessionID + "'.");
    		e.printStackTrace();
    	}
	}
	
	private static String getBasicAuthString() {
		return "-u " + SauceCredentialManager.getSauceUsername() + ":" 
    	        + SauceCredentialManager.getSauceKey() + " ";
	}
	
}
