package com.softech.test.core.lab;

import java.io.File;

public class GlobalReportDir {

	private static String dir = null;
	
	public static void setReportDir(String dir) {
		GlobalReportDir.dir = dir;
		
		if (GridManager.isEC2Agent()) {
			File reportDir = new File(dir);
			if (!reportDir.exists()) {
				reportDir.mkdir();
			}
		}
	}
	
	public static String getReportDir() {
		return dir;
	}
	
}
