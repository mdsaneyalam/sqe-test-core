package com.softech.test.core.infocenter;

import java.util.Arrays;
import java.util.List;

import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.SleepUtils;

public class GetUnhealthyDevices {

	public static void main(String[] args) {
		
		String psql = Constants.PSQL_PATH;
		String host = Constants.MQE_LAB_DB_IP;
		String db = Constants.MQE_LAB_DB_NAME;

		List<String> unhealthyiOSDevices = Arrays.asList(CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT device_id "
				+ "FROM iosdevices where device_status = 'inactive'\"", null).split(" "));
		if (unhealthyiOSDevices.size() > 1) {
			CommandExecutor.execCommand("say 'The following iOS devices are unhealthy and requirement maintenance.", null, null);
			for (String unhealthyDevice : unhealthyiOSDevices) {
				if (unhealthyDevice.length() > 5) {
					String machineIP = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT machine_ip FROM iosdevices "
							+ "where device_id = '" + unhealthyDevice.trim() + "'\"", null);
					String machineName = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT machine_name FROM "
							+ "agentmachines where machine_ip = '" + machineIP.trim() + "'\"", null);
					String deviceName = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT device_name FROM "
							+ "iosdevices where device_id = '" + unhealthyDevice.trim() + "'\"", null);
					CommandExecutor.execCommand("say 'The " + deviceName + " on mac mini " + machineName.split("_")[1] + "'", null, null);
					SleepUtils.sleep(100);
				}
			}
		} else {
			CommandExecutor.execCommand("say 'There are no unhealthy iOS devices on the lab.", null, null);
		}
		
		List<String> unhealthyAndroidDevices = Arrays.asList(CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT device_id "
				+ "FROM androiddevices where device_status = 'inactive'\"", null).split(" "));
		if (unhealthyAndroidDevices.size() > 1) {
			CommandExecutor.execCommand("say 'The following Android devices are unhealthy and requirement maintenance.", null, null);
			for (String unhealthyDevice : unhealthyAndroidDevices) {
				if (unhealthyDevice.length() > 4) {
					String machineIP = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT machine_ip FROM androiddevices "
							+ "where device_id = '" + unhealthyDevice.trim() + "'\"", null);
					String machineName = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT machine_name FROM "
							+ "agentmachines where machine_ip = '" + machineIP.trim() + "'\"", null);
					String deviceName = CommandExecutor.execMultiCommand(psql + " -h " + host + " -U postgres -d " + db + " -t -c \"SELECT device_name FROM "
							+ "androiddevices where device_id = '" + unhealthyDevice.trim() + "'\"", null);
					CommandExecutor.execCommand("say 'The " + deviceName + " on mac mini " + machineName.split("_")[1] + "'", null, null);
					SleepUtils.sleep(100);
				}
			}
		} else {
			CommandExecutor.execCommand("say 'There are no unhealthy Android devices on the lab.", null, null);
		}
		
	}

}

