package com.softech.test.core.sauce;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import com.saucelabs.ci.sauceconnect.SauceConnectFourManager;
import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.DependencyManager;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;
import com.softech.test.core.util.RandomData;

public class SauceTunnelsManager {
	
	private static final Integer TUNNEL_RETRY_COUNT = 3;
	private static final Integer SC_TUNNEL_TIMEOUT_S = 30;
	
	private static final String SAUCE_ZIP = "sauce.zip";
	
	private static final String USER_DIR = System.getProperty("user.dir");
	private static String scPath;
	
	private static ThreadLocal<SauceTunnelManager> sauceTunnel = new ThreadLocal<SauceTunnelManager>();
	private static ThreadLocal<String> tunnelIdentifier = new ThreadLocal<String>();
	private static ThreadLocal<String> tunnelOptions = new ThreadLocal<String>();
	private static ThreadLocal<String> logFile = new ThreadLocal<String>();
	private static ThreadLocal<String> pidFile = new ThreadLocal<String>();
	
	public static synchronized void downloadSC() {
		if (GridManager.isEC2Agent()) {
			scPath = System.getenv("SAUCE_COMMANDLINE_PATH");
		} else {
			// extract sauce connect to the working directory from the jar
			String sauceBinary = null;
			if (OSDetector.isWindows()) {
				sauceBinary = "sc.exe";
			} else if (OSDetector.isMac()) {
				sauceBinary = "sc";
			} else if (OSDetector.isLinux()) {
				sauceBinary = "sclin";
			}
				
			try {
	    		DependencyManager.setDependencyLoc(sauceBinary);
	    		if (!DependencyManager.dependencyExists()) {
	    			DependencyManager.downloadDependency(SAUCE_ZIP);
	    			DependencyManager.unzipDependency(SAUCE_ZIP);
	    			DependencyManager.setDependencyExec(sauceBinary);
	    		}
	    			
				scPath = DependencyManager.getDependencyLoc();
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to get dependency '" + sauceBinary + "'.");
				e.printStackTrace();
			}
		}
	}
	
	public static String getSauceTunnelId() {
		return tunnelIdentifier.get();
	}
	
	public static Boolean startSauceTunnel(String tunnelId, String proxyPort) {
		// attempt to start the tunnel
		Boolean sauceConnected = false;
		Exception sauceException = null;
		for (int i = 0; i < TUNNEL_RETRY_COUNT; i++) {
    		try {
            	ServerSocket serverSocket = null;
            	Integer tunnelPort = null;
                serverSocket = new ServerSocket(0);
        		tunnelPort = serverSocket.getLocalPort();
        		serverSocket.close();
                
                Integer scProxyPort = null;
                ServerSocket serverSocket2 = null;
                serverSocket2 = new ServerSocket(0);
        		scProxyPort = serverSocket2.getLocalPort();
        		serverSocket2.close();
        		
        		// set the ready file location
        		String baseDir = USER_DIR;
        		if (GridManager.isEC2Agent()) {
        			baseDir = getJenkinsTempDir();
        		}
        		File readyFile = new File(baseDir + File.separator + "tunnelReady" + RandomData.getCharacterString(40)); 
        		
        		// set the tunnel files
        		String logFile = baseDir + File.separator + RandomData.getCharacterString(40) + ".log";
        		String pidFile = baseDir + File.separator + RandomData.getCharacterString(40) + ".pid";
        		
        		// add the log/pid files to remove later
        		SauceTunnelsManager.logFile.set(logFile);
        		SauceTunnelsManager.pidFile.set(pidFile);
        		
        		// set the tunnel id
        		tunnelIdentifier.set(tunnelId);
        		
        		tunnelOptions.set("--no-proxy-caching -B all -i " + tunnelIdentifier.get() + " -p localhost:" + proxyPort + " --pidfile " 
        				+ pidFile + " --logfile " + logFile + " --scproxy-port " + scProxyPort.toString() + " -f " 
        				+ readyFile.getAbsolutePath() + " --no-cert-verify");
            		
        		SauceTunnelManager sauceTunnel = new SauceConnectFourManager(false);
        		SauceTunnelsManager.sauceTunnel.set(sauceTunnel);
            	sauceTunnel.openConnection(SauceCredentialManager.getSauceUsername(), SauceCredentialManager.getSauceKey(), tunnelPort, null, tunnelOptions.get(), 
                    	null, false, scPath);
            	
        		for (int readyIter = 0; readyIter <= SC_TUNNEL_TIMEOUT_S; readyIter++) {
        			if (readyIter == SC_TUNNEL_TIMEOUT_S) {
        				readyFile.delete();
        				closeSauceTunnel();
        				throw new RuntimeException("SC Spin up failure.");
        			}
        			
        			if (readyFile.exists()) {
        				Logger.logConsoleMessage("Tunnel with id '" + tunnelIdentifier.get()  + "' is ready for testing.");
        				readyFile.delete();
        				break;
        			}
        			Thread.sleep(1000);
        		}
        		sauceConnected = true;
        		break;
        	}
        	catch (Exception e) {
        		sauceException = e;
        		Logger.logConsoleMessage("Failed to spin up a sauce tunnel on with proxy port '" + proxyPort.toString() + "'. Retrying...");
        	}
    	}
		
		if (!sauceConnected) {
			sauceException.printStackTrace();
		}
		return sauceConnected;
    }
	
