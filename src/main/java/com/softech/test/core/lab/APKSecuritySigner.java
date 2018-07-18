package com.softech.test.core.lab;

import java.io.File;

import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

public class APKSecuritySigner {
	
	private static final Integer MAX_SIGN_WAIT_S = 120;
	private static final String NEW_RESIGNED_TXT = "_new";
	
	public static String updateAPKSecuritySettings(GatewayIP gatewayIP, String pathToAPK) {
		Logger.logConsoleMessage("Updating security settings for apk file for ssl validation on various versions of Android on '" + gatewayIP + "'.");
		String[] command = {"bash", System.getenv("ANDROID_SECURITY_UPDATE_PATH"), pathToAPK};
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.execCommand(command, null, MAX_SIGN_WAIT_S);
		
		return new File(pathToAPK.replace(Constants.APK_EXT, "") + NEW_RESIGNED_TXT + Constants.APK_EXT).getName();
	}
  
}
