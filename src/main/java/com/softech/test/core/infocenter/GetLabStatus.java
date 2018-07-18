package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.util.SleepUtils;

public class GetLabStatus {

	public static void main(String[] args) {
		
		CommandExecutor.execCommand("say 'Checking the status of Jenkins and the agents now.'", null, null);
		String response = CommandExecutor.execCommand("curl --silent http://mqe.ad.viacom.com:8080/computer/api/json", null, null);
		
		SleepUtils.sleep(500);
		if (response.contains("\"offline\":true")) {
			CommandExecutor.execCommand("say 'Jenkins or one of its agents is off line, contact an MQE administrator immediately'", null, null);
		} else if (response.contains("\"offline\":false")) {
			CommandExecutor.execCommand("say 'Jenkins and all agent machines are on line and accepting tests!'", null, null);
		} else {
			CommandExecutor.execCommand("say 'Jenkins or one of its agents is off line, contact an MQE administrator immediately'", null, null);
		}
		
	}

}
