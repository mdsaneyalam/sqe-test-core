package com.softech.test.core.infocenter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.util.SleepUtils;

public class GetBuildSystemStatus {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		// TODO - config what should be config'd
		Boolean jenkinsOnline = true;
		String response = "empty";
		List<String> onlineAgents = new ArrayList<String>();
		List<String> offlineAgents = new ArrayList<String>();
		String buildUrl = "https://build.viacom.com";
		
		//CommandExecutor.execCommand("say 'Checking the status of the build system and the agents now.'", null, null);
		String basicAuth = "-u " + "todo" + ":" + "todo"; // TODO - need to retrieve these securely
    	response = CommandExecutor.execCommand("curl " + basicAuth + " --silent " + buildUrl + "/computer/api/json", null, null);
		JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        JSONArray computerArr = null;
        
        try {
            if (response != null && !response.isEmpty()) {
            	CommandExecutor.execCommand("say 'The build system, is online and available for builds.", null, null);
                Object obj = parser.parse(response);
                jsonObject = (JSONObject) obj;
                computerArr = (JSONArray) jsonObject.get("computer");
            } else {
            	CommandExecutor.execCommand("say 'The build system, is offline, please contact a build engineer immediately!", null, null);
                jenkinsOnline = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (jenkinsOnline) {
        	// check the agents and ensure they are online
	        Iterator<JSONObject> iterator = computerArr.iterator();
            Integer iter = 0;
            while (iterator.hasNext()) {
                JSONObject nodeObj = iterator.next();
                if (nodeObj.get("offline").toString().contains("true")) {
                	// check that it's not temporarily offline
                	if (nodeObj.get("temporarilyOffline").toString().contains("false")) {
                		// the agent is offline and should not be, log it
                		offlineAgents.add(nodeObj.get("displayName").toString());
                	}
                } else {
                	onlineAgents.add(nodeObj.get("displayName").toString());
                }
                iter++;
            }
        }
        
        if (jenkinsOnline) {
        	CommandExecutor.execCommand("say 'The build system currently has " + onlineAgents.size() + " agents assigned.'", null, null);
        	SleepUtils.sleep(500);
        	if (offlineAgents.isEmpty()) {
        		CommandExecutor.execCommand("say 'all agent machines are online and accepting builds'", null, null);
        	} else {
        		CommandExecutor.execCommand("say 'the following agent machines are offline and not accepting builds.'", null, null);
        		for (String buildMachine : offlineAgents) {
        			SleepUtils.sleep(300);
        			CommandExecutor.execCommand("say '" + buildMachine + "'", null, null);
        		}
        	}
        }
        
        Integer jobCount = 0;
        Integer runningJobCount = 0;
        if (jenkinsOnline) {
        	// get the count of current jobs
            response = CommandExecutor.execCommand("curl " + basicAuth + " --silent " + buildUrl + "/api/xml?tree=jobs", null, null);
            jobCount = StringUtils.countMatches(response, "job _class");
            CommandExecutor.execCommand("say 'There are " + jobCount + " jobs available to build.'", null, null);
            
            response = CommandExecutor.execCommand("curl " + basicAuth + " --silent " + buildUrl + "/api/json?pretty=true", null, null);
            runningJobCount = StringUtils.countMatches(response, "blue_anime");
            CommandExecutor.execCommand("say 'of the available jobs, " + runningJobCount + "' are currently building.'", null, null);
        }
		
	}

}
