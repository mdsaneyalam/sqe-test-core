package com.softech.test.core.sikuli;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.sikuli.script.Finder;
import org.sikuli.script.Pattern;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.TestRun;

public class SikuliUtil {

	private static Integer pollingTimeMS = 500;
	private static String screenshotDir = null;
	
	public static void setPollingTime(Integer timeInMS) {
		pollingTimeMS = timeInMS;
	}
	
	/**********************************************************************************************
     * Waits for two screenshots captured by the device be either equal or not equal before timing out.
     * 
     * @param timeoutInSec - {@link Integer} - The time to wait in seconds for the screenshots to be equal/not equal before timing out.
     * @param imagesEqual - {@link Boolean} - Should image check for the images to be equal or not equal.
     * @param screenshotDirectoryPath - {@link Integer} - The project directory path where local screenshots are being saved for comparison.
     * @author Brandon Clark created March 22, 2016
     * @version 1.0 March 22, 2016
     ***********************************************************************************************/
	public static void waitForScreenComparison(Integer timeoutInSec, Boolean imagesEqual, String screenshotDirectoryPath) {
		screenshotDir = screenshotDirectoryPath;
		if (GridManager.isEC2Agent()) {
			screenshotDir = GlobalReportDir.getReportDir();
		}
		
		ExpectedCondition<Boolean> waitCondition = new ExpectedCondition<Boolean>() {
			Finder finder = null;
            
			@Override
			public Boolean apply(WebDriver input) {
				try {
					File screenImage1 = getScreenImageFile(screenshotDir);
	                
	                //execute a paused delay
	                Thread.sleep(500);
	                if (TestRun.isMobile()) {
	                    DriverManager.getAppiumDriver().manage().window().getSize();
	                } else {
	                	DriverManager.getWebDriver().manage().window().getSize();
	                }
	                    
	                //get the current device screenshot
	                File screenImage2 = getScreenImageFile(screenshotDir);
	                
	                finder = new Finder(screenImage1.getAbsolutePath());
	                Pattern pattern = new Pattern(screenImage2.getAbsolutePath());
	                finder.find(pattern.exact());
				} catch (Exception e) {
					Logger.logConsoleMessage("Failed to compare video images.");
					e.printStackTrace();
				}
				
				if (imagesEqual) {
					return finder.hasNext();
				} else {
					return !finder.hasNext();
				}
			}
		};
		
		if (TestRun.isMobile()) {
		    new FluentWait<WebDriver>(DriverManager.getAppiumDriver()).withTimeout(timeoutInSec, TimeUnit.SECONDS)
		        .pollingEvery(pollingTimeMS, TimeUnit.MILLISECONDS).until(waitCondition);
		} else {
			new FluentWait<WebDriver>(DriverManager.getWebDriver()).withTimeout(timeoutInSec, TimeUnit.SECONDS)
	        	.pollingEvery(pollingTimeMS, TimeUnit.MILLISECONDS).until(waitCondition);
		}
	}
	
	public static File getScreenImageFile(String screenshotDirectoryPath) {
		String filePath = null;
		try {
			String fileSep = "";
			if (!screenshotDirectoryPath.endsWith(File.separator)) {
				fileSep = File.separator;  
			}
			
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMddyyhhmmssa");
	        String screenshotDateTime1 = dateTimeFormat.format(new Date());
	        filePath = screenshotDirectoryPath + fileSep + screenshotDateTime1 + ".png";
	        if (TestRun.isMobile()) {
	            FileUtils.copyFile(((TakesScreenshot) DriverManager.getAppiumDriver()).getScreenshotAs(OutputType.FILE), 
				    new File(filePath));
	        } else {
	        	FileUtils.copyFile(((TakesScreenshot) DriverManager.getWebDriver()).getScreenshotAs(OutputType.FILE), 
	    			    new File(filePath));
	        }
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to get screen image.");
			e.printStackTrace();
		}
		
		return new File(filePath);
	}
	
	public static Boolean imagesEqual(File image1, File image2) {
		Finder finder = null;
		try {
			finder = new Finder(image1.getAbsolutePath());
	        Pattern pattern = new Pattern(image2.getAbsolutePath());
	        finder.find(pattern.exact());
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to compare images.");
			e.printStackTrace();
		}
		
        if (finder.hasNext()) {
        	return true;
        } else {
        	return false;
        }
	}
	
}
