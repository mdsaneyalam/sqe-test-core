package com.softech.test.core.lab;

import java.util.List;

import com.softech.test.core.driver.DriverFactory;
import com.softech.test.core.props.BrowserType;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.MobileOS;
import com.softech.test.core.util.Constants;
import com.softech.test.core.util.JenkinsAPIUtil;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.SleepUtils;
import com.softech.test.core.util.TestRun;

public class AvailableDevicePoller {
	
	private static final Integer ACTIVE_IDLE_ITER_MAX = 15;
	
	private static MobileOS mobileOS = null;
	private static String priorityId = "";
	private static String priorityJobName = "";
	private static String priorityJobNumber = "";
	private static Integer priorityNumber = 1;
	private static Long priorityInitialRequestTime = null;
	
	private static ThreadLocal<Integer> activeIdleCounter = new ThreadLocal<Integer>() {
    	protected Integer initialValue() {
    		return 1;
    	}
    };
    
    private static ThreadLocal<Integer> activeIdleIterativeCounter = new ThreadLocal<Integer>() {
    	protected Integer initialValue() {
    		return 1;
    	}
    };
    
    private static ThreadLocal<Boolean> maintenancePollExceeded = new ThreadLocal<Boolean>() {
    	protected Boolean initialValue() {
    		return false;
    	}
    };
    
	public static void pollFarmForDevice(MobileOS mobileOS, String deviceID, Boolean specificDeviceRequest) {
		Integer position = getCurrentPriorityPosition();
		Integer waitTime = getPriorityWaitTime(position);
		
    	if (activeIdleCounter.get() >= Constants.DEVICE_WAIT_QUEUE_MAX_ITER) {
    		activeIdleCounter.set(0);
    		throw new RuntimeException("A device request has exceeded the maximum lab poll allotment of '" 
    			+ Constants.DEVICE_WAIT_QUEUE_MAX_ITER + "' iterations. Terminating the driver request.");
    	}
    	
    	if (specificDeviceRequest) {
    		Logger.logConsoleMessage("The specific requested device '" + deviceID + "' "
    		    + "is currently in use. Device request has been polling for '" + activeIdleCounter.get() + "' iteration(s). "
    		    		+ "The priority position compared to other projects is at position '" + position + "'.");
    	} else {
    		Logger.logConsoleMessage("Requested a device from the device virtual farm on thread '" + Thread.currentThread().getId() 
    			+ "' but none are available. "
    			+ "Device request has been polling for '" + activeIdleCounter.get() + "' iteration(s). "
    			+ "The priority position compared to other projects is at position '" + position + "'.");
    	}
		
    	activeIdleCounter.set(activeIdleCounter.get() + 1);
    	SleepUtils.sleep(waitTime);
    }
	
	public static void pollFarmForBrowser(String machineName, BrowserType browserType, Boolean specificMachineRequest) {
    	Integer idleWaitIter = DriverFactory.isMaintenanceCheck() ? Constants.DEVICE_WAIT_QUEUE_MAX_MAINT_ITER 
    			: Constants.DEVICE_WAIT_QUEUE_MAX_ITER;
		if (activeIdleCounter.get() >= idleWaitIter) {
			activeIdleCounter.set(0);
			if (DriverFactory.isMaintenanceCheck()) {
				maintenancePollExceeded.set(true);
			}
    		throw new RuntimeException("A browser node request has exceeded the maximum lab poll allotment of '" 
    			+ idleWaitIter + "' iterations. Terminating the driver request.");
    	}
    	
    	if (specificMachineRequest) {
    		Logger.logConsoleMessage("The specific requested browser type of '" + browserType.value() + "' on machine '" + machineName + "' is currently in use. Browser "
    				+ "request has been polling for '" + activeIdleCounter.get() + "' iteration(s).");
    	} else {
    		if (TestRun.isSauceRun()) {
    			Logger.logConsoleMessage("Requested a sauce node from the browser virtual farm on thread '" + Thread.currentThread().getId() 
        	    		+ "' but none are available. Sauce request has been polling for '" + activeIdleCounter.get() + "' iteration(s).");
    		} else {
    			Logger.logConsoleMessage("Requested a browser type of '" + browserType.value() + "' from the browser virtual farm on thread '" + Thread.currentThread().getId() 
    	    		+ "' but none are available. Browser request has been polling for '" + activeIdleCounter.get() + "' iteration(s).");
    		}
    	}
		
    	activeIdleCounter.set(activeIdleCounter.get() + 1);
		try { Thread.sleep(RandomData.getInteger(10000, 30000)); } catch (InterruptedException e) { e.printStackTrace(); } // check every 10-11 seconds
    }
	
