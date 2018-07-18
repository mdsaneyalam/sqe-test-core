package com.softech.test.core.interact;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScreenOrientation;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.google.common.base.Function;
import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.util.Config;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.TestRun;

public class WebInteract {

    private RemoteWebDriver driver;
    private Integer timeoutInSec = 0;
    private Integer pollingTimeInMS;
    private static Integer smallPauseInMS = 500;
    private final String IFRAME = "//iframe";
    
    // locator data
    private By locator;
    private HashMap<String, String> locatorData;
    private String simpleName;
    
    // element data
    private Boolean elementPresent;
    private Boolean elementVisible;
    private WebElement webElement;
    private List<WebElement> allWebElements;
    private List<WebElement> allChildElements;
    private Boolean hasAttribute;
    
    public WebInteract(By locator, HashMap<String, String> locatorData) {
    	if (TestRun.isMobileWeb()) {
    		driver = DriverManager.getAppiumDriver();
    	} else {
    		driver = DriverManager.getWebDriver();
    	}
        timeoutInSec = Config.getMaxWaitTime();
        pollingTimeInMS = Config.getPollingTime();
        this.locator = locator;
        this.locatorData = locatorData;
        if (locator != null) {
            simpleName = locatorData.get("SimpleName");
        }
        
        // sets a small implicit wait to prevent SO timeout
        if (!TestRun.isMobileWeb()) {
        	driver.manage().timeouts().implicitlyWait(50, TimeUnit.MILLISECONDS);
        }
    }

    public WebInteract setLocator(By locator) {
    	this.locator = locator;
    	return this;
    }
    
    public WebInteract setSimpleName(String simpleName) {
    	this.simpleName = simpleName;
    	return this;
    }
    
//    public MobileInteract setToMobileInteract() {
//    	return new MobileInteract(locator, locatorData);
//    }
    
    public WebInteract setTimeout(Integer timeoutInSec) {
    	this.timeoutInSec = timeoutInSec;
    	return this;
    }
    
    public WebInteract setPollingTime(Integer pollingTimeInMS) {
    	this.pollingTimeInMS = pollingTimeInMS;
    	return this;
    }
    
    public WebElement getWebElement() {
    	return webElement;
    }
    
    public By getElementLocator() {
    	return locator;
    }
    
    public String getElementSimpleName() {
    	return simpleName;
    }
    
    public List<WebElement> getAllWebElements() {
    	return allWebElements;
    }
    
    public List<WebElement> getAllChildWebElements() {
    	return allChildElements;
    }
    
    /**********************************************************************************************
	 * Get parent WebElement from child WebElement
	 * 
	 * @param childElement {@link WebElement} - Child element
	 * @param parentLevel (@link int) - parent level above child
	 * @author Saney Alam created February 28, 2018           
	 * @return {@link WebElement} - Parent Element
	***********************************************************************************************/
	public WebElement getParentFromChild(WebElement childElement,
			int parentLevel) {
		Logger.logMessage("Getting parent element from child element.");
		String parentNodeXPath = "parent::node()";
		WebElement parent = null, subParent = null;
		for (int i = 1; i <= parentLevel; ++i) {
			if (i == 1) {
				subParent = childElement;
			} else {
				subParent = subParent.findElement(By.xpath(parentNodeXPath));
			}
		}

		parent = subParent.findElement(By.xpath(parentNodeXPath));
		return parent;
	}
     
