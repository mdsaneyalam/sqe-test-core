package com.softech.test.core.props;

public enum BrowserType {
	
  CHROME ("chrome"),
  FIREFOX ("firefox"),
  SAFARI ("safari"),
  IEXPLORE ("internet explorer"),
  EDGE ("MicrosoftEdge");

  private final String value;

  private BrowserType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static BrowserType getEnumByString(String value) {
      for (BrowserType browserType : BrowserType.values()) {
    	  if (value.equals(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
}
