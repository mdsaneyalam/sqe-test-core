package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;

public class ItsPartyTime {

	public static void main(String[] args) {
		
		CommandExecutor.execCommand("say 'Getting the party started.'", null, null);
		CommandExecutor.execCommand("open /Users/mqeadmin/InfoCenter/PlaySafetyDance.command", null, null);
		CommandExecutor.execCommand("open /Users/mqeadmin/InfoCenter/HuePartyMode.command", null, null);
		
	}

}
