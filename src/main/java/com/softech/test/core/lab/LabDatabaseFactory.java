package com.softech.test.core.lab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;

public class LabDatabaseFactory {

	// TODO - a lot of redundant ugliness in this class - refactor as time allows
    public static List<String> getResults(String query) {
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> allResults = new ArrayList<String>();
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            if (!query.toLowerCase().contains("select")) {
            	statement.executeUpdate(query);
            } else {
            	resultSet = statement.executeQuery(query);
            }
            
            if (resultSet != null) {
            	while (resultSet.next()) {
                	allResults.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return allResults;
    }
    
    public static HashMap<String, String> getIOSDeviceInfo(String deviceID) {
    	HashMap<String, String> deviceInfo = new HashMap<String, String>();
    	String query = "select machine_ip, device_category, device_proxy_port, device_code, device_id, device_name, device_os_version, device_status, "
    			+ "device_in_use, unhealthy_during_test from iosdevices where device_id = '" + deviceID + "'";
    	
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            if (resultSet != null) {
            	while (resultSet.next()) {
            		deviceInfo.put("machine_ip", resultSet.getString(1));
            		deviceInfo.put("device_category", resultSet.getString(2));
            		deviceInfo.put("device_proxy_port", resultSet.getString(3));
            		deviceInfo.put("device_code", resultSet.getString(4));
            		deviceInfo.put("device_id", resultSet.getString(5));
            		deviceInfo.put("device_name", resultSet.getString(6));
            		deviceInfo.put("device_os_version", resultSet.getString(7));
            		deviceInfo.put("device_status", resultSet.getString(8));
            		deviceInfo.put("device_in_use", String.valueOf(resultSet.getBoolean(9)));
            		deviceInfo.put("unhealthy_during_test", String.valueOf(resultSet.getBoolean(10)));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return deviceInfo;
    }
    
    public static HashMap<String, String> getAndroidDeviceInfo(MobileOS mobileOS, String deviceID) {
    	HashMap<String, String> deviceInfo = new HashMap<String, String>();
    	String query = "select machine_ip, device_category, device_proxy_port, device_id, device_name, device_os_version, device_status, "
    			+ "device_in_use, unhealthy_during_test from " + getOSTable(mobileOS) + " where device_id = '" + deviceID + "'";
    	
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            if (resultSet != null) {
            	while (resultSet.next()) {
            		deviceInfo.put("machine_ip", resultSet.getString(1));
            		deviceInfo.put("device_category", resultSet.getString(2));
            		deviceInfo.put("device_proxy_port", resultSet.getString(3));
            		deviceInfo.put("device_id", resultSet.getString(4));
            		deviceInfo.put("device_name", resultSet.getString(5));
            		deviceInfo.put("device_os_version", resultSet.getString(6));
            		deviceInfo.put("device_status", resultSet.getString(7));
            		deviceInfo.put("device_in_use", String.valueOf(resultSet.getBoolean(8)));
            		deviceInfo.put("unhealthy_during_test", String.valueOf(resultSet.getBoolean(9)));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return deviceInfo;
    }
    
    public static HashMap<String, String> getRokuDeviceInfo(String deviceID) {
    	HashMap<String, String> deviceInfo = new HashMap<String, String>();
    	String query = "select device_id, device_ip, device_username, device_password, harmony_device_id, device_proxy_port, device_os_version, device_status, "
    			+ "device_in_use from rokudevices where device_id = '" + deviceID + "'";
    	
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            if (resultSet != null) {
            	while (resultSet.next()) {
            		deviceInfo.put("device_id", resultSet.getString(1));
            		deviceInfo.put("device_ip", resultSet.getString(2));
            		deviceInfo.put("device_username", resultSet.getString(3));
            		deviceInfo.put("device_password", resultSet.getString(4));
            		deviceInfo.put("harmony_device_id", resultSet.getString(5));
            		deviceInfo.put("device_proxy_port", resultSet.getString(6));
            		deviceInfo.put("device_os_version", resultSet.getString(7));
            		deviceInfo.put("device_status", resultSet.getString(8));
            		deviceInfo.put("device_in_use", String.valueOf(resultSet.getBoolean(9)));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return deviceInfo;
    }
    
    public static HashMap<String, String> getAppleTVDeviceInfo(String deviceID) {
    	HashMap<String, String> deviceInfo = new HashMap<String, String>();
    	String query = "select machine_ip, device_id, harmony_device_id, device_proxy_port, device_os_version, device_status, "
    			+ "device_in_use from appletvdevices where device_id = '" + deviceID + "'";
    	
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            if (resultSet != null) {
            	while (resultSet.next()) {
            		deviceInfo.put("machine_ip", resultSet.getString(1));
            		deviceInfo.put("device_id", resultSet.getString(2));
            		deviceInfo.put("harmony_device_id", resultSet.getString(5));
            		deviceInfo.put("device_proxy_port", resultSet.getString(6));
            		deviceInfo.put("device_os_version", resultSet.getString(7));
            		deviceInfo.put("device_status", resultSet.getString(8));
            		deviceInfo.put("device_in_use", String.valueOf(resultSet.getBoolean(9)));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return deviceInfo;
    }
    
    public static HashMap<String, String> getFireTVDeviceInfo(String deviceID) {
    	HashMap<String, String> deviceInfo = new HashMap<String, String>();
    	String query = "select machine_ip, device_id, harmony_device_id, device_proxy_port, device_os_version, device_status, "
    			+ "device_in_use from firetvdevices where device_id = '" + deviceID + "'";
    	
    	Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = connectToDB();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            
            if (resultSet != null) {
            	while (resultSet.next()) {
            		deviceInfo.put("machine_ip", resultSet.getString(1));
            		deviceInfo.put("device_id", resultSet.getString(2));
            		deviceInfo.put("harmony_device_id", resultSet.getString(5));
            		deviceInfo.put("device_proxy_port", resultSet.getString(6));
            		deviceInfo.put("device_os_version", resultSet.getString(7));
            		deviceInfo.put("device_status", resultSet.getString(8));
            		deviceInfo.put("device_in_use", String.valueOf(resultSet.getBoolean(9)));
                }
            }
        } catch (SQLException e) {
            Logger.logConsoleMessage("Failed to query lab database with query '" + query + "'.");
            e.printStackTrace();
        } finally {
        	try {
        		if (connection != null) {
        			connection.close();
                	statement.close();
                	if (resultSet != null) {
                	    resultSet.close();
                	}
        		}
        	} catch (SQLException e) {
        	    Logger.logConsoleMessage("Failed to close database cons");
        	    e.printStackTrace();
        	}
        	
        }
        
        return deviceInfo;
    }
    
    public static String getOSTable(MobileOS mobileOS) {
    	if (mobileOS.equals(MobileOS.IOS)) {
    		return Constants.MQE_LAB_DB_IOS_DEVICES;
    	} else if (mobileOS.equals(MobileOS.ANDROID)) {
    		return Constants.MQE_LAB_DB_ANDROID_DEVICES;
    	} else if (mobileOS.equals(MobileOS.IOS_SIM)) {
    		return Constants.MQE_LAB_DB_IOS_SIMULATORS;
    	} 
    	return Constants.MQE_LAB_DB_ANDROID_SIMULATORS;
    }
    
    public static String getOSTable(EmergingOS emergingOS) {
    	if (emergingOS.equals(EmergingOS.APPLE_TV)) {
    		return Constants.MQE_LAB_DB_APPLE_TV_DEVICES;
    	} else if (emergingOS.equals(EmergingOS.ROKU)) {
    		return Constants.MQE_LAB_DB_ROKU_DEVICES;
    	}
    	return null; // TODO - more device types for emerging os coming
    }
    
    private static String getUrl() {
    	String ip = System.getenv("PSQL_IP");
    	String url = "jdbc:postgresql://" + ip + "/" + Constants.MQE_LAB_DB_NAME;
        
        return url;
    }
    
    private static Connection connectToDB() {
    	String url = getUrl();
        String user = Constants.MQE_LAB_DB_USER;
        String password = "";
    	Connection connection = null;
    	for (int i = 1; i < 4; i++) {
    		try {
    			connection = DriverManager.getConnection(url, user, password);
    			break;
    		} catch (Exception e) {
    			Logger.logConsoleMessage("MQE PSQL lab db connection failed on attempt '" +  i + "'. Retrying...");
    			if (i == 1) {
    				try { Thread.sleep(RandomData.getInteger(250, 500)); } catch (InterruptedException e1) { } // network might be slightly busy
    			} else if (i == 2) {
    				try { Thread.sleep(RandomData.getInteger(500, 5000)); } catch (InterruptedException e1) { } // network might be busy
    			} else {
    				try { Thread.sleep(RandomData.getInteger(5000, 10000)); } catch (InterruptedException e1) { } // network might be VERY busy
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	return connection;
    }
    
}
