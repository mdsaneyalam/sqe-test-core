package com.softech.test.core.sikuli;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.codehaus.plexus.util.FileUtils;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.FluentWait;
import org.sikuli.script.Button;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Finder;
import org.sikuli.script.Key;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.sikuli.script.ScreenImage;
import org.testng.Assert;

import com.google.common.base.Function;
import com.softech.test.core.util.Config;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;

public class SikuliInteract {

	private Screen sikuliDriver;
	private Integer timeout = 0;
    private Integer pollingTime;
    
    // locator data
    private String locator;
    private String simpleName;
    
    // element data
    private Boolean elementPresent;
    
    public SikuliInteract(String locator, HashMap<String, String> locatorData) {
    	sikuliDriver = SikuliDriverManager.getSikuliDriver();
        timeout = Config.getMaxWaitTime();
        pollingTime = Config.getPollingTime();
        org.sikuli.basics.Settings.MinSimilarity = .75;
        this.locator = locatorHandler(locator, null, null);
        if (locator != null) {
            simpleName = locatorData.get("SimpleName");
        }
        
        if (locator == null) {
        	this.locator = "";
        }
    }

    public SikuliInteract setTimeout(Integer timeout) {
    	this.timeout = timeout;
    	return this;
    }
    
    public SikuliInteract setPollingTime(Integer pollingTime) {
    	this.pollingTime = pollingTime;
    	return this;
    }
    
    public SikuliInteract setMinImageMatch(Double value) {
    	org.sikuli.basics.Settings.MinSimilarity = value;
    	return this;
    }
    
    public SikuliInteract setLocator(String locatorSource) {
    	this.locator = locatorHandler(locatorSource, null, null);
    	return this;
    }
    
    public SikuliInteract setLocator(String locatorSource, Integer imageHeight, Integer imageWidth) {
    	this.locator = locatorHandler(locatorSource, imageHeight, imageWidth);
    	return this;
    }
    
    public String getElementLocator() {
    	return locator;
    }
    
    public String getElementSimpleName() {
    	return simpleName;
    }
    
    public ScreenImage getScreenImage() {
    	ScreenImage screenImage = SikuliDriverManager.getSikuliDriver().capture(
				SikuliDriverManager.getSikuliDriver().getBounds());
    	return screenImage;
    }
    
