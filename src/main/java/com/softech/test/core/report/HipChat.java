package com.softech.test.core.report;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.softech.test.core.lab.GridManager;
import com.softech.test.core.util.Logger;

public class HipChat {

	/**********************************************************************************************
     * Sends a hipchat message to a room with a desired color.
     * 
     * @param String chatRoomURL - {@link String} - The chatroom hook url to send the chat message to.
     * @param String htmlMessage - {@link String} - The simple html formatted message to send.
     * @param String color - {@link String} - The color to set the message to (yellow, red, green, etc).
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     ***********************************************************************************************/
	public static void sendChat(String chatRoomURL, String htmlMessage, String color) {
		if (!GridManager.isEC2Agent()) {
			Logger.logConsoleMessage("Not sending HipChat notification as the execution is not running on the MQE lab.");
			return;
		}
		
    	String payload = "{\"color\":\"" + color + "\",\"message\":\"" + htmlMessage + "\""
        		+ ",\"notify\":false,\"message_format\":\"html\"}";
        String failureMsg = "Failed to send report to HipChat Room '" + chatRoomURL + ".";
    	
        try {
    		HttpClient httpclient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(chatRoomURL);

            StringEntity entity = new StringEntity(payload);
        	request.addHeader("content-type", "application/json");
            request.setEntity(entity);

            HttpResponse response = httpclient.execute(request);
        	if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                Logger.logConsoleMessage(failureMsg);
                Logger.logConsoleMessage(response.getStatusLine().getReasonPhrase());
            } else {
                Logger.logConsoleMessage("Successfully sent chat report to HipChat Room '" + chatRoomURL + "'.");
            }
    	} catch (Exception e) {
    		Logger.logConsoleMessage(failureMsg);
    		e.printStackTrace();
    	}
    }

}