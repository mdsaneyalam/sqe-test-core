package com.softech.test.core.lab;

import java.util.ArrayList;
import java.util.List;

import com.softech.test.core.util.JenkinsAPIUtil;
import com.softech.test.core.util.Logger;

public class MVNProcessManager {

	private static String mvnProcessId = null;

	public static void initMVNProcessId() {
		String pomPath = System.getProperty("user.dir");
		if (pomPath == null) {
			Logger.logConsoleMessage("The path of the project POM was not retrieved during runtime.");
			return;
		}

		List<String> processIds = getRunningSurefireProcessIds(pomPath);

		if (processIds.isEmpty()) {
			Logger.logConsoleMessage("No matching maven process ids were found during execution.");
		} else if (processIds.size() > 1) {
			Logger.logConsoleMessage("Multiple matching process ids were found during execution. "
					+ "This typically means that multiple jobs using the same project are running.");
		} else {
			mvnProcessId = processIds.get(0);
		}

		if (mvnProcessId != null) {
			Logger.logConsoleMessage("Setting project running maven process id to '" + processIds.get(0) + "'.");
			String mavenJobName = JenkinsAPIUtil.getRunningJobName();
			String mavenJobNumber = JenkinsAPIUtil.getRunningBuildId();
			Long mavenInitialRequestTime = System.currentTimeMillis();
			setInitialEntry(mvnProcessId, mavenJobName, mavenJobNumber, mavenInitialRequestTime, pomPath);
		}
	}

	public static void removeMVNProcessId() {
		if (mvnProcessId != null) {
			LabDatabaseFactory
					.getResults("delete from mavenprocesslog where maven_process_id = '" + mvnProcessId + "'");
		}
	}

	public static void removeMVNProcessId(String processId) {
		LabDatabaseFactory.getResults("delete from mavenprocesslog where maven_process_id = '" + processId + "'");
	}

	public static List<String> getAllMavenProcessIds() {
		return LabDatabaseFactory.getResults("select maven_process_id from mavenprocesslog");
	}

	public static Boolean isMavenProcessRunning(String processId) {
		String mavenJobName = getMavenJobName(processId);
		String mavenJobNumber = getMavenJobNumber(processId);

		return JenkinsAPIUtil.isJobBuilding(mavenJobName, mavenJobNumber);
	}

	public static Boolean isMavenProcessOrphaned(String processId) {
		String mavenPOMPath = LabDatabaseFactory
				.getResults("select maven_pom_path from mavenprocesslog where maven_process_id = '" + processId + "'")
				.get(0);

		Boolean isRunning = isMavenProcessRunning(processId);
		Boolean idsMatch = false;

		List<String> allRunningProcesses = getRunningSurefireProcessIds(mavenPOMPath);
		if (allRunningProcesses.size() > 0) {
			if (allRunningProcesses.contains(processId)) {
				idsMatch = true;
			}
		}

		return !isRunning && idsMatch;
	}

	public static void killOrphanedMavenProcess(String processId) {
		CommandExecutor.setEC2CommandHop(false);
		String[] cmd = { "kill", "-9", processId };
		CommandExecutor.execCommand(cmd, null, null);
	}

	public static String getMavenJobName(String processId) {
		return LabDatabaseFactory
				.getResults("select maven_job_name from mavenprocesslog where maven_process_id = '" + processId + "'")
				.get(0);
	}

	public static String getMavenJobNumber(String processId) {
		return LabDatabaseFactory
				.getResults("select maven_job_number from mavenprocesslog where maven_process_id = '" + processId + "'")
				.get(0);
	}

	private static void setInitialEntry(String processId, String mavenJobName, String mavenJobNumber,
			Long mavenInitialRequestTime, String mavenPOMPath) {
		LabDatabaseFactory.getResults("INSERT INTO mavenprocesslog VALUES ('" + processId + "', '" + mavenJobName
				+ "', '" + mavenJobNumber + "', " + mavenInitialRequestTime + ", '" + mavenPOMPath + "')");
	}

	private static List<String> getRunningSurefireProcessIds(String pomPath) {
		List<String> allRunningProcessIds = new ArrayList<String>();

		CommandExecutor.setEC2CommandHop(false);
		String[] cmd = { "ps", "-ef" };
		String cmdResult = CommandExecutor.execCommand(cmd, null, null);
		String[] cmdLines = cmdResult.split("\\r?\\n");

		for (String cmdLine : cmdLines) {
			try {
				if (cmdLine.contains("-c cd") && cmdLine.contains("jenkins") && cmdLine.contains("surefire")
						&& cmdLine.contains(pomPath)) {
					allRunningProcessIds.add(cmdLine.trim().split("jenkins\\s+")[1].split("\\s+")[0]);

				}
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to retrieve maven process id of the running job.");
				e.printStackTrace();
			}
		}

		return allRunningProcessIds;
	}

}
