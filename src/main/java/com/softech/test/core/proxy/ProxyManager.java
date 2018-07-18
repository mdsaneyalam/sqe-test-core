package com.softech.test.core.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.littleshoot.proxy.HttpFiltersSource;

import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.RequestFilterAdapter;
import net.lightbody.bmp.mitm.KeyStoreFileCertificateSource;

import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.proxy.CaptureType;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;

public class ProxyManager {

	private static final String REQUEST_FILTER_ADAPTER = "RequestFilterAdapter";
	private static final String RESPONSE_FILTER_ADAPTER = "ResponseFilterAdapter";
	
	private static HashMap<Integer, BrowserMobProxyServer> proxyServers = new HashMap<Integer, BrowserMobProxyServer>();
	private static ThreadLocal<BrowserMobProxyServer> proxyServer = new ThreadLocal<BrowserMobProxyServer>() {
    	protected BrowserMobProxyServer initialValue() {
    		return null;
    	}
    };
	private static ThreadLocal<Integer> proxyServerPort = new ThreadLocal<Integer>();
	private static ThreadLocal<ImpersonatingMitmManager> mitmManager = new ThreadLocal<ImpersonatingMitmManager>() {
    	protected ImpersonatingMitmManager initialValue() {
    		return null;
    	}
    };
	
	private static ThreadLocal<Boolean> mitmEnabled = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
	private static ThreadLocal<Boolean> certsDelivered = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
	private static File p12File = null;
	
	/**********************************************************************************************
     * Gets all the server instances on the lab.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return HashMap<Integer, BrowserMobProxyServer> - HashMap of all the running proxy server instances keyed to their ports.
     ***********************************************************************************************/
    public static HashMap<Integer, BrowserMobProxyServer> getAllProxyServers() {
    	return proxyServers;
    }
	
    /**********************************************************************************************
     * Sets a single instance of the proxy server as identified by the device proxy port, to be used at the test level.
     * 
     * @param port - {@link Integer} - The proxy port of the proxy.
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
    public static synchronized void setProxyServer(Integer proxyPort) {
    	try {
			Logger.logMessage("Setting active proxy server to proxy server with port '" + proxyPort + "'.");
			if (proxyServers.isEmpty()) {
				proxyServer.set(new BrowserMobProxyServer());
			} else {
				proxyServer.set(getAllProxyServers().get(proxyPort));
			}
	    	proxyServerPort.set(proxyPort);
	    	disableAllFilters();
	    	clearLog();
		} catch (Exception e) {
			Logger.logMessage("Failed to set active proxy server to server with port '" + proxyPort + "'.");
			e.printStackTrace();
		}
    }
    
    /**********************************************************************************************
     * Gets a single instance of the proxy server as identified by the device proxy port.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return BrowserMobProxyServer - An instance of the proxy server.
     ***********************************************************************************************/
    public static BrowserMobProxyServer getProxyServer() {
    	return proxyServer.get();
    }
    
    /**********************************************************************************************
     * Starts the proxy.
     * 
     * @author Brandon Clark created April 7, 2016
     * @version 1.0 April 7, 2016
     ***********************************************************************************************/
    public static Boolean startProxyServer() {
    	if (ProxyFactory.isProxyPortInUse(proxyServerPort.get())) {
    		Logger.logConsoleMessage("Requested a new proxy on '" + proxyServerPort.get() + "' but the port is already in use.");
    		return false;
    	}
    	
    	try {
    		Logger.logConsoleMessage("Starting proxy server on port '" + proxyServerPort.get() + "'.");
    		if (!mitmEnabled.get()) {
        	    disableMITM(); // disable MITM unless specified by user
    		} else if (!certsDelivered.get()) {
    			disableMITM(); // in the event MITM was enabled but the certs failed to be distributed
    		} else {
    			proxyServer.get().setMitmManager(mitmManager.get()); // enable MITM
    			proxyServer.get().setTrustAllServers(true);
    			proxyServer.get().setConnectTimeout(ProxyFactory.PROXY_TIMEOUT, TimeUnit.SECONDS);
    			proxyServer.get().setIdleConnectionTimeout(ProxyFactory.PROXY_TIMEOUT, TimeUnit.SECONDS);
    			proxyServer.get().setRequestTimeout(ProxyFactory.PROXY_TIMEOUT, TimeUnit.SECONDS);
    		}
    		
    		proxyServer.get().start(proxyServerPort.get());
    		proxyServer.get().addHttpFilterFactory(blacklistAppleUpdate());
    		proxyServer.get().newHar();
    		enableLogging();
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to start active proxy and enable logging with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
			return false;
		}
    	
    	return true;
    }
    
