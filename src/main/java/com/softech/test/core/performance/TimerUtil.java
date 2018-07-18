package com.softech.test.core.performance;

public class TimerUtil {

	private static ThreadLocal<Long> timerStart = new ThreadLocal<Long>();
	private static ThreadLocal<Long> timerEnd = new ThreadLocal<Long>();
	
	public static void startTimer() {
		timerStart.set(System.currentTimeMillis());
	}
	
	public static void stopTimer() {
		timerEnd.set(System.currentTimeMillis());
	}
	
	public static Long getTime() {
		return (timerEnd.get() - timerStart.get());
	}
}