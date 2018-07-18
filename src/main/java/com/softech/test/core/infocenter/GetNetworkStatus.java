package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;

public class GetNetworkStatus {

	public static void main(String[] args) {
		
		CommandExecutor.execCommand("say 'Checking the status of the Lab network now.'", null, null);
		String result = CommandExecutor.execCommand("ping -c 5 8.8.8.8", null, 15);
		
		if (result.contains("100.0% packet loss")) {
			CommandExecutor.execCommand("say 'The lab network is down, contact an MQE administrator immediately!", null, null);
		} else {
			CommandExecutor.execCommand("say 'The lab network is online, in good shape, and accepting tests!", null, null);
		}
		
	}

}
