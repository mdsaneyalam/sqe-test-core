package com.softech.test.core.util;

public class OSDetector {

    public static Boolean isMac() {
    	return getOS().contains("mac");
    }
    
    public static Boolean isWindows() {
    	return getOS().contains("win");
    }

    public static Boolean isLinux() {
    	String os = getOS();
    	return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }
    
    private static String getOS() {
    	return System.getProperty("os.name").toLowerCase();
    }

}
