package com.softech.test.core.util;

public class Constants {

	// TODO - a lot of these values should go into the config in resources. Core task as time allows...
	public static final String LOCALHOST = "localhost";
	public static final String LAB_WEBDRIVER_HUB_ADDRESS = "localhost";
	public static final String LAB_WEBDRIVER_HUB_IP = "10.15.15.10";
	public static final String LAB_01_GATEWAY_IP = "172.18.1.40";
	public static final String LAB_02_GATEWAY_IP = "172.18.1.41";
	public static final String LAB_IOS_HUB_PORT = "4445";
	public static final String LAB_IOS_SIM_HUB_PORT = "4448";
	public static final String LAB_ANDROID_HUB_PORT = "4446";
	public static final String LAB_ANDROID_SIM_HUB_PORT = "4447";
	public static final String LAB_WEB_HUB_PORT = "4443";
	public static final String EC2_REBOOT_SCRIPT_PATH = "/Users/mqeadmin/CommandScripts/RebootEC2Instance.sh";
	public static final String GRID_HUB_MACHINE_NAME = "MQEMACPRO-01.local";
	public static final String GRID_HUB_EC2_MACHINE_NAME = "EC2AMAZ";
	public static final String HUB_APP_PACKAGE_DIR = "/Users/mqeadmin/AppPackages/";
	public static final String NODE_APP_PACKAGE_DIR = "/Users/admin/AppPackages/";
	public static final String HUB_APPIUM_LOG_DIR = "/Users/mqeadmin/AppiumLogs";
	public static final String NODE_APPIUM_LOG_IOS = "/Users/admin/appiumlog_ios.log";
	public static final String NODE_APPIUM_LOG_ANDROID = "/Users/admin/appiumlog_android.log";
	public static final String DEVICE_ID = "deviceID";
	public static final String STATUS = "status";
	public static final String MACHINE_NAME = "machineName";
	public static final String DEVICE_CODE = "deviceCode";
	public static final String DEVICE_OS_VERSION = "deviceOSVersion";
	public static final String DEVICE_CATEGORY = "deviceCategory";
	public static final String DEVICE_NAME = "deviceName";
	public static final String DEVICE_PROXY_PORT = "deviceProxyPort";
	public static final String ACTIVE = "active";
	public static final String INACTIVE = "inactive";
	public static final String DISABLED = "disabled";
	public static final String PACKAGE_DATE_FORMAT = "MMddyyhhmmss";
	public static final String CODE_RESIGN_CERTIFICATE = "DE41EC9E6C117F86497473CBE7855ABCF0AD3ABE";
	public static final Integer DRIVER_CONNECT_TIMEOUT_MS = 60000;
    
	public static final Integer PROXY_LOG_TIMEOUT_S = 10;
	public static final Integer PROXY_LOG_POLLING_MS = 500;
	public static final Integer DRIVER_RECYLE_TIMEOUT_MS = 5000;
	public static final Integer DRIVER_NODE_RECYLE_PAUSE_MS = 15000;
	public static final Integer DEVICE_REBOOT_PAUSE_MS = 60000;
	public static final Integer DEVICE_HOME_ELEMENT_TIMEOUT_S = 30;
	public static final Integer DEVICE_WAIT_QUEUE_MAX_ITER = 500; // 1 iter = 10 second wait
	public static final Integer DEVICE_WAIT_QUEUE_MAX_MAINT_ITER = 5;
	public static final Integer DEVICE_WAIT_QUEUE_SLEEP_MS = 10000;
	
	public static final String I_DEVICE_PATH = "/usr/local/Cellar/ideviceinstaller/HEAD/bin/ideviceinstaller";
	public static final String I_DEVICE_DIAGNOSTICS_PATH = "/usr/local/Cellar/libimobiledevice/1.2.0/bin/idevicediagnostics";
	public static final String ADB_PATH = "/usr/local/Cellar/android-platform-tools/23.0.1/bin/adb";
	
