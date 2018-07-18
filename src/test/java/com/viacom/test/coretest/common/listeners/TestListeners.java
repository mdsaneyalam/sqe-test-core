package com.viacom.test.coretest.common.listeners;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;

import org.testng.IRetryAnalyzer;
import org.testng.ITestContext;
import org.testng.ITestListener;

import org.testng.ITestResult;

import com.softech.test.core.util.Logger;
import com.viacom.test.coretest.common.BaseTest;

public class TestListeners extends BaseTest implements IRetryAnalyzer, ITestListener, IInvokedMethodListener {

    @Override
    public void onFinish(final ITestContext context) {
    	
    }

    @Override
    public void onStart(final ITestContext test) {
    	
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(final ITestResult result) {
        
    }

    @Override
    public void onTestStart(final ITestResult result) {
    	try {
    		// log the test initiation
        	Logger.logMessage("========NEW TEST SESSION========");
        	Logger.logConsoleMessage("Test: " + result.getInstanceName() + "." + result.getName());
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to log startup data.");
    		e.printStackTrace();
    	}
    }
    
    @Override
    public void onTestSuccess(final ITestResult result) {
    	
    }

    @Override
    public void onTestFailure(final ITestResult result) {
    	
    }

    @Override
    public void onTestSkipped(final ITestResult result) {
    	
    }

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult result) {
		
	}

	@Override
	public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {
		
	}

	@Override
	public boolean retry(ITestResult arg0) {
		return false;
	}
	
}
