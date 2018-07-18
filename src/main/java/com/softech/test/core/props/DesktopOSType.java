package com.softech.test.core.props;

public enum DesktopOSType {
	
  MQE_MAC ("MAC"),
  MQE_WINDOWS ("WINDOWS"),
  SAUCE_MAC_SIERRA ("macOS 10.12"),
  SAUCE_MAC_EL_CAPITAN ("OS X 10.11"),
  SAUCE_MAC_YOSEMITE ("OS X 10.10"),
  SAUCE_MAC_MAVERICKS ("OS X 10.9"),
  SAUCE_MAC_MOUNTAIN_LION ("OS X 10.8"),
  SAUCE_WINDOWS_10 ("Windows 10"),
  SAUCE_WINDOWS_8_POINT_1 ("Windows 8.1"),
  SAUCE_WINDOWS_7 ("Windows 7"),
  SAUCE_WINDOWS_XP ("Windows XP");

  private final String value;

  private DesktopOSType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static DesktopOSType getEnumByString(String value) {
      for (DesktopOSType osType : DesktopOSType.values()) {
    	  if (value.equals(osType.value)) {
        	 return osType;
          }
      }
      return null;
  }
  
}
