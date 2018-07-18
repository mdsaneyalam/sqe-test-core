package com.softech.test.core.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.utils.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.softech.test.core.lab.AvailableDevicePoller;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.CoreVersionChecker;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.MVNProcessManager;
import com.softech.test.core.lab.ProxyUseLogger;
import com.softech.test.core.props.AllureScreenshotType;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.DependencyManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;
import com.softech.test.core.util.RandomData;
import com.amazonaws.services.s3.transfer.TransferManager;

public class AllureManager {

	private static final String ALLURE_AGENT_PATH = System.getenv("ALLURE_LINUX_COMMANDLINE_PATH");
	private static final String ALLURE = "allure";
	private static final String ALLURE_ZIP = "allure.zip";
	
    private static final String FAILURE_SPLITTER = "::";
	private static final String TEMP_AGENT_DIR_WIN = "C:\\TEMP\\ReportDir";
	private static final String TEMP_AGENT_DIR_LIN = System.getenv("JENKINS_AGENT_TEMP_PATH") 
		+ "/ReportDir";
	private static final Integer MIN_SCREENSHOT_BYTES = 25;

	//private String reportDir;
	private String s3ProjectBucketName;
	private String s3ReportDir;
	private String s3ReportUrl;
	
	private Boolean reportGenSuccess = false;
	
	public AllureManager setReportDir(String reportDir) {
		// every project calls this on start, so for now hijack this to set the lab priority and the maven process id
		CoreVersionChecker.checkCoreVersion();
		
		if (GridManager.isEC2Agent()) {
			AvailableDevicePoller.initPriority();
			MVNProcessManager.initMVNProcessId();
		}
		
		// if it's an agent on the lab, ignore the passed user report dir and create a temp dir
		String reportDirectory = null;
		if (GridManager.isEC2Agent()) {
			if (OSDetector.isWindows()) {
				reportDirectory = TEMP_AGENT_DIR_WIN + RandomData.getCharacterString(25);
			} else if (OSDetector.isLinux()) {
				reportDirectory = TEMP_AGENT_DIR_LIN + RandomData.getCharacterString(25);
			}
		} else { // user specified local directory for the report
			reportDir.replace("/", File.separator);
			if (reportDir.endsWith(File.separator)) {
				reportDir = reportDir.substring(0, reportDir.length() - 1);
			}
			reportDirectory = reportDir;
		}
		
		GlobalReportDir.setReportDir(reportDirectory);
		System.setProperty("allure.results.directory", reportDirectory);
		return this;
	}
	
	public AllureManager deleteReportDir() {
    	File directory = new File(GlobalReportDir.getReportDir());
    	
    	// delete the dir and recreate if local run
    	if (directory.exists() && !GridManager.isQALabHub()) {
    		try {
    			FileUtils.deleteDirectory(directory);
    			FileUtils.forceMkdir(directory);
    		} catch (Exception e) {
    			Logger.logConsoleMessage("Failed to re-create local reporting directory.");
    			e.printStackTrace();
    		}
    	}
    	
    	// delete the screenshots dir and recreate
    	File screenshotDir = new File(System.getProperty("user.dir") + File.separator + "test-output" + File.separator + "screenshots");
    	if (screenshotDir.exists() && screenshotDir.isDirectory()) {
    		try {
    			FileUtils.deleteDirectory(screenshotDir);
    			FileUtils.forceMkdir(screenshotDir);
    		} catch (Exception e) {
    			Logger.logConsoleMessage("Failed to remove/recreate screenshot directory.");
    			e.printStackTrace();
    		}
    	}
    	
    	return this;
    }
	
    public AllureManager removePendingTests() {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each pending node
            XPathExpression expression = getXPathExpression("//test-case[@status='pending']");
			
            // get all the pending test case nodes and remove them
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i < results.getLength(); i++) {
            	Node node = results.item(i);
            	node.getParentNode().removeChild(node);
            }
            
            // rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to remove pending tests.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager removeFailures(List<String> failuresToRemove) {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();
    		
    		// for each failure, get the failure node and remove it
    		for (String failure : failuresToRemove) {
    			XPathExpression failedExpression = getXPathExpression("//test-case[@status='failed']//title[text()='" + failure + "']");
                NodeList failedNodes = (NodeList) failedExpression.evaluate(document, XPathConstants.NODESET);
                for (int i=0; i < failedNodes.getLength(); i++) {
                	Node failedNode = failedNodes.item(i);
                	if (failedNode.getTextContent().contains(failure)) {
                		Logger.logConsoleMessage("Removing Allure Test with id '" + failure + "'.");
                		Node subNode = failedNode.getParentNode().getParentNode().getParentNode();
                		subNode.getParentNode().removeChild(subNode);
                	}
                }
    		}
    		