    public SikuliInteract pause(Integer waitTime, TimeUnit units) {
        Logger.logMessage("Pause for '" + waitTime + "' " + units.name());
        try {
            units.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    public SikuliInteract pause(Integer waitTimeMS) {
        Logger.logMessage("Pause for '" + waitTimeMS + "' milliseconds.");
        try {
            Thread.sleep(waitTimeMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    //ELEMENT PRESENCE
    public Boolean isPresent() {
    	elementPresent = false;
    	try {
    		if (sikuliDriver.exists(locator) != null) {
    			elementPresent = true;
    		}
    	} catch (Exception e) { 
    		// element is not present
    	}
    	
        Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
        return elementPresent;
    }

    public Boolean isPresent(Integer timeout) {
    	try {
    		new FluentWait<String>(locator).withTimeout(timeout, TimeUnit.SECONDS).pollingEvery(
    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(Exception.class)
    	            .until(new Function<String, Boolean>() {
    	                @Override
    	                public Boolean apply(final String loc) {
    	                    elementPresent = false;
    	                    if (sikuliDriver.exists(locator) != null) {
    	            			elementPresent = true;
    	            		}
    	                    return elementPresent;
    	                }
    	            });
    	} catch (TimeoutException e) {
    	    // ignore
    	}
    	Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
    	return elementPresent;
    }
    
    //ELEMENT WAITS
    public SikuliInteract waitForPresent() {
        Logger.logMessage("Verify the '" + simpleName + "' element is present.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is not present.")
	        .until(new Function<String, Boolean>() {
	            @Override
	            public Boolean apply(final String loc) {
	                elementPresent = false;
	                if (sikuliDriver.exists(locator) != null) {
	            	    elementPresent = true;
	            	}
	                return elementPresent;
	            }
	        });
        
        return this;
    }
    
    public SikuliInteract waitForNotPresent() {
        Logger.logMessage("Verify the '" + simpleName + "' element is NOT present.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage(simpleName + " element with locator '" + locator.toString() + "' is present and should NOT be.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                    elementPresent = true;
                    if (sikuliDriver.exists(locator) == null) {
            	        elementPresent = false;
            	    }
                    return !elementPresent;
                }
            });
        
        return this;
    }
    
    public SikuliInteract waitForScreenImagesEqual(ScreenImage screenImage1, ScreenImage screenImage2) {
        Logger.logMessage("Verify 2 screen images are equal.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Screen image 1 is not the same as screen image 2.")
	        .until(new Function<String, Boolean>() {
	            @Override
	            public Boolean apply(final String loc) {
	            	Finder finder = new Finder(screenImage1);
	                Pattern pattern = new Pattern(screenImage2);
	                finder.find(pattern.exact());
	                return finder.hasNext();
	            }
	        });
        
        return this;
    }
    
    public SikuliInteract waitForScreenImagesNotEqual(ScreenImage screenImage1, ScreenImage screenImage2) {
        Logger.logMessage("Verify 2 screen images are NOT equal.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Screen image 1 is the same as screen image 2.")
	        .until(new Function<String, Boolean>() {
	            @Override
	            public Boolean apply(final String loc) {
	            	Finder finder = new Finder(screenImage1);
	                Pattern pattern = new Pattern(screenImage2);
	                finder.find(pattern.exact());
	                return !finder.hasNext();
	            }
	        });
        
        return this;
    }
    
    public SikuliInteract waitForTextPresent(final String text) {
        Logger.logMessage("Verify the text '" + text + "' is present on screen.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Text '" + text + "' not present on screen.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                	Boolean textPresent = false;
                	if (sikuliDriver.text().contains(text)) {
                		textPresent = true;
                	}
                	
                    return textPresent;
                }
            });
        
        return this;
    }
     
    public SikuliInteract waitForTextNotPresent(final String text) {
    	Logger.logMessage("Verify the text '" + text + "' is NOT present on screen.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Text '" + text + "' present on screen and should NOT be.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                	Boolean textPresent = true;
                	if (!sikuliDriver.text().contains(text)) {
                		textPresent = false;
                	}
                	
                    return !textPresent;
                }
            });
        
        return this;
    }
    
    public SikuliInteract waitForScrolledUpTo(Integer maxNumOfScrollsUp) {
    	int scrollIter = 0;
        while (scrollIter <= maxNumOfScrollsUp && !this.isPresent()) {
            if (scrollIter == maxNumOfScrollsUp) {
                Assert.fail("Element not visible after '" + maxNumOfScrollsUp + "' scrolls up.");
            }
            this.scrollUp(1);
            scrollIter++;
        }
        
        return this;
    }
    
    public SikuliInteract waitForScrolledDownTo(Integer maxNumOfScrollsDown) {
    	int scrollIter = 0;
        while (scrollIter <= maxNumOfScrollsDown && !this.isPresent()) {
            if (scrollIter == maxNumOfScrollsDown) {
                Assert.fail("Element not visible after '" + maxNumOfScrollsDown + "' scrolls down.");
            }
            this.scrollDown(1);
            scrollIter++;
        }
        
        return this;
    }
    
    //UI INTERACTIONS
    public SikuliInteract scrollUp(Integer numOfScrollsUp) {
    	Integer scrollIter = 0;
    	while (scrollIter < numOfScrollsUp) {
    		Logger.logMessage("Scroll up.");
    	    sikuliDriver.wheel(Button.WHEEL_DOWN, 10);
    	    scrollIter++;
    	}
    	
    	return this;
    }
    
    public SikuliInteract scrollDown(Integer numOfScrollsDown) {
    	Integer scrollIter = 0;
    	while (scrollIter < numOfScrollsDown) {
    		Logger.logMessage("Scroll down.");
    	    sikuliDriver.wheel(Button.WHEEL_UP, 10);
    	    scrollIter++;
    	}
    	
    	return this;
    }
    
    public SikuliInteract click() {
    	Logger.logMessage("Click the '" + simpleName + "' element.");
        try {
			sikuliDriver.click(locator);
		} catch (FindFailed e) {
			// element not found
		}
        return this;
    }
    
    public SikuliInteract type(String text) {
        Logger.logMessage("Type '" + text + "' into the '" + simpleName + "' element.");
        sikuliDriver.type(locator, text);
        return this;
    }
    