    /**********************************************************************************************
     * Starts all the proxy instances. Typically used in Suite startup methods to start all proxy instances prior
     * to any test executions.
     * 
     * @author Brandon Clark created April 7, 2016
     * @version 1.0 April 7, 2016
     ***********************************************************************************************/
    public static void startAllProxyServers() {
    	List<Thread> startProxyThreads = new ArrayList<Thread>();
    	for (Map.Entry<Integer, BrowserMobProxyServer> proxyServer : proxyServers.entrySet()) {
    		startProxyThreads.add(new Thread() {
    		    public void run() {
    		    	setProxyServer(proxyServer.getKey());
    	    		startProxyServer();
    		    }
            });
    	}
    	for (Thread thread : startProxyThreads) {
		     thread.start();
		}
		for (Thread thread : startProxyThreads) {
		     try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**********************************************************************************************
     * Stops the proxy.
     * 
     * @author Brandon Clark created April 7, 2016
     * @version 1.0 April 7, 2016
     ***********************************************************************************************/
    public static void stopProxyServer() {
    	try {
    		if (!proxyServer.get().isStopped()) {
    			Logger.logConsoleMessage("Stopping proxy server on port '" + proxyServerPort.get() + "'.");
    			proxyServer.get().abort();
    		}
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to stop active proxy with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
		}
    }
    
    /**********************************************************************************************
     * Stops all the proxy instances. Typically used in Suite teardown methods to stop all proxy instances after
     * all test executions.
     * 
     * @author Brandon Clark created April 7, 2016
     * @version 1.0 April 7, 2016
     ***********************************************************************************************/
    public static void stopAllProxyServers() {
    	List<Thread> stopProxyThreads = new ArrayList<Thread>();
    	for (Map.Entry<Integer, BrowserMobProxyServer> proxyServer : proxyServers.entrySet()) {
    		stopProxyThreads.add(new Thread() {
    		    public void run() {
    		    	setProxyServer(proxyServer.getKey());
    	    		stopProxyServer();
    		    }
            });
    	}
    	for (Thread thread : stopProxyThreads) {
		     thread.start();
		}
		for (Thread thread : stopProxyThreads) {
		     try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**********************************************************************************************
     * Gets the Har file report.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     * @return Har - The Har file report.
     ***********************************************************************************************/
    public static Har getLog() {
    	Har proxyLog = null;
    	try {
    		proxyLog = proxyServer.get().getHar();
		} catch (Exception e) {
			Logger.logMessage("Failed to get har log for proxy with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
		}
    	
    	if (proxyLog == null) {
    		Logger.logMessage("The proxy log for proxy with port '" + proxyServerPort.get() + "' is null.");
    	}
    	
    	return proxyLog;
    }
    
    /**********************************************************************************************
     * Gets the Har report log entries.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     * @return List<HarEntry - A list of all the entries in the Har file log.
     ***********************************************************************************************/
    public static List<HarEntry> getLogEntries() {
    	List<HarEntry> harEntries = null;
    	try {
    		harEntries = proxyServer.get().getHar().getLog().getEntries();
		} catch (Exception e) {
			Logger.logMessage("Failed to get log entries for proxy with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
		}
    	
    	return harEntries;
    }
    
    /**********************************************************************************************
     * Clears the Har log.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     ***********************************************************************************************/
    public static void clearLog() {
    	try {
    		Logger.logMessage("Clearing proxy log for proxy server on port '" + proxyServerPort.get() + "'.");
    		proxyServer.get().newHar();
    		enableLogging();
		} catch (Exception e) {
			Logger.logMessage("Failed to clear log for proxy with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
		}
    }
    
    /**********************************************************************************************
     * Disables any traffic filtering that is in place at the project level.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     ***********************************************************************************************/
    public static void disableAllFilters() {
    	List<HttpFiltersSource> filters = null;
    	
    	try {
    		filters = proxyServer.get().getFilterFactories();
    	} catch (Exception e) {
    		Logger.logMessage("Failed to get list of proxy filters for proxy server with port '" + proxyServerPort.get() + "'.");
    	    e.printStackTrace();
    	}
    	
    	if (filters != null) {
    		for (HttpFiltersSource filter : filters) {
        		if (filter.toString().contains(REQUEST_FILTER_ADAPTER) || filter.toString().contains(RESPONSE_FILTER_ADAPTER)) {
        			try {
        				Logger.logMessage("Removing proxy filter '" + filter.toString() + "' for proxy server with port '" + proxyServerPort.get() + "'.");
            			proxyServer.get().getFilterFactories().remove(filter);
        			} catch (Exception e) {
        				Logger.logMessage("Failed to remove proxy filter '" + filter.toString() + "' for proxy server with port '" + proxyServerPort.get() + "'.");
        			    e.printStackTrace();
        			}
        		}
        	}
    		proxyServer.get().addHttpFilterFactory(blacklistAppleUpdate());
    	}
    }
    
    /**********************************************************************************************
     * Disables MITM. Note - MITM and https traffic capturing is disabled by default as it requires the
     * user has the MQE_BMP_Keystore.p12 cert installed on their device under test.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     ***********************************************************************************************/
    public static void disableMITM() {
    	proxyServer.get().setMitmDisabled(true);
    }
    
    /**********************************************************************************************
     * Enables MITM and https traffic capturing. Note that the user MUST have the MQE_BMP_Keystore.p12 cert
     * installed on their device under test.
     * 
     * @author Brandon Clark created April 8, 2016
     * @version 1.0 April 8, 2016
     ***********************************************************************************************/
    public static synchronized void enableMITM() {
    	Logger.logConsoleMessage("Enabling MITM ssl traffic capturing.");
    	// enable mitm in core
    	mitmEnabled.set(true);
    	String userDir = System.getProperty("user.dir") + File.separator;
    	p12File = new File(userDir + Constants.BMP_P12_FILE);
    	
    	// access the mqe bmp certs from core
    	if (!p12File.exists()) {
    		Logger.logConsoleMessage("The BMP MITM cert does not exist in the user dir. Copying to the project env.");
    		InputStream inputStream = ProxyManager.class.getClassLoader().getResourceAsStream(p12File.getName());
        	FileOutputStream fileOutputStream = null;
        	try {
        		if (p12File.exists()) {
        			p12File.delete();
        		}
        						
        	    fileOutputStream = new FileOutputStream(p12File);
        	    byte[] buf = new byte[2048];
        		int r = inputStream.read(buf);
        		while(r != -1) {
        		    fileOutputStream.write(buf, 0, r);
        			r = inputStream.read(buf);
        		}
        	} catch (Exception e) {
        		Logger.logConsoleMessage("Failed to copy file '" + p12File.getName() + "' to the local user's system.");
        		e.printStackTrace();
        	} finally {
        		if(fileOutputStream != null) {
        		    try {
        			    fileOutputStream.close();
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        		}
        	}
    	} else {
    		certsDelivered.set(true);
    	}
    	
    	if (!p12File.exists()) {
    		Logger.logConsoleMessage("The BMP MITM cert was not properly delivered for ssl/http capturing. "
    				+ "MITM is being disabled.");
    		certsDelivered.set(false);
    	}
    	
    	if (p12File != null) {
    		KeyStoreFileCertificateSource fileCertificateSource = new KeyStoreFileCertificateSource(
                    "PKCS12",                         
                    p12File,
                    Constants.BMP_ALIAS,
    				Constants.BMP_PASS);
    		
        	// generate the mitm manager
        	mitmManager.set(ImpersonatingMitmManager.builder().rootCertificateSource(fileCertificateSource).trustAllServers(true).build());
    	}
    }
    
    private static HttpFiltersSource blacklistAppleUpdate() {
    	RequestFilterAdapter.FilterSource requestFilter = new RequestFilterAdapter.FilterSource(new RequestFilter() {
            @Override
            public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, 
                    HttpMessageInfo messageInfo) {
            	if (request.getUri().contains(ProxyFactory.APPLE_UPDATE_1) || request.getUri().contains(ProxyFactory.APPLE_UPDATE_2)) {
            		request.setUri("null");
            	}
                return null;
            }
        });
    	
    	return requestFilter;
    }
    
    private static void enableLogging() {
    	try {
    		proxyServer.get().setHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.REQUEST_COOKIES, 
        			CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_CONTENT, CaptureType.RESPONSE_COOKIES, 
        			CaptureType.RESPONSE_HEADERS);
		} catch (Exception e) {
			Logger.logMessage("Failed to enable logging for proxy with port '" + proxyServerPort.get() + "'.");
			e.printStackTrace();
		}
    }
	
}