	public static final String IOS_KILL_REMOTE_NODE_PATH = "/Users/admin/CommandScripts/KillRemoteiOSNode.sh";
	public static final String IOS_SIM_KILL_REMOTE_NODE_PATH = "/Users/admin/Desktop/KillRemoteiOSSimNode.command";
	public static final String ANDROID_KILL_REMOTE_NODE_PATH = "/Users/admin/CommandScripts/KillRemoteAndroidNode.sh";
	public static final String ANDROID_SIM_KILL_REMOTE_NODE_PATH = "/Users/admin/Desktop/KillRemoteAndroidSimNode.command";
	public static final String WEB_KILL_REMOTE_NODE_PATH = "/Users/admin/CommandScripts/KillRemoteWebNode.sh";
	public static final String WEB_KILL_REMOTE_NODE_PATH_WIN = "/cygdrive/c/Selenium/KillRemoteWebNode.bat";
	public static final String IOS_START_REMOTE_NODE_PATH = "/Users/admin/Desktop/StartNode_iOS.command";
	public static final String IOS_START_WEB_DEBUGGER_PATH = "/Users/admin/CommandScripts/StartiOSWebDebugProxy.command";
	public static final String IOS_STOP_WEB_DEBUGGER_PATH = "/Users/admin/CommandScripts/StopiOSWebDebugProxy.command";
	public static final String IOS_SIM_START_REMOTE_NODE_PATH = "/Users/admin/Desktop/StartNode_iOS_Sim.command";
	public static final String ANDROID_START_REMOTE_NODE_PATH = "/Users/admin/Desktop/StartNode_Android.command";
	public static final String ANDROID_SIM_START_REMOTE_NODE_PATH = "/Users/admin/Desktop/StartNode_Android_Sim.command";
	public static final String WEB_START_REMOTE_NODE_PATH = "/Users/admin/Desktop/StartNode_Web.command";
	public static final String WEB_START_REMOTE_NODE_PATH_WIN = "/cygdrive/c/Selenium/StartNode_Web.bat";
	public static final String ANDROID_SIM_START_PATH = "/Users/admin/Desktop/StartAndroidSim.command";
	public static final String IOS_SIM_START_PATH = "/Users/admin/Desktop/StartiOSSim.command";
	public static final String ANDROID_SIM_STOP_PATH = "/Users/admin/Desktop/StopAndroidSim.command";
	public static final String IOS_SIM_STOP_PATH = "/Users/admin/Desktop/StopiOSSim.command";
	
	public static final String FFMPEG_DIR = "/usr/local/Cellar/ffmpeg";
	public static final String MP4_EXT = ".mp4";
	public static final String FLV_EXT = ".flv";
	public static final String MOV_EXT = ".mov";
	public static final long INVALID_FILE_SIZE = 50;
	public static final long INVALID_APP_FILE_SIZE= 262144;
	public static final String APK_EXT = ".apk";
	public static final String IPA_EXT = ".ipa";
	public static final String ZIP_EXT = ".zip";
	public static final String JMX_EXT = ".jmx";
	public static final String HTTP = "http";
	
	public static final Integer DRIVER_MAX_SESSION_ATTEMPTS = 3;
	public static final String DRIVER_FAILURE_EMAIL_ADDRESS = "brandon.clark@viacom.com";
	public static final String DRIVER_FAILURE_EMAIL_SENDER_ADDRESS = "NOREPLY@MQE-DRIVER-MONITORING.com";
	
	public static final String SPLUNK_PATH = "/Applications/Splunk/bin/splunk";
	public static final String SPLUNK_GLOBAL_TEST_DATA_DIR = "/Users/mqeadmin/splunkdata/globaltestdata/";
	public static final Integer SPLUNK_MAX_POST_SIZE = 1024000;
	public static final String SPLUNK_LAB_USERNAME = System.getenv("SPLUNK_USERNAME");
	public static final String SPLUNK_LAB_PASSWORD = System.getenv("SPLUNK_PASSWORD");
	public static final String SPLUNK_LAB_PORT = System.getenv("SPLUNK_PORT");
	public static final String SPLUNK_LAB_GLOBAL_INDEX = "globaltestdata";
	public static final String SPLUNK_LAB_JMETER_INDEX = "jmetersummarytestdata";
	
