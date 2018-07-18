package com.softech.test.core.lab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.ArrayUtils;

import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.OSDetector;

public class CommandExecutor {

	private static final String REMOTE_ADMIN_USER = "admin@";
	private static final String REMOTE_WIN_ADMIN_USER = "Administrator@";
	protected static final String REMOTE_MQE_ADMIN_USER = "mqeadmin@";
	protected static final String REMOTE_JENKINS_USER = "jenkins@";
	private static final Integer DEFAULT_TIMEOUT_SEC = 20;
	
	private static ThreadLocal<String> command = new ThreadLocal<String>();
	private static ThreadLocal<String> output = new ThreadLocal<String>();
	private static ThreadLocal<Integer> exitCode = new ThreadLocal<Integer>();
	private static ThreadLocal<Boolean> ec2Hop = new ThreadLocal<Boolean>() {
	    protected Boolean initialValue() {
	    	return true;
	    }
	};
	
	private static ThreadLocal<Boolean> ec2SCPHop = new ThreadLocal<Boolean>() {
	    protected Boolean initialValue() {
	    	return true;
	    }
	};
	
	private static ThreadLocal<Boolean> recursiveSCP = new ThreadLocal<Boolean>() {
	    protected Boolean initialValue() {
	    	return false;
	    }
	};
	
	private static ThreadLocal<Process> process = new ThreadLocal<Process>();
	
	private static ThreadLocal<Integer> commandTimeouts = new ThreadLocal<Integer>() {
	    protected Integer initialValue() {
	    	return 0;
	    }
	};
	
	private static ThreadLocal<GatewayIP> targetGatewayIP = new ThreadLocal<GatewayIP>();
	
	/**********************************************************************************************
     * Gets the last executed command.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return The previously executed command.
     ***********************************************************************************************/
	public static String getCommand() {
		return command.get();
	}
	
	/**********************************************************************************************
     * Gets the last executed exit code.
     * 
     * @author Brandon Clark created March 22, 2016
     * @version 1.0 March 22, 2016
     * @return The previously executed exit code.
     ***********************************************************************************************/
	public static Integer getExitCode() {
		return exitCode.get();
	}
	
	/**********************************************************************************************
     * Gets the output (or error) of the last executed command.
     * 
     * @author Brandon Clark created February 1, 2016
     * @version 1.0 February 1, 2016
     * @return The output or error of the previously executed command.
     ***********************************************************************************************/
	public static String getOutput() {
		return output.get();
	}
	
	public static Process getProcess() {
		return CommandExecutor.process.get();
	}
	
