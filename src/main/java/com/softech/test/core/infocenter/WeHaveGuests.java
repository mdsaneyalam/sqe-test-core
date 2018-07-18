package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.util.SleepUtils;

public class WeHaveGuests {

	public static void main(String[] args) {
		
		CommandExecutor.execCommand("say 'Welcome, to the Quality Engineering Lab!'", null, null);
		SleepUtils.sleep(300);
		CommandExecutor.execCommand("say 'I am the Info Center, your lab digital assistant'", null, null);
		SleepUtils.sleep(400);
		CommandExecutor.execCommand("say 'How can I help you?'", null, null);
		SleepUtils.sleep(300);
		CommandExecutor.execCommand("say 'you can say things like...'", null, null);
		SleepUtils.sleep(300);
		CommandExecutor.execCommand("say 'Get Lab Status'", null, null);
		SleepUtils.sleep(300);
		CommandExecutor.execCommand("say 'or'", null, null);
		SleepUtils.sleep(300);
		CommandExecutor.execCommand("say 'Get Device Status'", null, null);
		SleepUtils.sleep(500);
		CommandExecutor.execCommand("say 'I am here to help, my phenominal digital mastery is brought to you by your friends in quality engineering'", null, null);
		
	}

}