	public static void closeSauceTunnel() {
		File logFile = null;
		try {
			String baseDir = GridManager.isEC2Agent() ? getJenkinsTempDir() : USER_DIR;
			logFile = new File(baseDir + File.separator 
				+ RandomData.getCharacterString(20) + "tunnelCloseFile.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			PrintStream logStream = new PrintStream(logFile);
			sauceTunnel.get().closeTunnelsForPlan(SauceCredentialManager.getSauceUsername(), tunnelOptions.get(), logStream);
			logStream.close();
			logFile.delete();
			if (tunnelIdentifier.get() != null) {
				terminateTunnelsOnSauce(tunnelIdentifier.get());
			}
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to delete sauce tunnel.");
			e.printStackTrace();
			if (logFile != null) {
				if (logFile.exists()) {
					logFile.delete();
				}
			}
		}
	}
	
	public static void terminateTunnelsOnSauce(String specificTunnelId) {
		List<String> tunnelsToClose = Arrays.asList(specificTunnelId);
		if (specificTunnelId == null) {
			tunnelsToClose = getAllTunnelIds();
		}
							
		List<Thread> closeThreads = new ArrayList<Thread>();
		for (String tunnelId : tunnelsToClose) {
			closeThreads.add(new Thread() {
			    public void run() {
			    	Logger.logConsoleMessage("Deleting sauce labs tunnel with id '" + tunnelId + "'.");
			    	CommandExecutor.setEC2CommandHop(false);
			    	CommandExecutor.execCommand("curl https://saucelabs.com/rest/v1/" + SauceCredentialManager.getSauceUsername() 
			    		+ "/tunnels/" + tunnelId + " -u " + SauceCredentialManager.getSauceUsername() + ":" 
			    		+ SauceCredentialManager.getSauceKey() + " -X DELETE", null, null);
			    	}
			});
		}
					
		for (Thread thread : closeThreads) {
			thread.start();
		}
		
		for (Thread thread : closeThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				Logger.logConsoleMessage("Failed to close sauce tunnels.");
				e.printStackTrace();
			}
		}
	}
	
	private static String getJenkinsTempDir() {
		return System.getenv("JENKINS_AGENT_TEMP_PATH");
	}
	
	@SuppressWarnings("unchecked")
	private static List<String> getAllTunnelIds() {
		// get all the active tunnel ids
		List<String> allTunnelIds = new ArrayList<String>();
		String tunnelsUrl = "https://" + SauceCredentialManager.getSauceUsername() + ":" + SauceCredentialManager.getSauceKey() 
				+ "@saucelabs.com/rest/v1/" + SauceCredentialManager.getSauceUsername() + "/tunnels";
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet getTunnelsRequest = new HttpGet(tunnelsUrl);

		HttpResponse getTunnelsResponse = null;
		try {
		    getTunnelsResponse = httpclient.execute(getTunnelsRequest);
		} catch (Exception e) {
		    Logger.logConsoleMessage("Failed to get list of active tunnels from sauce.");
		    e.printStackTrace();
		}
		        
		if (getTunnelsResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
		    Logger.logConsoleMessage("Failed to get list of active tunnels from sauce.");
		    Logger.logConsoleMessage(getTunnelsResponse.getStatusLine().getReasonPhrase());
		} else {
		    try {
		        InputStream input = getTunnelsResponse.getEntity().getContent();
		        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		        StringBuilder sb = new StringBuilder();
		        String line = null;
		        while ((line = reader.readLine()) != null) {
		            sb.append(line).append("\n");
		        }
		        reader.close();
		        input.close();
		                
		        String responseBody = sb.toString();
		        JSONParser parser = new JSONParser();
		        JSONArray tunnelIDArr = (JSONArray) parser.parse(responseBody);
		        Iterator<String> iterator = tunnelIDArr.iterator();
		        while (iterator.hasNext()) {
		            allTunnelIds.add(iterator.next());
		        }
		    } catch (Exception e) {
		        Logger.logConsoleMessage("Failed to get list of active tunnels from sauce.");
		        e.printStackTrace();
		    }
		}
				
		return allTunnelIds;
	}
   
}