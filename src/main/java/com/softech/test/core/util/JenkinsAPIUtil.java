package com.softech.test.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.softech.test.core.lab.CommandExecutor;

public class JenkinsAPIUtil {

	public static String getRunningJobName() {
		return System.getenv("JOB_NAME");
	}
	
	public static String getRunningBuildId() {
		return System.getenv("BUILD_ID");
	}
	
	@SuppressWarnings("rawtypes")
	public static Boolean startJob(String jobName, HashMap<String, String> paramsAndValues) {
		StringBuilder json = new StringBuilder();
		json.append("--data-urlencode json='{\"parameter\": [");
		Iterator iterator = paramsAndValues.entrySet().iterator();
	    while (iterator.hasNext()) {
	        Map.Entry pair = (Map.Entry) iterator.next();
	        json.append("{\"name\":\"" + pair.getKey() + "\",\"value\":\"" + pair.getValue() + "\"},");
	        iterator.remove();
	    }
	    json.append("]}'");
	    String jsonComplete = json.toString().replace(",]}", "]}");
	    
	    Logger.logConsoleMessage("Starting job '" + jobName + "'.");
	    CommandExecutor.setEC2CommandHop(false);
		CommandExecutor.execMultiCommand("curl -u " + getReadAuth() + " -X POST " + getBaseUrl() 
			+ "job/" + jobName + "/build --data token=" + getBuildToken() + " " + jsonComplete, null);
		return false;
	}
	
	public static Long getJobStartTime(String jobName, String jobNumber) throws Exception {
		CommandExecutor.setEC2CommandHop(false);
		String response = CommandExecutor.execCommand("curl --silent -u " + getReadAuth() + " " + getBaseUrl() + "/job/" 
		    + jobName + "/" + jobNumber + "/api/json?tree=timestamp", null, null);
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(response);
        JSONObject jsonObject = (JSONObject) obj;
        return Long.parseLong(jsonObject.get("timestamp").toString());
	}
	
	public static Boolean isJobBuilding(String jobName, String jobNumber) {
		try {
			CommandExecutor.setEC2CommandHop(false);
			String response = CommandExecutor.execCommand("curl --silent -u " + getReadAuth() + " " + getBaseUrl() + "/job/" 
			    + jobName + "/" + jobNumber + "/api/json?tree=building", null, null);
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(response);
	        JSONObject jsonObject = (JSONObject) obj;
	        return (Boolean) jsonObject.get("building");
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to determine job building status for job '" + jobName + "' with build number '" + jobNumber + "'.");
			return true;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Boolean isJobInQueue(String jobName) {
		try {
			CommandExecutor.setEC2CommandHop(false);
			String response = CommandExecutor.execCommand("curl --silent -u " + getReadAuth() + " " + getBaseUrl() + "/queue/api/json", null, null);
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(response);
	        JSONObject jsonObject = (JSONObject) obj;
	        JSONArray jsonArr = (JSONArray) jsonObject.get("discoverableItems");
	        Iterator<JSONObject> iterator = jsonArr.iterator();
            Integer iter = 0;
            while (iterator.hasNext()) {
                JSONObject mainObj = iterator.next();
                JSONObject taskObj = (JSONObject) mainObj.get("task");
                if (taskObj.get("name").toString().contains(jobName)) {
                	return true;
                }
                iter++;
            }
            return false;
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to determine job queue status for job '" + jobName + "'.");
			return null;
		}
	}
	
	public static String getJobStatus(String jobName, String jobNumber) {
		try {
			CommandExecutor.setEC2CommandHop(false);
			String response = CommandExecutor.execCommand("curl --silent -u " + getReadAuth() + " " + getBaseUrl() + "/job/" 
			    + jobName + "/" + jobNumber + "/api/json?tree=result", null, null);
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(response);
	        JSONObject jsonObject = (JSONObject) obj;
	        return (String) jsonObject.get("result");
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to determine job result status for job '" + jobName + "' with build number '" + jobNumber + "'.");
			return null;
		}
	}
	
	private static String getBaseUrl() {
		return System.getenv("HUDSON_URL");
	}
	
	private static String getReadAuth() {
		return System.getenv("LAB_READ_ONLY_USERNAME") + ":" + System.getenv("LAB_READ_ONLY_PASSWORD");
	}
	
	private static String getBuildToken() {
		return System.getenv("LAB_BUILD_TOKEN");
	}
	
}