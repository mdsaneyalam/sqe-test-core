package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.util.JenkinsAPIUtil;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.SleepUtils;
import com.viacom.test.coretest.common.BaseTest;

import com.viacom.test.coretest.common.util.props.IProps.GroupProps;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class CoreDeployTest extends BaseTest {

	private static final String IOS_JOB_NAME = "MQETestCore-iOS";
	private static final String ANDROID_JOB_NAME = "MQETestCore-Android";
	private static final String WEB_JOB_NAME = "MQETestCore-DesktopWeb";
	
	private static final String LAST_BUILD = "lastBuild";
	private static final String IOS = "iOS";
	private static final String ANDROID = "Android";
	private static final String WEB = "Web";
	private static final String CODE_BRANCH_SYS = "system.test.codebranch";
	private static final String CODE_BRANCH = "CODE_BRANCH";
	private static final String TEST_GROUP = "TEST_GROUP";
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		
    }
	
	@SuppressWarnings("rawtypes")
	@Test(groups = { GroupProps.CORE_DEPLOY })
    @Features(GroupProps.CORE_DEPLOY)
	public void deployCoreTest() {
    	Logger.logConsoleMessage("Trigger the iOS Test Suite.");
    	HashMap<String, String> iOSParams = new HashMap<String, String>();
    	iOSParams.put(CODE_BRANCH, System.getProperty(CODE_BRANCH_SYS));
    	iOSParams.put(TEST_GROUP, IOS);
    	JenkinsAPIUtil.startJob(IOS_JOB_NAME, iOSParams);
    	
    	Logger.logConsoleMessage("Trigger the Android Test Suite.");
    	HashMap<String, String> androidParams = new HashMap<String, String>();
    	androidParams.put(CODE_BRANCH, System.getProperty(CODE_BRANCH_SYS));
    	androidParams.put(TEST_GROUP, ANDROID);
    	JenkinsAPIUtil.startJob(ANDROID_JOB_NAME, androidParams);
    	
    	Logger.logConsoleMessage("Trigger the Web Test Suite.");
    	HashMap<String, String> webParams = new HashMap<String, String>();
    	webParams.put(CODE_BRANCH, System.getProperty(CODE_BRANCH_SYS));
    	webParams.put(TEST_GROUP, WEB);
    	JenkinsAPIUtil.startJob(WEB_JOB_NAME, webParams);
    	
    	Logger.logConsoleMessage("Poll until all jobs have been built.");
    	SleepUtils.sleep(30000);
    	HashMap<String, Boolean> isBuildingMap = new HashMap<String, Boolean>();
    	Boolean testBuildsComplete = false;
    	for (int i = 0; i < 120; i++) {
    		isBuildingMap.put(IOS_JOB_NAME, JenkinsAPIUtil.isJobBuilding(IOS_JOB_NAME, LAST_BUILD));
    		isBuildingMap.put(ANDROID_JOB_NAME, JenkinsAPIUtil.isJobBuilding(ANDROID_JOB_NAME, LAST_BUILD));
    		isBuildingMap.put(WEB_JOB_NAME, JenkinsAPIUtil.isJobBuilding(WEB_JOB_NAME, LAST_BUILD));
    		
    		Boolean iOSBuilding = true;
    		Boolean androidBuilding = true;
    		Boolean webBuilding = true;
    		
    		Iterator iterator = isBuildingMap.entrySet().iterator();
    	    while (iterator.hasNext()) {
    	        Map.Entry pair = (Map.Entry) iterator.next();
    	        if (pair.getValue().equals(false)) {
    	        	if (pair.getKey().equals(IOS_JOB_NAME) && !JenkinsAPIUtil.isJobInQueue(IOS_JOB_NAME)) {
    	        		iOSBuilding = false;
    	        	} else if (pair.getKey().equals(ANDROID_JOB_NAME) && !JenkinsAPIUtil.isJobInQueue(ANDROID_JOB_NAME)) {
    	        		androidBuilding = false;
    	        	} else if (pair.getKey().equals(WEB_JOB_NAME) && !JenkinsAPIUtil.isJobInQueue(ANDROID_JOB_NAME)) {
    	        		webBuilding = false;
    	        	}
    	        }
    	        
    	        iterator.remove();
    	    }
    	    
    	    if (!iOSBuilding && !androidBuilding && !webBuilding) {
    	    	Logger.logConsoleMessage("All jobs completed.");
    	    	testBuildsComplete = true;
    	    	break;
    	    } else {
    	    	Logger.logConsoleMessage("Jobs continuing to queue/test, continuing to poll for completion.");
    	    }
    	    
    		SleepUtils.sleep(5000);
    	}
    	
    	Assert.assertTrue(testBuildsComplete, "All tests did not complete within a reasonable amount of time.");
    	
    	Logger.logConsoleMessage("Get the job result status.");
    	String iOSStatus = JenkinsAPIUtil.getJobStatus(IOS_JOB_NAME, LAST_BUILD);
    	String androidStatus = JenkinsAPIUtil.getJobStatus(ANDROID_JOB_NAME, LAST_BUILD);
    	String webStatus = JenkinsAPIUtil.getJobStatus(WEB_JOB_NAME, LAST_BUILD);
    	
    	Logger.logConsoleMessage("iOS Test Status: " + iOSStatus);
    	Logger.logConsoleMessage("Android Test Status: " + androidStatus);
    	Logger.logConsoleMessage("Web Test Status: " + webStatus);
    	
		
		// set the version
		//CommandExecutor.setEC2CommandHop(false);
		//CommandExecutor.execCommand("mvn versions:set -DnewVersion=1.0.3-SNAPSHOT", null, null);
	}
    
}
