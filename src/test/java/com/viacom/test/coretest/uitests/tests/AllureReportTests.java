package com.viacom.test.coretest.uitests.tests;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.props.AllureScreenshotType;
import com.softech.test.core.report.AllureAttachment;
import com.softech.test.core.report.AllureManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.SleepUtils;
import com.viacom.test.coretest.common.BaseTest;
import com.viacom.test.coretest.common.util.props.IProps.ConfigProps;
import com.viacom.test.coretest.common.util.props.IProps.GroupProps;
import com.viacom.test.coretest.uitests.support.DefaultCapabilityFactory;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ru.yandex.qatools.allure.annotations.Features;

public class AllureReportTests extends BaseTest {

	DefaultCapabilityFactory defaultCaps = null;
	AllureManager allureManager;
	
	@BeforeMethod(alwaysRun = true)
    public void setupTest() {
		defaultCaps = new DefaultCapabilityFactory();
		allureManager = new AllureManager();
    }
	
	@Test(groups = { GroupProps.WEB, GroupProps.DEBUG })
    @Features(GroupProps.WEB)
	public void globalReportDirWebTest() {
    	Logger.logMessage("Verify the global report dir was set/created.");
    	allureManager.setReportDir("");
    	String globalReportDir = GlobalReportDir.getReportDir();
    	Assert.assertNotNull(globalReportDir);
    	Assert.assertTrue(new File(globalReportDir).exists());
	}
	
	@Test(dependsOnMethods = { "globalReportDirWebTest" }, groups = { GroupProps.WEB, GroupProps.DEBUG })
    @Features(GroupProps.WEB)
	public void attachScreenshotWebTest() {
		Logger.logMessage("Start a webdriver instance on the lab.");
		DriverFactory.initiateWebDriver(defaultCaps.desktopWeb());
		
		Logger.logMessage("Open a webpage.");
		DriverManager.getWebDriver().get("http://www.seleniumhq.org");
		SleepUtils.sleep(1000);
		
		Logger.logMessage("Attach a screenshot to the allure report.");
		byte[] imageContent = AllureAttachment.attachScreenshot(AllureScreenshotType.SUCCESS.value());
		Assert.assertNotNull(imageContent);
		Assert.assertTrue(imageContent.length > 25);
	}
	
	@Test(dependsOnMethods = { "attachScreenshotWebTest" }, groups = { GroupProps.WEB, GroupProps.DEBUG })
    @Features(GroupProps.WEB)
	public void generateWebReportTest() throws IOException {
		Logger.logConsoleMessage("Copy the test report xml to the global report dir.");
		File sampleReportDir = new File(System.getProperty("user.dir") + "/src/test/resources/AllureReportSample");
		File globalReportDir = new File(GlobalReportDir.getReportDir());
		Assert.assertTrue(globalReportDir.exists());
		FileUtils.copyFileToDirectory(new File(sampleReportDir + File.separator 
				+ "b41fb551-7329-4331-8290-26ba1bb77c95-testsuite.xml"), globalReportDir);
		FileUtils.copyFileToDirectory(new File(sampleReportDir + File.separator 
				+ "58a593bf-d4c8-4b3a-a60a-e5b76bd028e7-attachment.png"), globalReportDir);
		
		Logger.logMessage("Generate the report and verify success.");
    	allureManager.generateReport();
    	Assert.assertTrue(allureManager.getReportGenResult());
    	
    	Logger.logMessage("Upload the report to S3.");
    	String reportUrl = allureManager.uploadReportToS3(ConfigProps.S3_BUCKET_NAME);
    	Assert.assertNotNull(reportUrl);
    	
    	Logger.logMessage("Verify the report is accessible.");
    	CommandExecutor.setEC2CommandHop(false);
    	String result = CommandExecutor.execCommand("curl " + reportUrl, null, null);
    	Assert.assertTrue(result.contains("<title>MQE Dashboard</title>"));
	}
    
}
