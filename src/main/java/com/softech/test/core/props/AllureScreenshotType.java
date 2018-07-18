package com.softech.test.core.props;

public enum AllureScreenshotType {
	
  FAILURE ("Failure Screenshot"),
  SUCCESS ("Success Screenshot"),
  IN_PROGRESS ("In Progress Screenshot");

  private final String value;

  private AllureScreenshotType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
  
}
