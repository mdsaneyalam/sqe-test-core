#Multi-Platform Quality Engineering Core Automation Project
Welcome to the core automation project for MQE! The goal of this project is to provide easy to provide an automation framework that is:

1. Easy to incorporate and use
2. Standardized across groups and teams
3. A common core repository to be shared by everyone and consumed easily at the project level

If it's not providing value or making your life easier than we're not doing it right :-)

##Features and Examples

###Driver Declaration
A driver factory accepts a simple Webdriver or Appium Capabilities object that you define and pass to it. This class then hydrates the DriverManager class automatically with the declared driver object that can be called from anywhere within your test classes

Mobile Example:
Pass a capabilities object, the desired MobileOS, and desired DeviceCategory to the DriverFactory().initiateAppiumDriver() method.
     
     DesiredCapabilities capabilities = new DesiredCapabilities();
     capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "iOS");
     capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, ""); //required but not used
     capabilities.setCapability(CapabilityType.PLATFORM, "MAC");
     capabilities.setCapability(CapabilityType.BROWSER_NAME, "iOS");
     capabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, "true");
 
     capabilities.setCapability(MobileCapabilityType.APP, "com.somepackage");
     new DriverFactory().initiateAppiumDriver(MobileOS.IOS, DeviceCategory.PHONE, capabilities);

This will spin up an appium session on the lab on a target device. You can then retrieve the driver session anywhere from your tests with:
`DriverManager.getAppiumDriver()`
Or a particular flavor the OS driver that is also initiated in parallel, i.e.
`DriverManager.getIOSDriver()` or `DriverManager.getAndroidDriver()`

Web Example:

###App Installation
App installation on the lab is facilitated through a command line executor in the JVM environment. You can call 
this CommandExecutor class directly, but convenience methods for everything exist in the LabDeviceManager class. 
To install the app on every device in the lab prior to a test run you can add the necessary commands to either 
your "@BeforeSuite" method or your SuiteListener "onStart" method. Below is a sample script in a project that uses 
the core class method "GlobalInstall.freshInstall()" to:
1. Download the app package from the web.
2. Uninstall any pre-existing versions of the app on all the devices of the OS execution you desire.
3. Install the new app on all the devices.
     
        // install the application on every device in the lab
    	MobileOS mobileOS = null;
    	String fileExt = null;
        if (TestRun.isAndroid()) {
        	mobileOS = MobileOS.ANDROID;
        	fileExt = ".apk";
        } else {
        	mobileOS = MobileOS.IOS;
        	fileExt = ".ipa";
        }
    	if (!ConfigProps.PREINSTALLED) {
    		String appURL = "url to app package";
        	String appPackageID = "your app package id";
        	SimpleDateFormat dateTimeFormat = new SimpleDateFormat(StaticProps.PACKAGE_DATE_FORMAT);
            String fileName = dateTimeFormat.format(new Date()) + fileExt;
            
            Boolean installSuccess = false;
            try {
            	installSuccess = GlobalInstall.freshInstall(mobileOS, appURL, fileName, appPackageID);
            } catch (Exception e) {
            	installSuccess = false;
            	e.printStackTrace();
            }
            if (!installSuccess) {
            	System.out.println("GLOBAL INSTALLATION FAILED!");
            	throw new SkipException("App failed to install properly. Aborting test run.");
        	} else {
        		System.out.println("GLOBAL INSTALLATION SUCCESSFUL!");
        	}
    	}

