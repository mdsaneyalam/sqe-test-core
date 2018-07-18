package com.softech.test.core.util;

public class SleepUtils {

	public static void sleep(Integer sleepInMS) {
		try {
			Thread.sleep(sleepInMS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