    /**********************************************************************************************
     * Pauses the test action.
     * 
     * @param waitTime - {@link Integer} - The amount of time in milliseconds to pause.
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract pause(Integer waitTime) {
        Logger.logMessage("Pause for '" + waitTime + "' milliseconds.");
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    /**********************************************************************************************
     * Pauses the test action.
     *
     * @param waitTime - {@link Integer} - The amount of time  to pause.
     * @param units - {@link TimeUnit } - the time unit for the {@code waitTime} argument
     * @author Everardo Lopez created April 22, 2018
     * @version 1.0 April 22, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract pause(Integer waitTime, TimeUnit units) {
        Logger.logMessage("Pause for '" + waitTime + "' " + units.name());
        try {
            units.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    //ELEMENT PRESENCE
    /**********************************************************************************************
    * Determines if an element is present or not.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public Boolean isPresent() {
    	elementPresent = false;
    	allWebElements = driver.findElements(locator);
    	if (allWebElements.size() > 0) {
    	    webElement = allWebElements.get(0);
    	    elementPresent = true;
    	}
        Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
        return elementPresent;
    }

    /**********************************************************************************************
    * Determines if an element is present or not within a given polling period.
    * 
    * @param timeout - {@link Integer} - The max amount of time in seconds to poll for the element's presence.
    * @author Saney Alam created October 1, 2018 
    * @version 1.1 March 31, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public Boolean isPresent(Integer timeoutInSec) {
    	try {
    		new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
    	            pollingTimeInMS, TimeUnit.MILLISECONDS).ignoring(WebDriverException.class)
    	            .until(new Function<By, Boolean>() {
    	                @Override
    	                public Boolean apply(final By loc) {
    	                    elementPresent = false;
    	                    webElement = driver.findElement(loc);
    	                    elementPresent = true;
    	                    return elementPresent;
    	                }
    	            });
    	} catch (TimeoutException e) {
    	    // ignore
    	}
    	Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
        
    	return elementPresent;
    }
    
    /**********************************************************************************************
    * Determines if an element is visible or not.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public Boolean isVisible() {
    	elementPresent = false;
    	elementVisible = false;
    	allWebElements = driver.findElements(locator);
    	if (allWebElements.size() > 0) {
    	    webElement = allWebElements.get(0);
    	    elementPresent = true;
    	    elementVisible = webElement.isDisplayed();
    	}
    	
        Logger.logMessage("Is the '" + simpleName + "' element visible: " + elementVisible.toString());
        return elementVisible;
    }

    /**********************************************************************************************
    * Determines if an element is present and visible or not within a given polling period.
    * 
    * @param timeout - {@link Integer} - The max amount of time in seconds to poll for the element's visibility.
    * @author Saney Alam created October 1, 2018 
    * @version 1.1 March 31, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public Boolean isVisible(Integer timeoutInSec) {
    	try {
    		new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
    	            pollingTimeInMS, TimeUnit.MILLISECONDS).ignoring(WebDriverException.class)
    	            .until(new Function<By, Boolean>() {
    	                @Override
    	                public Boolean apply(final By loc) {
    	                	elementPresent = false;
    	                	elementVisible = false;
    	                	allWebElements = driver.findElements(locator);
    	                	if (allWebElements.size() > 0) {
    	                	    webElement = allWebElements.get(0);
    	                	    elementPresent = true;
    	                	    elementVisible = webElement.isDisplayed();
    	                	}
    	                	return elementVisible;
    	                }
    	            });
    	} catch (TimeoutException e) {
    	    // ignore
    	}
    	
        Logger.logMessage("Is the '" + simpleName + "' element visible: " + elementVisible.toString());
        return elementVisible;
    }
    
    //ATTRIBUTE PRESENCE
    /**********************************************************************************************
    * Determines if an element has a specific attribute value or not.
    * 
    * @param attribute - {@link String} - The specific attribute type to evaluate.
    * @param attributeValue - {@link String} - The value of the attribute to evaluate.
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public Boolean hasAttribute(String attribute, String attributeValue) {
        try {
            if (webElement.getAttribute(attribute).equals(attributeValue)) {
                hasAttribute = true;
            } else {
                hasAttribute = false;
            }
        } catch (WebDriverException e) {
            hasAttribute = false;
        }
        Logger.logMessage("The '" + simpleName + "' element has attribute '" + attribute + "' with value "
        		+ "'" + attributeValue + "': " + hasAttribute.toString());
        return hasAttribute;
    }
    
    //ELEMENT WAITS
    /**********************************************************************************************
    * Waits for an element to be present before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element is not present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForPresent() {
        Logger.logMessage("Verify the '" + simpleName + "' element is present.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not present.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    elementPresent = false;
                    webElement = driver.findElement(loc);
                    elementPresent = true;
                    return elementPresent;
                }
            });
        return this;
    }
    
    /**********************************************************************************************
     * Waits for an element to be present in a frame or subframe before timing out.
     * 
     * @author Saney Alam created April 8, 2018
     * @version 1.0 April 8, 2018
     * @return Fluid instance of the Interact class
     * @exception TimeoutException - An element is not present in a frame/subframe within the given timeout period.
     ***********************************************************************************************/
    public WebInteract waitForPresentInFrame() {
    	Logger.logMessage("Verify the '" + simpleName + "' element is present.");
    	driver.switchTo().defaultContent();
    	this.byWait(locator)
        	.ignoring(WebDriverException.class)
            .withMessage("Element not present in frame.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    elementPresent = false;
                    if (!isPresent()) {
                    	List<WebElement> allFrames = driver.findElements(By.xpath(IFRAME));
                    	for (WebElement frame : allFrames) {
                        	driver.switchTo().defaultContent();
                        	driver.switchTo().frame(frame);
                        	if (isPresent()) {
                        		elementPresent = true;
                        		break;
                        	}
                        	
                        	List<WebElement> allSubFrames = driver.findElements(By.xpath(IFRAME));
                        	if (allSubFrames.size() > 0) {
                        		for (WebElement subFrame : allSubFrames) {
                        			driver.switchTo().frame(subFrame);
                        			if (isPresent()) {
                        				break;
                                	}
                        			driver.switchTo().defaultContent();
                        			driver.switchTo().frame(frame);
                        		}
                        	}
                        }
                    }
                    return elementPresent;
                }
            });
    	return this;
    }
    
    /**********************************************************************************************
    * Waits for all/multiple instance of an element to be present before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - All instances of an element are not present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForAllPresent() {
        Logger.logMessage("Verify the '" + simpleName + "' elements are present.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(simpleName + " elements with locator '" + locator.toString() + "' are not present.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    allWebElements = driver.findElements(loc);
                    return allWebElements.size() > 0;
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for an element to not be present before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element is present during the entire given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForNotPresent() {
        Logger.logMessage("Verify the '" + simpleName + "' element is NOT present.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is present and should NOT be.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    elementPresent = true;
                    allWebElements = driver.findElements(loc);
                    if (allWebElements.size() == 0) {
                        elementPresent = false;
                    }
                    return !elementPresent;
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for an element to be present and visible before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element is not visible within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForVisible() {
        Logger.logMessage("Verify the '" + simpleName + "' element is visible.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not visible.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    webElement = driver.findElement(loc);
                    return webElement.isDisplayed();
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for an element to not be visible before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element is visible during the entire given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForNotVisible() {
        Logger.logMessage("Verify the '" + simpleName + "' element is NOT visible.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is visible and should not be.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    elementPresent = false;
                    webElement = driver.findElement(loc);
                    elementPresent = true;
                    elementVisible = webElement.isDisplayed();
                    return elementPresent && !elementVisible;
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for an element to not be present, or be present but not visible before timing out.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element is present/visible during the entire given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForNotPresentOrVisible() {
    	Logger.logMessage("Verify the '" + simpleName + "' element is NOT present, or present "
            + "but not visible.");
        
        for (int i = 0; i <= timeoutInSec; i++) {
        	if (i == timeoutInSec) {
        		Assert.fail(simpleName + " element with locator '" + locator.toString() + "' is present, or present and visible, and should not be.");
        	}
        	
        	elementPresent = false;
        	elementVisible = false;
        	allWebElements = driver.findElements(locator);
        	if (allWebElements.size() > 0) {
        	    elementPresent = true;
        	    webElement = allWebElements.get(0);
        	}
        	Logger.logMessage(simpleName + " element present: " + elementPresent);
        	
        	if (elementPresent) {
        		elementVisible = webElement.isDisplayed();
        	}
        	Logger.logMessage(simpleName + " element visible: " + elementVisible);
        	
        	if (!elementPresent || !elementVisible) {
        		break;
        	}
            
            this.pause(smallPauseInMS);
        }
        return this;
    }
     
    /**********************************************************************************************
    * Waits for an attribute to be present before timing out.
    * 
    * @param attribute - {@link String} - The specific attribute type to evaluate.
    * @param attributeValue - {@link String} - The specific attribute value to evaluate.
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An attribute with value is not present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForAttribute(final String attribute, final String attributeValue) {
        Logger.logMessage("Verify the '" + simpleName + "' element attribute '" + attribute + "' equals '" 
                + attributeValue + "'.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage("Attribute '" + attribute + "' with value '" + attributeValue + "' not present "
                + "in " + simpleName + " element with locator '" + locator.toString() + "'.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    webElement = driver.findElement(loc);
                    return webElement.getAttribute(attribute).contains(attributeValue);
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for an attribute to NOT be present before timing out.
    * 
    * @param attribute - {@link String} - The specific attribute type to evaluate.
    * @param attributeValue - {@link String} - The specific attribute value to evaluate.
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An attribute with value is present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForNotAttribute(final String attribute, final String attributeValue) {
        Logger.logMessage("Verify the '" + simpleName + "' element attribute '" + attribute + "' does NOT equal '" 
                + attributeValue + "'.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage("Attribute '" + attribute + "' with value '" + attributeValue + "' present "
                + "in '" + simpleName + " element with locator '" + locator.toString() + "' and should NOT be.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    webElement = driver.findElement(loc);
                    return !webElement.getAttribute(attribute).contains(attributeValue);
                }
            });
        return this;
    }
    
    /**********************************************************************************************
    * Waits for text to be present in an element before timing out.
    * 
    * @param text - {@link String} - The text to evaluate.
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element's text is not present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForText(final String text) {
        Logger.logMessage("Verify the '" + simpleName + "' element text equals '" + text + "'.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage("Text '" + text + "' not present in " + simpleName + " element with locator '" + locator.toString() + "'.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    webElement = driver.findElement(loc);
                    return webElement.getText().contains(text);
                }
            });
        return this;
    }
     
    /**********************************************************************************************
    * Waits for text to NOT be present in an element before timing out.
    * 
    * @param text - {@link String} - The text to evaluate.
    * @author Saney Alam created December 20, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    * @exception TimeoutException - An element's text is present within the given timeout period.
    ***********************************************************************************************/
    public WebInteract waitForNotText(final String text) {
        Logger.logMessage("Verify the '" + simpleName + "' element text does NOT equal '" + text + "'.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage("Text '" + text + "' present in " + simpleName + "' element with locator '" + locator.toString() + "' and should not be.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                    webElement = driver.findElement(loc);
                    return !webElement.getText().contains(text);
                }
            });
        return this;
    }
    
    /**********************************************************************************************
     * Waits for children elements to be present in a parent element before timing out.
     * 
     * @param childSimpleName - {@link String} - The simple name of the child element(s).
     * @param childLocator - {@link By} - The By locator of the child element(s).
     * @author Saney Alam created September 8, 2018
     * @version 1.0 September 8, 2018
     * @return Fluid instance of the Interact class
     * @exception TimeoutException - Children elements are not present within the given timeout period.
     ***********************************************************************************************/
	public WebInteract waitForChildElementsPresent(String childSimpleName, By childLocator) {
		Logger.logMessage("Verify the '" + childSimpleName + "' elements are present in parent '" + simpleName + "'.");
        this.byWait(locator)
            .ignoring(WebDriverException.class)
            .withMessage(childSimpleName + " elements with locator '" + childLocator.toString() + "' are not present"
            		+ " in parent '" + simpleName + "' element with locator '" + locator.toString() + "'.")
            .until(new Function<By, Boolean>() {
                @Override
                public Boolean apply(final By loc) {
                	webElement = driver.findElement(loc);
                	allChildElements = webElement.findElements(childLocator);
                    return allChildElements.size() > 0;
                }
            });
        return this;
	}
	
    // UI INTERACTIONS
	public WebInteract scroll(Integer numOfScrolls, Integer xNum, Integer yNum) {
        int scrollIter = 0;
        while (scrollIter < numOfScrolls) {
        	Logger.logMessage("Scroll by " + xNum + "," + yNum + ".");
        	JavascriptExecutor js = (JavascriptExecutor) driver;
    		js.executeScript("window.scrollBy(" + xNum + "," + yNum + ")", "");
            scrollIter++;
        }
        return this;
    }
    
    public WebInteract waitForScrolledTo(Integer maxScrolls, Integer xNum, Integer yNum) {
    	int scrollIter = 0;
        while (scrollIter <= maxScrolls && this.isVisible().equals(false)) {
            if (scrollIter == maxScrolls) {
                Assert.fail("Element not visible after '" + maxScrolls + "' scrolls.");
            }
            this.scroll(1, xNum, yNum);
            scrollIter++;
        }
        return this;
    }
    
    public WebInteract waitForClickable() {
         Logger.logMessage("Verify the '" + simpleName + "' element is clickable.");
         this.byWait(locator)
             .ignoring(WebDriverException.class)
             .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not clickable.")
             .until(new Function<By, Boolean>() {
                 @Override
                 public Boolean apply(final By loc) {
                     webElement = driver.findElement(loc);
                     return (webElement.isDisplayed() && webElement.isEnabled());
                 }
             });
         return this;
     }
    
    /**********************************************************************************************
    * Clicks the identified web element.
    * 
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public WebInteract click() {
    	Logger.logMessage("Click the '" + simpleName + "' element.");
    	getNonStaleElement().click();
    	injectGrowl();
        return this;
    }
    
    public WebInteract doubleClick() {
     	Logger.logMessage("Double click the '" + simpleName + "' element.");
     	new Actions(driver).doubleClick(getNonStaleElement()).build().perform();
     	injectGrowl();
         return this;
     }
    
    /**********************************************************************************************
     * Clicks the identified web element by javascript.
     * 
     * @author Saney Alam created April 19, 2018 
     * @version 1.0 April 19, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
     public WebInteract clickByJS() {
    	Logger.logMessage("Click the '" + simpleName + "' element by javascript event.");
     	((JavascriptExecutor)driver).executeScript("arguments[0].click();", getNonStaleElement());
     	injectGrowl();
        return this;
     }
    
    /**********************************************************************************************
    * Types a string of text into an element.
    * 
    * @param text - {@link String} - The string that will be typed into the element.
    * @author Saney Alam created October 1, 2018 
    * @version 1.0 October 1, 2018
    * @return Fluid instance of the Interact class
    ***********************************************************************************************/
    public WebInteract type(String text) {
        Logger.logMessage("Type '" + text + "' into the '" + simpleName + "' element.");
        getNonStaleElement().sendKeys(text);
        return this;
    }
    
    /**********************************************************************************************
     * Selects a drop down list option from a WebElement by the option text. Note - If the option is not
     * present in the element after timeout, the standard wait timeout exception will be thrown. 
     * 
     * @param option - {@link String} - The drop down list option text to select from the element.
     * @author Saney Alam created October 1, 2018 
     * @version 1.1 March 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
     public WebInteract selectByText(String option) {
         Logger.logMessage("Select the '" + option + "' option from the '" + simpleName + "' element.");
         Select select = new Select(getNonStaleElement());
         this.driverWait()
         .ignoring(WebDriverException.class)
         .withMessage("Option '" + option + "' not present in " + simpleName + "' element with locator '" + locator.toString() + "'.")
         .until(new Function<RemoteWebDriver, Boolean>() {
             @Override
             public Boolean apply(final RemoteWebDriver dr) {
                 Boolean optionPresent = false;
                 for (WebElement element : select.getOptions()) {
                     if (element.getText().equals(option)) {
                    	 optionPresent = true;
                    	 break;
                     }
                 }
                 return optionPresent;
             }
         });
         select.selectByVisibleText(option);
         return this;
     }

    /**********************************************************************************************
     * Navigates back via the browser back button.
     * 
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract goBack() {
    	Logger.logMessage("Navigate back.");
        driver.navigate().back();
        injectGrowl();
        return this;
    }
    
    /**********************************************************************************************
     * Navigates forward via the browser forward button.
     * 
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract goForward() {
    	Logger.logMessage("Navigate forward.");
        driver.navigate().forward();
        injectGrowl();
        return this;
    }
    
    /**********************************************************************************************
     * Opens a url.
     * 
     * @param url - {@link String} - The url to open in the browser. 
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract openUrl(String url) {
    	Logger.logMessage("Open url '" + url + "'.");
        driver.navigate().to(url);
        injectGrowl();
        return this;
    }
    
    /**********************************************************************************************
     * Submits on the web element.
     * 
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract submit() {
    	Logger.logMessage("Submit on the '" + simpleName + "' element.");
        getNonStaleElement().submit();
        injectGrowl();
        return this;
    }
    
    /**********************************************************************************************
     * Clears the text from the web element.
     * 
     * @author Saney Alam created October 1, 2018 
     * @version 1.0 October 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract clearText() {
        Logger.logMessage("Clear the text in the '" + simpleName + "' element.");
        getNonStaleElement().clear();
        return this;
    }
    
    /**********************************************************************************************
     * Mouse over the web element using a javascript injections. NOTE - may not work with all browsers.
     * 
     * @author Saney Alam created March 1, 2018 
     * @version 1.0 March 1, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract mouseOverByJS() {
        Logger.logMessage("Mouse over the '" + simpleName + "' element.");
        String mouseOverJS = "var evObj = document.createEvent('MouseEvents');evObj.initMouseEvent"
        		+ "(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
        		+ "arguments[0].dispatchEvent(evObj);";
        ((JavascriptExecutor)driver).executeScript(mouseOverJS, getNonStaleElement());
        return this;
    }
    
    /**********************************************************************************************
     * Mouse over the web element using the touch action class.
     * 
     * @author Saney Alam created March 22, 2018 
     * @version 1.0 March 22, 2018
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public WebInteract mouseOver() {
        Logger.logMessage("Mouse over the '" + simpleName + "' element.");
        new Actions(driver).moveToElement(getNonStaleElement()).build().perform();
        return this;
    }
    
    /**********************************************************************************************
	 * Return current browser window title
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link String} title
	 ***********************************************************************************************/
	public String getTitle() {
		Logger.logMessage("Getting title of current browser window.");
		return driver.getTitle();
	}

	/**********************************************************************************************
	 * Return currently loaded URL in browser window
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link String} URL
	***********************************************************************************************/
	public String getCurrentUrl() {
		Logger.logMessage("Getting current url loaded in browser.");
		return driver.getCurrentUrl();
	}

	/**********************************************************************************************
	 * Maximize browser window
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract maximize() {
		Logger.logMessage("Miximizing current browser window (if supported).");
		try {
		    driver.manage().window().maximize();
		} catch (Exception e) {
			// ignore as not all browsers support.
		}
		return this;
	}

	/**********************************************************************************************
	 * Close current browser window
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract closeBrowser() {
		Logger.logMessage("Closing current active browser window.");
		driver.close();
		return this;
	}

	/**********************************************************************************************
	 * Return WebDriver sessionID
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link SessionId} webdriver sessionID
	 ***********************************************************************************************/
	public SessionId getSessionID() {
		Logger.logMessage("Getting webdriver sessionID.");
		return driver.getSessionId();
	}

	/**********************************************************************************************
	 * Get JavascriptExecutor
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link JavascriptExecutor} JavascriptExecutor
	 ***********************************************************************************************/
	public JavascriptExecutor getScriptExecutor() {
		Logger.logMessage("Getting javascript executor.");
		return (JavascriptExecutor) driver;
	}

	/**********************************************************************************************
	 * Get active window handle
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link String} window handle
	 ***********************************************************************************************/
	public String getCurrentWindowHandle() {
		Logger.logMessage("Getting active browser window handle.");
		return driver.getWindowHandle();
	}

	/**********************************************************************************************
	 * Get all window handles for webdriver
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return {@link Set<String>} all window handles
	 ***********************************************************************************************/
	public Set<String> getAllWindowHandles() {
		Logger.logMessage("Getting all available browser window handles.");
		return driver.getWindowHandles();
	}

	/**********************************************************************************************
	 * Switch to window
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract switchToWindow(String windowHandle) {
		Logger.logMessage("Switching to custom window.");
		driver.switchTo().window(windowHandle);
		return this;
	}

	/**********************************************************************************************
	 * Get current window size
	 * @
	 * @author Saney Alam created February 02, 2017
	 * return {@link Dimension} current window handle
	 ***********************************************************************************************/
	public Dimension getWindowSize() {
		Logger.logMessage("Getting window size.");
		return driver.manage().window().getSize();
	}

	/**********************************************************************************************
	 * Add cookie
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract addCookie(Cookie cookie) {
		Logger.logMessage("Adding cookie.");
		driver.manage().addCookie(cookie);
		return this;
	}

	/**********************************************************************************************
	 * Delete all cookies
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract deleteCookies() {
		Logger.logMessage("Deleteing all cookies.");
		driver.manage().deleteAllCookies();
		return this;
	}

	/**********************************************************************************************
	 * Refresh page
	 * 
	 * @author Saney Alam created February 02, 2017
	 * @return Fluid instance of the Interact class
	 ***********************************************************************************************/
	public WebInteract refresh() {
		Logger.logMessage("Refreshing page.");
		driver.navigate().refresh();
		return this;
	}
	
	public WebInteract rotateScreen(ScreenOrientation orientation) {
        Logger.logMessage("Rotate the screen to the '" + orientation.toString() + "' orientation.");
        DriverManager.getAppiumDriver().rotate(orientation);
        return this;
    }
	
	/************************************************************************************************
	 * Method that scrolls an element to view
	 * 
	 * @author Saney Alam created Oct 12, 2017
	 * @return WebInteract
	 ************************************************************************************************/
	public WebInteract scrollElementToView(){
		Logger.logMessage("Scrolling element: " + simpleName + " into view");
		this.byWait(locator)
		.ignoring(WebDriverException.class)
		.until(new Function<By, Boolean>(){
			@Override
			public Boolean apply(By loc) {
				elementPresent = false;
				WebElement ele = driver.findElement(loc);
				elementPresent = true;
				JavascriptExecutor js = (JavascriptExecutor) driver;
				js.executeScript("arguments[0].scrollIntoView();", ele);
				return elementPresent;
			}});
		return this;
	}
	
    // stale element safety check
    private WebElement getNonStaleElement() {
    	for (int i = 0; i <= 5; i++) {
    		try {
                webElement.isEnabled();
                break;
            } catch (StaleElementReferenceException e) {
            	Logger.logMessage("Element '" + simpleName + "' with locator '" + locator + "' is stale "
            		+ "on check '" + i + "'.");
            	pause(smallPauseInMS);
            	waitForPresent();
            }
    	}
    	
    	return webElement;
    }
    
    // initiate fluent waits
    private FluentWait<By> byWait(final By locator) {
        return new FluentWait<By>(locator).withTimeout(timeoutInSec, TimeUnit.SECONDS).pollingEvery(
            pollingTimeInMS, TimeUnit.MILLISECONDS);
    }

    private FluentWait<RemoteWebDriver> driverWait() {
        return new FluentWait<RemoteWebDriver>(driver).withTimeout(timeoutInSec, TimeUnit.SECONDS)
                .pollingEvery(pollingTimeInMS, TimeUnit.MILLISECONDS);
    }
    
    private void injectGrowl() {
    	// enable growl notifications if enabled
        if (Logger.isGrowlEnabled()) {
        	try {
        		DriverManager.getWebDriver().executeScript("if (!window.jQuery) {" +
                    "var jquery = document.createElement('script'); jquery.type = 'text/javascript';" +
                    "jquery.src = 'https://ajax.googleapis.com/ajax/libs/jquery/2.0.2/jquery.min.js';" +
                    "document.getElementsByTagName('head')[0].appendChild(jquery);" +
                    "}");
                	
                // inject growl if need be
                DriverManager.getWebDriver().executeScript("$.getScript('http://the-internet.herokuapp.com"
                	+ "/js/vendor/jquery.growl.js')");
                DriverManager.getWebDriver().executeScript("$('head').append('<link rel=\"stylesheet\" "
                	+ "href=\"http://the-internet.herokuapp.com/css/jquery.growl.css\" type=\"text/css\" />');");
                Logger.setGrowlReady(true);
        	} catch (Exception e) {
        		Logger.setGrowlReady(false);
        		e.printStackTrace();
        		// ignore as growl not being enabled isn't a critical issue
        	}
        }
    }
    
}
