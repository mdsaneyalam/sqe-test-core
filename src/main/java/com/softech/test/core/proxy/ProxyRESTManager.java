package com.softech.test.core.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.util.ExceptionUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.DependencyManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;

public class ProxyRESTManager {

	private static final String PROXY_TTL = "900"; // 15 minutes before a
													// proxy is released
													// automatically
	private static final Integer PROXY_RETRY_COUNT = 3;
	private static final Integer MAX_SERVER_START_ITER = 60;
	private static final Integer DEFAULT_PROXY_SERVER_PORT = 15001;
	private static final String BMP_PROXY_VERSION = "browsermobproxy-2.1.4";
	private static final String BMP_PROXY_USER = System.getenv("BMP_PROXY_USER");

	private static ThreadLocal<Boolean> proxyServerStarted = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};
	private static ThreadLocal<Integer> proxyServerPort = new ThreadLocal<Integer>();
	private static ThreadLocal<String> proxyMachineIP = new ThreadLocal<String>();
	private static ThreadLocal<Integer> proxyInstancePort = new ThreadLocal<Integer>();

	private static ThreadLocal<Boolean> proxyInstanceStarted = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	private static ThreadLocal<Boolean> useEllipticCurve = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return true;
		}
	};

	public static synchronized void setProxyServerInstance(String machineIP, Integer proxyInstPort) {
		proxyServerPort.set(getProxyServerPort());
		proxyMachineIP.set(machineIP);
		proxyInstancePort.set(proxyInstPort);
	}

	public static void startProxyServer() {
		String bmpPath = "";
		if (GridManager.isEC2Agent()) {
			bmpPath = System.getenv("START_BMP_PROXY_PATH"); // mac mini remote
																// server
																// location
			if (proxyMachineIP.get() == null) {
				bmpPath = System.getenv("START_BMP_PROXY_EC2_PATH"); // ec2 bmp
																		// location
			}
		} else {
			// check if bmp lives in the project home dir
			String bmpBinName = OSDetector.isWindows() ? "browsermob-proxy.bat" : "browsermob-proxy";
			String bmpFileName = BMP_PROXY_VERSION + ".zip";
			try {
				DependencyManager.setDependencyLoc(BMP_PROXY_VERSION + "/bin/" + bmpBinName);
				if (!DependencyManager.dependencyExists()) {
					DependencyManager.downloadDependency(bmpFileName);
					DependencyManager.unzipDependency(bmpFileName);
					DependencyManager.setDependencyExec(BMP_PROXY_VERSION);
				}

				bmpPath = DependencyManager.getDependencyLoc();
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to get dependency '" + bmpPath + "'.");
				e.printStackTrace();
			}
		}

		Boolean proxyServerReady = false;
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				String failureMsg = "Proxy server failure startup on server '" + proxyServerPort.get() + "'.";
				Logger.logMessage(failureMsg);
				Logger.logToSysFile(failureMsg);
				throw new RuntimeException("Proxy server startup failure!");
			}

			// if the proxy server isn't started, start it up
			if (!isProxyServerRunning()) {
				String intro = GridManager.isEC2Agent() ? BMP_PROXY_USER : "nohup";
				String outro = GridManager.isEC2Agent() ? ">> " + getBMPServerFilePath() : "& disown";

				String command = intro + " " + bmpPath + " -port " + proxyServerPort.get() + " --ttl " + PROXY_TTL + " "
						+ outro;

				// TODO - better local windows handling
				if (!GridManager.isEC2Agent() && OSDetector.isWindows()) {
					command = "cmd /c " + bmpPath + " -port " + proxyServerPort.get().toString() + " --ttl "
							+ PROXY_TTL;
				}

				Logger.logConsoleMessage(
						"Starting proxy server on '" + hostLoc() + "' on port '" + proxyServerPort.get() + "'.");
				commandHandler(command);

				// wait for the proxy server to be running
				for (int i2 = 0; i2 <= MAX_SERVER_START_ITER; i2++) {
					if (i2 == MAX_SERVER_START_ITER) {
						String failureMsg = "Proxy server on port '" + proxyServerPort.get().toString()
								+ "' did not start in a reasonable amount of time.";
						Logger.logMessage(failureMsg);
						if (!GridManager.isEC2Agent()) {
							throw new RuntimeException(failureMsg);
						}
						break;
					}

					if (isProxyServerRunning()) {
						Logger.logConsoleMessage("Proxy server is running and accepting proxy instance "
								+ "requests on '" + proxyServerPort.get() + "'.");
						proxyServerReady = true;
						break;
					}

					SleepUtils.sleep(1000);
				}
			} else {
				Logger.logConsoleMessage("Proxy server is running and accepting proxy instance " + "requests on '"
						+ proxyServerPort.get() + "'.");
				proxyServerReady = true;
			}

			if (proxyServerReady) {
				proxyServerStarted.set(true);
				break;
			}

			// get a new proxy server port (EC2)
			if (GridManager.isEC2Agent()) {
				Logger.logMessage("Failed to start a valid proxy server on port '" + proxyServerPort.get().toString()
						+ "'. Attempting to start a new proxy server on a new port.");
				proxyServerPort.set(getProxyServerPort());
			}
			SleepUtils.sleep(1000);
		}
	}

	public static void stopProxyServer() {
		try {
			Logger.logConsoleMessage("Stopping proxy server on port '" + proxyServerPort.get() + "'.");
			String[] processLns = commandHandler("ps aux | grep '" + proxyServerPort.get() + "'").split("\\r?\\n");
			for (String processLn : processLns) {
				if (processLn.contains(proxyServerPort.get().toString()) && processLn.contains(BMP_PROXY_VERSION)) {
					// get the process id
					String processId = processLn.trim().split("\\s+")[1];
					commandHandler(BMP_PROXY_USER + " kill -9 " + processId);

					File bmpServerFile = new File(getBMPServerFilePath());
					if (bmpServerFile.exists()) {
						bmpServerFile.delete();
					}
				}
			}
		} catch (Exception e) {
			Logger.logMessage("Failed to stop proxy server on port '" + proxyServerPort.get());
			Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
		}
	}

	public static synchronized Boolean isProxyInstanceRunning() {
		return ProxyFactory.isProxyPortInUse(proxyInstancePort.get());
	}

	public static void startProxyInstance() {
		// double check the proxy port isn't already in use and terminate if on
		// the lab
		if (isProxyInstanceRunning()) {
			String proxyInstRunningMsg = "Requested a new proxy instance on '" + proxyInstancePort.get().toString()
					+ "' but the " + "proxy port is already in use!";
			Logger.logMessage(proxyInstRunningMsg);
			throw new RuntimeException(proxyInstRunningMsg);
		}

		// start the proxy instance
		Logger.logMessage("Starting proxy instance on '" + hostLoc() + "' on port '" + proxyInstancePort.get()
				+ "' on server '" + proxyServerPort.get() + "'.");
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				String proxyInstanceFailureMsg = "Proxy instance on port '" + proxyInstancePort.get().toString()
						+ "' on server '" + proxyServerPort.get().toString() + "' did not start in an "
						+ "appropriate amount of time!";
				Logger.logMessage(proxyInstanceFailureMsg);
				throw new RuntimeException(proxyInstanceFailureMsg);
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost("http://localhost:" + proxyServerPort.get() + "/proxy");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("port", proxyInstancePort.get().toString()));
				if (useEllipticCurve.get()) {
					postParams.add(new BasicNameValuePair("useEcc", "true"));
				}

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				String content = null;
				try {
					content = EntityUtils.toString(response.getEntity(), "UTF-8");
				} catch (Exception e2) {
					// ignore
				}

				if (response == null || content == null || !content.contains(proxyInstancePort.get().toString())) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute for proxy instance '" + proxyInstancePort.get() + "' on attempt '"
							+ i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				}
			} catch (Exception e) {
				// ignore
			}

			if (isProxyInstanceRunning()) {
				SleepUtils.sleep(250);
				proxyInstanceStarted.set(true);
				break;
			}

			SleepUtils.sleep(5000);
		}

		if (TestRun.isMobile() && TestRun.isIos()) {
			blacklistAppleUpdate();
		}

		Boolean loggingEnabled = enableLogging();
		if (!loggingEnabled) {
			String loggingFailureMsg = "Logging was not successfully enabled on proxy server '"
					+ proxyServerPort.get().toString() + "' with instance port '" + proxyInstancePort.get().toString()
					+ "'.";
			Logger.logMessage(loggingFailureMsg);
			throw new RuntimeException(loggingFailureMsg);
		}

		setTimeouts(ProxyFactory.PROXY_TIMEOUT, ProxyFactory.PROXY_TIMEOUT, ProxyFactory.PROXY_TIMEOUT);
	}

	public static void stopProxyInstance() {
		Logger.logMessage("Stopping proxy instance on port '" + proxyInstancePort.get()
				+ "' on proxy server with port '" + proxyServerPort.get() + "'.");

		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage(
						"Proxy instance on port '" + proxyInstancePort.get() + "' did not stop successfully!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpDelete request = new HttpDelete(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get());

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		SleepUtils.sleep(1000);
	}

	public static void resetProxy() {
		Logger.logMessage("Resetting proxy instance on port '" + proxyInstancePort.get() + "'. "
				+ "Note that this will reset all filters, logs, etc.");
		stopProxyInstance();
		startProxyInstance();
	}

	public static Boolean enableLogging() {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Har logging for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not enabled successfully!");
				return false;
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/har");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("captureHeaders", "true"));
				postParams.add(new BasicNameValuePair("captureCookies", "true"));
				postParams.add(new BasicNameValuePair("captureContent", "true"));

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				Integer responseCode = null;
				try {
					response = httpclient.execute(request);
					responseCode = response.getStatusLine().getStatusCode();
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (responseCode != null
						&& (responseCode == HttpStatus.OK_200 || responseCode == HttpStatus.NO_CONTENT_204)) {
					// TODO = determine why this call returns either a 200 or
					// 204 for the same post...
					break;
				} else {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				}
			} catch (Exception e) {
				Logger.logToSysFile(e.getMessage());
			}

			SleepUtils.sleep(1000);
		}

		return true;
	}

	public static Har getLog() {
		HarReader harReader = new HarReader();
		Har har = null;
		try {
			har = harReader.readFromString(getLogAsString().replaceAll("PATCH", "PUT"));
		} catch (HarReaderException e) {
			Logger.logMessage("Failed to get harlog from proxy port!");
		}

		return har;
	}

	public static String getLogAsString() {
		String content = null;
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Har log retrieval for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
			}

			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(
					"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/har");

			try {
				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					content = EntityUtils.toString(response.getEntity(), "UTF-8");
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return content;
	}

	public static List<HarEntry> getLogEntries() {
		return getLog().getLog().getEntries();
	}

	public static void clearLog() {
		enableLogging();
	}

	public static Boolean isProxyServerStarted() {
		return proxyInstanceStarted.get();
	}

	public static Boolean isProxyInstanceStarted() {
		return proxyInstanceStarted.get();
	}

	public static void setHeader(String header, String value) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy header setting of '" + header + "=" + value + "' for proxy instance on port '"
						+ proxyInstancePort.get() + "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/headers");

				StringEntity stringEntity = new StringEntity("{\"" + header + "\" : \"" + value + "\"}");
				request.addHeader("content-type", "application/json");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}
			SleepUtils.sleep(1000);
		}
	}

	public static Boolean applyRequestFilters(String requestFilter) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy request filter setting of '" + requestFilter + "' for proxy instance on port '"
						+ proxyInstancePort.get() + "' was not successfull!");
				return false;
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/filter/request");

				StringEntity stringEntity = new StringEntity(requestFilter);
				request.addHeader("content-type", "text/plain");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return true;
	}

	public static Boolean applyResponseFilters(String responseFilter) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy response filter setting of '" + responseFilter
						+ "' for proxy instance on port '" + proxyInstancePort.get() + "' was not successfull!");
				return false;
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPost request = new HttpPost("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/filter/response");

				StringEntity stringEntity = new StringEntity(responseFilter);
				request.addHeader("content-type", "text/plain");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return true;
	}

	public static void setTimeouts(Integer requestTimeout, Integer readTimeout, Integer connectionTimeout) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy timeout settings for proxy instance on port '" + proxyInstancePort.get()
						+ "' were not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/timeout");

				StringEntity stringEntity = new StringEntity(
						"{\"requestTimeout\" : \"" + requestTimeout + "\", \"readTimeout\" : \"" + readTimeout
								+ "\", \"connectionTimeout\" : \"" + connectionTimeout + "\"}");
				request.addHeader("content-type", "application/json");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}
	}

	public static String getWhitelistedUrls() {
		String content = null;

		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy whitelist request for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet request = new HttpGet("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/whitelist");

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					content = EntityUtils.toString(response.getEntity(), "UTF-8");
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return content;
	}

	public static void setWhiteListedUrls(String urlPattern, Integer statusCodeToReturn) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy whitelist setting request for proxy instance on port '"
						+ proxyInstancePort.get() + "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/whitelist");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("regex", urlPattern));
				postParams.add(new BasicNameValuePair("status", statusCodeToReturn.toString()));

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + ". Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}
	}

	public static void clearWhiteListedUrls() {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy whitelist clear request for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpDelete request = new HttpDelete("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/whitelist");

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}
	}

	public static String getBlacklistedUrls() {
		String content = null;
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy blacklist request for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpGet request = new HttpGet("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/blacklist");

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logConsoleMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					content = EntityUtils.toString(response.getEntity(), "UTF-8");
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return content;
	}

	public static Boolean setBlackListedUrls(String urlPattern, Integer statusCodeToReturn) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy blacklist set request for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
				return false;
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/blacklist");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("regex", urlPattern));
				postParams.add(new BasicNameValuePair("status", statusCodeToReturn.toString()));

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		return true;
	}

	public static void clearBlackListedUrls() {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy blacklist clear request for proxy instance on port '" + proxyInstancePort.get()
						+ "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpDelete request = new HttpDelete("http://localhost:" + proxyServerPort.get() + "/proxy/"
						+ proxyInstancePort.get() + "/blacklist");

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}

		if (TestRun.isMobile() && TestRun.isIos()) {
			blacklistAppleUpdate();
		}
	}

	public static void setBandwidth(Integer downstreamKbps, Integer upstreamKbps) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy bandwidth request of '" + downstreamKbps + "/" + upstreamKbps
						+ "' for proxy instance on port '" + proxyInstancePort.get() + "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/limit");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("downstreamKbps", downstreamKbps.toString()));
				postParams.add(new BasicNameValuePair("upstreamKbps", downstreamKbps.toString()));

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "application/x-www-form-urlencoded");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}
	}

	public static void setLatency(Integer latency) {
		for (int i = 0; i <= PROXY_RETRY_COUNT; i++) {
			if (i == PROXY_RETRY_COUNT) {
				Logger.logMessage("Proxy latency request of '" + latency + "' for proxy instance on port '"
						+ proxyInstancePort.get() + "' was not successfull!");
			}

			try {
				HttpClient httpclient = HttpClientBuilder.create().build();
				HttpPut request = new HttpPut(
						"http://localhost:" + proxyServerPort.get() + "/proxy/" + proxyInstancePort.get() + "/limit");

				ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
				postParams.add(new BasicNameValuePair("latency", latency.toString()));

				StringEntity stringEntity = new UrlEncodedFormEntity(postParams, "UTF-8");
				request.addHeader("content-type", "text/plain");
				request.setEntity(stringEntity);

				HttpResponse response = null;
				try {
					response = httpclient.execute(request);
				} catch (HttpHostConnectException httpE) {
					// connection refused
				}

				if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
					Logger.logMessage("Proxy request at '" + request.getURI().toString()
							+ "' failed to execute on attempt '" + i + "'. Retrying...");
					if (response != null) {
						Logger.logMessage(response.getStatusLine().getReasonPhrase());
					}
				} else {
					break;
				}
			} catch (Exception e) {
				Logger.logToSysFile(ExceptionUtils.getStackTrace(e));
			}

			SleepUtils.sleep(1000);
		}
	}

	public static void disableEllipticCurve() {
		Logger.logConsoleMessage(
				"Disabling elliptic curve cyptography on proxy instance '" + proxyInstancePort.get() + "'.");
		useEllipticCurve.set(false);
	}

	public static synchronized Integer getProxyServerPort() {
		if (GridManager.isEC2Agent()) {
			return Integer.parseInt(ProxyFactory.getUnusedProxyPort(15000, 20000));
		}
		return Integer.parseInt(DEFAULT_PROXY_SERVER_PORT.toString());
	}

	private static void blacklistAppleUpdate() {
		List<String> blacklistUrls = Arrays.asList(ProxyFactory.APPLE_UPDATE_1, ProxyFactory.APPLE_UPDATE_2);

		List<Boolean> result = new ArrayList<Boolean>();
		for (String blacklistUrl : blacklistUrls) {
			result.add(setBlackListedUrls(blacklistUrl, 403));
			result.add(applyRequestFilters("if (request.getUri().toString().contains('" + blacklistUrl + "')) "
					+ "{ request.setUri(null); }"));
		}

		if (result.contains(false)) {
			throw new RuntimeException("Failed to set Apple update blacklist policies. Terminating proxy "
					+ "instance as a protection against apple update alerts!");
		}
	}

	private static String commandHandler(String command) {
		if (proxyMachineIP.get() == null) {
			CommandExecutor.setEC2CommandHop(false);
		}

		if (OSDetector.isMac() || OSDetector.isLinux()) {
			return CommandExecutor.execRemoteMultiCommand(command, proxyMachineIP.get(), null);
		} else {
			// TODO - better local windows implementation
			return CommandExecutor.execCommand(command, proxyMachineIP.get(), 1);
		}
	}

	private static String hostLoc() {
		if (proxyMachineIP.get() == null) {
			return "localhost";
		}
		return proxyMachineIP.get();
	}

	private static Boolean isProxyServerRunning() {
		if (GridManager.isEC2Agent()) {
			File bmpServerFile = new File(getBMPServerFilePath());
			if (bmpServerFile.exists()) {
				try {
					String output = FileUtils.readFileToString(bmpServerFile, "UTF-8");
					return (StringUtils.countOccurrencesOf(output.toLowerCase(), "started") >= 2
							&& ProxyFactory.isProxyPortInUse(proxyServerPort.get()));
				} catch (Exception e) {
					Logger.logConsoleMessage("Failed to read proxy server log contents for " + "proxy server '"
							+ proxyServerPort.get().toString() + "'.");
					e.printStackTrace();
				}
			}

			return false;
		} else {
			if (OSDetector.isMac()) {
				String output = commandHandler("ps aux | grep '" + proxyServerPort.get().toString() + "'");
				return (output != null && output.contains("browsermob"));
			} else {
				// TODO not the perfect solution for local windows
				return ProxyFactory.isProxyPortInUse(proxyServerPort.get());
			}
		}
	}

	private static String getBMPServerFilePath() {
		return System.getenv("JENKINS_AGENT_TEMP_PATH") + File.separator + "BMPServer-"
				+ proxyServerPort.get().toString();
	}

}
