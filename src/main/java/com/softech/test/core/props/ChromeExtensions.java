package com.softech.test.core.props;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.softech.test.core.util.Logger;

public enum ChromeExtensions {
	
  ADGUARD_AD_BLOCKER ("AdGuard_AdBlocker");
  private final String value;

  private ChromeExtensions(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static String getExtensionPath(DesktopOSType desktopOS, ChromeExtensions chromeExtension) {
	  String path = desktopOS.equals(DesktopOSType.MQE_MAC) ? System.getenv("MAC_CHROME_EXTENSION_DIR") 
			  : System.getenv("WIN_CHROME_EXTENSION_DIR");
	  return path + chromeExtension.value;
  }
  
  public static ChromeExtensions getEnumByString(String value) {
      for (ChromeExtensions browserType : ChromeExtensions.values()) {
    	  if (value.equals(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
  public static DesiredCapabilities applyChromeExtension(DesiredCapabilities capabilities, DesktopOSType desktopOS) {
	  String extension = (String) capabilities.getCapability(MQEDriverCaps.MQE_CHROME_EXTENSION.value());
	  if (extension != null) {
		  Logger.logMessage("Install chrome extension '" + extension + "'.");
		  // apply the extension
		  ChromeExtensions extensions = ChromeExtensions.getEnumByString(extension);
		  ChromeOptions options = new ChromeOptions();
          options.addArguments("load-extension=" + ChromeExtensions.getExtensionPath(desktopOS, extensions));
          capabilities.setCapability(ChromeOptions.CAPABILITY, options);
	  }
	
	  return capabilities;
  }
  
}
