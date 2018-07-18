package com.softech.test.core.performance;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.softech.test.core.lab.CommandExecutor;
//import com.softech.test.core.report.SplunkManager;
import com.softech.test.core.util.Logger;

public class YSlowManager {

	private static ThreadLocal<String> requestUrl = new ThreadLocal<String>();
	private static ThreadLocal<String> results = new ThreadLocal<String>();
	private static ThreadLocal<String> ySlowJSPath = new ThreadLocal<String>();
	
	private static final String PHANTOMJS_PATH = "/usr/local/lib/node_modules/phantomjs/bin/phantomjs";
	private static final String NODE_PATH = "/usr/local/Cellar/node/5.5.0/bin";
	
	public static void setYSlowJSPath(String pathToYslowJS) {
		ySlowJSPath.set(pathToYslowJS);
	}
	
	public static String getYSlowData(String url) {
		Logger.logMessage("Getting Yslow data with request '" + url + "'.");
		requestUrl.set(url);
		String[] command = { PHANTOMJS_PATH, ySlowJSPath.get(), 
				"--info", "all", url };
		Map<String, String> environment = new HashMap<String, String>();
		environment.put("PATH", NODE_PATH);
		String response = CommandExecutor.execEnvironmentCommand(command, environment, null, null);
		
		String failureMsg = "Failed to get Yslow data with request '" + requestUrl.get() + "'.";
		String parsedResult = null;
		try {
			parsedResult = parseResultToJson(response);
		} catch (Exception e) {
			Logger.logConsoleMessage(failureMsg);
		}
		
		results.set(parsedResult);
        return parsedResult;
	}
	
	public static void postResultToSplunk(String splunkIndex, String resultJSON) {
		Logger.logConsoleMessage("Posting Yslow results to Splunk for request '" + requestUrl.get() + "'.");
//    	new SplunkManager().connectToSplunk().setIndex(splunkIndex).postEvent(resultJSON);
	}
	
	@SuppressWarnings("unchecked")
	private static String parseResultToJson(String result) throws Exception {
		JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(result);
        
        JSONObject jsonToPost = obj;
		jsonToPost.put("postType", "ySlow");
		jsonToPost.put("testUrl", requestUrl.get());
        
		return jsonToPost.toJSONString();
	}
	
}