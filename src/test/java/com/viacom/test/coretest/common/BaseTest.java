package com.viacom.test.coretest.common;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.softech.test.core.util.Logger;

public class BaseTest {

	public void setRunParams(String runParams) {
    	
    }
    
    @BeforeMethod(alwaysRun = true)
    public void startTest() {
    	
    }
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
    	// disable log4j
    	Logger.disableLog4JConsoleOutput();
    }
    
}
