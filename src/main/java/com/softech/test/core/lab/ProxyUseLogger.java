package com.softech.test.core.lab;

import java.util.ArrayList;
import java.util.List;

import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.JenkinsAPIUtil;
import com.softech.test.core.util.RandomData;

public class ProxyUseLogger {
	
	private static ThreadLocal<String> proxyID = new ThreadLocal<String>();
	public static MobileOS jobMobileOS = null;
	
	public static void initUseLog(MobileOS mobileOS, String proxyPort) {
		jobMobileOS = mobileOS;
		proxyID.set(Long.toString(System.currentTimeMillis()) + "-" + RandomData.getCharacterString(20));
		String proxyJobName = JenkinsAPIUtil.getRunningJobName();
		String proxyJobNumber = JenkinsAPIUtil.getRunningBuildId();
		String proxyPortNumber = proxyPort;
		Long proxyInitialRequestTime = System.currentTimeMillis();
		
		setInitialEntry(mobileOS, proxyID.get(), proxyJobName, proxyJobNumber, proxyPortNumber, proxyInitialRequestTime);
	}
	
	public static void removeUseLog(MobileOS mobileOS, String proxyPort) {
		LabDatabaseFactory.getResults("delete from " + getTable(mobileOS) + " where proxy_port_number = '" + proxyPort + "'");
	}
	
	public static void removeAllJobUseLogs() {
		if (jobMobileOS != null) {
			LabDatabaseFactory.getResults("delete from " + getTable(jobMobileOS) + " where proxy_job_name = '" 
					+ JenkinsAPIUtil.getRunningJobName() + "' and proxy_job_number = '" + JenkinsAPIUtil.getRunningBuildId() + "'");
		}
	}
	
	public static Boolean isProxyClaimedByRunningJob(MobileOS mobileOS, String proxyPort) {
		List<String> allProxyIds = LabDatabaseFactory.getResults("select proxy_id from " + getTable(mobileOS) + " where proxy_port_number = '" + proxyPort + "'");
		
		List<Boolean> hasJobsRunning = new ArrayList<Boolean>();
		for (String proxyId : allProxyIds) {
			String proxyJobName = LabDatabaseFactory.getResults("select proxy_job_name from " + getTable(mobileOS) + " where proxy_id = '" + proxyId + "'").get(0);
			String proxyJobNumber = LabDatabaseFactory.getResults("select proxy_job_number from " + getTable(mobileOS) + " where proxy_id = '" + proxyId + "'").get(0);
			hasJobsRunning.add(JenkinsAPIUtil.isJobBuilding(proxyJobName, proxyJobNumber));
		}
		
		return hasJobsRunning.contains(true);
	}
	
	private static void setInitialEntry(MobileOS mobileOS, String proxyId, String proxyJobName, String proxyJobNumber, String proxyPortNumber, Long proxyInitialRequestTime) {
		LabDatabaseFactory.getResults("INSERT INTO " + getTable(mobileOS) + " VALUES ('" + proxyId + "', '" + proxyJobName + "', '" + proxyJobNumber + "', " 
				+ proxyPortNumber + ", " + proxyInitialRequestTime + ")");
	}
	
	private static String getTable(MobileOS mobileOS) {
		return mobileOS.equals(MobileOS.ANDROID) ? Constants.MQE_LAB_DB_ANDROID_PROXY_USE_LOG : Constants.MQE_LAB_DB_IOS_PROXY_USE_LOG;
	}
	
}
