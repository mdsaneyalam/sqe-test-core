package com.softech.test.core.lab;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.amazonaws.AmazonServiceException;
//import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.PutObjectRequest;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.SleepUtils;

public class IOSCodeResigner {

	private static final String S3_BUCKET = System.getenv("S3_RESIGNED_PACKAGE_DIR");
	private static String appDir = null;
	
	public static synchronized String resignApp(GatewayIP gatewayIP, File appPackage, File mobileProvision, String updatedBundleId) {
		setS3AppDir();
		
		File resignedApp = new File(appPackage.getAbsolutePath().replace(".ipa", "_resigned.ipa"));
		
		if (updatedBundleId == null) {
			updatedBundleId = "null.null";
		}
		
		// construct the command file
		String command = "bash " + getPathToResignScript() + " " + appPackage.getAbsolutePath() 
		+ " " + mobileProvision.getAbsolutePath() + " " + resignedApp.getAbsolutePath() + " " + updatedBundleId;
		
		File codeResignCMDFile = new File(GlobalReportDir.getReportDir() + File.separator + "codeResign.command");
		try {
			FileUtils.writeStringToFile(codeResignCMDFile, command, "UTF-8");
		} catch (IOException e) {
			Logger.logConsoleMessage("Failed to create code resign file.");
			e.printStackTrace();
		}
		
		// copy the file to the hub
		CommandExecutor.setEC2CommandHop(false);
		CommandExecutor.execCommand("chmod 777 " + codeResignCMDFile.getAbsolutePath(), null, null);
		FileDeployer.deployFileToGatewayFromEC2Agent(gatewayIP, codeResignCMDFile);
		
		// resign the app
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.execCommand("open " + Constants.HUB_APP_PACKAGE_DIR + codeResignCMDFile.getName(), null, null);
		
		Logger.logConsoleMessage("Starting app code resigning.");
		Boolean resignSuccess = false;
		for (int i = 0; i < 12; i++) {
			CommandExecutor.setTargetGatewayIP(gatewayIP);
			String result = CommandExecutor.execCommand("[ -f " + resignedApp.getAbsolutePath() + " ] "
					+ "&& echo \"Exists\" || echo \"Does Not Exist\"", null, null);
			if (result.contains("Exists")) {
				resignSuccess = true;
				break;
			}
			Logger.logConsoleMessage("App code resign currently underway...");
			SleepUtils.sleep(5000);
		}
		
		if (!resignSuccess) {
			throw new RuntimeException("Failed to code resign app package.");
		}
		
		if (resignSuccess) {
			File appToUpload = new File(FileDeployer.pullFileFromGatewayToAgent(gatewayIP, resignedApp, 
					GlobalReportDir.getReportDir()));
			uploadResignedAppToS3(appToUpload);
			
			// push the re-signed file from the agent to the 02 gateway
			FileDeployer.deployFileToGatewayFromEC2Agent(GatewayIP.LAB_02, appToUpload);
		}
		
		return resignedApp.getAbsolutePath();
	}
	
	private static String getPathToResignScript() {
		return System.getenv("CODE_RESIGN_PATH");
	}
	
	private static String uploadResignedAppToS3(File resignedApp) {
    	String appFileUrl = null;
    	if (GridManager.isQALabHub()) {
    		@SuppressWarnings("deprecation")
			AmazonS3 amazon = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
            
    		try {
            	appFileUrl = getS3ReportUrlBase();
            	amazon.putObject(new PutObjectRequest("mqetestreports/" + S3_BUCKET + "/" + appDir , "resigned.ipa", resignedApp));
            	appFileUrl = appFileUrl + "resigned.ipa";
            	Logger.logConsoleMessage("Successfully uploaded resigned app to directory on Amazon S3 at '" + appFileUrl + "'.");
            } catch (AmazonServiceException e) {
            	Logger.logConsoleMessage("Error: " + e.getMessage());
            	Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
            } catch (AmazonClientException e) {
            	Logger.logConsoleMessage("Error: " + e.getMessage());
            } 
    	}
        return appFileUrl;
    }
	
	private static String getS3ReportUrlBase() {
    	return "https://s3.amazonaws.com/mqetestreports/" + S3_BUCKET + "/" + appDir + "/";
    }
	
	private static void setS3AppDir() {
		appDir = RandomData.getCharacterString(20);
		SimpleDateFormat dirFormat = new SimpleDateFormat("MMddyyhhmmssSSSa");
        appDir = appDir + dirFormat.format(new Date());
    }
	
}