package com.softech.test.core.report;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

public class HueLighting {

	private static String hueLightID = null;
	private static String color;
	private static Boolean singleFailPolicy = false;
	
	public static void enableSingleFailPolicy() {
		singleFailPolicy = true;
	}
	
	public static void setTestSuiteStart(String hueLightID) {
		HueLighting.hueLightID = hueLightID;
        color = "46920"; // blue (test run starting)
		setLightState();
	}
	
	public static void setTestSuiteResult(Integer passTestCount, Integer failTestCount) {
		Integer totalCount = passTestCount + failTestCount;
		Double failurePerc = (double) failTestCount / totalCount;
		if (singleFailPolicy) {
			if (failTestCount > 0) {
				color = "0"; // red (failed)
			} else {
				color = "25717"; // green (success)
			}
		} else {
			if (failTestCount == 0 || failurePerc < 0.05) {
				color = "25717"; // green (success)
			} else if (failurePerc < 0.10) {
				color = "33920"; // yellow
			} else {
				color = "0"; // red (failed)
			}
		}
		
		setLightState();
	}
	
	private static void setLightState() {
		if (GridManager.isQALabHub()) {
			try {
				Logger.logConsoleMessage("Updating hue light id '" + hueLightID + "'.");
				CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
	        	CommandExecutor.execCommand("bash " + Constants.HUB_HUE_LIGHT_SCRIPT + " " 
	        		+ System.getenv("HUE_BRIDGE_IP") + " " + System.getenv("HUE_USER_ID") + " " 
	        		+ hueLightID + " " + color, null, null);
	        } catch (Exception e) {
	        	Logger.logConsoleMessage("Failed to update hue lights.");
	        	e.printStackTrace();
	        }
		}
	}

}