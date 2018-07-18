package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;

public class ResetLabLights {

	public static void main(String[] args) {
		
		CommandExecutor.execCommand("say 'Resetting the lab hue lights now.'", null, null);
		CommandExecutor.execCommand("open /Users/mqeadmin/InfoCenter/ResetHueLights.command", null, null);

	}

}
