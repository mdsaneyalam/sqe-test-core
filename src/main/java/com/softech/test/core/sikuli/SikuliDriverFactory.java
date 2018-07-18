package com.softech.test.core.sikuli;

import org.sikuli.script.Screen;

public class SikuliDriverFactory {

	private static ThreadLocal<Screen> sikuliDriver = new ThreadLocal<Screen>();
	
	public static void initSikuliDriver() {
		sikuliDriver.set(new Screen());
		SikuliDriverManager.setSikuliDriver(sikuliDriver.get());
		
		org.sikuli.basics.Settings.OcrTextRead = true;
		org.sikuli.basics.Settings.OcrTextSearch = true;
	}
	
}
