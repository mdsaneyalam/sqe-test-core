package com.softech.test.core.props;

public enum ProxyType {
	
  BMP ("BrowserMobProxy"),
  BMP_REST ("BrowserMobProxyREST");

  private final String value;

  private ProxyType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static ProxyType getEnumByString(String value) {
      for (ProxyType osType : ProxyType.values()) {
    	  if (value.equals(osType.value)) {
        	 return osType;
          }
      }
      return null;
  }
  
}
