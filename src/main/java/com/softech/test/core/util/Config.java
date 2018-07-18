package com.softech.test.core.util;

public class Config {

    private static String systemTestProp = "system.test.";
    
    public static Integer getMaxWaitTime() {
        String property = System.getProperty(systemTestProp + "waitforwaittime");
        if (property == null) {
        	return 5; // seconds
        }
        return Integer.parseInt(property);
    }
    
    public static Integer getPollingTime() {
        String property = System.getProperty(systemTestProp + "pollingtime");
        if (property == null) {
        	return 500; // milliseconds
        }
        return Integer.parseInt(property);
    }
    
}