	public static void pollFarmForDevice(EmergingOS emergingOS, String deviceID, Boolean specificDeviceRequest) {
    	if (activeIdleIterativeCounter.get() >= ACTIVE_IDLE_ITER_MAX) {
    		activeIdleIterativeCounter.set(0);
    		
    		// check if there are active sessions
			Logger.logConsoleMessage("Current device request has iteratively been polling for more than '" + ACTIVE_IDLE_ITER_MAX + "' iterations on thread '" + Thread.currentThread().getId() + "'. "
					+ "WARNING!");
		}
    	
    	if (activeIdleCounter.get() >= Constants.DEVICE_WAIT_QUEUE_MAX_ITER) {
    		throw new RuntimeException("A device request has exceeded the maximum lab poll allotment of '" + Constants.DEVICE_WAIT_QUEUE_MAX_ITER + "' iterations. Terminating the driver request.");
    	}
    	
    	if (specificDeviceRequest) {
    		Logger.logConsoleMessage("The specific requested device '" + deviceID + "' "
    		    + "is currently in use. Device request has been polling for '" + activeIdleCounter.get() + "' iteration(s).");
    	} else {
    		Logger.logConsoleMessage("Requested a device from the device virtual farm on thread '" + Thread.currentThread().getId() + "' but none are available. "
    				+ "Device request has been polling for '" + activeIdleCounter.get() + "' iteration(s).");
    	}
		
    	activeIdleCounter.set(activeIdleCounter.get() + 1);
		activeIdleIterativeCounter.set(activeIdleIterativeCounter.get() + 1);
		try { Thread.sleep(RandomData.getInteger(10000, 30000)); } catch (InterruptedException e) { } // check every 10 seconds
    }
	
	public static Boolean isMaintenancePollExceeded() {
		return maintenancePollExceeded.get();
	}
	
	public static Boolean isDevicePriority() {
		return System.getProperty("system.test.devicepriority") != null;
	}
	
	public static void initPriority() {
		String mobileOSStr = System.getProperty("system.test.mobileos");
		if (mobileOSStr != null) {
			try {
				mobileOS = MobileOS.getEnumByString(mobileOSStr);
			} catch (Exception e) {
				Logger.logConsoleMessage("Failed to get starting mobile os and set mobile priority.");
			}
		}
		
		if (!isDevicePriority() && mobileOS != null) {
			priorityId = Long.toString(System.currentTimeMillis()) + "-" + RandomData.getCharacterString(20);
			priorityJobName = JenkinsAPIUtil.getRunningJobName();
			priorityJobNumber = JenkinsAPIUtil.getRunningBuildId();
			priorityNumber = getInitialPriorityNumber();
			priorityInitialRequestTime = System.currentTimeMillis();
		
			setInitialPollEntry(priorityId, priorityJobName, priorityJobNumber, priorityNumber, priorityInitialRequestTime);
		}
	}
	
	public static void removePriority() {
		if (!isDevicePriority() && mobileOS != null) {
			LabDatabaseFactory.getResults("delete from " + getPriorityTable(mobileOS) + " where priority_id = '" + priorityId + "'");
		}
	}
	
	public static void removePriority(MobileOS mobileOS, String priorityId) {
		LabDatabaseFactory.getResults("delete from " + getPriorityTable(mobileOS) + " where priority_id = '" + priorityId + "'");
	}
	
	public static void reorderPriorityPositions(MobileOS mobileOS) {
		List<String> allPriorityNums = LabDatabaseFactory.getResults("select priority_number from " + getPriorityTable(mobileOS) + " order by priority_number asc");
		if (!allPriorityNums.isEmpty()) {
			for (int i = 0; i < allPriorityNums.size(); i++) {
				Logger.logConsoleMessage("Reordering priority list for current number '" + allPriorityNums.get(i) 
					+ "' to new number '" + (i + 1) + "'.");
				LabDatabaseFactory.getResults("update " + getPriorityTable(mobileOS) + " set priority_number = " 
						+ (i + 1) + " where priority_number = " + allPriorityNums.get(i) + "");
			}
		}
	}
	
