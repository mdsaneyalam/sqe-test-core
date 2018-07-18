package com.softech.test.core.props;

/**
 * The possible mobile OS targets for execution.
 */
public enum MobileOS {
	
  ANDROID ("Android"),
  ANDROID_SIM ("Android_Sim"),
  IOS ("iOS"),
  IOS_SIM ("iOS_Sim");

  private final String value;

  private MobileOS(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static MobileOS getEnumByString(String value) {
      for (MobileOS mobileOS : MobileOS.values()) {
    	  if (value.equals(mobileOS.value)) {
        	 return mobileOS;
          }
      }
      return null;
  }
  
}
