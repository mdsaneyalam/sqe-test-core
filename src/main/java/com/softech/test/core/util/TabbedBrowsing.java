package com.softech.test.core.util;

import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import com.softech.test.core.driver.DriverManager;

/**
 * Class for tabbed browsing
 * 
 * @author Jitendra Khare
 *
 */
public class TabbedBrowsing {

	private static ThreadLocal<String> mainWindowHandle = new ThreadLocal<String>();
	private static ThreadLocal<Set<String>> tabs = new ThreadLocal<Set<String>>();
	private static ThreadLocal<WebDriver> driver = new ThreadLocal<WebDriver>();

	public TabbedBrowsing() {
		driver.set(DriverManager.getWebDriver());
		mainWindowHandle.set(driver.get().getWindowHandle());
	}

	/**
	 * Navigate to tab on passed index
	 * 
	 * @param {@link Integer} - index
	 * @author Jitendra Khare created March 2, 2017
	 */
	public void navigateToTab(int index) {
		tabs.set((Set<String>) driver.get().getWindowHandles());
		driver.get().switchTo().window(tabs.get().toArray()[index].toString());
	}

	/**
	 * Navigate back to main window
	 * 
	 * @author Jitendra Khare created March 2, 2017
	 */
	public void navigateToMainWindow() {
		driver.get().switchTo().window(mainWindowHandle.get());
	}

	/**
	 * Clase tab on passed index
	 * 
	 * @param {@link Integer} - index
	 * @author Jitendra Khare created March 2, 2017
	 */
	public void closeTab(int index) {
		tabs.set((Set<String>) driver.get().getWindowHandles());
		driver.get().switchTo().window(tabs.get().toArray()[index].toString());
		driver.get().close();
	}

	/**
	 * Gets total number of tabs
	 * 
	 * @author Jitendra Khare created March 2, 2017
	 * @return Number of tabs
	 */
	public int getTabsCount() {
		tabs.set((Set<String>) driver.get().getWindowHandles());
		return tabs.get().size();
	}

	/**
	 * Opens a new tab and then switches to it.
	 * 
	 * @param {@link
	 * 			Integer} - index
	 * @author Jitendra Khare created March 2, 2017
	 */
	public void openTab(Integer index) {
		((JavascriptExecutor) DriverManager.getWebDriver()).executeScript("window.open('%s')");
		navigateToTab(index);
	}
}
