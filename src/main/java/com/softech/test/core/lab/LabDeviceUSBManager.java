package com.softech.test.core.lab;

import com.softech.test.core.util.Logger;

public class LabDeviceUSBManager {

	private static final String SUCCESS_INT = "65";

	public static Boolean enableUSBPort(String machineIP, Integer usbPortNumber) {
		Logger.logConsoleMessage("Enabling usb hub port '" + usbPortNumber + "' on '" + machineIP + "'.");
		String output = CommandExecutor.execCommand("java -jar " + getUSBControllerPath() + " -a " + usbPortNumber,
				machineIP, null);
		return output.contains(SUCCESS_INT);
	}

	public static Boolean disableUSBPort(String machineIP, Integer usbPortNumber) {
		Logger.logConsoleMessage("Disabling usb hub port '" + usbPortNumber + "' on '" + machineIP + "'.");
		String output = CommandExecutor.execCommand("java -jar " + getUSBControllerPath() + " -d " + usbPortNumber,
				machineIP, null);
		return output.contains(SUCCESS_INT);
	}

	private static String getUSBControllerPath() {
		return System.getenv("USB_CONTROLLER_PATH");
	}

}
