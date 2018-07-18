package com.softech.test.core.driver;

import io.appium.java_client.android.AndroidDriver;

import java.net.URL;

import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteTouchScreen;

@SuppressWarnings("rawtypes")
public class SelendroidDriver extends AndroidDriver implements HasTouchScreen {

	private RemoteTouchScreen touch = null;
	
	public SelendroidDriver(URL url, DesiredCapabilities capabilities) {
		super(url, capabilities);
		touch = new RemoteTouchScreen(getExecuteMethod());
	}

	public TouchScreen getTouch() {
		return touch;
	}

}