    		// rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to remove failures.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setRepeatFailuresAsBroken(List<String> failuresToUpdate) {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();
    		
    		// for each failure, get the failure node and set it to broken
    		for (String failure : failuresToUpdate) {
    			XPathExpression failedExpression = getXPathExpression("//test-case[@status='failed']//title[text()='" + failure + "']");
                NodeList failedNodes = (NodeList) failedExpression.evaluate(document, XPathConstants.NODESET);
                for (int i=0; i < failedNodes.getLength(); i++) {
                	Logger.logConsoleMessage("Updating Allure Test with id '" + failure + "' to broken status on successful repeat.");
                	failedNodes.item(i).getParentNode().getParentNode().getParentNode().getAttributes()
                        .getNamedItem("status").setTextContent("broken");
                }
    		}
    		
    		// rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to remove failures.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setFailuresAsBrokenByException(List<String> exceptionTypes) {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();
    		
    		// for each exception type, get the failure node and set it to broken
    		for (String exception : exceptionTypes) {
    			XPathExpression failedExpression = getXPathExpression("//test-case[@status='failed']/failure/stack-trace");
                NodeList failedNodes = (NodeList) failedExpression.evaluate(document, XPathConstants.NODESET);
                for (int i=0; i < failedNodes.getLength(); i++) {
                	Logger.logConsoleMessage("Updating Allure Tests with with exception type '" + exception + "' to broken status.");
                	
                	Node node = failedNodes.item(i);
                	if (node.getTextContent().contains(exception)) {
                		node.getParentNode().getParentNode().getAttributes().getNamedItem("status").setTextContent("broken");
                	}
                }
    		}
    		
    		// rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update failures.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setBrokenTestsAsFailures() {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each pending node
            XPathExpression expression = getXPathExpression("//test-case[@status='broken']");
            