	public static final String SELENDROID = "Selendroid";
	
	public static final String BMP_P12_FILE = "ca-keystore-rsa.p12";
	public static final String BMP_PASS = "password";
	public static final String BMP_ALIAS = "MQESelfSigned";
	
	public static final String JMETER_PATH = "/Applications/jmeter/bin/jmeter";
	public static final String JMETER_START_NODE_PATH = "/Users/admin/Desktop/StartJMeterServer.command";
	public static final String JMETER_STOP_NODE_PATH = "/Users/admin/Desktop/StopJMeterServer.command";
	public static final String JMETER_START_WEB_HUB_PATH = "/Users/mqeadmin/CommandScripts/StartHub_Perf_Web.command";
	public static final String JMETER_STOP_WEB_HUB_PATH = "/Users/mqeadmin/CommandScripts/StopHub_Perf_Web.command";
	public static final String JMETER_START_WEB_NODE_PATH = "/Users/admin/CommandScripts/StartNode_Perf_Web.command";
	public static final String JMETER_STOP_WEB_NODE_PATH = "/Users/admin/CommandScripts/StopNode_Perf_Web.command";
	public static final String JMETER_WEB_HUB_PORT = "4447";
	public static final Integer JMETER_AGENT_THREAD_LIMIT = 100;
	public static final Integer JMETER_DURATION_LIMIT = 3600; // 1 hour
	public static final Integer JMETER_LOOP_COUNT_LIMIT = 1000;
	public static final Integer JMETER_NODE_WAIT_MS = 10000;
	
	public static final String MQE_LAB_DB_NAME = "MQELab";
	public static final String MQE_LAB_DB_IP = "10.242.152.65";
	public static final String PSQL_PATH = "/usr/local/Cellar/postgresql/9.5.3/bin/psql";
	public static final String MQE_LAB_DB_HOST = "localhost";
	public static final Integer MQE_LAB_DB_PORT = 5432;
	public static final String MQE_LAB_DB_USER = "mqeadmin";
	public static final String MQE_LAB_DB_AGENT_MACHINES = "agentmachines";
	public static final String MQE_LAB_DB_IOS_DEVICES = "iosdevices";
	public static final String MQE_LAB_DB_APPLE_TV_DEVICES = "appletvdevices";
	public static final String MQE_LAB_DB_ROKU_DEVICES = "rokudevices";
	public static final String MQE_LAB_DB_IOS_SIMULATORS = "iossimulators";
	public static final String MQE_LAB_DB_ANDROID_DEVICES = "androiddevices";
	public static final String MQE_LAB_DB_ANDROID_SIMULATORS = "androidsimulators";
	public static final String MQE_LAB_DB_BROWSER_NODES = "browsernodes";
	public static final String MQE_LAB_DB_ANDROID_REQUESTS = "androidrequests";
	public static final String MQE_LAB_DB_IOS_REQUESTS = "iosrequests";
	public static final String MQE_LAB_DB_ANDROID_PRIORITY = "androidpriority";
	public static final String MQE_LAB_DB_IOS_PRIORITY = "iospriority";
	public static final String MQE_LAB_DB_ANDROID_PROXY_USE_LOG = "androidproxyuselog";
	public static final String MQE_LAB_DB_IOS_PROXY_USE_LOG = "iosproxyuselog";
	
	public static final String HUB_HUE_LIGHT_SCRIPT = "/Users/mqeadmin/CommandScripts/UpdateHueLights.sh";
	
	public static final String DEPENDENCY_URL = "https://s3.amazonaws.com/mqeappprodpackages/";
	
}
