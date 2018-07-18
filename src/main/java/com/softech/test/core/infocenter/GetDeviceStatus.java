package com.softech.test.core.infocenter;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.SleepUtils;

public class GetDeviceStatus {

	public static void main(String[] args) {
		
		String psql = Constants.PSQL_PATH;
		String host = Constants.MQE_LAB_DB_IP;
		String db = Constants.MQE_LAB_DB_NAME;

		String iosTotalCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c 'SELECT COUNT(*) "
				+ "FROM iosdevices'", null);
		CommandExecutor.execCommand("say There are " + iosTotalCount.trim() + " iOS devices on the lab", null, null);
		SleepUtils.sleep(500);
		
		String iosHealthyCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT COUNT(*) "
				+ "FROM iosdevices where device_status = 'active'\"", null);
		CommandExecutor.execCommand("say " + iosHealthyCount.trim() + " of the iOS devices are healthy and ready for tests", null, null);
		SleepUtils.sleep(500);

		String androidTotalCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT COUNT(*) "
				+ "FROM androiddevices\"", null);
		CommandExecutor.execCommand("say There are " + androidTotalCount.trim() + " Android devices on the lab", null, null);
		SleepUtils.sleep(500);
		
		String androidHealthyCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT COUNT(*) "
				+ "FROM androiddevices where device_status = 'active'\"", null);
		CommandExecutor.execCommand("say " + androidHealthyCount.trim() + " of the Android devices are healthy and ready for tests", null, null);
		SleepUtils.sleep(500);
		
		String machineTotalCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT COUNT(*) "
				+ "FROM agentmachines\"", null);
		CommandExecutor.execCommand("say There are " + machineTotalCount.trim() + " agent machines on the lab", null, null);
		SleepUtils.sleep(500);
		
		String machineHealthyCount = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT COUNT(*) "
				+ "FROM agentmachines where machine_status = 'active'\"", null);
		CommandExecutor.execCommand("say " + machineHealthyCount.trim() + " of the agent machines are healthy and ready for tests", null, null);
		
	}

}

