package com.softech.test.core.performance;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

//import com.softech.test.core.report.SplunkManager;
import com.softech.test.core.util.Logger;

public class PageSpeedManager {

	private static ThreadLocal<String> finalUrl = new ThreadLocal<String>();
	private static ThreadLocal<String> results = new ThreadLocal<String>();
	
	public static String getPageSpeedData(String userKey, String requestUrl, String strategy, List<String> additionalArguments) {
        // construct the url
		StringBuilder url = new StringBuilder();
		url.append("https://www.googleapis.com/pagespeedonline/v2/runPagespeed?url=" + requestUrl + "&strategy=" + strategy 
				+ "&key=" + userKey);
		if (additionalArguments != null) {
			for (String argument : additionalArguments) {
				url.append("&" + argument);
			}
		}
		finalUrl.set(url.toString());
				
        // get the page speed data
		Logger.logMessage("Getting Google Page Speed data with request '" + finalUrl.get() + "'.");
		HttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(finalUrl.get());

        String failureMsg = "Failed to get Google Page Speed data with request '" + finalUrl.get() + "'.";
        HttpResponse response = null;
        try {
        	response = httpclient.execute(request);
        } catch (Exception e) {
        	Logger.logConsoleMessage(failureMsg);
        	e.printStackTrace();
        }
        
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            Logger.logConsoleMessage(failureMsg);
            Logger.logConsoleMessage(response.getStatusLine().getReasonPhrase());
        } else {
        	try {
            	InputStream input = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                input.close();
                
                String result = sb.toString();
                results.set(result);
            } catch (Exception e) {
            	Logger.logConsoleMessage(failureMsg);
            	e.printStackTrace();
            }
        }
        
        String resultObj = failureMsg;
        try {
        	resultObj = parseResultToJson(results.get(), strategy);
        } catch (Exception e) {
        	Logger.logConsoleMessage(failureMsg);
        	e.printStackTrace();
        }
        
        return resultObj;
	}
	
	public static void postResultToSplunk(String splunkIndex, String resultJSON) {
		Logger.logConsoleMessage("Posting Google Page Speed results to Splunk for request '" + finalUrl.get() + "'.");
//    	new SplunkManager().connectToSplunk().setIndex(splunkIndex).postEvent(resultJSON.replaceAll("\\s+",""));
	}
	
	@SuppressWarnings("unchecked")
	private static String parseResultToJson(String result, String strategy) throws Exception {
		JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(result);
        JSONObject ruleObj = (JSONObject) obj.get("ruleGroups");
        JSONObject speedObj = (JSONObject) ruleObj.get("SPEED");
        
        
        JSONObject jsonToPost = new JSONObject();
		jsonToPost.put("postType", "googlePageSpeed");
		jsonToPost.put("strategyType", strategy);
		jsonToPost.put("requestUrl", obj.get("id"));
		jsonToPost.put("responseCode", obj.get("responseCode"));
		jsonToPost.put("speedScore", speedObj.get("score"));
        
		return jsonToPost.toJSONString();
	}
	
}