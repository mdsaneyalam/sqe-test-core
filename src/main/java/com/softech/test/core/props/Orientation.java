package com.softech.test.core.props;

public enum Orientation {
  
    LANDSCAPE("landscape"), // android
    LANDSCAPE_RIGHT("4"), // ios
    LANDSCAPE_LEFT("3"), // ios
    PORTRAIT("portrait"), // android
    PORTRAIT_UPRIGHT("1"); // ios
    
    private final String value;

    private Orientation(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
    
}