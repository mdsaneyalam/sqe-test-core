package com.softech.test.core.lab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
//import com.amazonaws.services.ec2.AmazonEC2Client;
//import com.amazonaws.services.ec2.model.RebootInstancesRequest;
//import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

public class EC2Manager {

	private static final String AWS_PATH = "/usr/local/bin/aws";
	private static Boolean isJmeterRun = false;
	
	public static void setJMeterEC2(Boolean isJmeter) {
		isJmeterRun = isJmeter;
	}
	
	public static Boolean isEC2Machine(String machineAddress) {
		String status = LabDatabaseFactory.getResults("select ec2_machine from " + getEC2Table()
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (status.contains("t")) {
			return true;
		}
		return false;
	}
	
	public static Boolean isEC2MachineOnline(String machineAddress) {
		String result = LabDatabaseFactory.getResults("select ec2_machine_status from " + getEC2Table()
				+ " where machine_ip = '" + machineAddress + "'").get(0);
		if (result == null) {
			return false;
		} else if (result.equals("online")) {
			return true;
		}
		return false;
	}
	
	public static void setEC2MachineOnline(String machineAddress) {
		LabDatabaseFactory.getResults("update " + getEC2Table() + " set ec2_machine_status = 'online' "
				+ "where machine_ip = '" + machineAddress + "'");
	}
	
	public static  void setEC2MachineOffline(String machineAddress) {
		LabDatabaseFactory.getResults("update " + getEC2Table() + " set ec2_machine_status = 'offline' "
				+ "where machine_ip = '" + machineAddress + "'");
	}
	
	public static void setEC2MachineOnlineStartTime(String machineAddress, Long startTime) {
		LabDatabaseFactory.getResults("update " + getEC2Table() + " set ec2_machine_online_start = " + startTime.toString()
		    + " where machine_ip = '" + machineAddress + "'");
	}
	
	public static synchronized Long getEC2MachineOnlineStartTime(String machineAddress) {
		String result = LabDatabaseFactory.getResults("select ec2_machine_online_start from " + getEC2Table() + " where machine_ip = '" 
	        + machineAddress + "'").get(0);
		return Long.parseLong(result);
	}
	
	public static synchronized String getEC2MachineInstanceId(String machineAddress) {
		String result = LabDatabaseFactory.getResults("select ec2_instance_id from " + getEC2Table() + " where machine_ip = '" 
	        + machineAddress + "'").get(0);
		return result;
	}
	
	public static void startEC2Instance(String instanceId) {
		Logger.logConsoleMessage("Starting EC2 instance '" + instanceId + "'.");
		CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
		CommandExecutor.execCommand(AWS_PATH + " ec2 start-instances --region us-east-1 --instance-ids " + instanceId, null, null);
	}
	
	public static Boolean waitForEC2InstanceOnline(String instanceId) {
		Boolean ec2NodeOnline = false;
		for (int i = 1; i < Integer.parseInt(System.getenv("EC2_ONLINE_POLL_MAX")); i++) {
			Logger.logConsoleMessage("Checking EC2 instance online status for '" + instanceId + "' attempt '" + i + "'.");
			CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
			String status = CommandExecutor.execCommand(AWS_PATH + " ec2 describe-instance-status --region us-east-1 --instance-ids " + instanceId, null, null);
			if (StringUtils.countMatches(status, "passed") >= 2) {
				ec2NodeOnline = true;
				try { Thread.sleep(30000); } catch (InterruptedException e) { }
				Logger.logConsoleMessage("EC2 instance '" + instanceId + "' is online!");
				break;
			}
			try { Thread.sleep(30000); } catch (InterruptedException e) { }
		}
		
		return ec2NodeOnline;
	}
	
	public static void startAllEC2Instances() {
		Logger.logConsoleMessage("Starting all EC2 Nodes. Please note that if the machines are offline this can take a few minutes...");
		for (String machine : getAllEC2MachineAddresses()) {
			startEC2Instance(getEC2MachineInstanceId(machine));
		}
	}
	
	public static Boolean waitForAllEC2InstancesOnline() {
		Boolean ec2NodesOnline = false;
		List<String> onlineMachines = new ArrayList<String>();
		List<String> allMachines = getAllEC2MachineAddresses();
		for (int i = 1; i < 15; i++) {
			Logger.logConsoleMessage("Checking EC2 instance online status attempt '" + i + "'.");
			for (String machine : allMachines) {
				String instanceId = getEC2MachineInstanceId(machine);
				CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
				String status = CommandExecutor.execCommand(AWS_PATH + " ec2 describe-instance-status --region us-east-1 --instance-ids " + instanceId, null, null);
				if (StringUtils.countMatches(status, "passed") >= 2 && !onlineMachines.contains(machine)) {
					onlineMachines.add(machine);
				}
			}
			
			if (onlineMachines.size() == allMachines.size()) {
				Logger.logConsoleMessage("All EC2 Nodes online and ready for tests!");
				ec2NodesOnline = true;
				break;
			}
			Logger.logConsoleMessage("EC2 Nodes still initializing. Continuing to poll until they are ready.");
			try { Thread.sleep(30000); } catch (InterruptedException e) { }
		}
		
		return ec2NodesOnline;
	}
	
	public static void stopEC2Instance(String instanceId) {
		Logger.logConsoleMessage("Stopping EC2 instance '" + instanceId + "'.");
		CommandExecutor.setTargetGatewayIP(GatewayIP.LAB_01);
		CommandExecutor.execCommand(AWS_PATH + " ec2 stop-instances --region us-east-1 --instance-ids " + instanceId, null, null);
	}
	
	public static void rebootEC2Instance(String instanceId) {
		Logger.logConsoleMessage("Rebooting EC2 instance '" + instanceId + "'.");
		@SuppressWarnings("deprecation")
		AmazonEC2Client ec2Client = new AmazonEC2Client(new EnvironmentVariableCredentialsProvider());
		RebootInstancesRequest reboot = new RebootInstancesRequest();
		reboot.setInstanceIds(Arrays.asList(instanceId));
		RebootInstancesResult result = ec2Client.rebootInstances(reboot);
		Logger.logConsoleMessage(result.toString());
	}
	
	public static void stopAllEC2Instances() {
		for (String machine : getAllEC2MachineAddresses()) {
			stopEC2Instance(getEC2MachineInstanceId(machine));
		}
	}
	
	public static List<String> getAllEC2MachineAddresses() {
		String query = "select machine_ip from " + getEC2Table() + " where ec2_machine = true and machine_status = 'active'";
		return LabDatabaseFactory.getResults(query);
	}
	
	@SuppressWarnings("unused")
	private static String getAWSCLIBoxIP() {
		if (GridManager.isEC2Agent()) {
			return Constants.LAB_01_GATEWAY_IP;
		}
		return null;
	}
	
	private static String getEC2Table() {
		if (isJmeterRun) {
			return "jmeternodes";
		}
		return "agentmachines";
	}
    
}
