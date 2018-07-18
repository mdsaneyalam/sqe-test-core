package com.softech.test.core.props;

public enum MQEDriverCaps {
	
  MQE_CHROME_EXTENSION ("MQEChromeExtension"),
  MQE_PROXY_TYPE ("MQEProxyType"),
  MQE_INTL_LOCATION ("MQEIntlLocation"),
  MQE_NO_MITM ("MQENoMITM"),
  MQE_IOS_RESIGN_PROVISION ("MQEIOSResignProvision"),
  MQE_IOS_RESIGN_BUNDLE_ID ("MQEIOSResignBundleId"),
  MQE_IOS_NATIVE_AUTO_ACCEPT_ALERTS ("MQEiOSNativeAutoAcceptAlerts");
  	
  private final String value;

  private MQEDriverCaps(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static MQEDriverCaps getEnumByString(String value) {
      for (MQEDriverCaps browserType : MQEDriverCaps.values()) {
    	  if (value.equals(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
}
