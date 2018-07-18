package com.softech.test.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.report.FileZipper;
import com.softech.test.core.util.Logger;

public class DependencyManager {

	private static ThreadLocal<String> binPath = new ThreadLocal<String>();
	private static final String MQE_DEPENDENCY_DIR = System.getProperty("user.dir") + File.separator + "mqe-dependencies";
	
	public static void setDependencyLoc(String dependencyLoc) {
		File dependencyDir = new File(MQE_DEPENDENCY_DIR);
		if (!dependencyDir.exists()) {
			Logger.logConsoleMessage("Creating the '" + MQE_DEPENDENCY_DIR + "' directory.");
			dependencyDir.mkdirs();
		}
		binPath.set(MQE_DEPENDENCY_DIR + File.separator 
				+ dependencyLoc.replace("/", File.separator));
	}
	
	public static String getDependencyLoc() {
		return binPath.get();
	}
	
	public static Boolean dependencyExists() {
		return new File(binPath.get()).exists();
	}
	
	public static void downloadDependency(String fileName) throws Exception {
		Logger.logConsoleMessage("Downloading dependency '" + fileName + "' to the user dir.");
		String fileUrl = Constants.DEPENDENCY_URL + fileName;
		URL url = new URL(fileUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = MQE_DEPENDENCY_DIR + File.separator + fileName;
             
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            Logger.logConsoleMessage("Dependency saved to '" + saveFilePath + "'.");
        } else {
            System.out.println("No dependency found at url '" + fileUrl + "'. Response code: " + responseCode);
        }
        httpConn.disconnect();
	}
	
	public static void unzipDependency(String fileName) {
		Logger.logConsoleMessage("Unzipping dependency files.");
        new FileZipper().unzipZipFile(MQE_DEPENDENCY_DIR + File.separator + fileName, MQE_DEPENDENCY_DIR);
        new File(MQE_DEPENDENCY_DIR + File.separator + fileName).delete();
	}
	
	public static void setDependencyExec(String fileOrDirName) {
		if (OSDetector.isLinux() || OSDetector.isMac()) {
			Logger.logConsoleMessage("Setting dependency as executable.");
        	CommandExecutor.execCommand("chmod -R 777 " + MQE_DEPENDENCY_DIR + File.separator + fileOrDirName, null, null);
        }
	}
	
	public static File getMQEDependencyDirLoc() {
		return new File(MQE_DEPENDENCY_DIR);
	}
	
}