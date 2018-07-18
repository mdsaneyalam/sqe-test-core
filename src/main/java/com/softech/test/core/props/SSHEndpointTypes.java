package com.softech.test.core.props;

public enum SSHEndpointTypes {
	
  ABSOLUTE ("Absolute"), // executed against target @ipaddress without any ssh string manipulation
  EC2_AGENT ("EC2Agent"), // executed direct on the ec2 agent
  HUB_AGENT ("HubAgent"), // sent from ec2 agent to hub agent (mac pro) and executed
  TEST_AGENT ("TestAgent"); // ssh hop from ec2 to the hub agent then to the test agent (mac mini or windows)

  private final String value;

  private SSHEndpointTypes(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
  public static SSHEndpointTypes getEnumByString(String value) {
      for (SSHEndpointTypes browserType : SSHEndpointTypes.values()) {
    	  if (value.equals(browserType.value)) {
        	  return browserType;
          }
      }
      return null;
  }
  
}
