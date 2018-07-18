//package com.softech.test.core.report;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.softech.test.core.lab.GridManager;
//import com.softech.test.core.util.Constants;
//import com.softech.test.core.util.Logger;
//import com.splunk.Args;
//import com.splunk.HttpService;
//import com.splunk.SSLSecurityProtocol;
//import com.splunk.Service;
//import com.splunk.ServiceArgs;
//
//public class SplunkManager {
//	
//	private Service service = null;
//	private com.splunk.Index index = null;
//	
//	private static ThreadLocal<Boolean> splunkConnectSuccess = new ThreadLocal<Boolean>() {
//    	protected Boolean initialValue() {
//    		return false;
//    	}
//    };
//	
//	/**********************************************************************************************
//     * Sets the username, password, and host/port for the splunk instance.
//     * 
//     * @author Saney Alam created March 23, 2018
//     * @version 1.0.1 May 29, 2017
//     ***********************************************************************************************/
//	public SplunkManager connectToSplunk() {
//		if (GridManager.isQALabHub()) {
//			try {
//				// set the username, password, and host/port values
//				ServiceArgs loginArgs = new ServiceArgs();
//				loginArgs.setUsername(Constants.SPLUNK_LAB_USERNAME);
//				loginArgs.setPassword(Constants.SPLUNK_LAB_PASSWORD);
//				loginArgs.setHost(getSplunkHost());
//				loginArgs.setPort(Integer.parseInt(Constants.SPLUNK_LAB_PORT));
//				
//				// login to the splunk instance and set the splunk service
//				HttpService.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
//				service = Service.connect(loginArgs);
//				splunkConnectSuccess.set(true);
//			} catch (Exception e) {
//				Logger.logConsoleMessage("Failed to connect to splunk.");
//				Logger.logConsoleMessage(e.getMessage());
//			}
//		} else {
//			Logger.logConsoleMessage("Execution is local - not attempting to connect/post to splunk.");
//			splunkConnectSuccess.set(false);
//		}
//		
//		return this;
//	}
//	
//	/**********************************************************************************************
//     * Sets the index of the splunk instance you wish to post data to.
//     * 
//     * @param indexName - {@link String} - The name of the index to post to i.e. "videoplayer".
//     * @author Saney Alam created March 23, 2018
//     * @version 1.0.0 March 23, 2018
//     ***********************************************************************************************/
//	public SplunkManager setIndex(String indexName) {
//		if (GridManager.isQALabHub()) {
//			try {
//				// set the index you wish to post data to
//				if (splunkConnectSuccess.get()) {
//				    index = service.getIndexes().get(indexName);
//				}
//			} catch (Exception e) {
//				Logger.logConsoleMessage("Failed to get splunk index '" + indexName + "'.");
//				Logger.logConsoleMessage(e.getMessage());
//			}
//		}
//		
//		return this;
//	}
//	
//	/**********************************************************************************************
//     * Posts a fully constructed json block to splunk as a splunk event. NOTE - all link breaks are trimmed
//     * from the event body automatically to prevent problematic event references in splunk.
//     * 
//     * @param eventJson - {@link String} - A fully constructed and valid json object.
//     * @author Saney Alam created March 23, 2018
//     * @version 1.0.0 March 23, 2018
//     ***********************************************************************************************/
//	public SplunkManager postEvent(String eventJson) {
//		if (GridManager.isQALabHub()) {
//			Integer splunkMaxSizeByte = Integer.parseInt(System.getenv("SPLUNK_MAX_POST_SIZE_BYTES"));
//			
//			if (isJSONValid(eventJson) && splunkConnectSuccess.get()) {
//				// strip out extraneous space
//				String eventJsonClean = eventJson.replace("\n", "").replace("\r", "");
//				
//				// get the size of the post
//				Integer sizeOfPost = eventJsonClean.getBytes().length;
//				Logger.logConsoleMessage("Requesting splunk post with event size: " + sizeOfPost);
//				
//				if (sizeOfPost <= splunkMaxSizeByte) {
//					try {
//						// post the json formatted event
//						Args eventArgs = new Args();
//						eventArgs.put("sourcetype", "json");
//						index.submit(eventArgs, eventJsonClean);
//					} catch (Exception e) {
//						Logger.logConsoleMessage("Failed to post event to splunk.");
//						Logger.logConsoleMessage(e.getMessage());
//					}
//				} else {
//				    Logger.logConsoleMessage("Splunk post was not completed as splunk event request exceeded "
//				    	+ "max size of: " + splunkMaxSizeByte.toString());
//				}
//			} else {
//				Logger.logConsoleMessage("The splunk json request is not valid json.");
//			}
//		}
//		
//		return this;
//	}
//	
//	private boolean isJSONValid(String json) {
//	    try {
//	        new JSONObject(json);
//	        return true;
//	    } catch (JSONException objectException) {
//	        try {
//	            new JSONArray(json);
//	            return true;
//	        } catch (JSONException arrayException) {
//	            return false;
//	        }
//	    }
//	}
//	
//	private String getSplunkHost() {
//		return System.getenv("SPLUNK_IP");
//	}
//	
//}
