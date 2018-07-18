package com.softech.test.core.sikuli;

import org.sikuli.script.Screen;

public class SikuliDriverManager {

	private static ThreadLocal<Screen> sikuliDriver = new ThreadLocal<Screen>();
    
	public static synchronized void setSikuliDriver(Screen driver) {
        sikuliDriver.set(driver);
    }
    
    public static synchronized Screen getSikuliDriver() {
        return sikuliDriver.get();
    }
	
}
