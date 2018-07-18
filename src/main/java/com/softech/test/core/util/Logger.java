package com.softech.test.core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Level;
import org.testng.Reporter;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.GridManager;

import ru.yandex.qatools.allure.annotations.Step;

public abstract class Logger {

	private static Boolean growlEnabled = false;
	private static Boolean growlReady = false;

	/**********************************************************************************************
	 * Logs a message to the console, the allure reports, and the testng
	 * reports.
	 * 
	 * @author Brandon Clark created April 7, 2016
	 * @version 1.1 June 14, 2016
	 ***********************************************************************************************/
	@Step("{0}")
	public static void logMessage(String inMessage) {
		logConsoleMessage(inMessage);
		Reporter.log(inMessage);
		if (growlEnabled && DriverManager.getWebDriver() != null && growlReady) {
			// log the growl message
			try {
				DriverManager.getWebDriver().executeScript("$.growl.notice({ title: 'Notice', message: '"
						+ inMessage.replace("'", "").replace("\"", "") + "' });");
			} catch (Exception e) {
				// ignore as growl notification logging isn't critical
			}
		}
	}

	/**********************************************************************************************
	 * Logs a message to the console, the allure reports, and the testng
	 * reports.
	 * 
	 * @param inMessage
	 *            Object that should be logged.
	 * @author Struneuski Oleg created September 30, 2016
	 ***********************************************************************************************/
	public static void logMessage(Object inMessage) {
		logMessage(String.valueOf(inMessage));
	}

	/**********************************************************************************************
	 * Logs a message to the console only.
	 * 
	 * @author Brandon Clark created April 7, 2016
	 * @version 1.0 April 7, 2016
	 ***********************************************************************************************/
	public static void logConsoleMessage(String inMessage) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date()) + " " + inMessage);
	}

	/**********************************************************************************************
	 * Disables log4j console output. Helpful if you don't want a lot of console
	 * clutter.
	 * 
	 * @author Brandon Clark created April 7, 2016
	 * @version 1.0 April 7, 2016
	 ***********************************************************************************************/
	public static void disableLog4JConsoleOutput() {
		Logger.logConsoleMessage("Disabling log4j console output.");
		org.apache.log4j.Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
	}

	/**********************************************************************************************
	 * Enables log4j console output.
	 * 
	 * @author Brandon Clark created April 7, 2016
	 * @version 1.0 April 7, 2016
	 ***********************************************************************************************/
	public static void enableLog4JConsoleOutput() {
		Logger.logConsoleMessage("Enabling log4j console output.");
		org.apache.log4j.Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.ALL);
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.ALL);
	}

	/**********************************************************************************************
	 * Enables growl notifications that will output test step logging in the
	 * browser DOM.
	 * 
	 * @author Brandon Clark created June 14, 2016
	 * @version 1.0 June 14, 2016
	 ***********************************************************************************************/
	public static void enableGrowlOutput() {
		Logger.logConsoleMessage("Enabling Growl output to the browser window.");
		growlEnabled = true;
	}

	/**********************************************************************************************
	 * Indicates if the user enabled growl notifications. Typically used in the
	 * WebInteract openUrl() method to let it know if it should inject growl
	 * jquery or not.
	 * 
	 * @author Brandon Clark created June 14, 2016
	 * @version 1.0 June 14, 2016
	 * @return Boolean - is growl enabled.
	 ***********************************************************************************************/
	public static Boolean isGrowlEnabled() {
		return growlEnabled;
	}

	/**********************************************************************************************
	 * Indicates if growl was enabled by the user AND successfully injected into
	 * the browser DOM and is ready to rock and roll.
	 * 
	 * @author Brandon Clark created June 14, 2016
	 * @version 1.0 June 14, 2016
	 ***********************************************************************************************/
	public static void setGrowlReady(Boolean isReady) {
		growlReady = isReady;
	}

	public static void logToSysFile(String inMessage) {
		if (!GridManager.isEC2Agent()) {
			return;
		}

		BufferedWriter bufferedWriter = null;
		try {
			String message = "=== NEW LOG ENTRY === " + JenkinsAPIUtil.getRunningJobName() + "-"
					+ JenkinsAPIUtil.getRunningBuildId() + " "
					+ new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS").format(new Date()) + ": " + inMessage;
			File logFile = new File(System.getenv("MQE_SYS_LOG_PATH"));

			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			if (logFile.exists() && logFile.length() > Integer.parseInt(System.getenv("MQE_SYS_LOG_MAX_SIZE"))) {
				logFile.delete();
				logFile.createNewFile();
			}

			bufferedWriter = new BufferedWriter(new FileWriter(logFile.getAbsolutePath(), true));
			bufferedWriter.write(message);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to write message to system log!");
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

	}

}
