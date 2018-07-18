package com.softech.test.core.util;

import java.net.HttpURLConnection;
import java.net.URL;

import org.openqa.selenium.WebElement;

/**
 * Wrapper class for html link related utility methods
 * Typical use is as follows : new Link(Webelement, "Link description").verifyIsBroken();
 * 
 * @author Jitendra Khare created September 21, 2017
 *
 */
public class LinkUtils {

	// Object properties
	private static ThreadLocal<WebElement> link = new ThreadLocal<WebElement>();
	private static ThreadLocal<String> linkSimpleName = new ThreadLocal<String>();

	// Constructor
	public LinkUtils(WebElement linkObj, String simpleName) {
		link.set(linkObj);
		linkSimpleName.set(simpleName);

	}

	/**
	 * Verify if link text is correct
	 * 
	 * @param link
	 *            - {@link WebElement} Html link element
	 * @param expectedText
	 *            - {@link String} Expected link text
	 * @param checkFlag
	 *            - (@link Integer) check flag for test verification 1 - Exact
	 *            Match 2 - Starts With 3 - Ends With 4 - Contains
	 * @return - {@link boolean} - true if link text matches with expected text
	 */
	public boolean verifyText(String expectedText, ExpectedComparison checkFlag) {
		String actualText = link.get().getText();
		boolean returnValue = false;
		switch (checkFlag) {
		case EQUALS:
			if (!actualText.equals(expectedText)) {
				Logger.logMessage(String.format("%s text is not proper.Expected = %s | Actual = %s",
						linkSimpleName.get(), expectedText, actualText));
				returnValue = false;
			} else {
				Logger.logMessage(linkSimpleName.get() + " text verified successfully.");
				returnValue = true;
			}
			break;
		case STARTS_WITH:
			if (!actualText.startsWith(expectedText)) {
				Logger.logMessage(String.format("%s text is not proper.Expected = %s | Actual = %s",
						linkSimpleName.get(), expectedText, actualText));
				returnValue = false;
			} else {
				Logger.logMessage(linkSimpleName.get() + " text verified successfully.");
				returnValue = true;
			}
			break;
		case ENDS_WITH:
			if (!actualText.endsWith(expectedText)) {
				Logger.logMessage(String.format("%s text is not proper.Expected = %s | Actual = %s",
						linkSimpleName.get(), expectedText, actualText));
				returnValue = false;
			} else {
				Logger.logMessage(linkSimpleName.get() + " text verified successfully.");
				returnValue = true;
			}
			break;
		case CONTAINS:
			if (!actualText.contains(expectedText)) {
				Logger.logMessage(String.format("%s text is not proper.Expected = %s | Actual = %s",
						linkSimpleName.get(), expectedText, actualText));
				returnValue = false;
			} else {
				Logger.logMessage(linkSimpleName.get() + " text verified successfully.");
				returnValue = true;
			}
			break;

		}

		return returnValue;
	}

	/**
	 * Verify clicking on link navigates properly
	 * 
	 * @param link
	 *            - {@link WebElement} Html link element tobe verified
	 * @param navigateToNewTab
	 *            - {@link Boolean} 'true' if link should open a new tab
	 * @param expectedUrl
	 *            - {@link String} Expected navigation url after click on link
	 * @return {@link boolean} - true if link text matches with expected text
	 */
	public boolean verifyLinkNavigation(boolean navigateToNewTab, String expectedUrl) {
			expectedUrl = getFinalRedirectedUrl(expectedUrl);
		
		if(!verifyNavigationURLIsValid(expectedUrl)){
			Logger.logMessage("link navigation url is not valid.Invalid url : "+expectedUrl);
		}else{
			Logger.logMessage("####################################");
			Logger.logMessage("Link navigates to url : "+expectedUrl);
			Logger.logMessage("####################################");
		}
		
		// Compare expected behavior from method argument with actual behavior
		if (navigateToNewTab) {			
			if(navigatesToNewTab()){
				return true;
			}else{
				return false;
			}
		}else{
			if(navigatesToNewTab()){
				return false;
			}else{
				return true;
			}
		}

	}

	/**
	 * Verify link is broken or not using status codes Few status codes are as
	 * follows : Informational - 1xx Success - 2xx Redirection - 3xx Client
	 * Error - 4xx Server Error - 5xx
	 *
	 * @return - 'true' if link is broken
	 */
	public static boolean verifyIsBroken(String... hrefStr) {

		String href = null;
		if (hrefStr.length > 0) {
			href = hrefStr[0];
		} else {
			href = link.get().getAttribute("href");
		}

		Logger.logMessage(href);
		try {
			URL url = new URL(href);
			HttpURLConnection httpURLConnect = (HttpURLConnection) url.openConnection();
			httpURLConnect.setConnectTimeout(2000);

			httpURLConnect.connect();
			String responseCode = String.valueOf(httpURLConnect.getResponseCode());
			Logger.logMessage(String.format("Verifying URL:%s Status code=%s", href, responseCode));

			if (responseCode.startsWith("4") || responseCode.startsWith("5")) {
				Logger.logMessage(String.format("URL %s is broken with status code %s", href, responseCode));
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			Logger.logMessage(e.toString());
			if (hrefStr.length > 0) {
				Logger.logMessage(String.format("Link URL is broken for %s with RUNTIME ERROR : %s", href, e));
			} else {
				Logger.logMessage(
						String.format("Link URL is broken for %s with RUNTIME ERROR : %s", link.get().getText(), e));
			}

			return true;
		}

	}

	/**
	 * Get redirected url - {@link String} from original url
	 * 
	 * @param url
	 *            - Original URL
	 * @return {@link String} - redirection url mapped with original url
	 */
	private String getFinalRedirectedUrl(String url) {

		HttpURLConnection connection;
		String finalUrl = url;
		try {
			connection = (HttpURLConnection) new URL(finalUrl).openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.setUseCaches(false);
			connection.setRequestMethod("GET");
			connection.connect();
			int responseCode = connection.getResponseCode();
			Logger.logConsoleMessage("ResponseCodeReceived : "+responseCode);
			if (responseCode >= 300 && responseCode < 400) {
				String redirectedUrl = connection.getHeaderField("Location");
				if (null != redirectedUrl)
					finalUrl = redirectedUrl;
			}
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalUrl;
	}
	
	/**
	   * Checks whether the given URL is valid.
	   * 
	   * @param url to be validated if passed as parameter else object href will be used
	   * @return true if the url is valid, false otherwise.
	   */
	public boolean verifyNavigationURLIsValid(String... expectedUrl) {
		String url = null;
		if(expectedUrl.length == 1){
			url = expectedUrl[0];
		}else{
			url = link.get().getAttribute("href");
		}
		if (url == null) {
			return false;
		} else {

			String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
			return url.matches(urlPattern);

		}

	}
	
	/**
	 * Verify if click on link navigates to new tab / window
	 * @return true if clicking link navigates to new tab
	 */
	public boolean navigatesToNewTab() {
		String target = link.get().getAttribute("target");
		if (!target.isEmpty() && target.equals("_blank")) {
			return true;
		} else {
			return false;
		}
	}

}
