package com.softech.test.core.util;

import java.util.Arrays;
import java.util.List;

public class HueUtils {

	private static final String HUE_HUB_IP = "10.15.17.9";
	private static final String HUE_API_KEY = "0PXrC6zk-3-j5NzsjjzG50N6wDI9LgsG8AqhcYYd";
	
	public static List<Integer> getHueLightIds() {
		return Arrays.asList(1, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 
			19, 20, 21, 22, 23, 24, 25, 26, 27, 28);
	}
	
	public static Integer getRandomHueColor() {
		List<String> hueColors = Arrays.asList("25717", "12750", "25500", "46920", "56100", "65280");
		return Integer.parseInt(hueColors.get(RandomData.getInteger(0, hueColors.size())));
	}
	
	public static Integer getRandomLightId() {
		List<Integer> hueLights = getHueLightIds();
		return hueLights.get(RandomData.getInteger(0, hueLights.size()));
	}
	
	public static String getHueHubIP() {
		return HUE_HUB_IP;
	}
	
	public static String getHueApiKey() {
		return HUE_API_KEY;
	}
	
}