You can also perform app installations at the individual test level with logic either in your "@BeforeTest" method or in your TestListener "onStart" method. An example might look like:

    // identify the target OS of the execution
    MobileOS mobileOS = TestRun.isAndroid() ? MobileOS.ANDROID : MobileOS.IOS;

    // get the running session of the lab
     String sessionIP = GridManager.getRunningSessionIP();
 
    // set the relevant information for the app installation
    String user = "user@" + GridManager.getRunningSessionIP();
    String deviceID = LabDeviceManager.getDeviceID(sessionIP, mobileOS);
    String appPackageID = "your app id";
    String appPackagePath = "path to where you downloaded your app package file";

    // close the app and uninstall
    DriverManager.getAppiumDriver().closeApp();
    LabDeviceManager.uninstallApp(user, mobileOS, deviceID, appPackageID);

    // re-install the app and re-launch
    LabDeviceManager.installApp(user, mobileOS, deviceID, appPackagePath);
    DriverManager.getAppiumDriver().launchApp();

###Proxy Setup
Each device on the lab has it's own dedicated proxy server up that MUST have an instance spun up prior to test execution. Convenience methods exist in the proxy package and can take care of all the initiation at runtime, but you MUST add the appropriate startup and shutdown server methods to your respective "@BeforeSuite" and "@AfterSuite" methods or listeners. The proxy setup includes both the legacy jetty proxy implementation and the newer littleproxy implementation. For legacy support you simply need to initiate the proxy instances for all the devices on the lab and then do your usual magic:

     // initiate the proxy servers for every device on the lab
     projectProxyManager = new ProjectProxyManager();
     ProxyManager.initAllProxyServers(LabDeviceManager.getAllDeviceProxyPorts(MobileOS.Android));
     HashMap<String, RunProxyServer> proxyServers = ProxyManager.getAllProxyServers();
     for (Map.Entry<String, RunProxyServer> proxyServer : proxyServers.entrySet()) {
         // start the proxy servers per your usual requirements and logic.
     }
 
 ALSO KINDLY REMEMBER TO TEAR DOWN ALL THE PROXY INSTANCES IN YOUR "@AfterSuite" METHOD TO FREE UP RESOURCES!!!
 
 Additionally, you can retrieve a single device instance of the proxy at the test level by simply querying the device proxy port of your running device as follows:
 `RunProxyServer proxyServer = ProxyManager.getProxyServer(LabDeviceManager.getDeviceProxyPort(GridManager.getRunningSessionIP(), mobileOS));`
 
 ###Reporting
 						OMNITURE UTIL CLASS DESCRIPTION
						
Main idea of this class is support collection and validation of Omniture/Bento reporting functionalities.
On the test level, for each transaction which we would like to check reporting functionality we needed to surround by 2 lines of code.
1st line (before transaction) should clear the proxy log
					 
					 //*************************
					 ProxyManager.clearLog();
					 //*************************
after that we have test's transaction for example I just used one below:

					homePage.headerBlock().gamesHubBtn().waitForPresent().click();
					
2nd line of code is actual Omniture validation methode:

				omnitureValidation(softAssert, "Games Hub");
				
This line will always be the same in all insertion points except the second parameter in it. This parameter is 
a name of the transaction we are validating. This name we use in the name of expected atributes and values node.									
Under resources folder we need to store a source file which conteins all expected attributes and values.
Below is a sample of such file - src/test/resources/OmnitureSources/NickJr_Omniture_Expected.json		

