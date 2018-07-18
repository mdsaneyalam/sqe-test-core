package com.softech.test.core.lab;

import java.io.File;

import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.Logger;

public class FileDeployer extends CommandExecutor {

	public static String deployFileToNodeFromEC2Agent(String targetNodeIP, File agentSourceFilePath, String nodeTargetFileDir) {
		if (!GridManager.isEC2Agent()) {
			String msg = "Executor is not running on the lab. Not deploying file!";
			Logger.logConsoleMessage(msg);
			return msg;
		}
		
		// push the file from the agent to the lab hub
		CommandExecutor.setEC2SCPHop(false);
		CommandExecutor.copyFileFromTo(agentSourceFilePath.getAbsolutePath(), 
				Constants.HUB_APP_PACKAGE_DIR, REMOTE_MQE_ADMIN_USER + Constants.LAB_01_GATEWAY_IP, null);
		
		// push the file from the lab hub to the target node
		String hubFilePath = Constants.HUB_APP_PACKAGE_DIR + agentSourceFilePath.getName();
		CommandExecutor.copyFileFromTo(hubFilePath, Constants.NODE_APP_PACKAGE_DIR, targetNodeIP, null);
	
		return Constants.NODE_APP_PACKAGE_DIR + agentSourceFilePath.getName();
	}
	
	public static String deployFileToGatewayFromEC2Agent(GatewayIP gatewayIP, File agentSourceFilePath) {
		if (!GridManager.isEC2Agent()) {
			String msg = "Executor is not running on the lab. Not deploying file!";
			Logger.logConsoleMessage(msg);
			return msg;
		}
		
		// push the file from the agent to the lab hub
		CommandExecutor.setEC2SCPHop(false);
		CommandExecutor.setSCPRecursive(true);
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.copyFileFromTo(agentSourceFilePath.getAbsolutePath(), 
				Constants.HUB_APP_PACKAGE_DIR, REMOTE_MQE_ADMIN_USER + gatewayIP.value(), null);
		
		return Constants.HUB_APP_PACKAGE_DIR + agentSourceFilePath.getName();
	}
	
	public static String deployFileFromGatewayToNode(GatewayIP gatewayIP, String targetNodeIP, File gatewaySourceFilePath) {
		if (!GridManager.isEC2Agent()) {
			String msg = "Executor is not running on the lab. Not deploying file!";
			Logger.logConsoleMessage(msg);
			return msg;
		}
		
		// push the file from the gateway to the target node
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.copyFileFromTo(gatewaySourceFilePath.getAbsolutePath(), 
				Constants.NODE_APP_PACKAGE_DIR, targetNodeIP, null);
		
		return Constants.NODE_APP_PACKAGE_DIR + gatewaySourceFilePath.getName();
	}
	
	public static String pullFileFromGatewayToAgent(GatewayIP gatewayIP, File gatewayFilePath, String agentTargetFileDir) {
		if (!GridManager.isEC2Agent()) {
			String msg = "Executor is not running on the lab. Not pulling file!";
			Logger.logConsoleMessage(msg);
			return msg;
		}
		
		CommandExecutor.setEC2SCPHop(false);
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.copyFileToFrom(gatewayFilePath.getAbsolutePath(), agentTargetFileDir, null, null);
		
		return agentTargetFileDir + File.separator + gatewayFilePath.getName();
	}
	
	public static String pullFileFromNodeToGateway(GatewayIP gatewayIP, String targetNodeIP, File targetNodeFilePath, String gatewayTargetFileDir) {
		if (!GridManager.isEC2Agent()) {
			String msg = "Executor is not running on the lab. Not pulling file!";
			Logger.logConsoleMessage(msg);
			return msg;
		}
		
		CommandExecutor.setTargetGatewayIP(gatewayIP);
		CommandExecutor.copyFileToFrom(targetNodeFilePath.getAbsolutePath(), gatewayTargetFileDir, targetNodeIP, null);
		
		return gatewayTargetFileDir + File.separator + targetNodeFilePath.getName();
	}
	
}
