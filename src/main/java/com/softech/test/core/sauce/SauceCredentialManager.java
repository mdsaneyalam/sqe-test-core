package com.softech.test.core.sauce;

import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.TestRun;

public class SauceCredentialManager {

	private static String sauceUsername = null;
    private static String sauceKey = null;
    
    /**********************************************************************************************
     * Sets the sauce username and key.
     * 
     * @param String - {@link String} - The sauce username.
     * @param String - {@link String} - The sauce key.
     * @author Brandon Clark created March 9, 2016
     * @version 1.0 March 9, 2016
     ***********************************************************************************************/
    public static void setSauceCreds(String username, String key) {
    	TestRun.setSauceRun(true);
    	sauceUsername = username;
    	sauceKey = key;
    }
    
    /**********************************************************************************************
     * Gets the sauce username.
     * 
     * @author Brandon Clark created March 9, 2016
     * @version 1.0 March 9, 2016
     * @return String - The sauce username.
     ***********************************************************************************************/
    public static String getSauceUsername() {
    	if (GridManager.isEC2Agent()) {
    		return System.getenv("SAUCE_USERNAME");
    	}
    	return sauceUsername;
    }
    
    /**********************************************************************************************
     * Gets the sauce key.
     * 
     * @author Brandon Clark created March 9, 2016
     * @version 1.0 March 9, 2016
     * @return String - The sauce key.
     ***********************************************************************************************/
    public static String getSauceKey() {
    	if (GridManager.isEC2Agent()) {
    		return System.getenv("SAUCE_KEY");
    	}
    	return sauceKey;
    }

}
