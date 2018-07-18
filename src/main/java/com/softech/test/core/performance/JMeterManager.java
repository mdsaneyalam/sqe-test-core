package com.softech.test.core.performance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer.TransferState;
import com.softech.test.core.lab.BrowserNodeManager;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.EC2Manager;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.lab.LabDeviceManager;
import com.softech.test.core.props.DesktopOSType;
import com.softech.test.core.proxy.ProxyManager;
import com.softech.test.core.report.FileZipper;
//import com.softech.test.core.report.SplunkManager;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.SleepUtils;
import com.amazonaws.services.s3.transfer.TransferManager;

import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.proxy.CaptureType;

public class JMeterManager {

	// TODO - right now this is not thread safe - but it should be... Replace static
	// variables with ThreadLocal instances
	private static String testLocation;
	private static String reportDirLocation;
	private static String reportDashboardDirLocation;
	private static String logLocation;
	private static File currentHarLog = null;
	private static List<File> harLogFiles = new ArrayList<File>();
	private static List<String> summaryEntries = new ArrayList<String>();
	private static List<String> iterativeSummaryEntries = new ArrayList<String>();

	private static Integer duration = 0;
	private static Integer trueDuration = 0;

	private static String s3ReportDir;
	private static String s3ReportUrl;
	private static HashMap<String, String> reportUrls = new HashMap<String, String>();

	private static Boolean runCompleted = false;
	private static Integer finalThreadCount = null;
	private static Boolean runOnAgents = null;
	private static Boolean isWebdriver = false;

	private static BrowserMobProxyServer proxyServer = null;

	private static final Integer CALC_WAIT_SEC = 30;
	private static final Integer CALC_WAIT_MS = 30000;
	private static final String JENKINS_USERNAME = System.getenv("JENKINS_AGENT_LINUX_USERNAME") + "@";
	private static final String JMETER_HOME = System.getenv("JENKINS_JMETER_HOME");
	private static final String JMETER_BIN_LOC = JMETER_HOME + File.separator + "jmeter";
	private static final String JMETER_SERVER_BIN_LOC = JMETER_HOME + File.separator + "jmeter-server";
	private static final String JMETER_ASSOCIATED_LOC = System.getenv("JENKINS_JMETER_ASSOCIATED_LOCATION");
	private static final String JMETER_S3_BUCKET = System.getenv("JENKINS_JMETER_S3_BUCKET");

	private static final String JMETER_TEST_FILE = "JMETER_TEST_FILE";
	private static final String JMETER_TEST_FILE_NAME = System.getProperty("system.test.jmetertestfile");
	private static final String ASSOCIATED_FILE_1 = "ASSOCIATED_FILE_1";
	private static final String ASSOCIATED_FILE_1_NAME = System.getProperty("system.test.associatedfile1");
	private static final String ASSOCIATED_FILE_2 = "ASSOCIATED_FILE_2";
	private static final String ASSOCIATED_FILE_2_NAME = System.getProperty("system.test.associatedfile2");
	private static final String ASSOCIATED_FILE_3 = "ASSOCIATED_FILE_3";
	private static final String ASSOCIATED_FILE_3_NAME = System.getProperty("system.test.associatedfile3");

	public static void executeJMeterTest() {
		initTest();

		if (testLocation == null) {
			throw new RuntimeException("Test location is not set.");
		}

		if (reportDirLocation == null) {
			throw new RuntimeException("Report directory not set.");
		}

		if (!jMeterTestExists()) {
			throw new RuntimeException(
					"JMeter .jmx test file does not exist at '" + testLocation + "'. Did you set your test location?");
		}

		// run the test
		runJMetertest();
	}

	private static Boolean jMeterTestExists() {
		Boolean exists = getJMeterXMLFile().exists();
		Logger.logConsoleMessage("Does JMeter test exist at specified location: " + exists.toString());
		return exists;
	}