            // get all the broken test case nodes and update them to failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i < results.getLength(); i++) {
            	Node node = results.item(i);
            	node.getAttributes().getNamedItem("status").setTextContent("failed");
            }
            
            // rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update tests to failed status.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setFailedBeforeMethodsBroken() {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each pending node
            XPathExpression expression = getXPathExpression("//test-case[@status='failed']//description[contains(text(), '@Before')]/..");
            
            // get all the failed @BeforeMethod test case nodes and update them to broken
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i < results.getLength(); i++) {
            	Node node = results.item(i);
            	node.getAttributes().getNamedItem("status").setTextContent("broken");
            }
            
            // rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update @BeforeMethod to broken status.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setTestIDsAsTestName(String testIdentifier) {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();
    		
    		// get every test case node
    		XPathExpression expression = getXPathExpression("//test-case/name");
            NodeList testCaseNodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            
            // set the test name to the test id of every test case
            for (int i=0; i < testCaseNodes.getLength(); i++) {
            	Node node = testCaseNodes.item(i);
            	Node idNode = (Node) getXPath().evaluate("./..//title[contains(text(), '" + testIdentifier + "')]", 
            			node, XPathConstants.NODE);
            	try {
            		String testId = idNode.getTextContent().replace(testIdentifier, "");
                	node.setTextContent(testId);
            	} catch (NullPointerException e) {
            		// ignore any nodes that don't contain the subsequent child
            	}
            }
            
    		// rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update test case name.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public AllureManager setSuiteName(String suiteName) {
    	try {
    		// get the report file xml
    		Document document = getReportDocument();
    		
    		// set the suite name
    		Node suiteNameNode = (Node) getXPathExpression("//name").evaluate(document, XPathConstants.NODE);
            suiteNameNode.setTextContent(suiteName);
            
            // set the suite title
    		Node titleNameNode = (Node) getXPathExpression("//title").evaluate(document, XPathConstants.NODE);
            titleNameNode.setTextContent(suiteName);
            
    		// rebuild the xml report file
            rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update test case name.");
    		e.printStackTrace();
    	}
    	return this;
    }
    
    public Integer getFailureCount() {
    	Integer failureCount = null;
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each failed node
            XPathExpression expression = getXPathExpression("//test-case[@status='failed']");
            
            // get the count of the failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            failureCount = results.getLength();
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the count of failed tests.");
    		e.printStackTrace();
    	}
    	return failureCount;
    }
    
    public Integer getPassedCount() {
    	Integer passCount = null;
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each passed node
            XPathExpression expression = getXPathExpression("//test-case[@status='passed']");
            
            // get the count of the failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            passCount = results.getLength();
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the count of failed tests.");
    		e.printStackTrace();
    	}
    	return passCount;
    }
    
    public Integer getSkippedCount() {
    	Integer skipCount = null;
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each skipped node
            XPathExpression expression = getXPathExpression("//test-case[@status='canceled']");
            
            // get the count of the failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            skipCount = results.getLength();
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the count of skipped tests.");
    		e.printStackTrace();
    	}
    	return skipCount;
    }
    
    public Integer getBrokenCount() {
    	Integer brokenCount = null;
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each broken node
            XPathExpression expression = getXPathExpression("//test-case[@status='broken']");
            
            // get the count of the failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            brokenCount = results.getLength();
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the count of broken tests.");
    		e.printStackTrace();
    	}
    	return brokenCount;
    }
    
    public List<String> getFailedTestNames() {
    	List<String> failedTests = new ArrayList<String>();
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each failed node
            XPathExpression expression = getXPathExpression("//test-case[@status='failed']/name");
            
            // get the name of all the test failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i < results.getLength(); i++) {
            	Node node = results.item(i);
            	failedTests.add(node.getTextContent());
            }
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the names of all the failed test cases.");
    		e.printStackTrace();
    	}
    	return failedTests;
    }
    
    @SuppressWarnings("unchecked")
	private List<String> getFailedScreenshotData() {
    	List<String> failureData = new ArrayList<String>();
    	try {
    		// get the report file xml
    		Document document = getReportDocument();

            // construct xpath query for each failed node
            XPathExpression expression = getXPathExpression("//test-case[@status='failed']");
            
            // get the test name and screenshot attachment name of all the test failures
            NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            for (int i=0; i < results.getLength(); i++) {
            	Node node = results.item(i);
            	Node nameNode = (Node) getXPath().evaluate("./name", node, XPathConstants.NODE);
            	Node screenshotNode = (Node) getXPath().evaluate(".//attachments/attachment[@title='" + AllureScreenshotType.FAILURE.value() + "']", node, 
            			XPathConstants.NODE);
            	Node featureNode = (Node) getXPath().evaluate(".//labels/label[@name='feature']", node, XPathConstants.NODE);
            	
            	String testName = nameNode.getTextContent();
            	
            	String screenshotName = "";
            	if (screenshotNode != null) {
            		screenshotName = screenshotNode.getAttributes().getNamedItem("source").getTextContent();
            	} else {
            		screenshotName = "No Screenshot";
            	}
            	
            	String featureName = "";
            	if (featureNode != null) {
            		featureName = featureNode.getAttributes().getNamedItem("value").getTextContent();
            	} else {
            		featureName = "No Feature";
            	}
            	
            	// get the url data and build the screenshot link
            	String suiteID = null;
            	String testID = null;
            	String attachmentID = null;
            	String reportLoc = GlobalReportDir.getReportDir() + File.separator + "allure-report" + File.separator;
            	JSONObject xunitObj = getReportFileDataJson(reportLoc + "data" + File.separator + "xunit.json");
            	JSONArray testSuitesArr = (JSONArray) xunitObj.get("testSuites");
            	JSONObject suiteObj = (JSONObject) testSuitesArr.get(0);
            	suiteID = suiteObj.get("uid").toString();
            	testID = null;
            	attachmentID = null;
            	JSONArray testCasesArr = (JSONArray) suiteObj.get("testCases");
            	Iterator<JSONObject> testCasesIterator = testCasesArr.iterator();
                while (testCasesIterator.hasNext()) {
                    JSONObject testCaseObj = testCasesIterator.next();
                    if (testCaseObj.get("name").toString().contains(testName)) {
                    	String uid = testCaseObj.get("uid").toString();
                    	Boolean failureInList = false;
                    	for (String entry : failureData) {
                    		if (entry.contains(uid)) {
                    			failureInList = true;
                    		}
                    	}
                    	
                    	if (!failureInList) {
                    		testID = uid;
                    		break;
                    	}
                    }
                }
                
                JSONObject testObj = getReportFileDataJson(reportLoc + "data" + File.separator + testID + "-testcase.json");
                JSONArray attachmentsArr = (JSONArray) testObj.get("attachments");
                Iterator<JSONObject> attachmentsIterator = attachmentsArr.iterator();
                while (attachmentsIterator.hasNext()) {
                    JSONObject attachmentObj = attachmentsIterator.next();
                    if (attachmentObj.get("title").toString().contains("Failure Screenshot")) {
                    	attachmentID = attachmentObj.get("uid").toString();
                    	break;
                    }
                }
                String attachmentURL = "#/xunit/" + suiteID + "/" + testID + "/" + attachmentID;
            	
                failureData.add(testName + FAILURE_SPLITTER + screenshotName + FAILURE_SPLITTER + featureName + FAILURE_SPLITTER + attachmentURL);
            }
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to get the test names/associated screenshots of the failed test cases.");
    		e.printStackTrace();
    	}
    	return failureData;
    }
    
    @SuppressWarnings("rawtypes")
	public AllureManager createAllurePropertyFile(String propertyFileName, LinkedHashMap<String, String> allureProps) {
    	File propertyFile = new File(GlobalReportDir.getReportDir() + File.separator + propertyFileName);
        
        propertyFile.mkdirs();
        
        if (propertyFile.exists()) {
        	propertyFile.delete();
        } else {
        	try {
				propertyFile.createNewFile();
			} catch (IOException e) {
				Logger.logConsoleMessage("Failed to generate allure properties file at: " 
			        + propertyFile.getAbsolutePath());
				e.printStackTrace();
			}
        }
        
        BufferedWriter writer = null;
        try {
			writer = new BufferedWriter(new FileWriter(propertyFile));
			
			String props = "";
			Iterator iterator = allureProps.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry pair = (Map.Entry) iterator.next();
				String prop = pair.getKey() + "=" + pair.getValue() + System.getProperty("line.separator");
			    props += prop;
			    
			}
			writer.write(props);
			writer.close();
		} catch (IOException e) {
			Logger.logConsoleMessage("Failed to write properties to prop file.");
			e.printStackTrace();
		}
        return this;
    }
    
    public AllureManager generateReport() {
    	// TODO every project calls this on close, so for now hijack this to remove the lab priority
    	// and the proxy use logs
    	if (GridManager.isEC2Agent()) {
    		AvailableDevicePoller.removePriority();
    		ProxyUseLogger.removeAllJobUseLogs();
    		MVNProcessManager.removeMVNProcessId();
    	}
    			
    	// Override the build status of the test job if all retried tests pass on retry
    	if (GridManager.isEC2Agent() && this.getFailureCount().equals(0) && this.getBrokenCount() > 0) {
    		// NOTE - this entry should match the regex key RETRY_SUCCESS_ID on the lab
    		Logger.logConsoleMessage("All tests passed on retry. Overriding build status to success.");
    	}
    	
    	String allurePath = null;
    	if (GridManager.isEC2Agent()) {
    		allurePath = ALLURE_AGENT_PATH;
    	} else {
    		// check if allure lives in the project home dir
    		String allureBinName = OSDetector.isWindows() ? "allure.bat" : ALLURE;
    		String allureFileName = ALLURE_ZIP;
    		try {
    			DependencyManager.setDependencyLoc("allure/dist/bin/" + allureBinName);
    			if (!DependencyManager.dependencyExists()) {
    				DependencyManager.downloadDependency(ALLURE_ZIP);
    				DependencyManager.unzipDependency(ALLURE_ZIP);
    				DependencyManager.setDependencyExec(ALLURE);
    			}
    			
				allurePath = DependencyManager.getDependencyLoc();
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to get dependency '" + allureFileName + "'.");
				e.printStackTrace();
			}
    	}
        
        // generate the html report
    	if (GridManager.isEC2Agent()) {
    		CommandExecutor.setEC2CommandHop(false);
    	}
    	String reportDir = GlobalReportDir.getReportDir();
    	String output = CommandExecutor.execCommand(allurePath + " generate " + reportDir + " -o " + reportDir + File.separator + "allure-report", null, 120);
        if (output.contains("success")) {
    		reportGenSuccess = true;
    		
    		// customize report
            modifyReportForViacom();
            
            // drop the report data into splunk
            uploadReportDataToSplunk();
    	} else {
    		Logger.logConsoleMessage("Failed to generate allure report - " + output);
    	}
        
    	return this;
    }
    
    public AllureManager uploadReportToJenkins(String jenkinsWorkspaceReportPath) {
    	String reportPath = GlobalReportDir.getReportDir() + "allure-report";
        try {
        	// copy the allure report directory
        	FileUtils.copyDirectory(new File(reportPath), new File(jenkinsWorkspaceReportPath));		
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to copy report artifact to jenkins.");
			e.printStackTrace();
		}
        return this;
    }
    
    /**********************************************************************************************
     * Uploads the report to project's Amazon S3 bucket.
     * 
     * @param projectBucketName - {@link String} - The project's S3 bucketname where the reports live.
     * @author Brandon Clark created April 13, 2016
     * @version 1.1 April 19, 2016
     * @return - String - The public url of the report.
     ***********************************************************************************************/
	public String uploadReportToS3(String projectBucketName) {
		AmazonS3 amazon = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
        
		s3ProjectBucketName = projectBucketName;
		s3ReportDir = RandomData.getCharacterString(20);
		try {
        	SimpleDateFormat dirFormat = new SimpleDateFormat("MMddyyhhmmssSSSa");
        	s3ReportDir = s3ReportDir + dirFormat.format(new Date());
        	String reportDirComplete = GlobalReportDir.getReportDir() + File.separator + "allure-report";
        	
        	TransferManager transferManager = new TransferManager(amazon);
        	MultipleFileUpload upload = transferManager.uploadDirectory("mqetestreports/" + s3ProjectBucketName, 
        			s3ReportDir, new File(reportDirComplete), true);
        	upload.waitForCompletion();
        	if (upload.getState().equals(TransferState.Completed)) {
        		s3ReportUrl = "https://s3.amazonaws.com/mqetestreports/" + s3ProjectBucketName + "/" + s3ReportDir + "/index.html";
        	    Logger.logConsoleMessage("Successfully uploaded test report to Amazon S3 at '" + s3ReportUrl + "'.");
        	}
        } catch (AmazonServiceException e) {
        	Logger.logConsoleMessage("Error: " + e.getMessage());
        	Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
        	e.printStackTrace();
        } catch (AmazonClientException e) {
        	Logger.logConsoleMessage("Error: " + e.getMessage());
        	e.printStackTrace();
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        if (s3ReportUrl.isEmpty()) {
        	Logger.logConsoleMessage("Failed to upload test report to Amazon S3.");
        }
        return s3ReportUrl;
    }
	
	/**********************************************************************************************
     * Uploads an additional file to the already uploaded Amazon S3 report bucket.
     * 
     * @param filePathToUpload - {@link String} - The path to the local file you want to upload.
     * @param fileName - {@link String} - The desired file name.
     * @author Brandon Clark created April 19, 2016
     * @version 1.0 April 19, 2016
     * @return - String - The public url of the uploaded file in the report.
     ***********************************************************************************************/
	public String uploadFileToS3ReportDir(String filePathToUpload, String fileName) {
		AmazonS3 amazon = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());
        
		String s3FileUrl = null;
		try {
        	amazon.putObject(new PutObjectRequest("mqetestreports/" + s3ProjectBucketName + "/" + s3ReportDir , fileName, new File(filePathToUpload)));
        	s3FileUrl = s3ReportUrl.replace("index.html", "") + fileName;
        	Logger.logConsoleMessage("Successfully uploaded test file to report directory on Amazon S3 at '" + s3FileUrl + "'.");
        } catch (AmazonServiceException e) {
        	Logger.logConsoleMessage("Error: " + e.getMessage());
        	Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
        } catch (AmazonClientException e) {
        	Logger.logConsoleMessage("Error: " + e.getMessage());
        }
        
        if (s3FileUrl == null) {
        	Logger.logConsoleMessage("Failed to upload test file to Amazon S3.");
        }
        return s3FileUrl;
    }
    
	public Boolean getReportGenResult() {
		return reportGenSuccess;
	}
	
    private File getReportXMLFile() {
    	File directory = new File(GlobalReportDir.getReportDir());
        File reportFile = null;
        for (File file : directory.listFiles()) {
        	if (file.getName().endsWith(".xml")) {
        		reportFile = file;
        		break;
        	}
        }
        return reportFile;
    }
    
    private Document getReportDocument() throws Exception {
    	File reportFile = getReportXMLFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
        return dbf.newDocumentBuilder().parse(reportFile);
    }
    
    private void rebuildXMLFile(Document inDocument) throws Exception {
    	TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(inDocument), new StreamResult(getReportXMLFile()));
    }
    
    private XPathExpression getXPathExpression(String xpathQuery) throws Exception {
    	XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        return xpath.compile(xpathQuery);
    }
    
    private XPath getXPath() throws Exception {
    	XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        return xpath;
    }
    
    private void modifyReportForViacom() {
    	// TODO - most of this is data that should come from a resource file. Refactor as time allows.
    	
    	// get the generated report location
    	String reportLoc = GlobalReportDir.getReportDir() + File.separator + "allure-report" + File.separator;
    	String data = null;
    	
    	// remove the allure header
    	data = getReportFileData(reportLoc + "index.html");
    	updateReportFileData(reportLoc + "index.html", data.replace("Allure</a>", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    			+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>"));
    	
    	// modify the feedback sections
    	data = getReportFileData(reportLoc + "index.html");
    	updateReportFileData(reportLoc + "index.html", data.replace("<a class=\"feedback_item\" target=\"_blank\" href=\"https://github.com/allure-framework/allure-core/wiki/FAQ-and-Troubleshooting\">FAQ</a>", ""));
    	data = getReportFileData(reportLoc + "index.html");
    	updateReportFileData(reportLoc + "index.html", data.replace("<a class=\"feedback_item\" target=\"_blank\" href=\"https://github.com/allure-framework/allure-core/issues/new\">GitHub</a>", ""));
    	data = getReportFileData(reportLoc + "index.html");
    	updateReportFileData(reportLoc + "index.html", data.replace("mailto:allure@yandex-team.ru", "mailto:mqe_feedback@viacom.com?Subject=MQE%20Report%20Feedback/Contact%20Request"));
    	
    	// update the report title
    	data = getReportFileData(reportLoc + "index.html");
    	updateReportFileData(reportLoc + "index.html", data.replace("<title>Allure Dashboard</title>", "<title>MQE Dashboard</title>"));
    	
    	// update the report name
    	data = getReportFileData(reportLoc + "data" + File.separator + "report.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "report.json", data.replace("Allure Test Pack", 
    			"Multi-Platform Quality Engineering"));
    	
    	// update the defect names
    	data = getReportFileData(reportLoc + "data" + File.separator + "defects.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "defects.json", data.replace("Product defects", 
    			"Defects"));
    	data = getReportFileData(reportLoc + "data" + File.separator + "defects.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "defects.json", data.replace("Test defects", 
    			"Possible defects (debug)"));
    	
    	// update the timeline
    	data = getReportFileData(reportLoc + "plugins" + File.separator + "timeline" + File.separator + "en.json");
    	updateReportFileData(reportLoc + "plugins" + File.separator + "timeline" + File.separator + "en.json", data.replace("\"HEADER\": \"Timeline\"", 
    			"\"HEADER\": \"Timeline By Parallel Test Thread\""));
    	
    	// remove the template widget safety
    	data = getReportFileData(reportLoc + "templates" + File.separator + "overview.html");
    	updateReportFileData(reportLoc + "templates" + File.separator + "overview.html", data.replace("<div ng-switch-default>Type \"{{widget.type}}\" not "
    			+ "supported for widgets.</div>", ""));
    	
    	// update the widget names
    	data = getReportFileData(reportLoc + "data" + File.separator + "widgets.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "widgets.json", data.replace("\"name\" : \"total\"", "\"name\" : \"total tests\""));
    	data = getReportFileData(reportLoc + "data" + File.separator + "widgets.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "widgets.json", data.replace("\"name\" : \"xunit\"", "\"name\" : \"tests by suite\""));
    	data = getReportFileData(reportLoc + "data" + File.separator + "widgets.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "widgets.json", data.replace("\"name\" : \"behaviors\"", "\"name\" : \"features\""));
    	data = getReportFileData(reportLoc + "data" + File.separator + "widgets.json");
    	updateReportFileData(reportLoc + "data" + File.separator + "widgets.json", data.replace("\"name\" : \"defects\"", "\"name\" : \"defect screenshots\""));
    	
    	// update the sidebar names
    	data = getReportFileData(reportLoc + "plugins" + File.separator + "xunit" + File.separator + "en.json");
    	updateReportFileData(reportLoc + "plugins" + File.separator + "xunit" + File.separator + "en.json", data.replace("xUnit", "Tests"));
    	data = getReportFileData(reportLoc + "plugins" + File.separator + "behaviors" + File.separator + "en.json");
    	updateReportFileData(reportLoc + "plugins" + File.separator + "behaviors" + File.separator + "en.json", data.replace("Behaviors", "Features"));
    	
    	// update the images
    	File noScreenshotImg = new File(reportLoc + "img" + File.separator + "NoScreenshot.PNG");
    	try {
			FileUtils.copyURLToFile(getClass().getResource("/allure-logo.png"), new File(reportLoc + "img" + File.separator + "allure-logo.png"));
			FileUtils.copyURLToFile(getClass().getResource("/favicon.ico"), new File(reportLoc + "img" + File.separator + "favicon.ico"));
			FileUtils.copyURLToFile(getClass().getResource("/tests_passed.jpg"), new File(reportLoc + "img" + File.separator + "tests_passed.jpg"));
			FileUtils.copyURLToFile(getClass().getResource("/" + noScreenshotImg.getName()), noScreenshotImg);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	// build the defects widget with failed screenshots on hover
    	data = getReportFileData(reportLoc + "css" + File.separator + "app.css");
    	data = data + ".screenshot-onhover{width:100px;height: 100px;}.screenshot-onhover:hover{width: 150px;height: 150px;}";
    	updateReportFileData(reportLoc + "css" + File.separator + "app.css", data);
    	String defectHtml = "<div>";
    	
    	// get a list of features with failures
    	List<String> failedData = this.getFailedScreenshotData();
    	if (failedData.size() > 0) {
    		List<String> failedFeatures = new ArrayList<String>();
        	for (String failure : failedData) {
        		String failedFeature = failure.split(FAILURE_SPLITTER)[2];
        		if (!failedFeatures.contains(failedFeature)) {
        			failedFeatures.add(failedFeature);
        		}
        	}
        	
        	for (int featureIter = 0; featureIter < failedFeatures.size(); featureIter++) {
        		defectHtml = defectHtml + "<h4>" + failedFeatures.get(featureIter) + "</h4>";
        		List<String> testDataByFeature = new ArrayList<String>();
        		for (String failure : failedData) {
        			String testFailedFeature = failure.split(FAILURE_SPLITTER)[2];
        			
        			if (testFailedFeature.equals(failedFeatures.get(featureIter))) {
        				testDataByFeature.add(failure.split(FAILURE_SPLITTER)[0] + FAILURE_SPLITTER + failure.split(FAILURE_SPLITTER)[1] + FAILURE_SPLITTER + failure.split(FAILURE_SPLITTER)[3]);
        			}
        		}
        		
        		java.util.Collections.sort(testDataByFeature);
        		Integer i = 1;
        		for (int entryIter = 0; entryIter < testDataByFeature.size(); entryIter++) {
        			String entry = testDataByFeature.get(entryIter);
        			
        			List<String> subEntries = new ArrayList<String>();
        			for (int subEntryIter = 0; subEntryIter < testDataByFeature.size(); subEntryIter++) {
        				String subEntry = testDataByFeature.get(subEntryIter);
        				if (entry.split(FAILURE_SPLITTER)[0].equals(subEntry.split(FAILURE_SPLITTER)[0])) {
        					subEntries.add(subEntry);
        				}
        			}
        			
                    if (!defectHtml.contains(entry.split(FAILURE_SPLITTER)[0])) {
                    	if (i != 1) {
                    		defectHtml = defectHtml + "<br />";
                    	}
                    	defectHtml = defectHtml + i + ". " + entry.split(FAILURE_SPLITTER)[0] + "<br />";
                	    i++;
            			for (int subEntryConsIter = 0; subEntryConsIter < subEntries.size(); subEntryConsIter++) {
            				if (subEntryConsIter < 3) {
            					String screenshot = "data/" + subEntries.get(subEntryConsIter).split(FAILURE_SPLITTER)[1];
            					File screenshotFile = new File(reportLoc + screenshot);
            					if (screenshotFile.exists()) {
            						if (screenshotFile.length() <= MIN_SCREENSHOT_BYTES) {
            							screenshot = "img/" + noScreenshotImg.getName();
            						}
            					} else if (!screenshotFile.exists() || screenshot.toLowerCase().contains("no screenshot")) {
            						screenshot = "img/" + noScreenshotImg.getName();
            					}
            					
            					defectHtml = defectHtml + "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"" + subEntries.get(subEntryConsIter)
            							.split(FAILURE_SPLITTER)[2] + "\"><img class=\"screenshot-onhover\" src=\"" + screenshot + "\"></a>";
            				}
            			}
        			}
        		}
        	}
    	} else {
    		defectHtml = defectHtml + "There are no defects/associated screenshots!";
    	}
    	
    	defectHtml = defectHtml + "</div>";
    	updateReportFileData(reportLoc + "templates" + File.separator + "overview" + File.separator + "defects.html", defectHtml);
    	
    	// order the webparts in a desired order
    	data = getReportFileData(reportLoc + "data" + File.separator + "widgets.json");
    	String splitter = "\"name\"";
    	String[] splitData = data.split(splitter);
    	String statistics = splitData[0];
    	String totalTests = splitData[1];
    	String defectsScreenshots = splitData[2];
    	String testsBySuite = splitData[3];
    	String features = splitData[4];
    	String environment = splitData[5];
    	data = statistics + splitter + totalTests + splitter + testsBySuite + splitter + features + splitter + defectsScreenshots + splitter + environment;
    	updateReportFileData(reportLoc + "data" + File.separator + "widgets.json", data);
    }
    
    private void uploadReportDataToSplunk() {
    	Logger.logConsoleMessage("Logging test run execution data to splunk.");
    	// remove the unecessary 'step' data
    	try {
    		Document document = getReportDocument();
    		XPathExpression stepExpression = getXPathExpression("//steps");
            NodeList stepNodes = (NodeList) stepExpression.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < stepNodes.getLength(); i++) {
                Node stepNode = stepNodes.item(i);
                Node parentNode = stepNode.getParentNode();
                parentNode.removeChild(stepNode);
            }
    		
    		rebuildXMLFile(document);
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to remove the step data from the allure report.");
    		e.printStackTrace();
    	}
    	
    	// remove whitespace and convert to json
    	File reportJSONFile = null;
        try {
        	Document document = getReportDocument();
        	BufferedReader reader = new BufferedReader(new StringReader(XMLUtils.DocumentToString(document)));
            StringBuffer result = new StringBuffer();
            String line;
            while ( (line = reader.readLine() ) != null) {
                result.append(line.trim());
            }
            org.json.JSONObject jsonXMLObj = XML.toJSONObject(result.toString());
            		
            // create the json report file
            reportJSONFile = new File(getReportXMLFile().getAbsoluteFile()
            		.toString().replace(".xml", ".json"));
            reportJSONFile.createNewFile();
            
            FileOutputStream fileOutputStream = new FileOutputStream(reportJSONFile, false);
            fileOutputStream.write(jsonXMLObj.toString().getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
        	Logger.logConsoleMessage("Failed to convert to xml and trim file.");
        	e.printStackTrace();
        }
        
        // post the data to splunk globaltestdata index
        if (GridManager.isQALabHub() && reportJSONFile != null) {
        	InputStream inputStream = null;
        	String input = null;
        	try {
        		inputStream = new FileInputStream(reportJSONFile.getAbsolutePath());
        		input = IOUtils.toString(new InputStreamReader(inputStream));
        	} catch (Exception e) {
        		Logger.logConsoleMessage("Failed to get json data from test file.");
        		e.printStackTrace();
        	} finally {
        		try {
    				inputStream.close();
    			} catch (IOException e) {
    				Logger.logConsoleMessage("Failed to close the input stream.");
    				e.printStackTrace();
    			}
        	}
        	
        	if (input != null) {
        		String splunkIndex = Constants.SPLUNK_LAB_GLOBAL_INDEX;
//        		new SplunkManager().connectToSplunk().setIndex(splunkIndex).postEvent(input);
        	}
        }
    }
    
    private String getReportFileData(String filePath) {
    	InputStream inputStream = null;
    	String input = null;
    	try {
    		inputStream = new FileInputStream(filePath);
    		input = IOUtils.toString(new InputStreamReader(inputStream));
    	} catch (FileNotFoundException e) {
    		Logger.logConsoleMessage("Failed to find report file path at '" + filePath + "'.");
    		e.printStackTrace();
    	} catch (IOException e) {
    		Logger.logConsoleMessage("Failed to get report file data at '" + filePath + "'.");
    		e.printStackTrace();
    	}
    	
    	return input;
    }
    
    private JSONObject getReportFileDataJson(String filePath) {
    	InputStream inputStream = null;
    	String input = null;
    	try {
    		inputStream = new FileInputStream(filePath);
    		input = IOUtils.toString(new InputStreamReader(inputStream));
    	} catch (FileNotFoundException e) {
    		Logger.logConsoleMessage("Failed to find report file path at '" + filePath + "'.");
    		e.printStackTrace();
    	} catch (IOException e) {
    		Logger.logConsoleMessage("Failed to get report file data at '" + filePath + "'.");
    		e.printStackTrace();
    	}
    	
    	JSONParser parser = new JSONParser();
        JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(input);
		} catch (ParseException e) {
			Logger.logConsoleMessage("Failed to parse report json.");
			e.printStackTrace();
		}
    	return json;
    }
    
    private void updateReportFileData(String filePath, String jsonString) {
    	// rewrite the json object in the allure report file
    	try {
    		FileWriter file = new FileWriter(filePath);
    		file.write(jsonString);
    		file.close();
    	} catch (Exception e) {
    		Logger.logConsoleMessage("Failed to update the test report.");
    		e.printStackTrace();
    	}
    }
}
