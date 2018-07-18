package com.softech.test.core.report;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.sikuli.script.ScreenImage;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.FileDeployer;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.proxy.ProxyFactory;
import com.softech.test.core.proxy.ProxyManager;
import com.softech.test.core.proxy.ProxyRESTManager;
import com.softech.test.core.sauce.SauceAssetManager;
import com.softech.test.core.sikuli.SikuliDriverManager;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.TestRun;

import ru.yandex.qatools.allure.annotations.Attachment;

public class AllureAttachment {

    private static String emptyString = "empty string";

    /**********************************************************************************************
     * Gets the screenshot of the browser window or device viewport (depending on mobile vs desktop run)
     * and attaches it to the test case in the allure report.
     *
     * @author Brandon Clark created February 8, 2016
     * @version 1.0 February 8, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "{0}", type = "image/png")
    public static byte[] attachScreenshot(String allureScreenshotType) {
        byte[] imageContent = emptyString.getBytes();
        try {
            if (TestRun.isMobile()) {
            	if (TestRun.isSelendroid() && DriverManager.getAppiumDriver() != null) {
            		if (GridManager.isQALabHub()) {
            			String adbPath = Constants.ADB_PATH;
                		String sessionIP = GridManager.getRunningSessionIP();
                		
                		// capture screenshot on device
                		String screenshotName = "screenshot" + RandomData.getCharacterString(40) + ".png";
                		CommandExecutor.execCommand(adbPath + " shell screencap -p /sdcard/" + screenshotName, sessionIP, null);
                		
                		// pull the screenshot from the device to the local mac
                		String localScreenshotPath = Constants.NODE_APP_PACKAGE_DIR + screenshotName;
                		CommandExecutor.execCommand(adbPath + " pull /sdcard/" + screenshotName + " " + Constants.NODE_APP_PACKAGE_DIR, sessionIP, null);
                		
                		// remove the screenshot from the device storage
                		CommandExecutor.execCommand(adbPath + " shell rm /sdcard/" + screenshotName, sessionIP, null);
                		
                		// pull the screenshot from the mac mini to the hub entry point
                		GatewayIP gatewayIP = LabDeviceManager.getMachineGatewayIP(sessionIP);
                		FileDeployer.pullFileFromNodeToGateway(gatewayIP, sessionIP, new File(localScreenshotPath), Constants.HUB_APP_PACKAGE_DIR);
                		
                		// pull the screenshot from the hub entry point to the agent
                		String hubScreenshotPath = Constants.HUB_APP_PACKAGE_DIR + screenshotName;
                		String reportScreenshotPath = GlobalReportDir.getReportDir() + File.separator + screenshotName;
                		CommandExecutor.setEC2SCPHop(false);
                		FileDeployer.pullFileFromGatewayToAgent(gatewayIP, new File(hubScreenshotPath), GlobalReportDir.getReportDir());
                		
                	    // clean up mac mini
                	    CommandExecutor.execCommand("rm -f " + localScreenshotPath, sessionIP, null);
                	    
                	    imageContent = Files.readAllBytes(Paths.get(reportScreenshotPath));
            		} else {
            			Logger.logConsoleMessage("Screenshot not captured for local execution. Selendroid "
            					+ "screencapture only supported on the lab currently.");
            			// TODO implement local screencapture fix for selendroid.
            		}
            	} else {
            		imageContent = ((TakesScreenshot) DriverManager.getAppiumDriver()).getScreenshotAs(OutputType.BYTES);
            	}
            } else {
                imageContent = ((TakesScreenshot) DriverManager.getWebDriver()).getScreenshotAs(OutputType.BYTES);
            }

        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture screenshot.");
            e.printStackTrace();
        }
        return imageContent;
    }

    /**********************************************************************************************
     * Gets the video of the browser window or device viewport (depending on mobile vs desktop run)
     * and attaches it to the test case in the allure report.
     *
     * @author Brandon Clark created May 10, 2016
     * @version 1.0 May 10, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Failure Video", type = "video/mp4")
    public static byte[] attachVideo(String pathToVideoFile) {
    	String filePath = pathToVideoFile;
    	if (GridManager.isEC2Agent()) {
    		filePath = SauceAssetManager.getScreenRecordingPath();
    	}
    	
        byte[] videoContent = emptyString.getBytes();
        try {
            videoContent = Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to get video data.");
            e.printStackTrace();
        }
        return videoContent;
    }

    /**********************************************************************************************
     * Gets the selenium log and attaches it to the test case in the allure report.
     *
     * @author Brandon Clark created May 15, 2016
     * @version 1.0 May 15, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Selenium Log", type = "text/plain")
    public static byte[] attachSeleniumLog(String pathToLogFile) {
    	String filePath = pathToLogFile;
    	if (GridManager.isEC2Agent()) {
    		filePath = SauceAssetManager.getLogPath();
    	}
    	
        byte[] logContent = emptyString.getBytes();
        try {
            logContent = Files.readAllBytes(Paths.get(filePath));
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to get log data.");
            e.printStackTrace();
        }
        return logContent;
    }
    
    /**********************************************************************************************
     * Gets the appium log and attaches it to the test case in the allure report. NOTE - only works on the lab
     *
     * @author Brandon Clark created May 15, 2016
     * @version 1.0 May 15, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Appium Log", type = "text/plain")
    public static byte[] attachAppiumLog() {
        byte[] logContent = emptyString.getBytes();
        if (GridManager.isQALabHub() && DriverManager.getAppiumDriver() != null) {
        	try {
                logContent = Files.readAllBytes(Paths.get(LabDeviceManager
                	.getAppiumLog(TestRun.getMobileOS(), GridManager.getRunningSessionIP()).getAbsolutePath()));
            } catch (Exception e) {
                Logger.logConsoleMessage("Failed to get log data.");
                e.printStackTrace();
            }
        }
        
        return logContent;
    }

    /**********************************************************************************************
     * Gets the full proxy log of a test execution and attaches it to the test case in the allure report in har format.
     *
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Session Proxy Log", type = "text/plain")
    public static byte[] attachProxyResults() {
        byte[] fileContent = emptyString.getBytes();
        try {
            StringWriter output = new StringWriter();
            if (ProxyFactory.isBMP()) {
                ProxyManager.getLog().writeTo(output);
            } else {
            	if (ProxyRESTManager.isProxyInstanceStarted() && ProxyRESTManager.isProxyInstanceRunning()) {
            		output.write(ProxyRESTManager.getLogAsString());
            	}
            }
            fileContent = output.toString().getBytes();
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture proxy log.");
            e.printStackTrace();
        }
        return fileContent;
    }

    /**********************************************************************************************
     * (MOBILE ONLY) Gets the app xml tree and attaches it to the test case in the allure report. Very helpful
     * in debugging tests executing on remote devices in the lab/cloud.
     *
     * @author Brandon Clark created February 8, 2016
     * @version 1.0 February 8, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "App XML Log", type = "text/plain")
    public static byte[] attachAppXMLTree() {
        byte[] fileContent = emptyString.getBytes();
        try {
            fileContent = DriverManager.getAppiumDriver().getPageSource().getBytes();
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture application xml tree.");
            e.printStackTrace();
        }
        return fileContent;
    }

    /**********************************************************************************************
     * (WEB ONLY) Gets the DOM tree and attaches it to the test case in the allure report. Very helpful
     * in debugging tests executing on remote browsers in the lab/cloud.
     *
     * @author Brandon Clark created February 8, 2016
     * @version 1.0 February 8, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "App DOM Log", type = "text/plain")
    public static byte[] attachDOMTree() {
        byte[] fileContent = emptyString.getBytes();
        try {
            fileContent = DriverManager.getWebDriver().getPageSource().getBytes();
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture dom tree.");
            e.printStackTrace();
        }
        return fileContent;
    }

    /**********************************************************************************************
     * Gets the Omniture File which contains Expected Values for the test and attaches 
     * it to the test case in the allure report. This is helpful in keeping the test requirements
     * in check with PMO.
     *
     * @author Danish Shah created May 20, 2016
     * @version 1.0 May 20, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Omniture Expected Calls", type = "text/plain")
    public static byte[] attachOmnitureFile(String pathToFile) {
        byte[] omnitureFile = emptyString.getBytes();
        try {
            omnitureFile = Files.readAllBytes(Paths.get(pathToFile));
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to get Omniture File");
            e.printStackTrace();
        }
        return omnitureFile;
    }

    /**********************************************************************************************
     * (MOBILE ONLY) Gets the iOS System device log if running an iOS test or the Android logcat
     * log if running an Android test.
     *
     * @author Yauheni Patotski created Jun 21, 2016
     * @version 1.1 June 22, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "Device Log", type = "text/plain")
    public static byte[] attachDeviceLog() {
    	byte[] fileContent = emptyString.getBytes();
        LogEntries logEntries;
        try {
            if (TestRun.isAndroid()) {
                logEntries = DriverManager.getAppiumDriver().manage().logs().get("logcat");
            } else {
                logEntries = DriverManager.getAppiumDriver().manage().logs().get("syslog");
            }

            StringBuffer stringBuffer = new StringBuffer();
            for (LogEntry logEntry : logEntries.getAll()) {
                stringBuffer.append(logEntry.getMessage()).append("\n");
            }
            fileContent = stringBuffer.toString().getBytes();
        } catch (Exception e) {
        	Logger.logConsoleMessage("Failed to get mobile device log.");
        	e.printStackTrace();
        }
        return fileContent;
    }
    
    /**********************************************************************************************
     * Attaches an html file to the allure report.
     *
     * @author Brandon Clark created September 8, 2016
     * @version 1.0 September 8, 2016
     * @return byte[] - The byte array of the output.
     ***********************************************************************************************/
    @Attachment(value = "{0}", type = "text/html")
    public static byte[] attachHtmlFile(String attachName, File file) {
        try {
            return com.google.common.io.Files.toByteArray(file);
        } catch (Exception e) {
        	Logger.logConsoleMessage("Failed to attach html table.");
        	e.printStackTrace();
        }
        return new byte[0];
    }
    
    @Attachment(value = "Failure Screenshot", type = "image/png")
    public static byte[] attachSikuliScreenshot() {
        byte[] imageContent = emptyString.getBytes();
        try {
        	if (SikuliDriverManager.getSikuliDriver() != null) {
        		ScreenImage screenImage = SikuliDriverManager.getSikuliDriver().capture(
        				SikuliDriverManager.getSikuliDriver().getBounds());
        		BufferedImage bufferedImage = screenImage.getImage();
        		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        		ImageIO.write(bufferedImage, "png", byteStream);
        		imageContent = byteStream.toByteArray();
        	}
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture screenshot.");
            e.printStackTrace();
        }
        return imageContent;
    }
    
    @Attachment(value = "{0}", type = "image/png")
    public static byte[] attachEmergingScreenshot(String allureScreenshotType, File imageFile) {
        byte[] imageContent = emptyString.getBytes();
        try {
        	if (imageFile.exists()) {
        		return com.google.common.io.Files.toByteArray(imageFile);
        	}
        } catch (Exception e) {
            Logger.logConsoleMessage("Failed to capture screenshot.");
            e.printStackTrace();
        }
        return imageContent;
    }

}
