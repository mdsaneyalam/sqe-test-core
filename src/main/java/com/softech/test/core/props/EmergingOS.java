package com.softech.test.core.props;

/**
 * The possible emerging OS targets for execution.
 */
public enum EmergingOS {
	
  APPLE_TV ("appletv"),
  ROKU ("roku"),
  CHROMECAST ("chromecast"),
  FIRE_TV ("firetv");

  private final String value;

  private EmergingOS(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
}