{
		"Games Hub":
		{
			"pageName": {"EQUAL": "GAMES:Hub page123"},
			"events": {"CONTAINS": "event16,event13"},
			"v16": {"MATCH": "GAMES:Hub page"},
			"ch": {"EQUAL": "GAMES"},
			"v49": {"EQUAL": "GAMES"},
			"c17": {"EQUAL": "Hub page123"},
			"v17": {"EQUAL": "Hub page"},  
			"v1": {"EQUAL": "nickjr"},
			"c1": {"EQUAL": "nickjr"},
			"v44": {"EQUAL": "Home"} 
		},
		"General"
		{
			"v12": {"MAKE": "landscapeportrait"},
			"c13": {"MATCH": "^(\d+)\s\|\s(\d)\s\|\s(\d)$"},
			"v20": {"MAKE": "dateofyear"},
			"c22": {"MAKE": "landscapeportrait"},
			"c23": {"MATCH": "(\d+)x(\d+)"},
			"c33": {"MAKE": "dayofweek"},
			"c34": {"MAKE": "daytime"},
			"c41": {"MATCH": "(New|Repeat)"},
			"v45": {"MAKE": "dayofweek"},
			"v46": {"MAKE": "daytime"}
		}
	}		
	
	
	As you see the name of my transaction should match one of the nodes in this file.
	Second parameter is a SoftAssert object. Because of Omniture validation is an additional validation of 
	current functional test final report will contain multiple asserts per each test.
	softAssert.assertAll(); will collect all asserts into the final test's report.
	Key inside HashMap can be EQUAL, MATCH, Contains and MAKE
	MAKE is an option to generate an expected value dynamically, during run time
	
	Omniture validation may generate multiple errors as well, that is why I recommend to add to BaseTest class
	method below:
	
	public void omnitureValidation(SoftAssert softAssert, String transactioName) {
        ArrayList<String> reportText = OmnitureSupport.validateOmniture(transactioName);
        for (String text : reportText) {
            softAssert.fail(text);
        }
    } 
    
    
    We need to write 1 more method on project level. I have additional optional String parameter. Some times
    to identify right call we need extra filter string. In my test I left it as empty string queryID = ""
    
     public static ArrayList<String> validateOmniture(String action,
            String... queryID) {
        String filter = "";
        if (queryID.length<1) {
            filter = "";
        }
        else {
            filter = queryID[0];
        }
        //parameters below will be used as part of Error message in the final report
        
  module below is trying to use proper parameters for error messages for final report     
        
        String MobileOS = Config.getString("MobileOS");
        String browser = Config.getString("Browser");
        String nameOrUrl = "";
        if (!(MobileOS.length()>0)) {
            nameOrUrl = Config.getString("SiteURL");
        }
        else {
            nameOrUrl = Config.getString("AppName");
        }
        
        //ConfigProps.OMNITURE_REPORT_INDENTIFIER is a part of URL to identify which contains Omniture
        //reporting. In my case it is - nickjr.112.2o7        
        String actualParameters =  ProxyLogUtils.getRequestQueryString(
                ConfigProps.OMNITURE_REPORT_INDENTIFIER, filter);
        //UrlConstants.PATH_TO_OMNITURE_SOURCE - is a path to the file containing expected reporting values
        //this file example I show above 
        String sourceOmnitureFileName = UrlConstants.PATH_TO_OMNITURE_SOURCE;
        //Rest of the magic makes a method below which is a part of Core project.
        //in cases when we should send expected run time values from the project level Map below is used
        Map<String, Map<String, String>> inputmap = new HashMap<String, Map<String, String>>();
        //if needed values for inputmap assigned here.
        ArrayList<String> reportText = OmnitureUtil.validateOmniture(action, MobileOS, browser, nameOrUrl, actualParameters, sourceOmnitureFileName, inputmap);
        
        return reportText;
    }
    In order to collect request query string we need to add 1 more method to proxyLogUtils 
    ProxyLogUtils.getRequestQueryString(
                ConfigProps.OMNITURE_REPORT_INDENTIFIER, filter);
                
                
    public static String getRequestQueryString(String url, String queryID){
        
        String parameters ="";
        List<HarEntry> logEntries = ProxyManager.getLogEntries();
        for (HarEntry entry : logEntries) {
            if (entry.getRequest().getUrl()
                    .contains(url)
                    && entry.getRequest().getUrl().contains(queryID)) {
                parameters = entry.getRequest().getQueryString().toString();
                Logger.logConsoleMessage("Actual parameters are " + parameters);
            }
        }
        return parameters;
    }          
                
    
    
	1 more things we need to remember. Almost all validation points have repeated attributes to validate,
	I created a separate group "General" and as you can see in the example it contains attributes and value which
	should be verified everywhere. 
		 
					 
					