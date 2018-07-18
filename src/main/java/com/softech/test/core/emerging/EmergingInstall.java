package com.softech.test.core.emerging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;

public class EmergingInstall {

	private static ThreadLocal<String> appFileName = new ThreadLocal<String>();
	
	public static void downloadAppPackage(EmergingOS emergingOS, String appPackageUrl) {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(Constants.PACKAGE_DATE_FORMAT);
		
	    String appExt = null;
		if (emergingOS.equals(EmergingOS.APPLE_TV)) {
			appExt = ".app";
		} else if (emergingOS.equals(EmergingOS.ROKU)) {
			appExt = ".zip";
		} else if (emergingOS.equals(EmergingOS.FIRE_TV)) {
			appExt = ".apk";
		}
		
		// download the app on the core machine
		appFileName.set(RandomData.getCharacterString(10) + dateTimeFormat.format(new Date()) + appExt);
		
		Integer maxDownloadAtt = 2;
		Boolean downloadSuccess = false;
		Integer downloadIter = 0;
		String result = null;
		while (!downloadSuccess && downloadIter < maxDownloadAtt) {
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				// download the app package to the gateway machines
				try {
					Logger.logConsoleMessage("Downloading app package from '" + appPackageUrl + "'" + " on '" + gateway 
					+ "' with filename '" + appFileName.get() + "'.");
					CommandExecutor.setTargetGatewayIP(gateway);
			        LabDeviceManager.downloadAppPackage(null, appPackageUrl, appFileName.get());
				} catch (Exception e) {
					Logger.logConsoleMessage("App package timed out during download.");
				}
			}
			
			// check the file size of the downloaded package to ensure it's valid
			List<Boolean> existChecks = new ArrayList<Boolean>();
			Boolean existCheck = true;
			List<Boolean> sizeChecks = new ArrayList<Boolean>();
			Boolean sizeCheck = true;
						
			for (GatewayIP gateway : GridManager.getOnlineLabGatewayIPs()) {
				CommandExecutor.setTargetGatewayIP(gateway);
				result = CommandExecutor.execCommand("ls -a " + Constants.HUB_APP_PACKAGE_DIR + appFileName.get(), null, null);
				if (result.toLowerCase().contains("no such file or directory")) {
					existChecks.add(false);
				}
							
				if (!existChecks.contains(false)) {
					CommandExecutor.setTargetGatewayIP(gateway);
					result = CommandExecutor.execCommand("wc -c " + Constants.HUB_APP_PACKAGE_DIR + appFileName.get(), null, null);
					Integer size = Integer.parseInt(result.replace("\n", "").trim().split(" ")[0]);
					if (size < Constants.INVALID_APP_FILE_SIZE) {
						sizeChecks.add(false);
					}
				}
			}
						
			if (!existChecks.contains(false) && !sizeChecks.contains(false)) {
				existCheck = true;
				sizeCheck = true;
			}
					    
			if (existCheck && sizeCheck) {
				Logger.logConsoleMessage("App package downloaded successfully.");
				downloadSuccess = true;
			} else {
				Logger.logConsoleMessage("App package failed to download successfully on attempt '" 
						+ downloadIter + "'. Retrying...");
			}
			downloadIter++;
		}
		
		if (!downloadSuccess) {
			throw new RuntimeException("The app package did not download successfully after '" 
		        + maxDownloadAtt + "' attempts.");
		}
	}
	
	public static String getHubAppFilePath() {
		return Constants.HUB_APP_PACKAGE_DIR + appFileName.get();
	}

}
