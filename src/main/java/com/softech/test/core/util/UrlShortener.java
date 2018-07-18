package com.softech.test.core.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.softech.test.core.util.Logger;

public class UrlShortener {

	public static String getShortUrl(String userKey, String urlToShorten) {
        String googleUrl = "https://www.googleapis.com/urlshortener/v1/url?key="+ userKey;
        String tinyUrl = null;
        
        HttpResponse response = null;
        String failureMsg = "Failed to get shortened url.";
        
        try {
        	// get the tiny url
    		HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(googleUrl);
            
            String payload = "{\"longUrl\":\"" + urlToShorten + "\"}";
            StringEntity entity = new StringEntity(payload);
        	post.addHeader("content-type", "application/json");
            post.setEntity(entity);
            
            response = httpclient.execute(post);
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
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(result);
                tinyUrl = json.get("id").toString();
            } catch (Exception e) {
            	Logger.logConsoleMessage(failureMsg);
            	e.printStackTrace();
            }
        }
        
        return tinyUrl;
	}
	
}