	public static void removeLongRunningPriorityEntries(MobileOS mobileOS) {
		Long currTime = System.currentTimeMillis();
		List<String> allTimes = LabDatabaseFactory.getResults("select priority_initial_request_time from " + getPriorityTable(mobileOS));
		for (String time : allTimes) {
			Long testDurationInSec = ((currTime - Long.parseLong(time)) / 1000);
    		Integer testDurationInMin = (int) (testDurationInSec / 60);
    		if (testDurationInMin >= 120) {
    			Logger.logConsoleMessage("Priority request with initial time '" + time + "' exceeds the maximum allotted runtime. Removing the priority entry.");
    			LabDatabaseFactory.getResults("delete from " + getPriorityTable(mobileOS) + " where priority_initial_request_time = " + time + "");
    		}
		}
	}
	
	public static void removeEntriesNoLongerRunning(MobileOS mobileOS) {
		List<String> allIds = LabDatabaseFactory.getResults("select priority_id from " + getPriorityTable(mobileOS));
		
		for (String id : allIds) {
			String jobName = LabDatabaseFactory.getResults("select priority_job_name from " + getPriorityTable(mobileOS) + " where priority_id = '" + id + "'").get(0);
			String jobNumber = LabDatabaseFactory.getResults("select priority_job_number from " + getPriorityTable(mobileOS) + " where priority_id = '" + id + "'").get(0);
			
			if (!JenkinsAPIUtil.isJobBuilding(jobName, jobNumber)) {
				Logger.logConsoleMessage("Job '" + jobName + "' with build number '" + jobNumber + "' is no longer building. Removing priority queue.");
				removePriority(mobileOS, id);
			} else {
				Logger.logConsoleMessage("Job '" + jobName + "' with build number '" + jobNumber + "' is still building. It will not be removed from the priority queue.");
			}
		}
	}
	
	public static Integer getCurrentPriorityPosition() {
		if (isDevicePriority() || mobileOS == null) {
			return 1;
		}
		
		String priorityNum = LabDatabaseFactory.getResults("select priority_number from " + getPriorityTable(mobileOS) + " where priority_id = '" + priorityId + "'").get(0);
		List<String> allPriorityNums = LabDatabaseFactory.getResults("select priority_number from " + getPriorityTable(mobileOS) + " order by priority_number asc");
		
		return (allPriorityNums.indexOf(priorityNum) + 1);
	}
	
	private static Integer getPriorityWaitTime(Integer currentPosition) {
		if (isDevicePriority() || mobileOS == null) {
			return 5000;
		}
		
		if (currentPosition == 1) {
			return 10000;
		}
		return (20000 * currentPosition);
	}
	
	private static void setInitialPollEntry(String priorityId, String priorityJobName, String priorityJobNumber, Integer priorityNumber, Long priorityInitialRequestTime) {
		Logger.logConsoleMessage("Setting initial priority info - priority id: '" + priorityId 
				+ "', priority job name: '" + priorityJobName + "', priority job number: '" + priorityJobNumber + "', priority number: '" + priorityNumber + "', priority initial request time: '" 
				+ priorityInitialRequestTime + "'.");
		LabDatabaseFactory.getResults("INSERT INTO " + getPriorityTable(mobileOS) + " VALUES ('" + priorityId + "', '" + priorityJobName + "', '" + priorityJobNumber + "', " 
				+ priorityNumber + ", " + priorityInitialRequestTime + ")");
	}
	
	private static Integer getInitialPriorityNumber() {
		List<String> results = LabDatabaseFactory.getResults("select priority_number from " + getPriorityTable(mobileOS) + " order by priority_number desc limit 1;");
		if (results.isEmpty()) {
			return 1;
		}
		return (Integer.parseInt(results.get(0)) + 1);
	}
	
	private static String getPriorityTable(MobileOS mobileOS) {
		return mobileOS.equals(MobileOS.ANDROID) ? Constants.MQE_LAB_DB_ANDROID_PRIORITY : Constants.MQE_LAB_DB_IOS_PRIORITY;
	}
	
}