    public SikuliInteract typeOffsetFrom(Integer xOffset, Integer yOffset, String text) {
        Logger.logMessage("Type '" + text + "' into the '" + simpleName + "' element offset by '" + xOffset + ":" 
            + yOffset + "'.");
        Pattern pattern = new Pattern(locator).targetOffset(xOffset, yOffset);
        Region region = sikuliDriver.exists(pattern, 1);
        try {
			sikuliDriver.click(region, 1);
			sikuliDriver.type(text);
		} catch (FindFailed e) {
			Logger.logConsoleMessage("Failed to find offset region.");
			e.printStackTrace();
		}
        return this;
    }
    
    public SikuliInteract keyRight() {
        Logger.logMessage("Key right.");
        sikuliDriver.type(Key.RIGHT);
        return this;
    }
    
    public SikuliInteract keyLeft() {
        Logger.logMessage("Key left.");
        sikuliDriver.type(Key.LEFT);
        return this;
    }
    
    public SikuliInteract keyUp() {
        Logger.logMessage("Key up.");
        sikuliDriver.type(Key.UP);
        return this;
    }
    
    public SikuliInteract keyDown() {
        Logger.logMessage("Key down.");
        sikuliDriver.type(Key.DOWN);
        return this;
    }
    
    public SikuliInteract keySubmit() {
        Logger.logMessage("Key submit.");
        sikuliDriver.type(Key.ENTER);
        return this;
    }
    
    public SikuliInteract keyBack() {
        Logger.logMessage("Key Back.");
        sikuliDriver.type(Key.BACKSPACE);
        return this;
    }
    
    private String locatorHandler(String locatorSrc, Integer imageHeight, Integer imageWidth) {
    	if (locatorSrc == null) {
    		return null;
    	}
    	
    	// locator is a url to a image
    	if (locatorSrc.contains("http://") || locatorSrc.contains("https://")) {
    		try {
    			// download the image
        		URL url = new URL(locatorSrc);
        		InputStream inputStream = new BufferedInputStream(url.openStream());
        		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        		byte[] buf = new byte[1024];
        		int n = 0;
        		while (-1!=(n=inputStream.read(buf))) {
        		   outputStream.write(buf, 0, n);
        		}
        		outputStream.close();
        		inputStream.close();
        		byte[] response = outputStream.toByteArray();
        		
        		// save the image
        		File locatorLoc = new File(System.getProperty("user.dir") + "/test-output/screenshots");
        		if (!locatorLoc.exists()) {
        			locatorLoc.mkdirs();
        		}
        		File imageFile = new File(locatorLoc + "/" + RandomData.getCharacterString(40));
        		imageFile.createNewFile();
        		FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
        		fileOutputStream.write(response);
        		fileOutputStream.close();
        		
        		// get the appropriate image format and rename the file
        		ImageInputStream imageInputStream = ImageIO.createImageInputStream(imageFile);
                Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
                ImageReader reader = iterator.next();
                String formatName = reader.getFormatName();
                imageInputStream.close();
                
                File completeFile = new File(imageFile.getAbsolutePath() + "." + formatName);
                FileUtils.rename(imageFile, completeFile);
                
        		// resize the image if indicated
                // TODO - this produces an image of sub-par quality but still seems to work with a relatively
                // ok image match of .80. In the future we should tweak this to resize with better quality
        		if (imageHeight != null && imageWidth != null) {
        			BufferedImage inputImage = ImageIO.read(completeFile);
        	        
        			BufferedImage outputImage = new BufferedImage(imageWidth,
        	                imageHeight, inputImage.getType());
        			
        			Graphics2D g2d = outputImage.createGraphics();
        	        g2d.drawImage(inputImage, 0, 0, imageWidth, imageHeight, null);
        	        g2d.dispose();
        	        
        	        ImageIO.write(outputImage, formatName, completeFile);
        		}
                
                return completeFile.getAbsolutePath();
    		} catch (Exception e) {
    			Logger.logConsoleMessage("Failed to download/save image locator from '" + locatorSrc + "'.");
    			e.printStackTrace();
    		}
    	}
    	
    	// locator is a path to pre-saved file in the project
    	return locatorSrc;
    }
   
}
