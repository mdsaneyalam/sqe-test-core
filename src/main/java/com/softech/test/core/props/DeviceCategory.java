package com.softech.test.core.props;

/**
 * The possible device categories for execution.
 */
public enum DeviceCategory {
	
  PHONE ("Phone"),
  TABLET ("Tablet");

  private final String value;

  private DeviceCategory(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
}
