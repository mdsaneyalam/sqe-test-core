package com.softech.test.core.props;

public enum SCPDeployType {
	
  PUSH_FILE ("PushFile"), // push a file from local machine to remote machine
  PULL_FILE ("PullFile"); // pull a file from remote machine to local machine

  private final String value;

  private SCPDeployType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static SCPDeployType getEnumByString(String value) {
      for (SCPDeployType browserType : SCPDeployType.values()) {
    	  if (value.equals(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
}
