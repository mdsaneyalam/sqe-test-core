package com.softech.test.core.props;

/**
 * The possible mobile OS targets for execution.
 */
public enum GatewayIP {
	
  LAB_01 (com.softech.test.core.util.Constants.LAB_01_GATEWAY_IP),
  LAB_02 (com.softech.test.core.util.Constants.LAB_02_GATEWAY_IP);

  private final String value;

  private GatewayIP(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static GatewayIP getEnumByString(String value) {
      for (GatewayIP mobileOS : GatewayIP.values()) {
    	  if (value.equals(mobileOS.value)) {
        	 return mobileOS;
          }
      }
      return null;
  }
  
}
