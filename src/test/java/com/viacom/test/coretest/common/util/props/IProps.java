package com.viacom.test.coretest.common.util.props;

import com.viacom.test.coretest.common.util.Config;

public class IProps {

	public interface ParamProps {
        String IOS = "iOS";
        String ANDROID = "Android";
        String ALL_DEVICES = StaticProps.ALL_DEVICES;
        String PHONE = StaticProps.PHONE;
        String TABLET = StaticProps.TABLET;
    }
    
	public interface GroupProps {
    	String FULL = "Full";
    	String WEB = "Web";
    	String IOS = "iOS";
    	String ANDROID = "Android";
    	String CORE_DEPLOY = "CoreDeploy";
    	String DEBUG = "Debug";
    }
	
    public interface AttributeProps {
        String TEXT = "text";
        String NAME = "name";
        String VALUE = "value";
        String INDEX = "index";
        String RESOURCE_ID = "resource-id";
    }
    
    public interface StaticProps {
    	String GLOBAL_USER = "admin@";
    	String IOS = "iOS";
    	String ANDROID = "Android";
        String ALL_DEVICES = "AllDevices";
        String PHONE = "Phone";
        String TABLET = "Tablet";
        String ATTACHMENT_DATE_FORMAT = "MMddyyhhmmssSSSa";
        String PACKAGE_DATE_FORMAT = "MMddyyhhmmss";
        Integer MINOR_PAUSE_MS = 500;
        Integer SMALL_PAUSE_MS = 1000;
        Integer SMALL_PAUSE_S = 1;
        Integer MEDIUM_PAUSE_MS = 2000;
        Integer MEDIUM_PAUSE_S = 2;
        Integer LARGE_PAUSE_MS = 3000;
        Integer XLARGE_PAUSE_MS = 5000;
        Integer IMPLICIT_WAIT_MILLISECOND = 20;
        String LOCATOR_VARIABLE_PACKAGE = "$inPackage";
        String LOCATOR_VARIABLE = "$inText";
        String LOCATOR_VARIABLE_UPPER = "$inTextU";
        String LOCATOR_VARIABLE_LOWER = "$inTextL";
        String LOCATOR_ANDROID_UIAUTOMATOR = "AndroidUIAutomator";
        String LOCATOR_IOS_UIAUTOMATION = "IosUIAutomation";
        String LOCATOR_ACCESSIBILITY_ID = "AccessibilityId";
        String LOCATOR_XPATH = "XPath";
        String MOBILE_OS_LOG = "Mobile OS: ";
        String APPLICATION_LOG = "Application: ";
        String LOCALE_LOG = "Locale: ";
        String DEVICE_CATEGORY_LOG = "Device Category: ";
        String DATA_PROVIDER = "getAvailableDevices";
        String BROWSER_DATA_PROVIDER = "getAvailableBrowsers";
        String TEST_ID = "TestID:";
    }
    
    public interface ConfigProps {
    	// RUNTIME CONFIG
    	String MOBILE_OS = Config.getString("MobileOS");
    	String MOBILE_BROWSER = Config.getString("MobileBrowser");
    	Boolean RUN_AS_FACTORY = Config.getBoolean("RunAsFactory");
    	Boolean UPLOAD_REPORT_JENKINS = Config.getBoolean("UploadReportToJenkins");
        Boolean SEND_REPORT_CHAT = Config.getBoolean("SendChatReport");
        Boolean RERUN_ON_FAILURE = Config.getBoolean("ReRunOnFailure");
        Integer RERUN_ON_FAILURE_COUNT = Config.getInt("ReRunOnFailureCount");
        Integer DEBUG_PROXY_PORT = Config.getInt("DebugProxyPort");
        
        // EMAIL CONFIG
        Boolean SEND_REPORT_AUTOEMAILS = Config.getBoolean("SendReportAutoEmails");
        String EMAIL_SENDER_ADDRESS = Config.getString("EmailSenderAddress");
        String SEND_REPORT_EMAIL_ADDRESS = Config.getString("SendReportEmailAddress");
        
        // APP CONFIG
        String ELEMENT_FILE_PATH = Config.getFilePath("PathToElements");
        String IOS_SCREENSHOT_WAIT = Config.getString("IOSAutoScreenshotWait");
        String MQE_HEALTH_TEST_ANDROID_APP_URL = Config.getString("MQEHealthTestAndroidAppUrl");
        String MQE_HEALTH_TEST_ANDROID_APP_PACKAGE = Config.getString("MQEHealthTestAndroidAppPackage");
        String MQE_HEALTH_TEST_ANDROID_LAUNCH_ACTIVITY = Config.getString("MQEHealthTestAndroidLaunchActivity");
        String ROKU_APP_URL = Config.getString("RokuAppUrl");
        String MQE_HEALTH_TEST_IOS_BUNDLE_ID = Config.getString("MQEHealthTestiOSBundleID");
        String MQE_HEALTH_TEST_IOS_URL = Config.getString("MQEHealthTestiOSUrl");
        String APPLICATION_TITLE = Config.getString("ApplicationTitle");
        String IOS_AUTO_ACCEPT_ALERTS = Config.getString("AutoAcceptiOSAlerts");
        String IOS_WAIT_FOR_APP_SCRIPT = Config.getString("iOSWaitForAppScript");
        
        // DEFAULT PROVIDER CONFIG
        String DEFAULT_PROVIDER = Config.getString("DefaultProvider");
        String DEFAULT_USERNAME = Config.getString("DefaultUsername");
        String DEFAULT_PASSWORD = Config.getString("DefaultPassword");
        
        // REPORT CONFIG
        String SCREENSHOT_FILE_PATH = Config.getFilePath("PathToScreenshots");
        String ANDROID_ALLURE_RESULTS_PATH = Config.getFilePath("PathToAllureAndroidResults");
        String IOS_ALLURE_RESULTS_PATH = Config.getFilePath("PathToAllureiOSResults");
        String WEB_ALLURE_RESULTS_PATH = Config.getFilePath("PathToAllureWebResults");
        String JENKINS_WORKSPACE_FILE_PATH =  Config.getString("PathToJenkinsWorkspace");
        String JENKINS_WORKSPACE_URL =  Config.getString("JenkinsWorkspaceUrl");
        String S3_BUCKET_NAME = Config.getString("S3BucketName");
        
        // SPLUNK CONFIG
        String SPLUNK_USERNAME = Config.getString("SplunkUsername");
        String SPLUNK_PASSWORD = Config.getString("SplunkPassword");
        String SPLUNK_INDEX = Config.getString("SplunkIndex");
        
        // CHAT CONFIG
        String DTE_CHAT_WEBHOOK_URL = Config.getString("DTEChatWebHookUrl");
        
        // WEBDRIVER CONFIG
        Integer MAX_WAIT_TIME = Config.getInt("WaitForWaitTime");
        String SERVER_COMMAND_TIMEOUT = Config.getString("ServerCommandTimeout");
        Integer POLLING_TIME = Config.getInt("PollingTime");
    }

}