	/**********************************************************************************************
     * Executes a user command in the bash environment.
     * 
     * @param command - {@link String} - The bash command to execute.
     * @param machineIPAddress - {@link String} - The machine IP address of the machine to run a remote command, ie 10.15.15.15.
     * @param timeoutInSec - {@link Integer} - The max time in seconds the command can run before timing out. Set to null to use the class default.
     * @author Brandon Clark created February 1, 2016
     * @version 1.3 April 27, 2016
     * @return The output of the executed command.
     ***********************************************************************************************/
	public static String execCommand(String command, String machineIPAddress, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		String user = getAdminUserSSHString(machineIPAddress, command);
		CommandExecutor.command.set(user + command);
		CommandExecutor.output.set(null);
		Process process = null;
		
		String errorMsg = "Command failed to execute.";
		try {
			process = Runtime.getRuntime().exec(CommandExecutor.command.get());
			process.waitFor(timeout, TimeUnit.SECONDS);
			exitCode.set(process.exitValue());
			CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		        	Logger.logConsoleMessage(errorMsg);
		            Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage(errorMsg);
            Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		ec2Hop.set(true);
		return CommandExecutor.output.get();
	}
	
	public static String execCommand(String[] command, String machineIPAddress, Integer timeoutInSec) {
		// TODO make this the standard implementation for all commands.
		// there should also be a separate thread/process spun up that controls the timeout and process
		// destroy in the event it is hanging. process.waitfor will never return in some cases like curl gets...
		String user = getAdminUserSSHString(machineIPAddress, Arrays.toString(command));
		StringBuilder friendlyCommand = new StringBuilder();
		for (String c : command) {
			friendlyCommand.append(c + " ");
		}
		CommandExecutor.command.set(user + friendlyCommand.toString());
		CommandExecutor.output.set(null);
		Process process = null;
		
		String[] readyCommand;
		if (user.isEmpty()) {
			readyCommand = command;
		} else {
			readyCommand = (String[]) ArrayUtils.addAll(user.split(" "), command);
		}
		
		String errorMsg = "The command failed to execute.";
		try {
		    ProcessBuilder processBuilder = new ProcessBuilder(readyCommand);
		    processBuilder.redirectErrorStream(true);
		    process = processBuilder.start();
		    CommandExecutor.process.set(process);
		    CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		        	Logger.logConsoleMessage(errorMsg);
		            Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage(errorMsg);
            Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		return CommandExecutor.output.get();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String execEnvironmentCommand(String[] command, Map<String, String> environmentProps, String machineIPAddress, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		String user = getAdminUserSSHString(machineIPAddress, command.toString());
		CommandExecutor.command.set(user + command.toString());
		CommandExecutor.output.set(null);
		Process process = null;
		
		String errorMsg = "The command '" + CommandExecutor.command.get() + "' failed to execute.";
		
		try {
		    ProcessBuilder processBuilder = new ProcessBuilder(command);
		    Map<String, String> environment = processBuilder.environment();
		    environment.clear();
		    Iterator iterator = environmentProps.entrySet().iterator();
		    while (iterator.hasNext()) {
		    	Map.Entry<String, String> pair = (Map.Entry<String, String>) iterator.next();
		    	environment.put(pair.getKey(), pair.getValue());
		    	iterator.remove();
		    }
		    process = processBuilder.start();
		    process.waitFor(timeout, TimeUnit.SECONDS);
		    CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		        	Logger.logConsoleMessage(errorMsg);
		            Logger.logConsoleMessage(e.getMessage());
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage(errorMsg);
            Logger.logConsoleMessage(e.getMessage());
		} finally {
			process.destroyForcibly();
		}
			
		return CommandExecutor.output.get();
	}
	
	/**********************************************************************************************
     * Executes a multi style local command in the bash environment.
     * 
     * @param command - {@link String} - The bash command to execute.
     * @author Brandon Clark created April 1, 2016
     * @version 1.0 April 1, 2016
     * @return The output of the executed command.
     ***********************************************************************************************/
	public static String execMultiCommand(String command, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		CommandExecutor.command.set(command);
		CommandExecutor.output.set(null);
		Process process = null;
		
		String errorMsg = "The command '" + CommandExecutor.command.get() + "' failed to execute.";
		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command});
			process.waitFor(timeout, TimeUnit.SECONDS);
			exitCode.set(process.exitValue());
			CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		        	Logger.logConsoleMessage(errorMsg);
		            Logger.logConsoleMessage(e.getMessage());
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage(errorMsg);
            Logger.logConsoleMessage(e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		return CommandExecutor.output.get();
	}
	
	public static String execRemoteMultiCommand(String command, String machineIPAddress, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		String user = getAdminUserSSHString(machineIPAddress, command);
		CommandExecutor.command.set(user + command.toString());
		CommandExecutor.output.set(null);
		Process process = null;
		
		String errorMsg = "Command failed to execute.";
		try {
			process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", CommandExecutor.command.get()});
			process.waitFor(timeout, TimeUnit.SECONDS);
			exitCode.set(process.exitValue());
			CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		        	Logger.logConsoleMessage(errorMsg);
		        	Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage(errorMsg);
        	Logger.logToSysFile(errorMsg  + CommandExecutor.command.get() + e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		ec2Hop.set(true);
		return CommandExecutor.output.get();
	}
	
	/**********************************************************************************************
     * Executes a scp user command in the bash environment.
     * 
     * @param srcFilePath - {@link String} - The local file path of the file to copy.
     * @param dstDirPath - {@link String} - The target directory path to copy the file to on the remote machine.
     * @param machineIPAddress - {@link String} - The ssh user string in the format of 'user@ipaddress' to run a remote command.
     * @author Brandon Clark created February 12, 2016
     * @version 1.0 February 12, 2016
     * @return The output of the executed command.
     ***********************************************************************************************/
	public static String copyFileFromTo(String srcFilePath, String dstDirPath, String machineIPAddress, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		String recursive = "";
		if (recursiveSCP.get()) {
			recursive = " -r ";
		}
		String user = "scp " + recursive + srcFilePath + " " + getAdminUserSCPString(machineIPAddress) + ":" + dstDirPath;
		if (GridManager.isEC2Agent() && ec2SCPHop.get()) {
			user = getAdminUserSSHString(null, null) + user;
		}
		
		CommandExecutor.command.set(user);
		CommandExecutor.output.set(null);
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(CommandExecutor.command.get());
			process.waitFor(timeout, TimeUnit.SECONDS);
		    CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage("Failed to execute scp command '" + command + "'.");
    		Logger.logConsoleMessage(e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		ec2SCPHop.set(true);
		return CommandExecutor.output.get();
	}
	
	/**********************************************************************************************
     * Executes a scp user command in the bash environment.
     * 
     * @param srcFilePath - {@link String} - The remote file path of the file to copy.
     * @param dstDirPath - {@link String} - The target directory path to save the file to on the local machine.
     * @param machineIPAddress - {@link String} - The target machine IP address to grab the file from.
     * @author Brandon Clark created May 10, 2016
     * @version 1.0 May 10, 2016
     * @return The output of the executed command.
     ***********************************************************************************************/
	public static String copyFileToFrom(String srcFilePath, String dstDirPath, String machineIPAddress, Integer timeoutInSec) {
		Integer timeout = setTimeout(timeoutInSec);
		String recursive = "";
		if (recursiveSCP.get()) {
			recursive = " -r ";
		}
		String user = "scp " + recursive  + getAdminUserSCPString(machineIPAddress) + ":" + srcFilePath + " " + dstDirPath;
		if (GridManager.isEC2Agent() && ec2SCPHop.get()) {
			user = getAdminUserSSHString(null, null) + user;
		}
		
		CommandExecutor.command.set(user);
		CommandExecutor.output.set(null);
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(CommandExecutor.command.get());
			process.waitFor(timeout, TimeUnit.SECONDS);
			CommandExecutor.output.set(Stream.of(process.getErrorStream(), process.getInputStream()).parallel().map((InputStream isForOutput) -> {
		        StringBuilder output = new StringBuilder();
		        try (BufferedReader br = new BufferedReader(new InputStreamReader(isForOutput))) {
		            String line;
		            while ((line = br.readLine()) != null) {
		                output.append(line);
		                output.append("\n");
		            }
		            br.close();
		        } catch (IOException e) {
		            throw new RuntimeException(e);
		        }
		        return output;
		    }).collect(Collectors.joining()));
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				commandTimeouts.set(commandTimeouts.get() + 1);
			}
			Logger.logConsoleMessage("Failed to execute scp command '" + command.get() + "'.");
    		Logger.logConsoleMessage(e.getMessage());
		} finally {
			process.destroyForcibly();
		}
		
		ec2SCPHop.set(true);
		return CommandExecutor.output.get();
	}
	
	public static void setEC2CommandHop(Boolean hopEnabled) {
		ec2Hop.set(hopEnabled);
	}
	
	public static void setEC2SCPHop(Boolean hopEnabled) {
		ec2SCPHop.set(hopEnabled);
	}
	
	public static void setSCPRecursive(Boolean recursive) {
		recursiveSCP.set(recursive);
	};
	
	public static Boolean isSuccess() {
	    return exitCode.get().equals(0);
	}
	
	public static Integer getCommandTimeoutCount() {
		return commandTimeouts.get();
	}
	
	public static void setTargetGatewayIP(GatewayIP gatewayIP) {
		targetGatewayIP.set(gatewayIP);
	}
	
	private static GatewayIP getTargetGatewayIP(String command) {
		GatewayIP handledTargetGatewayIP = null;
		if (targetGatewayIP.get() == null) {
			String msg = "A request to run ssh command from a gateway was received without a "
					+ "specificed target gateway indicated. The request will go to the '" 
					+ GatewayIP.LAB_01 + "' gateway by default.";
			Logger.logConsoleMessage(msg + " " + command);
			Logger.logToSysFile(msg + " " + command);
			handledTargetGatewayIP = GatewayIP.LAB_01;
		} else {
			handledTargetGatewayIP = targetGatewayIP.get();
		}
		
		targetGatewayIP.set(null);
		return handledTargetGatewayIP;
	}
	
	private static String getAdminUserSSHString(String sessionUserString, String command) {
		if (!GridManager.isEC2Agent()) { // local command executor with no ssh manipulation
			return "";
		}
		
		if (sessionUserString == null) {
			if (GridManager.isEC2Agent() && OSDetector.isLinux() && ec2Hop.get()) {
				return "ssh " + REMOTE_MQE_ADMIN_USER + getTargetGatewayIP(command).value() + " ";
			} else { // execute the command directly on the hub when running on the hub
				return "";
			}
		} else if (sessionUserString.contains("@")) {
			return "ssh " + sessionUserString + " ";
		} else {
			String gatewayIP = LabDeviceManager.getMachineGatewayIP(sessionUserString).value();
			if (GridManager.isEC2Agent() && OSDetector.isLinux()) {
				return "ssh " + REMOTE_MQE_ADMIN_USER + gatewayIP + " ssh " + getSSHUser(sessionUserString) + sessionUserString + " ";
			} else { // execute a command from the lab hub machine to a lab agent machine (mac mini or alpha) when running on the hub
				String machineName = LabDeviceManager.getMachineName(sessionUserString);
				if (machineName.toLowerCase().contains("ec2")) {
					return "ssh " + REMOTE_WIN_ADMIN_USER + sessionUserString + " ";
				} else {
					return "ssh " + REMOTE_ADMIN_USER + sessionUserString + " ";
				}
			}
		}
	}
	
	private static String getAdminUserSCPString(String sessionUserString) {
		if (sessionUserString == null || !ec2SCPHop.get()) {
			return REMOTE_MQE_ADMIN_USER + getTargetGatewayIP(null).value();
		} else if (sessionUserString.contains("@")) {
			return sessionUserString;
		} else {
			return REMOTE_ADMIN_USER + sessionUserString;
		}
	}
	
	private static String getSSHUser(String machineIP) {
		return EC2Manager.isEC2Machine(machineIP) ? REMOTE_WIN_ADMIN_USER : REMOTE_ADMIN_USER;
	}
	
	private static Integer setTimeout(Integer timeoutInSec) {
		if (timeoutInSec == null) {
			return DEFAULT_TIMEOUT_SEC;
		}
		return timeoutInSec;
	}
	
}
