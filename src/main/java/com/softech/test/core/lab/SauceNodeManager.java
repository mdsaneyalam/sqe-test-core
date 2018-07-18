package com.softech.test.core.lab;

import java.util.List;
import java.util.Random;

public class SauceNodeManager {

	public static List<String> getAllSauceNodeIds() {
		String query = "select node_id from saucenodes";
    	return LabDatabaseFactory.getResults(query);
	}
	
	public static String getAvailableSauceNodeId() {
		String query = "select node_id from saucenodes where node_status = 'active' and in_use = false";
		List<String> allFreeNodes = LabDatabaseFactory.getResults(query);
    	try {
    		// result found on query
    		return allFreeNodes.get(new Random().nextInt(allFreeNodes.size()));
    	} catch (Exception e) {
    		return null;
    	}
	}
	
	public static synchronized Boolean isSauceNodeInUse(String nodeId) {
		String result = LabDatabaseFactory.getResults("select in_use from saucenodes"
				+ " where node_id = '" + nodeId + "'").get(0);
		
		if (result.contains("t")) {
			return true;
		}
		return false;
	}
	
	public static synchronized void setSauceNodeInUse(String nodeId, Boolean inUse) {
		LabDatabaseFactory.getResults("update saucenodes set in_use = " + inUse.toString()
		    + " where node_id = '" + nodeId + "'");
	}
	
	public static synchronized void setSauceNodeUseDuration(String nodeId, Long useDuration) {
		LabDatabaseFactory.getResults("update saucenodes set in_use_duration = " + useDuration.toString()
		    + " where node_id = '" + nodeId + "'");
	}
	
	public static Long getSauceNodeUseDuration(String nodeId) {
		String duration = LabDatabaseFactory.getResults("select in_use_duration from saucenodes where node_id = '" 
	        + nodeId + "'").get(0);
		return Long.parseLong(duration);
	}
	
	public static synchronized void setSauceNodeTunnelId(String nodeId, String tunnelId) {
		LabDatabaseFactory.getResults("update saucenodes set tunnel_id = '" + tunnelId + "' where node_id = '" + nodeId + "'");
	}
	
	public static String getSauceNodeTunnelId(String nodeId) {
		String tunnelId = LabDatabaseFactory.getResults("select tunnel_id from saucenodes where node_id = '" 
	        + nodeId + "'").get(0);
		return tunnelId;
	}

}
