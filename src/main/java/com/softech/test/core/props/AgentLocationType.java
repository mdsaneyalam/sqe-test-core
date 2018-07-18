package com.softech.test.core.props;

public enum AgentLocationType {
	
  NEW_YORK_1515 ("1515 New York"),
  HAUPPAUGE_NEW_YORK ("Hauppauge New York"),
  EC2_EAST_1 ("us-east-1b"),
  BERLIN ("Berlin"),
  WARSAW ("Warsaw"),
  MILAN ("Milan");

  private final String value;

  private AgentLocationType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static AgentLocationType getEnumByString(String value) {
      for (AgentLocationType osType : AgentLocationType.values()) {
    	  if (value.equals(osType.value)) {
        	 return osType;
          }
      }
      return null;
  }
  
}
