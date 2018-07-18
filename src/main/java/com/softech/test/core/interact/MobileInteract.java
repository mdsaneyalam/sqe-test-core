//package com.softech.test.core.interact;
//
//import io.appium.java_client.AppiumDriver;
//import io.appium.java_client.MobileBy;
//import io.appium.java_client.MobileElement;
//import io.appium.java_client.TouchAction;
//import io.appium.java_client.android.AndroidDriver;
//import io.appium.java_client.android.AndroidElement;
//import io.appium.java_client.android.AndroidKeyCode;
//import io.appium.java_client.ios.IOSDriver;
//import io.appium.java_client.ios.IOSElement;
//import io.appium.java_client.remote.HideKeyboardStrategy;
//import io.selendroid.client.SelendroidKeys;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//import org.openqa.selenium.By;
//import org.openqa.selenium.NoSuchElementException;
//import org.openqa.selenium.ScreenOrientation;
//import org.openqa.selenium.TimeoutException;
//import org.openqa.selenium.WebDriverException;
//import org.openqa.selenium.interactions.Actions;
//import org.openqa.selenium.interactions.touch.TouchActions;
//import org.openqa.selenium.support.ui.FluentWait;
//import org.testng.Assert;
//
//import com.google.common.base.Function;
//import com.softech.test.core.driver.DriverManager;
//import com.softech.test.core.lab.CommandExecutor;
//import com.softech.test.core.lab.GridManager;
//import com.softech.test.core.lab.LabDeviceManager;
//import com.softech.test.core.props.Orientation;
//import com.softech.test.core.util.Config;
//import com.softech.test.core.util.Constants;
//import com.softech.test.core.util.Logger;
//import com.softech.test.core.util.TestRun;
//
//public class MobileInteract {
//
//	private AppiumDriver<MobileElement> driver;
//    private AndroidDriver<MobileElement> androidDriver;
//    private IOSDriver<MobileElement> iosDriver;
//    private Integer timeoutInSec = 0;
//    private Integer pollingTime;
//    private static Integer smallPause = 1000;
//    private static Integer minorPause = 500;
//    
//    // locator data
//    private By locator;
//    private HashMap<String, String> locatorData;
//    private String simpleName;
//    
//    // element data
//    private Boolean elementPresent;
//    private Boolean childElementPresent;
//    private Boolean elementVisible;
//    private MobileElement mobileElement;
//    private List<MobileElement> allMobileElements;
//    private List<MobileElement> allChildElements;
//    private Boolean hasAttribute;
//    
//    public MobileInteract(By locator, HashMap<String, String> locatorData) {
//        driver = DriverManager.getAppiumDriver();
//        androidDriver = DriverManager.getAndroidDriver();
//        iosDriver = DriverManager.getIOSDriver();
//        timeoutInSec = Config.getMaxWaitTime();
//        pollingTime = Config.getPollingTime();
//        this.locator = locator;
//        this.locatorData = locatorData;
//        if (locator != null) {
//            simpleName = locatorData.get("SimpleName");
//        }
//    }
//
//    public WebInteract setToWebInteract() {
//    	return new WebInteract(locator, locatorData);
//    }
//    
//    public MobileInteract setTimeout(Integer timeoutInSec) {
//    	this.timeoutInSec = timeoutInSec;
//    	return this;
//    }
//    
//    public MobileInteract setPollingTime(Integer pollingTime) {
//    	this.pollingTime = pollingTime;
//    	return this;
//    }
//    
//    public MobileElement getMobileElement() {
//    	return mobileElement;
//    }
//    
//    public IOSElement getIOSElement() {
//    	return (IOSElement) mobileElement;
//    }
//    
//    public AndroidElement getAndroidElement() {
//    	return (AndroidElement) mobileElement;
//    }
//    
//    public By getElementLocator() {
//    	return locator;
//    }
//    
//    public String getElementSimpleName() {
//    	return simpleName;
//    }
//    
//    public List<MobileElement> getAllMobileElements() {
//    	return allMobileElements;
//    }
//    
//    public List<MobileElement> getAllChildMobileElements() {
//    	return allChildElements;
//    }
//    
//    /**********************************************************************************************
//     * Pauses the test action.
//     * 
//     * @param waitTime - {@link Integer} - The amount of time in milliseconds to pause.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract pause(Integer waitTimeInMS) {
//        Logger.logMessage("Pause for '" + waitTimeInMS + "' milliseconds.");
//        try {
//            Thread.sleep(waitTimeInMS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//
//    /**********************************************************************************************
//     * Pauses the test action.
//     *
//     * @param waitTime - {@link Integer} - The amount of time  to pause.
//     * @param units - {@link TimeUnit } - the time unit for the {@code waitTime} argument
//     * @author Everardo Lopez created April 22, 2016
//     * @version 1.0 April 22, 2016
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract pause(Integer waitTime, TimeUnit units) {
//        Logger.logMessage("Pause for '" + waitTime + "' " + units.name());
//        try {
//            units.sleep(waitTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return this;
//    }
//    
//    //ELEMENT PRESENCE
//    /**********************************************************************************************
//    * Determines if an element is present or not.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public Boolean isPresent() {
//    	elementPresent = false;
//    	try {
//    		mobileElement = driver.findElement(locator);
//    		elementPresent = true;
//    	} catch (Exception e) { 
//    		// element is not present
//    	}
//    	
//        Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
//        return elementPresent;
//    }
//
//    /**********************************************************************************************
//    * Determines if an element is present or not within a given polling period.
//    * 
//    * @param timeout - {@link Integer} - The max amount of time in seconds to poll for the element's presence.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.1 March 31, 2016
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public Boolean isPresent(Integer timeoutInSec) {
//    	try {
//    		new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
//    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(WebDriverException.class)
//    	            .until(new Function<By, Boolean>() {
//    	                @Override
//    	                public Boolean apply(final By loc) {
//    	                    elementPresent = false;
//    	                    mobileElement = driver.findElement(loc);
//    	            		elementPresent = true;
//    	                    return elementPresent;
//    	                }
//    	            });
//    	} catch (TimeoutException e) {
//    	    // ignore
//    	}
//    	Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
//    	return elementPresent;
//    }
//    
//    /**********************************************************************************************
//    * Determines if an element is visible or not.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public Boolean isVisible() {
//    	elementPresent = false;
//    	elementVisible = false;
//    	try {
//    		mobileElement = driver.findElement(locator);
//    		elementPresent = true;
//    		elementVisible = mobileElement.isDisplayed();
//    	} catch (Exception e) {
//    		// element is not present
//    	}
//    	
//        Logger.logMessage("Is the '" + simpleName + "' element visible: " + elementVisible.toString());
//        return elementVisible;
//    }
//
//    /**********************************************************************************************
//    * Determines if an element is present and visible or not within a given polling period.
//    * 
//    * @param timeout - {@link Integer} - The max amount of time in seconds to poll for the element's visibility.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.1 March 31, 2016
//    * @return Boolean - true or false value of the element's visibility
//    ***********************************************************************************************/
//    public Boolean isVisible(Integer timeoutInSec) {
//    	try {
//    		new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
//    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(WebDriverException.class)
//    	            .until(new Function<By, Boolean>() {
//    	                @Override
//    	                public Boolean apply(final By loc) {
//    	                	elementPresent = false;
//    	                	elementVisible = false;
//    	                	allMobileElements = driver.findElements(locator);
//    	                	if (allMobileElements.size() > 0) {
//    	                	    mobileElement = allMobileElements.get(0);
//    	                	    elementPresent = true;
//    	                	    elementVisible = mobileElement.isDisplayed();
//    	                	}
//    	                	return elementVisible;
//    	                }
//    	            });
//    	} catch (TimeoutException e) {
//    	    // ignore
//    	}
//    	
//        Logger.logMessage("Is the '" + simpleName + "' element visible: " + elementVisible.toString());
//        return elementVisible;
//    }
//    
//    /**********************************************************************************************
//     * Determines if a child element(s) is present within a parent element or not within a given polling time.
//     * 
//     * @param childSimpleName - {@link String} - The simple name of the child element(s).
//     * @param childLocator - {@link By} - The By locator of the child element(s).
//     * @param timeoutSeconds - {@link Integer} - The max amount of time in seconds to poll for the element's presence.
//     * @author Saney Alam created September 8, 2016
//     * @version 1.0 September 8, 2016
//     * @return Boolean - true or false value of the child element's presence
//     ***********************************************************************************************/
//	public Boolean isChildElementPresent(String childSimpleName, By childLocator, Integer timeoutInSeconds) {
//		try {
//    		new FluentWait<By>(locator).withTimeout(timeoutInSeconds, TimeUnit.SECONDS).pollingEvery(
//    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(WebDriverException.class)
//    	            .until(new Function<By, Boolean>() {
//    	                @Override
//    	                public Boolean apply(final By loc) {
//    	                    elementPresent = false;
//    	                    childElementPresent = false;
//    	                    mobileElement = driver.findElement(loc);
//    	                    elementPresent = true;
//    	                    allChildElements = mobileElement.findElements(childLocator);
//    	                    if (allChildElements.size() > 0) {
//    	                    	childElementPresent = true;
//    	                    }
//    	                    return childElementPresent;
//    	                }
//    	            });
//    	} catch (TimeoutException e) {
//    	    // ignore
//    	}
//    	Logger.logMessage("Is the '" + childSimpleName + "' element present in parent '" + simpleName + "': " + childElementPresent.toString());
//    	return childElementPresent;
//	}
//	
//    //ATTRIBUTE PRESENCE
//    /**********************************************************************************************
//    * Determines if an element has a specific attribute value or not.
//    * 
//    * @param attribute - {@link String} - The specific attribute type to evaluate.
//    * @param attributeValue - {@link String} - The value of the attribute to evaluate.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public Boolean hasAttribute(String attribute, String attributeValue) {
//        try {
//            if (mobileElement.getAttribute(attribute).equals(attributeValue)) {
//                hasAttribute = true;
//            } else {
//                hasAttribute = false;
//            }
//        } catch (WebDriverException e) {
//            hasAttribute = false;
//        }
//        Logger.logMessage("The '" + simpleName + "' element has attribute '" + attribute + "' with value "
//        		+ "'" + attributeValue + "': " + hasAttribute.toString());
//        return hasAttribute;
//    }
//    
//    //ELEMENT WAITS
//    /**********************************************************************************************
//    * Waits for an element to be present before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element is not present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForPresent() {
//        Logger.logMessage("Verify the '" + simpleName + "' element is present.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not present.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    elementPresent = false;
//                    mobileElement = driver.findElement(locator);
//                    elementPresent = true;
//                    return elementPresent;
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for all/multiple instance of an element to be present before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - All instances of an element are not present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForAllPresent() {
//        Logger.logMessage("Verify the '" + simpleName + "' elements are present.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(simpleName + " elements with locator '" + locator.toString() + "' are not present.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    allMobileElements = driver.findElements(loc);
//                    return allMobileElements.size() > 0;
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for an element to not be present before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element is present during the entire given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForNotPresent() {
//        Logger.logMessage("Verify the '" + simpleName + "' element is NOT present.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is present and should NOT be.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    elementPresent = true;
//                    allMobileElements = driver.findElements(loc);
//                    if (allMobileElements.size() == 0) {
//                        elementPresent = false;
//                    }
//                    return !elementPresent;
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for an element to be present and visible before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element is not visible within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForVisible() {
//        Logger.logMessage("Verify the '" + simpleName + "' element is visible.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not visible.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                	mobileElement = driver.findElement(loc);
//            		return mobileElement.isDisplayed();
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for an element to not be visible before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element is visible during the entire given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForNotVisible() {
//        Logger.logMessage("Verify the '" + simpleName + "' element is NOT visible.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is visible and should not be.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    elementPresent = false;
//                    mobileElement = driver.findElement(locator);
//            		elementPresent = true;
//                    elementVisible = mobileElement.isDisplayed();
//                    return elementPresent && !elementVisible;
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for an element to not be present, or be present but not visible before timing out.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element is present/visible during the entire given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForNotPresentOrVisible() {
//    	Logger.logMessage("Verify the '" + simpleName + "' element is NOT present, or present "
//            + "but not visible.");
//        
//        for (int i = 0; i <= timeoutInSec; i++) {
//        	if (i == timeoutInSec) {
//        		Assert.fail(simpleName + " element with locator '" + locator.toString() + "' is present, or present and visible, and should not be.");
//        	}
//        	
//        	elementPresent = false;
//        	elementVisible = false;
//        	
//        	try {
//        		allMobileElements = driver.findElements(locator);
//            	if (allMobileElements.size() > 0) {
//            	    elementPresent = true;
//            	    mobileElement = allMobileElements.get(0);
//            	}
//            	Logger.logMessage(simpleName + " element present: " + elementPresent);
//            	
//            	if (elementPresent) {
//            		elementVisible = mobileElement.isDisplayed();
//            	}
//            	Logger.logMessage(simpleName + " element visible: " + elementVisible);
//        	} catch (Exception e) {
//        		// ignore
//        	}
//        	
//        	if (!elementPresent || !elementVisible) {
//        		break;
//        	}
//            
//            this.pause(smallPause);
//        }
//        return this;
//    }
//     
//    /**********************************************************************************************
//    * Waits for an attribute to be present before timing out.
//    * 
//    * @param attribute - {@link String} - The specific attribute type to evaluate.
//    * @param attributeValue - {@link String} - The specific attribute value to evaluate.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An attribute with value is not present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForAttribute(final String attribute, final String attributeValue) {
//        Logger.logMessage("Verify the '" + simpleName + "' element attribute '" + attribute + "' equals '" 
//                + attributeValue + "'.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage("Attribute '" + attribute + "' with value '" + attributeValue + "' not present "
//                + "in " + simpleName + " element with locator '" + locator.toString() + "'.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                	mobileElement = driver.findElement(locator);
//            		return mobileElement.getAttribute(attribute).contains(attributeValue);
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for an attribute to NOT be present before timing out.
//    * 
//    * @param attribute - {@link String} - The specific attribute type to evaluate.
//    * @param attributeValue - {@link String} - The specific attribute value to evaluate.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An attribute with value is present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForNotAttribute(final String attribute, final String attributeValue) {
//        Logger.logMessage("Verify the '" + simpleName + "' element attribute '" + attribute + "' does NOT equal '" 
//                + attributeValue + "'.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage("Attribute '" + attribute + "' with value '" + attributeValue + "' present "
//                + "in '" + simpleName + " element with locator '" + locator.toString() + "' and should NOT be.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                	mobileElement = driver.findElement(loc);
//            		return !mobileElement.getAttribute(attribute).contains(attributeValue);
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//    * Waits for text to be present in an element before timing out.
//    * 
//    * @param text - {@link String} - The text to evaluate.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element's text is not present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForText(final String text) {
//        Logger.logMessage("Verify the '" + simpleName + "' element text equals '" + text + "'.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage("Text '" + text + "' not present in " + simpleName + " element with locator '" + locator.toString() + "'.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    mobileElement = driver.findElement(loc);
//                    return mobileElement.getText().contains(text);
//                }
//            });
//        return this;
//    }
//     
//    /**********************************************************************************************
//    * Waits for text to NOT be present in an element before timing out.
//    * 
//    * @param text - {@link String} - The text to evaluate.
//    * @author Saney Alam created December 20, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - An element's text is present within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForNotText(final String text) {
//        Logger.logMessage("Verify the '" + simpleName + "' element text does NOT equal '" + text + "'.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage("Text '" + text + "' present in " + simpleName + "' element with locator '" + locator.toString() + "' and should not be.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                    mobileElement = driver.findElement(loc);
//                    return !mobileElement.getText().contains(text);
//                }
//            });
//        return this;
//    }
//     
//    /**********************************************************************************************
//    * Waits for the screen orientation to be either Portrait or Landscape before timing out.
//    * 
//    * @param text - {@link ScreenOrientation} - The Screen Orientation to evaluate.
//    * @author Saney Alam created December 28, 2015 
//    * @version 1.0 December 28, 2015
//    * @return Fluid instance of the Interact class
//    * @exception TimeoutException - The Screen Orientation is not correct within the given timeout period.
//    ***********************************************************************************************/
//    public MobileInteract waitForScreenOrientation(final Orientation orientation) {
//        Logger.logMessage("Verify the Screen Orienation equals '" + orientation.toString() + "'.");
//        this.driverWait()
//            .withMessage("Screen Orientation does not equal '" + orientation.toString() + "'.")
//            .until(new Function<AppiumDriver<MobileElement>, Boolean>() {
//                @Override
//                public Boolean apply(final AppiumDriver<MobileElement> dr) {
//                	Boolean success = false;
//                	if (TestRun.isAndroid()) {
//                		success = dr.getOrientation().toString().equals(orientation.toString());
//                	} else {
//                		success = iosDriver.executeScript("UIATarget.localTarget().frontMostApp()"
//                				+ ".interfaceOrientation()").toString().equals(orientation.value().toString());
//                	}
//                	return success;
//                }
//            });
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Waits for children elements to be present in a parent element before timing out.
//     * 
//     * @param childSimpleName - {@link String} - The simple name of the child element(s).
//     * @param childLocator - {@link By} - The By locator of the child element(s).
//     * @author Saney Alam created September 8, 2016
//     * @version 1.0 September 8, 2016
//     * @return Fluid instance of the Interact class
//     * @exception TimeoutException - Children elements are not present within the given timeout period.
//     ***********************************************************************************************/
//	public MobileInteract waitForChildElementsPresent(String childSimpleName, By childLocator) {
//		Logger.logMessage("Verify the '" + childSimpleName + "' elements are present in parent '" + simpleName + "'.");
//        this.byWait(locator)
//            .ignoring(WebDriverException.class)
//            .withMessage(childSimpleName + " elements with locator '" + childLocator.toString() + "' are not present"
//            		+ " in parent '" + simpleName + "' element with locator '" + locator.toString() + "'.")
//            .until(new Function<By, Boolean>() {
//                @Override
//                public Boolean apply(final By loc) {
//                	mobileElement = driver.findElement(loc);
//                	allChildElements = mobileElement.findElements(childLocator);
//                    return allChildElements.size() > 0;
//                }
//            });
//        return this;
//	}
//	
//     //ELEMENT SCROLLED TO
//    /**********************************************************************************************
//     * Waits for an element to be scrolled to and visible.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.1 December 15, 2015
//     * @return Fluid instance of the Interact class
//     * @exception NoSuchElementException - An element is not visible after scroll within the given timeout period.
//     ***********************************************************************************************/
//    public MobileInteract waitForScrolledTo() {
//        String locatorWithScroll = "";
//        
//        if (TestRun.isIos()) {
//            this.waitForPresent();
//        }
//        
//        if (!this.isVisible()) {
//            Logger.logMessage("Scroll to the '" + simpleName + "' element.");
//            if (TestRun.isIos()) {
//            	Assert.fail("Method not supported for iOS. Use a different interaction method!");
//            	// TODO = this might be possible with native xcuitest. Research and implement in future releases.
//            	locatorWithScroll = locator.toString().replace("By.IosUIAutomation: ", "") 
//                    + ".scrollToVisible()";
//                driver.findElement(MobileBy.IosUIAutomation(locatorWithScroll));  
//            	mobileElement = driver.findElement(locator);
//            } else {
//            	locatorWithScroll = locator.toString().replace("By.AndroidUIAutomator: ", 
//                        "new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView"
//                        + "(new UiSelector().") + ".instance(0))";
//                driver.findElement(MobileBy.AndroidUIAutomator(locatorWithScroll));
//                mobileElement = driver.findElement(locator);
//            }
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * ANDROID ONLY - Waits for an element to be visible while scrolling within a given scroll view index.
//     * 
//     * @param scroolViewIndex - {@link Integer} - The index of the scroll view to scroll within.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     * @exception NoSuchElementException - An element is not visible after scroll within the given timeout period.
//     ***********************************************************************************************/
//    public MobileInteract waitForScrolledTo(Integer scrollViewIndex) {
//        Logger.logMessage("Scroll to the '" + simpleName + "' element.");
//        String locatorWithScroll = "";
//        if (!this.isVisible()) {
//            locatorWithScroll = locator.toString().replace("By.AndroidUIAutomator: ", 
//                    "new UiScrollable(new UiSelector().scrollable(true).instance(" 
//                    + scrollViewIndex.toString() + ")).scrollIntoView(new UiSelector().") + ".instance(0))";
//            driver.findElement(MobileBy.AndroidUIAutomator(locatorWithScroll));
//            mobileElement = driver.findElement(locator);
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Waits for an element to be scrolled to and visible via provided scrollable coordinates.
//     * 
//     * @param maxScrolls - {@link Integer} - The max amount of scrolls to execute.
//     * @param startX - {@link Integer} - The absolute start X coordinate of the scroll.
//     * @param startY - {@link Integer} - The absolute start Y coordinate of the scroll.
//     * @param endX - {@link Integer} - The end X coordinate of the scroll offset from the start X coordinate. For example
//     * a startX value of 200 with an endX value of 100 will execute a scroll to 300.
//     * @param endY - {@link Integer} - The end Y coordinate of the scroll offset from the start Y coordinate. For example
//     * a startY value of 200 with an endY value of 100 will execute a scroll to 300.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.1 December 15, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
////    public MobileInteract waitForScrolledTo(Integer maxScrolls, Integer startX, Integer startY, Integer endX, Integer endY) {
////    	int scrollIter = 0;
////        while (scrollIter <= maxScrolls && this.isVisible().equals(false)) {
////            if (scrollIter == maxScrolls) {
////                Assert.fail("Element not visible after '" + maxScrolls + "' scrolls.");
////            }
////            this.scroll(1, startX, startY, endX, endY);
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Waits for an element to be visible within a max amount of right flicks.
//     * 
//     * @param maxScrolls - {@link Integer} - The max amount of right flicks to execute.
//     * @param startXFromRight - {@link Integer} - The starting x coordinate from the right of the screen.
//     * @param endXFromLeft - {@link Integer} - The ending x coordinate from the left of the screen.
//     * @author Saney Alam created November 15, 2015 
//     * @version 1.0 November 15, 2015
//     * @return Fluid instance of the Interact class
//     * @exception NoSuchElementException - An element is not visible after flick within the given timeout period.
//     ***********************************************************************************************/
////    public MobileInteract waitForFlickedRightTo(Integer maxScrolls, Integer startXFromRight, Integer endXFromLeft) {
////        int scrollIter = 0;
////        while (scrollIter <= maxScrolls && this.isVisible().equals(false)) {
////            if (scrollIter == maxScrolls) {
////                Assert.fail("Element not visible after '" + maxScrolls + "' flicks right.");
////            }
////            this.flickRight(1, startXFromRight, endXFromLeft);
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Waits for an element to be visible within a max amount of left flicks.
//     * 
//     * @param maxScrolls - {@link Integer} - The max amount of left flicks to execute.
//     * @param startXFromLeft - {@link Integer} - The starting x coordinate from the left of the screen.
//     * @param endXFromRight - {@link Integer} - The ending x coordinate from the right of the screen.
//     * @author Saney Alam created November 15, 2015 
//     * @version 1.0 November 15, 2015
//     * @return Fluid instance of the Interact class
//     * @exception NoSuchElementException - An element is not visible after flick within the given timeout period.
//     ***********************************************************************************************/
////    public MobileInteract waitForFlickedLeftTo(Integer maxScrolls, Integer startXFromLeft, Integer endXFromRight) {
////        int scrollIter = 0;
////        while (scrollIter <= maxScrolls && this.isVisible().equals(false)) {
////            if (scrollIter == maxScrolls) {
////                Assert.fail("Element not visible after '" + maxScrolls + "' flicks left.");
////            }
////            this.flickLeft(1, startXFromLeft, endXFromRight);
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Waits for an element to be visible within a max amount of up scrolls.
//     * 
//     * @param maxScrolls - {@link Integer} - The max amount of up scrolls to execute.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     * @exception NoSuchElementException - An element is not visible after scroll within the given timeout period.
//     ***********************************************************************************************/
//    public MobileInteract waitForScrolledUpTo(Integer maxScrolls, Integer startYFromBottom, Integer endYFromStart) {
//        int scrollIter = 0;
//        while (scrollIter <= maxScrolls && this.isVisible().equals(false)) {
//            if (scrollIter == maxScrolls) {
//                Assert.fail("Element not visible after '" + maxScrolls + "' scrolls up.");
//            }
//            this.scrollUp(1, startYFromBottom, endYFromStart);
//            scrollIter++;
//        }
//        return this;
//    }
//    
//    //UI INTERACTIONS
//    /**********************************************************************************************
//     * Drags screen from one x and y coordinate to another.
//     * 
//     * @param xStart - {@link Integer} - The horizontal starting x coordinate.
//     * @param yStart - {@link Integer} - The vertical starting y coordinate.
//     * @param xEnd - {@link Integer} - The horizontal ending x coordinate.
//     * @param yEnd - {@link Integer} - The vertical ending y coordinate.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
////    public MobileInteract dragFromTo(Integer xStart, Integer yStart, Integer xEnd, Integer yEnd) {
////        Logger.logMessage("Drag from point (" + xStart + "," + yStart + ") to point (" + xEnd + "," 
////            + yEnd + ").");
////        new TouchAction(driver).press(xStart, yStart).moveTo(xEnd, yEnd).release().perform();
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Scrolls screen from one x and y coordinate to another.
//     * 
//     * @param xStart - {@link Integer} - The horizontal starting x coordinate.
//     * @param yStart - {@link Integer} - The vertical starting y coordinate.
//     * @param xEnd - {@link Integer} - The horizontal ending x coordinate.
//     * @param yEnd - {@link Integer} - The vertical ending y coordinate.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
////    public MobileInteract scrollFromTo(Integer xStart, Integer yStart, Integer xEnd, Integer yEnd, Integer duration) {
////        Logger.logMessage("Scroll from point (" + xStart + "," + yStart + ") to point (" + xEnd + "," 
////                + yEnd + ").");
////        if (TestRun.isAndroid()) {
////        	driver.swipe(xStart, yStart, xEnd, yEnd, duration);
////        } else {
////        	if (TestRun.isXCUITest()) {
////        		driver.swipe(xStart, yStart, xEnd, yEnd, duration);
////        	} else {
////        		iosDriver.executeScript("UIATarget.localTarget()."
////        				+ "dragFromToForDuration({x:" + xStart + ", y:" + yStart + "}, {x:" + xEnd + ", y:"
////                        + yEnd + "}, 1)");
////        	}
////        }
////        return this;
////    }
//    
////    public MobileInteract flickRight(Integer numOfFlicks, Integer startXFromRight, Integer endXFromLeft) {
////        int scrollIter = 0;
////        Integer xStart = driver.manage().window().getSize().getWidth() - startXFromRight;
////        Integer xEnd = endXFromLeft;
////        Integer y = driver.manage().window().getSize().getHeight() / 2;
////        while (scrollIter < numOfFlicks) {
////            Logger.logMessage("Flick right.");
////            driver.swipe(xStart, y, xEnd, y, 250);
////            scrollIter++;
////        }
////        return this;
////    }
//    
////    public MobileInteract flickLeft(Integer numOfFlicks, Integer startXFromLeft, Integer endXFromRight) {
////        int scrollIter = 0;
////        Integer xStart = startXFromLeft;
////        Integer xEnd = driver.manage().window().getSize().getWidth() - endXFromRight;
////        Integer y = driver.manage().window().getSize().getHeight() / 2;
////        while (scrollIter < numOfFlicks) {
////            Logger.logMessage("Flick left.");
////            driver.swipe(xStart, y, xEnd, y, 250);
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Scrolls up a specific number of scrolls relative to coordinates provided from the bottom of the screen
//     * 
//     * @param numOfScrolls - {@link Integer} - The number of scrolls up to execute.
//     * @param startYFromBottom - {@link Integer} - The starting point of the y scroll offset from the bottom of the screen.
//     * @param endYFromBottom - {@link Integer} - The ending point of the y scroll offset from the bottom of the screen.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
////    public MobileInteract scrollUp(Integer numOfScrolls, Integer startYFromBottom, Integer endYFromStart) {
////        int scrollIter = 0;
////        while (scrollIter < numOfScrolls) {
////        	Integer x = driver.manage().window().getSize().getWidth() / 2;
////        	Integer startY = driver.manage().window().getSize().getHeight() - startYFromBottom;
////        	Integer endY = startY + endYFromStart;
////        	Logger.logMessage("Scroll up with starting coordinates " + x + "," + startY + " and ending coordinates " + x + "," + endY + ".");
////            driver.swipe(x, startY, x, endY, minorPause);
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Scrolls a specific number of scrolls relative to coordinates provided from the bottom of the screen
//     * 
//     * @param numOfScrolls - {@link Integer} - The number of scroll actions to execute.
//     * @param startX - {@link Integer} - The starting x point of the scroll.
//     * @param startY - {@link Integer} - The starting y point of the scroll.
//     * @param endX - {@link Integer} - The end x point offset from the startX point. So a startX value of 200 and
//     * an endX value of 100 will result in a scroll to x point 300. Use a negative endX value as needed.
//     * @param endY - {@link Integer} - The end y point offset from the startY point. So a startY value of 200 and
//     * an endY value of 100 will result in a scroll to y point 300. Use a negative endY value as needed.
//     * @author Saney Alam created February 16, 2016
//     * @version 1.0 February 16, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
////    public MobileInteract scroll(Integer numOfScrolls, Integer startX, Integer startY, Integer endX, Integer endY) {
////        int scrollIter = 0;
////        while (scrollIter < numOfScrolls) {
////        	Logger.logMessage("Scroll with starting coordinates " + startX + "," + startY + " offset to coordinates " + endX + "," + endY + ".");
////            new TouchAction(driver).press(startX, startY).moveTo(endX, endY).release().perform();
////            scrollIter++;
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//    * Taps a given x and y coordinate on the screen.
//    * 
//    * @param xCoord - {@link Integer} - The specific x coordinate to tap.
//    * @param yCoord - {@link Integer} - The specific y coordinate to tap.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
////    public MobileInteract tap(Integer xCoord, Integer yCoord) {
////        Logger.logMessage("Tap at point (" + xCoord + "," + yCoord + ").");
////        if (TestRun.isSelendroid()) {
////        	new TouchActions(DriverManager.getAppiumDriver()).down(xCoord, yCoord).up(xCoord, yCoord).perform();
////        } else {
////            driver.tap(1, xCoord, yCoord, minorPause);
////        }
////        return this;
////    }
//    
//    /**********************************************************************************************
//     * Taps a given x and y coordinate on the screen using ADB. Useful for rare scenarios where appium
//     * doesn't know about an element outside of its viewport.
//     * 
//     * @param xCoord - {@link Integer} - The specific x coordinate to tap.
//     * @param yCoord - {@link Integer} - The specific y coordinate to tap.
//     * @author Saney Alam created June 22, 2017
//     * @version 1.0 June 22, 2017
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//     public MobileInteract tapWithADB(Integer xCoord, Integer yCoord) {
//    	 if (!TestRun.isAndroid()) {
//    		 throw new RuntimeException("The tapWithADB method is for Android only.");
//    	 }
//         Logger.logMessage("Tap at point (" + xCoord + "," + yCoord + ") via ADB.");
//         String adbPath = null;
//         
//         if (GridManager.isEC2Agent()) {
//        	 adbPath = Constants.ADB_PATH;
//         } else {
//        	 String adbProp = System.getProperty("system.test.adbpath");
//        	 if (adbProp != null) {
//            	 adbPath = adbProp;
//             } else {
//            	 throw new RuntimeException("Could not find adb path property. Are you executing locally "
//            	 	+ "and did you set the 'system.test.adbpath' property pointing to your adb installation?");
//             }
//         }
//         
//         String targetAddress = null;
//         if (GridManager.isEC2Agent()) {
//        	 targetAddress = GridManager.getRunningSessionIP();
//         }
//         CommandExecutor.execCommand(adbPath + " shell input tap " + xCoord + " " + yCoord, targetAddress, null);
//         return this;
//     }
//
//    /**********************************************************************************************
//    * Taps the identified mobile element.
//    * 
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public MobileInteract tap() {
//    	Logger.logMessage("Tap the '" + simpleName + "' element.");
//        mobileElement.click();
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Taps the center of the identified mobile element.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//     public MobileInteract tapCenter() {
//         Logger.logMessage("Tap the center of the '" + simpleName + "' " + "element.");
//         Integer xPoint = mobileElement.getCenter().x;
//         Integer yPoint = mobileElement.getCenter().y;
//         new TouchAction(driver).tap(xPoint, yPoint).perform();
//         return this;
//     }
//     
//    /**********************************************************************************************
//     * Taps the x and y coordinate offset from the identified mobile element.
//     * 
//     * @param xCoord - {@link Integer} - The specific x coordinate to tap.
//     * @param yCoord - {@link Integer} - The specific y coordinate to tap.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//     public MobileInteract tapOffSetElement(Integer xCoord, Integer yCoord) {
//         Logger.logMessage("Tap at point (" + xCoord + "," + yCoord + ") offset from the '" + simpleName + "' "
//                     + "element.");
//         new TouchAction(driver).tap(mobileElement, xCoord, yCoord).perform();
//         return this;
//     }
//
//    /**********************************************************************************************
//    * Types a string of text into an element.
//    * 
//    * @param text - {@link String} - The string that will be typed into the element.
//    * @author Saney Alam created October 1, 2015 
//    * @version 1.0 October 1, 2015
//    * @return Fluid instance of the Interact class
//    ***********************************************************************************************/
//    public MobileInteract type(String text) {
//        Logger.logMessage("Type '" + text + "' into the '" + simpleName + "' element.");
//        mobileElement.sendKeys(text);
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Switch to a webview as identified by its index.
//     * 
//     * @param webIndex - {@link Integer} - The index of the webview you wish you to switch focus to. Note that this
//     * param is ignored if there is only 1 webview present.
//     * @author Saney Alam created June 14, 2016
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//     public MobileInteract switchToWebView(Integer webIndex) {
//    	 if (!this.isWebViewPresent()) {
//    		 throw new RuntimeException("There are no webviews present!");
//    	 } else {
//    		 Set<String> contexts = driver.getContextHandles();
//             if (contexts.size() == 2) {
//            	 Logger.logMessage("Switching to the only available webview.");
//            	 for (String context : contexts) {
//            		 if (context.toString().contains("WEBVIEW")) {
//            			 driver.context(context);
//            		 }
//            	 }
//             } else {
//            	 Logger.logMessage("Switching to webview with index '" + webIndex + "'.");
//            	 driver.context("WEBVIEW_" + webIndex.toString());
//             }
//    	 }
//         
//         
//         return this;
//     }
//     
//     public Boolean isWebViewPresent() {
//    	 Boolean webViewPresent = false;
//         Set<String> contexts = driver.getContextHandles();
//         for (String context : contexts) {
//        	 if (context.toString().contains("WEBVIEW")) {
//        		 webViewPresent = true;
//        		 break;
//        	 }
//         }
//         
//         if (TestRun.isIos() && GridManager.isQALabHub() && !webViewPresent) {
//        	 // check if the ios webkitdebugger is running
//        	 String sessionIP = GridManager.getRunningSessionIP();
//        	 
//        	 Integer webProxyIter = 0;
//        	 while (webProxyIter <= 3) {
//        		 if (webProxyIter == 3) {
//        			 throw new RuntimeException("IOS Webkit Debug Proxy did not start after 3 iterations.");
//        		 }
//        		 
//        		 if (!GridManager.isiOSWebDebugProxyRunning(sessionIP)) {
//            		 Logger.logConsoleMessage("IOS webkit debug proxy is not running. Restarting it.");
//            		 CommandExecutor.execCommand("open " + Constants.IOS_STOP_WEB_DEBUGGER_PATH, sessionIP, null);
//            		 try { Thread.sleep(250); } catch (InterruptedException e) { }
//            		 CommandExecutor.execCommand("open " + Constants.IOS_START_WEB_DEBUGGER_PATH, sessionIP, null);
//            		 try { Thread.sleep(250); } catch (InterruptedException e) { }
//            	 } else {
//            		 break;
//            	 }
//        		 webProxyIter++;
//        	 }
//        	 
//    		 for (String context : contexts) {
//            	 if (context.toString().contains("WEBVIEW")) {
//            		 webViewPresent = true;
//            		 break;
//            	 }
//             }
//    	 }
//         
//         return webViewPresent;
//     }
//     
//     /**********************************************************************************************
//      * Switch to the native app view.
//      * 
//      * @author Saney Alam created June 14, 2016
//      * @version 1.0 October 1, 2015
//      * @return Fluid instance of the Interact class
//      ***********************************************************************************************/
//      public MobileInteract switchToNativeView() {
//          driver.context("NATIVE_APP");
//          return this;
//      }
//    
//    /**********************************************************************************************
//     * Rotates the device screen.
//     * 
//     * @param orientation - {@link ScreenOrientation} - The screen orientation of device.
//     * @author Saney Alam created October 1, 2015 
//     * @version 2.0 December 28, 2015
//     * @return Fluid instance of the Interact class
//     * @exception TimeoutException - The device failed to be rotated to the given orientation after timeout.
//     ***********************************************************************************************/
//    public MobileInteract rotateScreen(ScreenOrientation orientation) {
//        Logger.logMessage("Rotate the screen to the '" + orientation.toString() + "' orientation.");
//        driver.rotate(orientation);
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Closes the soft keyboard.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     * @exception WebDriverException - Soft keyboard is not present.
//     ***********************************************************************************************/
//    public MobileInteract closeKeyboard() {
//        if (TestRun.isAndroid()) {
//            Logger.logMessage("Close the keyboard.");
//            try {
//            	androidDriver.hideKeyboard();
//            } catch (WebDriverException e) {
//                // ignore
//            }
//        } else {
//        	if (TestRun.isXCUITest()) {
//    			Logger.logConsoleMessage("Hide keyboard options not enabled for iOS 10 due"
//    					+ " to an underlying issue with XCUITest. The user must dismiss/interact with the"
//    					+ " keyboard within the usual workflow of the app.");
//    		} else {
//    			try {
//            		iosDriver.hideKeyboard(HideKeyboardStrategy.PRESS_KEY, "Done");
//            	} catch (Exception e) {
//            		iosDriver.hideKeyboard();
//            	}
//    		}
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Verifies the soft keyboard is closed.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     * @exception WebDriverException - The soft keyboard is open.
//     ***********************************************************************************************/
//    public MobileInteract verifyKeyboardNotPresent() {
//        Logger.logMessage("Verify the soft keyboard is closed.");
//        Boolean keyboardPresent = true;
//        if (TestRun.isAndroid()) {
//            try {
//                driver.hideKeyboard();
//                keyboardPresent = false;
//            } catch (WebDriverException e) {
//                keyboardPresent = true;
//            }
//        } else {
//            try {
//        		iosDriver.findElement(MobileBy.IosUIAutomation("UIATarget.localTarget().frontMostApp().keyboard()"));
//        		keyboardPresent = true;
//        	} catch (NoSuchElementException e) {
//        		keyboardPresent = false;
//        	}
//        }
//        Assert.assertFalse(keyboardPresent, "The soft keyboard is present.");
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Locks the device for a duration.
//     * 
//     * @author Saney Alam created November 24, 2015 
//     * @version 1.0 November 24, 2015
//     * @param duration - {@link Integer} - The duration to lock the screen (seconds).
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract lock(Integer duration) {
//        Logger.logMessage("Lock the device for '" + duration + "' second(s).");
//        if (TestRun.isAndroid()) {
//            androidDriver.lockDevice();
//            sleepSilently(duration);
//            if (GridManager.isQALabHub()) {
//            	LabDeviceManager.unlockDevice(GridManager.getRunningSessionIP());
//            } else {
//                androidDriver.unlockDevice();
//            }
//        } else {
//        	iosDriver.lockDevice(duration);
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Backgrounds the app for a duration.
//     * 
//     * @author Saney Alam created November 24, 2015 
//     * @version 1.0 November 24, 2015
//     * @param duration - {@link Integer} - The duration to background the app (seconds).
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract background(Integer duration) {
//        Logger.logMessage("Background the app for '" + duration + "' second(s).");
//        driver.runAppInBackground(duration);
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * ANDROID ONLY - Taps the Android back button.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract goBack() {
//        Logger.logMessage("Tap the back button");
//        if (TestRun.isSelendroid()) {
//        	new Actions(androidDriver).sendKeys(SelendroidKeys.BACK).perform();
//        } else {
//            androidDriver.pressKeyCode(AndroidKeyCode.BACK);
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * ANDROID ONLY Taps the submit/enter button.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract submit() {
//        Logger.logMessage("Tap the submit button");
//        if (TestRun.isAndroid()) {
//        	if (TestRun.isSelendroid()) {
//        		new Actions(androidDriver).sendKeys(SelendroidKeys.ENTER).perform();
//        	} else {
//                androidDriver.pressKeyCode(AndroidKeyCode.ENTER);
//        	}
//        } else {
//            throw new WebDriverException("Not supported.");
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * IOS ONLY Sets the text value of the element.
//     * 
//     * @author Saney Alam created April 19, 2016 
//     * @version 1.0 April 19, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract setValue(String value) {
//        Logger.logMessage("Tap the submit button");
//        if (TestRun.isIos()) {
//            getIOSElement().setValue(value);
//        } else {
//            throw new WebDriverException("Not supported.");
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * ANDROID ONLY - Opens the Android menu.
//     * 
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.0 October 1, 2015
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract openMenu() {
//        Logger.logMessage("Tap the menu button.");
//        if (TestRun.isSelendroid()) {
//        	new Actions(androidDriver).sendKeys(SelendroidKeys.MENU).perform();
//        } else {
//            androidDriver.pressKeyCode(AndroidKeyCode.MENU);
//        }
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * ANDROID ONLY - Taps the Android home button.
//     * 
//     * @author Saney Alam created May 5, 2016 
//     * @version 1.0 May 5, 2016
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract tapHomeButton() {
//        Logger.logMessage("Tap the home button.");
//        androidDriver.pressKeyCode(AndroidKeyCode.HOME);
//        return this;
//    }
//    
//    /**********************************************************************************************
//     * Clears the text from an element.
//     * 
//     * @param charLimit - {@link Integer} - The number or characters to delete for Android. Set to null
//     * for iOS as all characters will automatically be removed.
//     * @author Saney Alam created October 1, 2015 
//     * @version 1.1 April 19, 2016
//     * @return Fluid instance of the Interact class
//     ***********************************************************************************************/
//    public MobileInteract clearText(Integer charLimit) {
//        Logger.logMessage("Clear the text in the '" + simpleName + "' element.");
//        if (TestRun.isAndroid()) {
//            if (!TestRun.isSelendroid()) {
//            	Integer y = mobileElement.getCenter().y;
//                Integer farX = mobileElement.getSize().width - 10;
//                this.tap(farX, y);
//            }
//            for (int i = 0; i < charLimit; i++) {
//            	if (TestRun.isSelendroid()) {
//            		new Actions(androidDriver).sendKeys(SelendroidKeys.DPAD_RIGHT).perform();
//            		new Actions(androidDriver).sendKeys(SelendroidKeys.DEL).perform();
//            	} else {
//                    androidDriver.pressKeyCode(AndroidKeyCode.DEL);
//            	}
//            }
//        } else {
//        	getIOSElement().setValue("");
//        }
//        return this;
//    }
//    
//    private void sleepSilently(Integer seconds) {
//    	try {
//			Thread.sleep(seconds * 1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//    }
//    
//    // initiate fluent waits
//    private FluentWait<By> byWait(final By locator) {
//        return new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
//            pollingTime, TimeUnit.MILLISECONDS);
//    }
//
//    private FluentWait<AppiumDriver<MobileElement>> driverWait() {
//        return new FluentWait<AppiumDriver<MobileElement>>(driver).withTimeout(timeoutInSec, TimeUnit.SECONDS)
//                .pollingEvery(pollingTime, TimeUnit.MILLISECONDS);
//    }
//
//}