	private static void updateAssociatedFilePath(File associatedFile) {
		try {
			updateJMeterItem("//*[@name='filename'][contains(text(), '" + associatedFile.getName() + "')]",
					JMETER_ASSOCIATED_LOC + File.separator + associatedFile.getName());
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to update the associated file path for '" + associatedFile.getName()
					+ "'. Either the file does not exist, or the "
					+ "file path is set inside a jmeter script. You can manually set the script file path to '"
					+ JMETER_ASSOCIATED_LOC + "/" + associatedFile.getName() + "' as a workaround.");
		}
	}

	private static void runJMetertest() {
		// get the remote agent machine list if running on the lab
		List<String> allActiveAgents = null;
		if (runOnAgents) {
			allActiveAgents = EC2Manager.getAllEC2MachineAddresses();
		}

		// clean up any legacy jmeter instances and agent runners, and restart
		if (GridManager.isQALabHub()) {
			CommandExecutor.execCommand("bash " + JMETER_HOME + File.separator + "shutdown.sh", null, null);
			if (runOnAgents) {
				for (String machine : allActiveAgents) {
					CommandExecutor.execCommand("'nohup fuser -k 1099/tcp &>/dev/null &'", JENKINS_USERNAME + machine,
							null);
					CommandExecutor.execCommand("nohup bash " + JMETER_SERVER_BIN_LOC + " &>/dev/null &",
							JENKINS_USERNAME + machine, null);
				}
			}
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// calculate the desired thread count and determine if the run should be local
		// or across all agents
		if (hasJMeterItem("//*[contains(@testclass, 'SteppingThreadGroup')]")
				|| hasJMeterItem("//*[contains(@testclass, 'UltimateThreadGroup')]")) {
			Logger.logConsoleMessage(
					"Test does not leverage the standard Thread Group plugin. Not setting a static thread entry as "
							+ "the thread volume will increase/decrease during the execution.");
			finalThreadCount = null;
		} else {
			Integer threadCount = Integer.parseInt(getJMeterItem("//ThreadGroup//*[@name='ThreadGroup.num_threads']"));
			finalThreadCount = threadCount;
			if (runOnAgents) {
				finalThreadCount = (threadCount * allActiveAgents.size());
				Logger.logConsoleMessage(
						"Thread Count set to '" + finalThreadCount + "' to match scaled JMeter agent availability.");
			}
		}

		// check if the test is a webdriver test or not
		if (hasJMeterItem("//*[contains(@testclass, 'RemoteDriverConfig')]")) {
			isWebdriver = true;
			initWebDriverServers(true, true);
		}

		// generate the remote machine list
		String remoteMachines = "";
		String remoteStart = "";
		String remoteExit = "";
		if (runOnAgents) {
			for (String activeAgent : allActiveAgents) {
				remoteMachines = remoteMachines + "," + activeAgent;
			}
			remoteStart = " --remotestart " + remoteMachines.replaceFirst(",", "") + " ";
			remoteExit = " --remoteexit";
		}

		// start up a proxy to route the traffic through
		Integer proxyPort = null;
		try {
			ServerSocket serverSocket = new ServerSocket(0);
			proxyPort = serverSocket.getLocalPort();
			serverSocket.close();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to generate proxy ports/tunnel identifiers.");
			e.printStackTrace();
		}

		if (!isWebdriver) {
			ProxyManager.setProxyServer(proxyPort);
			ProxyManager.startProxyServer();
			proxyServer = ProxyManager.getProxyServer();
			updateMQEProxySettings(proxyPort);
		}

		// run the test
		String reportLocation = reportDirLocation + File.separator + "results.jtl";
		logLocation = reportDirLocation + File.separator + "jmeter.log";
		String command = JMETER_BIN_LOC + " -j " + logLocation
				+ " -n -J jmeter.save.saveservice.output_format=csv,jmeter.save.saveservice.response_data=true,"
				+ "jmeter.save.saveservice.samplerData=true,jmeter.save.saveservice.requestHeaders=true,jmeter.save.saveservice.url=true,jmeter.save.saveservice.responseHeaders=true,"
				+ "summariser.name=summary,summariser.interval=12,summariser.out=true,summariser.log=true -t "
				+ testLocation + " -l " + reportLocation + remoteStart + remoteExit;
		List<Thread> jmeterThreads = new ArrayList<Thread>();
		jmeterThreads.add(new Thread() {
			public void run() {
				Logger.logConsoleMessage("Running JMeter test with constructed command '" + command + "'");
				try {
					Logger.logConsoleMessage(CommandExecutor.execMultiCommand(command,
							(Integer.parseInt(System.getProperty("system.test.maxtesttimeout")) + CALC_WAIT_SEC)));
					runCompleted = true;
				} catch (IllegalThreadStateException e) {
					Logger.logConsoleMessage("An exception occurred during jmeter execution. Terminating test.");
					runCompleted = true;
				}
			}
		});

		jmeterThreads.add(new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				Boolean runDurLimitExceeded = false;
				if ((trueDuration / CALC_WAIT_SEC) >= (duration + CALC_WAIT_SEC)) {
					runDurLimitExceeded = true;
					Logger.logConsoleMessage(
							"The true runtime duration of the test has exceeded the maximum duration.");
				}

				while (!runCompleted && !runDurLimitExceeded) {
					Logger.logConsoleMessage("======== NEW LOG ENTRY ========");
					try {
						if (!isWebdriver) {
							currentHarLog = new File(reportDirLocation + File.separator + "JMeterPerformance-"
									+ (harLogFiles.size() + 1) + ".har");
							proxyServer.setHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES,
									CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_COOKIES,
									CaptureType.RESPONSE_HEADERS);
							proxyServer.getHar().writeTo(currentHarLog);

							Integer proxyCount = proxyServer.getHar().getLog().getEntries().size();

							if (proxyCount > 0) {
								Logger.logConsoleMessage(
										"A detailed har log of all current requests/responses is available.");
								uploadHarToS3();
							}

							if (currentHarLog.length() >= 10485760) {
								Logger.logConsoleMessage(
										"Har Log size is greater than 10 Megs, archiving the har data and cleaning the proxy logs.");
								// archive the har file and clear the proxy log
								harLogFiles.add(currentHarLog);
								proxyServer.newHar();
							}
						}
					} catch (Exception e) {
						Logger.logConsoleMessage("Failed to handle har files during test iteration!");
					}

					// get the local entries/summary data
					Boolean cumulativeEntryFound = false;
					BufferedReader logBufferedReader = null;
					try {
						File logFile = new File(logLocation);
						if (logFile.exists()) {
							logBufferedReader = new BufferedReader(new FileReader(logFile));

							String line = null;
							Boolean entryFound = false;
							while ((line = logBufferedReader.readLine()) != null) {
								if (line.contains("jmeter.reporters.Summariser: summary +")) {
									if (!iterativeSummaryEntries.contains(line)) {
										entryFound = true;
										Logger.logConsoleMessage("ITERATIVE SUMMARY ENTRY: " + line);
										String startedThreads = line.split("Started: ")[1].split(" Finished")[0];
										String finishedThreads = line.split("Finished: ")[1].split(" ")[0];
										finalThreadCount = Integer.parseInt(startedThreads)
												- Integer.parseInt(finishedThreads);
										iterativeSummaryEntries.add(line);
									}
								}

								if (line.contains("jmeter.reporters.Summariser: summary =")) {
									if (!summaryEntries.contains(line)) {
										entryFound = true;
										cumulativeEntryFound = true;
										// new entry
										summaryEntries.add(line);
										Logger.logConsoleMessage("CUMULATIVE SUMMARY ENTRY: " + line);
										if (GridManager.isQALabHub()) {
											// post the summary data to splunk
											JSONObject jsonToPost = new JSONObject();
											jsonToPost.put("dateTime", line.split(" INFO")[0]);
											line = line.replaceAll("\\s+", "");
											jsonToPost.put("currentUsers", finalThreadCount);
											jsonToPost.put("totalRequests", line.split("summary=")[1].split("in")[0]);
											jsonToPost.put("runDuration", line.split("in")[1].split("=")[0]);
											jsonToPost.put("requestsPerSecond",
													line.split("in")[1].split("=")[1].split("/sAvg:")[0]);
											jsonToPost.put("avgResponseTime", line.split("/sAvg:")[1].split("Min:")[0]);
											jsonToPost.put("errorCount", line.split("Err:")[1].split("\\(")[0]);
											postToSplunk(jsonToPost);
										}
									}
								}
							}

							if (!entryFound) {
								Logger.logConsoleMessage(
										"No iterative/culumative summaries were reported by JMeter on this iteration. Depending on the run "
												+ "type, server configuration, etc - it can take a few minutes for JMeter to report data back. Continuing the test...");
							}
						}
					} catch (Exception e) {
						Logger.logConsoleMessage("Failed to retrieve summary data during JMeter execution.");
						e.printStackTrace();
					} finally {
						// clean the log
						if (logBufferedReader != null) {
							try {
								logBufferedReader.close();
							} catch (IOException e) {
							}
						}
					}

					// calculate the local entries ourselves
					BufferedReader logCalcBufferedReader = null;
					List<String> entryLines = new ArrayList<String>();
					try {
						File jtlFile = new File(reportLocation);
						if (jtlFile.exists()) {
							Integer totalRequests = 0;
							Integer currentUsers = finalThreadCount;
							long runDuration = 0;
							long requestsPerSecond = 0;
							long avgResponseTime = 0;
							Integer medianResponseTime = 0;
							Integer errorCount = 0;

							logCalcBufferedReader = new BufferedReader(new FileReader(jtlFile));
							String line = null;
							while ((line = logCalcBufferedReader.readLine()) != null) {
								if (line.contains(",Thread Group")) {
									entryLines.add(line);
								}
							}
							totalRequests = entryLines.size();

							List<String> errorEntries = new ArrayList<String>();
							for (String entryLine : entryLines) {
								if (!entryLine.contains("200,OK")) {
									errorEntries.add(entryLine.split(",")[3]);
								}
							}
							errorCount = errorEntries.size();

							List<String> timeEntries = new ArrayList<String>();
							for (String entryLine : entryLines) {
								timeEntries.add(entryLine.split(",")[0]);
							}
							String startTime = Collections.min(timeEntries);
							Date startDate = new Date(Long.parseLong(startTime));
							String endTime = Collections.max(timeEntries);
							Date endDate = new Date(Long.parseLong(endTime));
							long timeDiffInMS = endDate.getTime() - startDate.getTime();
							runDuration = timeDiffInMS / 1000;
							requestsPerSecond = totalRequests / runDuration;

							Integer totalRequestTime = 0;
							List<Integer> allResponseTimes = new ArrayList<Integer>();
							for (String entryLine : entryLines) {
								Integer time = Integer.parseInt(entryLine.split(",")[1]);
								allResponseTimes.add(time);
								totalRequestTime = totalRequestTime + time;
							}
							avgResponseTime = totalRequestTime / totalRequests;

							Collections.sort(allResponseTimes);
							Integer medianListSize = allResponseTimes.size();
							int middle = medianListSize / 2;
							if (medianListSize % 2 == 1) {
								medianResponseTime = allResponseTimes.get(middle);
							} else {
								medianResponseTime = (allResponseTimes.get(middle - 1) + allResponseTimes.get(middle))
										/ 2;
							}

							if (totalRequests != 0 && requestsPerSecond != 0) {
								Logger.logConsoleMessage("CALCULATED SUMMARY ENTRY: " + "current users=" + currentUsers
										+ ", total requests=" + totalRequests + ", true run duration=" + trueDuration
										+ ", calculated run duration=" + runDuration + ", requests per second="
										+ requestsPerSecond + ", average response time=" + avgResponseTime
										+ ",error count=" + errorCount);
								if (GridManager.isQALabHub() && !cumulativeEntryFound) {
									// post the summary data to splunk
									JSONObject jsonToPost = new JSONObject();
									jsonToPost.put("currentUsers", currentUsers);
									jsonToPost.put("totalRequests", totalRequests);
									jsonToPost.put("runDuration", trueDuration);
									jsonToPost.put("requestsPerSecond", requestsPerSecond);
									jsonToPost.put("avgResponseTime", avgResponseTime);
									jsonToPost.put("medianResponseTime", medianResponseTime);
									jsonToPost.put("errorCount", errorCount);
									postToSplunk(jsonToPost);
								}
							}
						}
					} catch (Exception e) {
						// ignore for now as a bad single parse is acceptable
					} finally {
						// clean the log
						if (logCalcBufferedReader != null) {
							try {
								logCalcBufferedReader.close();
							} catch (IOException e) {
							}
						}
					}

					trueDuration = (trueDuration + CALC_WAIT_SEC);
					try {
						Thread.sleep(CALC_WAIT_MS);
					} catch (InterruptedException e) {
					}
				}
			}
		});

		for (Thread thread : jmeterThreads) {
			thread.start();
		}
		for (Thread thread : jmeterThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// terminate the jmeter servers
		if (GridManager.isQALabHub() && runOnAgents) {
			for (String activeAgent : allActiveAgents) {
				Logger.logConsoleMessage("Stopping JMeter agent server '" + activeAgent + "'.");
				CommandExecutor.execCommand("'nohup fuser -k 1099/tcp &>/dev/null &'", JENKINS_USERNAME + activeAgent,
						null);
			}
		}

		// terminate webdriver servers
		if (isWebdriver) {
			initWebDriverServers(false, true);
		}

		// generate the dashboard report
		Logger.logConsoleMessage("Generating dashboard report from result file at '" + reportLocation
				+ "' to reporting dashboard directory at '" + reportDashboardDirLocation + ".");

		// wait for the index file to be generated
		File indexFile = new File(reportDashboardDirLocation + "/index.html");
		Integer waitIter = 0;
		while (!indexFile.exists()) {
			if (waitIter == 30) {
				throw new RuntimeException("Report dashboard index not present at '" + indexFile.getAbsolutePath()
						+ "' after 30 seconds.");
			}

			// remove the last line of the file to avoid dashboard compilation issues
			try {
				RandomAccessFile randomAccessFile = new RandomAccessFile(reportLocation, "rw");
				byte b;
				long length = randomAccessFile.length();
				if (length != 0) {
					do {
						length -= 1;
						randomAccessFile.seek(length);
						b = randomAccessFile.readByte();
					} while (b != 10 && length > 0);
					randomAccessFile.setLength(length);
					randomAccessFile.close();
				}
			} catch (Exception e) {

			}

			CommandExecutor.execMultiCommand(JMETER_BIN_LOC + " -g " + reportLocation + " -o "
					+ reportDashboardDirLocation + " -j " + logLocation, null);

			waitIter++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		// modify the report output for viacom
		modifyReportForViacom();

		// upload the reports to S3
		uploadReportsToS3();
	}

	public static HashMap<String, String> getReportUrls() {
		return reportUrls;
	}

	// TODO - some redundant code here between this and the allure manager when
	// uploading files to S3. Conslidate and clean up.
	private static HashMap<String, String> uploadReportsToS3() {
		if (GridManager.isQALabHub()) {
			AmazonS3 amazon = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());

			try {
				TransferManager transferManager = new TransferManager(amazon);
				MultipleFileUpload upload = transferManager.uploadDirectory("mqetestreports/" + JMETER_S3_BUCKET,
						s3ReportDir, new File(reportDashboardDirLocation), true);
				upload.waitForCompletion();
				if (upload.getState().equals(TransferState.Completed)) {
					s3ReportUrl = getS3ReportUrlBase() + "index.html";
					Logger.logConsoleMessage(
							"Successfully uploaded test report to Amazon S3 at '" + s3ReportUrl + "'.");
				}
			} catch (AmazonServiceException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
				Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
			} catch (AmazonClientException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (s3ReportUrl.isEmpty()) {
				Logger.logConsoleMessage("Failed to upload jmeter dashboard to Amazon S3.");
			} else {
				reportUrls.put("dashboardUrl", s3ReportUrl);
			}

			String s3HarFileUrl = uploadHarToS3();
			reportUrls.put("harLogUrl", s3HarFileUrl);

			String s3LogFileUrl = null;
			File logFile = new File(logLocation);
			try {
				amazon.putObject(new PutObjectRequest("mqetestreports/" + JMETER_S3_BUCKET + "/" + s3ReportDir,
						logFile.getName(), logFile));
				s3LogFileUrl = getS3ReportUrlBase() + logFile.getName();
				Logger.logConsoleMessage(
						"Successfully uploaded jmeter log to report directory on Amazon S3 at '" + s3LogFileUrl + "'.");
			} catch (AmazonServiceException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
				Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
			} catch (AmazonClientException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
			}

			if (s3LogFileUrl == null) {
				Logger.logConsoleMessage("Failed to upload log file to Amazon S3.");
			}
			reportUrls.put("logUrl", s3LogFileUrl);
		
			// zip up everything!!!
			String s3ZipFileUrl = null;
			File zipFile = null;
			try {
				zipFile = new File(FileZipper.zipDirectory(GlobalReportDir.getReportDir()));
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to zip up report files!");
				e.printStackTrace();
			}
			
			if (zipFile != null && zipFile.exists()) {
				try {
					amazon.putObject(new PutObjectRequest("mqetestreports/" + JMETER_S3_BUCKET + "/" + s3ReportDir,
							zipFile.getName(), zipFile));
					s3ZipFileUrl = getS3ReportUrlBase() + zipFile.getName();
					Logger.logConsoleMessage(
							"Successfully uploaded zip file of all artifacts to report directory on Amazon S3 at '" + s3ZipFileUrl + "'.");
				} catch (AmazonServiceException e) {
					Logger.logConsoleMessage("Error: " + e.getMessage());
					Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
				} catch (AmazonClientException e) {
					Logger.logConsoleMessage("Error: " + e.getMessage());
				}
				
				if (s3ZipFileUrl == null) {
					Logger.logConsoleMessage("Failed to upload zip file to Amazon S3.");
				}
				reportUrls.put("zipUrl", s3ZipFileUrl);
			}
		}
		
		return reportUrls;
	}

	private static String uploadHarToS3() {
		if (isWebdriver) {
			return "webdriver";
		}

		String s3HarFileUrl = null;
		if (GridManager.isQALabHub()) {
			AmazonS3 amazon = new AmazonS3Client(new EnvironmentVariableCredentialsProvider());

			try {
				s3ReportUrl = getS3ReportUrlBase();
				amazon.putObject(new PutObjectRequest("mqetestreports/" + JMETER_S3_BUCKET + "/" + s3ReportDir,
						"harlog.har", currentHarLog));
				s3HarFileUrl = s3ReportUrl + "harlog.har";
				Logger.logConsoleMessage("Successfully uploaded jmeter har log to report directory on Amazon S3 at '"
						+ s3HarFileUrl + "'.");

			} catch (AmazonServiceException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
				Logger.logConsoleMessage("Status Code: " + e.getStatusCode());
			} catch (AmazonClientException e) {
				Logger.logConsoleMessage("Error: " + e.getMessage());
			}
		}
		return s3HarFileUrl;
	}

	private static void setS3ReportDir() {
		s3ReportDir = RandomData.getCharacterString(20);
		SimpleDateFormat dirFormat = new SimpleDateFormat("MMddyyhhmmssSSSa");
		s3ReportDir = s3ReportDir + dirFormat.format(new Date());
	}

	private static String getS3ReportUrlBase() {
		return "https://s3.amazonaws.com/mqetestreports/" + JMETER_S3_BUCKET + "/" + s3ReportDir + "/";
	}

	private static File getJMeterXMLFile() {
		return new File(testLocation);
	}

	private static Document getJMeterDocument() throws Exception {
		File reportFile = getJMeterXMLFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		return dbf.newDocumentBuilder().parse(reportFile);
	}

	private static XPathExpression getXPathExpression(String xpathQuery) throws Exception {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		return xpath.compile(xpathQuery);
	}

	private static void rebuildJMeterXML(Document inDocument) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.transform(new DOMSource(inDocument), new StreamResult(getJMeterXMLFile()));
	}

	private static void modifyReportForViacom() {
		// TODO - most of this is data that should come from a resource file. Refactor
		// as time allows.

		// get the generated report location
		String reportLoc = reportDashboardDirLocation;
		String data = null;

		// wait for the index file to be generated
		File indexFile = new File(reportLoc + "/index.html");
		Integer waitIter = 0;
		while (!indexFile.exists()) {
			if (waitIter == 30) {
				throw new RuntimeException("Report dashboard index not present at '" + indexFile.getAbsolutePath()
						+ "' after 30 seconds.");
			}
			waitIter++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		// update the dashboard header
		data = getReportFileData(indexFile.getAbsolutePath());
		updateReportFileData(indexFile.getAbsolutePath(),
				data.replace("Apache JMeter Dashboard", "MQE JMeter Dashboard"));

		// update the images
		try {
			FileUtils.copyURLToFile(JMeterManager.class.getResource("/favicon.ico"),
					new File(reportLoc + "/content/pages/icon-apache.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getReportFileData(String filePath) {
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

	private static void updateReportFileData(String filePath, String input) {
		// rewrite the json object in the allure report file
		try {
			FileWriter file = new FileWriter(filePath);
			file.write(input);
			file.close();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to update the test report.");
			e.printStackTrace();
		}
	}

	private static String getJMeterItem(String query) {
		String txtResult = null;
		try {
			Document document = getJMeterDocument();
			XPathExpression expression = getXPathExpression(query);
			NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			txtResult = results.item(0).getTextContent();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to get jmeter test item with query '" + query + "'.");
			e.printStackTrace();
		}
		return txtResult;
	}

	private static Boolean hasJMeterItem(String query) {
		try {
			Document document = getJMeterDocument();
			XPathExpression expression = getXPathExpression(query);
			NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			results.item(0).getTextContent();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void updateJMeterItem(String query, String updatedValue) throws Exception {
		Document document = getJMeterDocument();
		XPathExpression expression = getXPathExpression(query);
		NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
		results.item(0).setTextContent(updatedValue);
		rebuildJMeterXML(document);
	}

	private static void updateAllJMeterItems(String query, String updatedValue) {
		try {
			Document document = getJMeterDocument();
			XPathExpression expression = getXPathExpression(query);
			NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
			for (int i = 0; i < results.getLength(); i++) {
				Node node = results.item(i);
				node.setTextContent(updatedValue);
			}
			rebuildJMeterXML(document);
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to update the JMeter test items with query '" + query + "' to new value '"
					+ updatedValue + "'");
			e.printStackTrace();
		}
	}

	private static void updateMQEProxySettings(Integer proxyPort) {
		Logger.logConsoleMessage("Updating requests to route through BMP proxy.");
		String proxyHost = System.getenv("JENKINS_JMETER_HOST_IP");
		updateAllJMeterItems("//*[contains(@name, 'proxyHost')]", proxyHost);
		updateAllJMeterItems("//*[contains(@name, 'proxyPort')]", proxyPort.toString());

		// get the .jmx content
		String startingContent = null;
		try {
			startingContent = new String(Files.readAllBytes(Paths.get(testLocation)));
		} catch (IOException e) {
			Logger.logConsoleMessage("Failed to get jmx content.");
			e.printStackTrace();
		}

		// check if the doc already contains the proxy port entry
		if (startingContent.contains("proxyHost")) {
			updateAllJMeterItems("//*[contains(@name, 'proxyHost')]", proxyHost);
			updateAllJMeterItems("//*[contains(@name, 'proxyPort')]", proxyPort.toString());
		} else {
			try {
				Document document = getJMeterDocument();
				XPathExpression expression = getXPathExpression("//stringProp[@name='HTTPSampler.domain']");
				NodeList results = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
				for (int i = 0; i < results.getLength(); i++) {
					Node node = results.item(i);

					Text hostText = document.createTextNode(proxyHost);
					Element hostElement = document.createElement("stringProp");
					hostElement.setAttribute("name", "HTTPSampler.proxyHost");
					hostElement.appendChild(hostText);
					node.getParentNode().insertBefore(hostElement, node);

					Text portText = document.createTextNode(proxyPort.toString());
					Element portElement = document.createElement("stringProp");
					portElement.setAttribute("name", "HTTPSampler.proxyPort");
					portElement.appendChild(portText);
					node.getParentNode().insertBefore(portElement, node);
				}
				rebuildJMeterXML(document);
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to update the JMeter proxy/host entries");
				e.printStackTrace();
			}
		}
	}

	private static void postToSplunk(JSONObject jsonToPost) {
		String splunkIndex = Constants.SPLUNK_LAB_JMETER_INDEX;
//		new SplunkManager().connectToSplunk().setIndex(splunkIndex).postEvent(jsonToPost.toJSONString());
	}

	@SuppressWarnings("rawtypes")
	private static void initTest() {
		GlobalReportDir.setReportDir(
				System.getenv("JENKINS_AGENT_TEMP_PATH") + File.separator + RandomData.getCharacterString(25));

		// set the s3 report dir for report uploads
		setS3ReportDir();

		runOnAgents = Boolean.parseBoolean(System.getProperty("system.test.runonagents"));

		// set the report dirs
		reportDirLocation = GlobalReportDir.getReportDir();
		reportDashboardDirLocation = reportDirLocation + File.separator + "dashboard";

		// create the dashboard dir
		try {
			FileUtils.forceMkdir(new File(reportDashboardDirLocation));
		} catch (IOException e) {
			Logger.logConsoleMessage(
					"Failed to create report dashboard directory at '" + reportDashboardDirLocation + "'.");
			e.printStackTrace();
		}

		// set the jmx test location
		String jmeterWorkPath = System.getenv("JENKINS_JMETER_WORK_LOCATION") + File.separator;
		File uploadedJmeterTest = new File(jmeterWorkPath + JMETER_TEST_FILE);
		File readyJmeterTest = new File(jmeterWorkPath + JMETER_TEST_FILE_NAME);
		uploadedJmeterTest.renameTo(readyJmeterTest);

		if (!readyJmeterTest.exists()) {
			throw new RuntimeException("Could not find uploaded JMeter .jmx test at '"
					+ readyJmeterTest.getAbsolutePath() + "'. Did the user upload a JMeter test to run?");
		}

		try {
			FileUtils.copyFileToDirectory(readyJmeterTest, new File(reportDirLocation));
		} catch (IOException e) {
			Logger.logConsoleMessage("Failed to copy jmeter test to report directory");
			e.printStackTrace();
		}
		testLocation = reportDirLocation + File.separator + readyJmeterTest.getName();

		// start the jmeter ec2 agents
		if (runOnAgents) {
			EC2Manager.setJMeterEC2(true);
			EC2Manager.startAllEC2Instances();
			Boolean ec2MachinesOnline = EC2Manager.waitForAllEC2InstancesOnline();
			for (String ec2Agent : EC2Manager.getAllEC2MachineAddresses()) {
				EC2Manager.setEC2MachineOnline(ec2Agent);
				EC2Manager.setEC2MachineOnlineStartTime(ec2Agent, System.currentTimeMillis());
			}
			if (!ec2MachinesOnline) {
				throw new RuntimeException("EC2 Machines did not come online appropriately!");
			}
		}

		// associated file handling
		String associatedDirLoc = JMETER_ASSOCIATED_LOC;
		HashMap<String, String> associatedFiles = new HashMap<String, String>();
		Boolean isZip = false;
		String zipDirName = "";
		if (ASSOCIATED_FILE_1_NAME.contains(".zip")) {
			isZip = true;
			zipDirName = ASSOCIATED_FILE_1_NAME.replace(".zip", "");
			Logger.logConsoleMessage("Associated Files are Zipped Up.");
			CommandExecutor.setEC2CommandHop(false);
			CommandExecutor.execCommand(
					"/usr/bin/unzip " + jmeterWorkPath + ASSOCIATED_FILE_1 + " -d " + jmeterWorkPath, null, 10);
			File[] allUnzippedFiles = new File(jmeterWorkPath + zipDirName).listFiles();
			Integer fileCounter = 1;
			for (File unzippedFile : allUnzippedFiles) {
				associatedFiles.put("ASSOCIATED_FILE_" + fileCounter, unzippedFile.getName());
				fileCounter++;
			}
		} else {
			associatedFiles.put(ASSOCIATED_FILE_1, ASSOCIATED_FILE_1_NAME);
			associatedFiles.put(ASSOCIATED_FILE_2, ASSOCIATED_FILE_2_NAME);
			associatedFiles.put(ASSOCIATED_FILE_3, ASSOCIATED_FILE_3_NAME);
		}

		Iterator iterator = associatedFiles.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			if (pair.getKey().toString().length() > 1) {
				File associatedFile = null;
				if (isZip) {
					associatedFile = new File(
							jmeterWorkPath + zipDirName + File.separator + pair.getValue().toString());
				} else {
					associatedFile = new File(jmeterWorkPath + pair.getKey().toString());
				}

				if (associatedFile.exists()) {
					File readyAssociatedFileJMeter = null;
					if (isZip) {
						readyAssociatedFileJMeter = associatedFile;
					} else {
						readyAssociatedFileJMeter = new File(jmeterWorkPath + pair.getValue().toString());
						associatedFile.renameTo(readyAssociatedFileJMeter);
					}

					try {
						FileUtils.copyFileToDirectory(readyAssociatedFileJMeter, new File(associatedDirLoc));
					} catch (IOException e1) {
						Logger.logConsoleMessage("Failed to copy associated file '" + pair.getKey().toString()
								+ "' with original file name '" + pair.getValue().toString()
								+ "' from JMeter workspace to associated " + "file directory within jmeter.");
						e1.printStackTrace();
					}

					// update the associated file path in the test and copy to the remote machines
					updateAssociatedFilePath(readyAssociatedFileJMeter);
					if (runOnAgents) {
						for (String agentMachine : EC2Manager.getAllEC2MachineAddresses()) {
							Logger.logConsoleMessage("Copying associated file '" + pair.getValue().toString()
									+ "' to remote machine '" + agentMachine + "'.");
							CommandExecutor.setEC2CommandHop(false);
							CommandExecutor.copyFileFromTo(readyAssociatedFileJMeter.getAbsolutePath(),
									associatedDirLoc, JENKINS_USERNAME + agentMachine, null);
						}
					}
				} else {
					Logger.logConsoleMessage("Associated File does not exist: " + associatedFile.getAbsolutePath());
				}
			}

			iterator.remove();
		}

		Logger.logConsoleMessage(
				"Setting test location to '" + testLocation + "' with report directory '" + reportDirLocation + "'.");
	}

	private static void initWebDriverServers(Boolean start, Boolean stop) {
		// stop/start the perf hub
		String wdUser = Constants.MQE_LAB_DB_USER + "@" + Constants.LAB_01_GATEWAY_IP;
		if (stop) {
			Logger.logConsoleMessage("Stopping WebDriver Hub.");
			CommandExecutor.execCommand("open " + Constants.JMETER_STOP_WEB_HUB_PATH, wdUser, null);
			SleepUtils.sleep(1000);
		}

		if (start) {
			Logger.logConsoleMessage("Starting WebDriver Hub.");
			CommandExecutor.execCommand("open " + Constants.JMETER_START_WEB_HUB_PATH, wdUser, null);
		}

		List<String> activeMachines = new ArrayList<String>();
		for (String nodeMachine : BrowserNodeManager.getAllBrowserNodeAddresses(DesktopOSType.MQE_MAC)) {
			if (LabDeviceManager.isMachineNodeActive(nodeMachine)) {
				activeMachines.add(nodeMachine);
			}
		}

		// stop/start the perf nodes
		List<Thread> nodeThreads = new ArrayList<Thread>();
		for (String nodeMachine : activeMachines) {
			nodeThreads.add(new Thread() {
				public void run() {
					if (stop) {
						Logger.logConsoleMessage("Stopping WebDriver node on '" + nodeMachine + "'.");
						CommandExecutor.execCommand("open " + Constants.JMETER_STOP_WEB_NODE_PATH, nodeMachine, null);
						SleepUtils.sleep(1000);
					}

					if (start) {
						Logger.logConsoleMessage("Starting WebDriver node on '" + nodeMachine + "'.");
						CommandExecutor.execCommand("open " + Constants.JMETER_START_WEB_NODE_PATH, nodeMachine, null);
					}
				}
			});
		}
		for (Thread thread : nodeThreads) {
			thread.start();
		}
		for (Thread thread : nodeThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (start) {
			SleepUtils.sleep(Constants.JMETER_NODE_WAIT_MS);
		}
	}
}
