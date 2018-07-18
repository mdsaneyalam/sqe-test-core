package com.softech.test.core.emerging;

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
import org.openqa.selenium.support.ui.FluentWait;

import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Pattern;
import org.testng.Assert;

import com.google.common.base.Function;
import com.softech.test.core.lab.CommandExecutor;
import com.softech.test.core.lab.FileDeployer;
import com.softech.test.core.lab.GlobalReportDir;
import com.softech.test.core.lab.GridManager;
import com.softech.test.core.props.EmergingOS;
import com.softech.test.core.props.GatewayIP;
import com.softech.test.core.sikuli.SikuliUtil;
import com.softech.test.core.util.Config;
import com.softech.test.core.util.Logger;
import com.softech.test.core.util.RandomData;
import com.softech.test.core.util.TestRun;

public class EmergingInteract {

	private String harmonyIP = null;
	//private String deviceID = null;
	private String harmonyDeviceID = null;
	private String machineIP = null;
	
	private Integer timeout = 0;
    private Integer pollingTime;
    
    // locator data
    private String locator;
    private String simpleName;
    
    // element data
    private Boolean elementPresent;
    private Boolean textPresent;
    
    public EmergingInteract(String locator, HashMap<String, String> locatorData) {
    	harmonyIP = EmergingDriverManager.getHubHarmonyIP();
    	harmonyDeviceID = EmergingDriverManager.getHarmonyDeviceId();
    	//deviceID = EmergingDriverManager.getDeviceId();
    	machineIP = EmergingDriverManager.getMachineIP();
    	timeout = Config.getMaxWaitTime();
        pollingTime = Config.getPollingTime();
        org.sikuli.basics.Settings.MinSimilarity = .75;
        org.sikuli.basics.Settings.OcrTextRead = true;
		org.sikuli.basics.Settings.OcrTextSearch = true;
        this.locator = locatorHandler(locator, null, null);
        if (locator != null) {
            simpleName = locatorData.get("SimpleName");
        }
        
        if (locator == null) {
        	this.locator = "";
        }
    }

    public EmergingInteract setTimeout(Integer timeoutInSec) {
    	this.timeout = timeoutInSec;
    	return this;
    }
    
    public EmergingInteract setPollingTime(Integer pollingTimeInMS) {
    	this.pollingTime = pollingTimeInMS;
    	return this;
    }
    
    public EmergingInteract setMinImageMatch(Double value) {
    	org.sikuli.basics.Settings.MinSimilarity = value;
    	return this;
    }
    
    public EmergingInteract setLocator(String locatorSource) {
    	this.locator = locatorHandler(locatorSource, null, null);
    	return this;
    }
    
    public EmergingInteract setLocator(String locatorSource, Integer imageHeight, Integer imageWidth) {
    	this.locator = locatorHandler(locatorSource, imageHeight, imageWidth);
    	return this;
    }
    
    public String getElementLocator() {
    	return locator;
    }
    
    public String getElementSimpleName() {
    	return simpleName;
    }
    
    public File getDeviceImage() {
    	File screenImage = new File(getImageFromDevice());
    	return screenImage;
    }
    
    public File getDeviceSubImage(Integer subX, Integer subY, Integer width, Integer height) {
    	String imagePath = getImageFromDevice();
		String subImage = Image.create(imagePath).getSub(subX, subY, width, height).asFile();
    	return new File(subImage);
    }
    
    public String getTextFromDevice() {
        Logger.logMessage("Get text from device screen.");
        return getTesseractTextFromImage(getImageFromDevice());
    }
    
    public String getTextFromDeviceSubImage(Integer subX, Integer subY, Integer width, Integer height) {
        Logger.logMessage("Get text from device subscreen at 'x:'" + subX 
        		+ "', 'y:" + subY + "' with width '" + width + "' and height '" + height + "'.");
        String imagePath = getImageFromDevice();
        String imageWithText = Image.create(imagePath).getSub(subX, subY, width, height).asFile();
        return getTesseractTextFromImage(imageWithText);
    }
    
    public EmergingInteract pause(Integer waitTime, TimeUnit units) {
        Logger.logMessage("Pause for '" + waitTime + "' " + units.name());
        try {
            units.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    public EmergingInteract pause(Integer waitTimeInMS) {
        Logger.logMessage("Pause for '" + waitTimeInMS + "' milliseconds.");
        try {
            Thread.sleep(waitTimeInMS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this;
    }
    
    //ELEMENT PRESENCE
    public Boolean isPresent() {
    	elementPresent = false;
    	Finder finder = null;
    	try {
    		finder = new Finder(getImageFromDevice());
    		Pattern pattern = new Pattern(locator);
            finder.find(pattern.exact());
    		
    		if (finder.hasNext()) {
    			elementPresent = true;
    		}
    	} catch (Exception e) { 
    		Logger.logConsoleMessage("Exception occurred during element isPresent Check: '" + e.getMessage() + "'.");
    	} finally {
    		if (finder != null) {
    			finder.destroy();
    		}
    	}
    	
        Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
        return elementPresent;
    }

    public Boolean isPresent(Integer timeoutInSec) {
    	try {
    		new FluentWait<String>(locator).withTimeout(timeout, TimeUnit.SECONDS).pollingEvery(
    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(Exception.class)
    	            .until(new Function<String, Boolean>() {
    	                @Override
    	                public Boolean apply(final String loc) {
    	                    elementPresent = false;
    	                    Finder finder = null;
    	                    try {
    	                		finder = new Finder(getImageFromDevice());
    	                		Pattern pattern = new Pattern(locator);
    	                        finder.find(pattern.exact());
    	                		
    	                		if (finder.hasNext()) {
    	                			elementPresent = true;
    	                		}
    	                	} catch (Exception e) { 
    	                		Logger.logConsoleMessage("Exception occurred during element isPresent Check: '" + e.getMessage() + "'.");
    	                	} finally {
    	                		if (finder != null) {
    	                			finder.destroy();
    	                		}
    	                	}
    	                	
    	                    return elementPresent;
    	                }
    	            });
    	} catch (Exception e) {
    	    // ignore
    	}
    	Logger.logMessage("Is the '" + simpleName + "' element present: " + elementPresent.toString());
    	return elementPresent;
    }
    
    public Boolean isTextPresent(String text) {
        String deviceText = getTesseractTextFromImage(getImageFromDevice());
        textPresent = deviceText.contains(text);
        Logger.logMessage("Is the '" + text + "' present on the device: " + textPresent);
        return textPresent;
    }
    
    public Boolean isTextPresent(String text, Integer timeoutInSec) {
    	try {
    		new FluentWait<String>(locator).withTimeout(timeout, TimeUnit.SECONDS).pollingEvery(
    	            pollingTime, TimeUnit.MILLISECONDS).ignoring(Exception.class)
    	            .until(new Function<String, Boolean>() {
    	                @Override
    	                public Boolean apply(final String loc) {
    	                    textPresent = false;
    	                    try {
    	                		String deviceText = getTesseractTextFromImage(getImageFromDevice());
    	                		textPresent = deviceText.contains(text);
    	                	} catch (Exception e) { 
    	                		Logger.logConsoleMessage("Exception occurred during element isTextPresent Check: '" + e.getMessage() + "'.");
    	                	}
    	                    return textPresent;
    	                }
    	            });
    	} catch (Exception e) {
    	    // ignore
    	}
    	Logger.logMessage("Is the '" + text + "' present on the device: " + textPresent);
    	return textPresent;
    }
    
    //ELEMENT WAITS
    public EmergingInteract waitForPresent() {
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
	                Finder finder = null;
	                try {
	                	finder = new Finder(getImageFromDevice());
	            		Pattern pattern = new Pattern(locator);
	            		finder.find(pattern);
	            		
	            		if (finder.hasNext()) {
	            			elementPresent = true;
	            		}
	            	} catch (Exception e) {
	            		Logger.logConsoleMessage("Exception occurred during element waitForPresent Check: '" + e.getMessage() + "'.");
	            	} finally {
	            		if (finder != null) {
	            			finder.destroy();
	            		}
	            	}
	                return elementPresent;
	            }
	        });
        
        return this;
    }
    
    public EmergingInteract waitForNotPresent() {
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
	                Finder finder = null;
	                try {
	            		finder = new Finder(getImageFromDevice());
	            		Pattern pattern = new Pattern(locator);
	            		finder.find(pattern);
	            		
	            		if (!finder.hasNext()) {
	            			elementPresent = false;
	            		}
	            	} catch (Exception e) { 
	            		Logger.logConsoleMessage("Exception occurred during element waitForNotPresent Check: '" + e.getMessage() + "'.");
	            	} finally {
	            		if (finder != null) {
	            			finder.destroy();
	            		}
	            	}
	                return !elementPresent;
	            }
	        });
        
        return this;
    }
    
    public EmergingInteract waitForTextPresent(String text) {
        Logger.logMessage("Verify the text '" + text + "' is present on screen.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Text '" + text + "' not present on screen.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                	textPresent = false;
                	try {
                		String imageText = getTesseractTextFromImage(getImageFromDevice());
                		if (imageText.contains(text)) {
                			textPresent = true;
                		}
                	} catch (Exception e) { 
                		Logger.logConsoleMessage("Exception occurred during element waitForTextPresent Check: '" + e.getMessage() + "'.");
                	}
                	
                    return textPresent;
                }
            });
        
        return this;
    }
    
    public EmergingInteract waitForTextNotPresent(String text) {
        Logger.logMessage("Verify the text '" + text + "' is NOT present on screen.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Text '" + text + "' not present on screen and should NOT be.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                	Boolean textPresent = false;
                	try {
                		String imageText = getTesseractTextFromImage(getImageFromDevice());
                		if (imageText.contains(text)) {
                			textPresent = true;
                		}
                	} catch (Exception e) { 
                		Logger.logConsoleMessage("Exception occurred during element waitForTextNotPresent Check: '" + e.getMessage() + "'.");
                	}
                	
                    return !textPresent;
                }
            });
        
        return this;
    }
    
    public EmergingInteract waitForTextPresentInSubImage(String text, Integer subX, Integer subY, Integer width, Integer height) {
        Logger.logMessage("Verify the text '" + text + "' is present on screen in subimage captured at 'x:'" + subX 
        		+ "', 'y:" + subY + "' with width '" + width + "' and height '" + height + "'.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Text '" + text + "' not present in sub image.")
            .until(new Function<String, Boolean>() {
                @Override
                public Boolean apply(final String loc) {
                	Boolean textPresent = false;
                	try {
                		String imagePath = getImageFromDevice();
                		String textFile = Image.create(imagePath).getSub(subX, subY, width, height).asFile();
                		String imageText = getTesseractTextFromImage(textFile);
                		if (imageText.contains(text)) {
                			textPresent = true;
                		}
                	} catch (Exception e) { 
                		Logger.logConsoleMessage("Exception occurred during element waitForTextPresentInSubImage Check: '" + e.getMessage() + "'.");
                	}
                	
                    return textPresent;
                }
            });
        
        return this;
    }
    
    public EmergingInteract waitForScreenState(Boolean atRest) {
    	String notTxt = atRest ? " " : " not ";
        Logger.logMessage("Verify the device screen is" + notTxt + "at rest.");
        new FluentWait<String>(locator)
            .withTimeout(timeout, TimeUnit.SECONDS)
            .pollingEvery(pollingTime, TimeUnit.MILLISECONDS)
            .ignoring(Exception.class)
            .withMessage("Device screen is NOT in the indicated restful state.")
	        .until(new Function<String, Boolean>() {
	            @Override
	            public Boolean apply(final String loc) {
	            	Boolean success = false;
	                String image1 = getImageFromDevice();
	                String image2 = getImageFromDevice();
	                Boolean imagesEqual = SikuliUtil.imagesEqual(new File(image1), new File(image2));
	                if (atRest) {
	                	if (imagesEqual) {
	                		success = true;
	                	}
	                } else {
	                	if (!imagesEqual) {
	                		success = true;
	                	}
	                }
	                return success;
	            }
	        });
        
        return this;
    }
    
    //UI INTERACTIONS
    /**********************************************************************************************
     * Scrolls in the provided direction until either the element is present, or the maximum number of scroll events is reached.
     * 
     * @param direction - {@link String} - The direction of the scroll - 'Up', 'Down', 'Left', or 'Right'
     * @param startX - {@link Integer} - The maximum number of scrolls to perform before timing out and failing.
     * @author Brandon Clark created February 22, 2017 
     * @version 1.0 February 22, 2017
     * @return Fluid instance of the Interact class
     ***********************************************************************************************/
    public EmergingInteract waitForScrolledTo(String direction, Integer maxScrolls) {
    	int navIter = 0;
        while (navIter <= maxScrolls && !this.isPresent()) {
            if (navIter == maxScrolls) {
                Assert.fail("Element '" + simpleName + "' not present after '" + maxScrolls + "' scrolls '" + direction + "'.");
            }
            
            if (direction.equals("Right")) {
            	this.pressRightArrowBtn();
            } else if (direction.equals("Left")) {
            	this.pressLeftArrowBtn();
            } else if (direction.equals("Up")) {
            	this.pressUpArrowBtn();
            } else if (direction.equals("Down")) {
            	this.pressDownArrowBtn();
            }
            navIter++;
        }
        return this;
    }
    
    public synchronized EmergingInteract pressRightArrowBtn() {
    	Logger.logMessage("Press the right arrow button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "DirectionRight");
    	keyMap.put(EmergingOS.ROKU, "Right");
    	keyMap.put(EmergingOS.FIRE_TV, "22");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressRightArrowBtn(Integer numOfTimes) {
    	for (int i = 0; i < numOfTimes; i++) {
    		pressRightArrowBtn();
    	}
    	return this;
    }
    
    public synchronized EmergingInteract pressLeftArrowBtn() {
    	Logger.logMessage("Press the left arrow button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "DirectionLeft");
    	keyMap.put(EmergingOS.ROKU, "Left");
    	keyMap.put(EmergingOS.FIRE_TV, "21");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressLeftArrowBtn(Integer numOfTimes) {
    	for (int i = 0; i < numOfTimes; i++) {
    		pressLeftArrowBtn();
    	}
    	return this;
    }
    
    public synchronized EmergingInteract pressUpArrowBtn() {
    	Logger.logMessage("Press the up arrow button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "DirectionUp");
    	keyMap.put(EmergingOS.ROKU, "Up");
    	keyMap.put(EmergingOS.FIRE_TV, "19");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressUpArrowBtn(Integer numOfTimes) {
    	for (int i = 0; i < numOfTimes; i++) {
    		pressUpArrowBtn();
    	}
    	return this;
    }
    
    public synchronized EmergingInteract pressDownArrowBtn() {
    	Logger.logMessage("Press the down arrow button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "DirectionDown");
    	keyMap.put(EmergingOS.ROKU, "Down");
    	keyMap.put(EmergingOS.FIRE_TV, "20");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressDownArrowBtn(Integer numOfTimes) {
    	for (int i = 0; i < numOfTimes; i++) {
    		pressDownArrowBtn();
    	}
    	return this;
    }
    
    public synchronized EmergingInteract pressSelectBtn() {
    	Logger.logMessage("Press the select button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Select");
    	keyMap.put(EmergingOS.ROKU, "Select");
    	keyMap.put(EmergingOS.FIRE_TV, "66");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressBackBtn() {
    	Logger.logMessage("Press the back button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Back");
    	keyMap.put(EmergingOS.ROKU, "Back");
    	keyMap.put(EmergingOS.FIRE_TV, "4");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressHomeBtn() {
    	Logger.logMessage("Press the home button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Home");
    	keyMap.put(EmergingOS.ROKU, "Home");
    	keyMap.put(EmergingOS.FIRE_TV, "3");
    	keyCommandHandler(keyMap);
    	return this;
    }
    /* TODO - determine if we want to implement this - i would lean towards no...
    public synchronized EmergingInteract reboot() {
    	Logger.logMessage("Reboot the device.");
    	if (TestRun.isAppleTV()) {
    		keyCommandHandler("Reboot");
    	} else if (TestRun.isRoku()) {
    		String command = getNodePath() + " " + getHarmonyCLIPath()
			+ " -l " + harmonyIP + " -d '" + harmonyDeviceID + "' -c '[\"Home\",\"Home\",\"Home\",\"Home\",\"Home\",\"DirectionUp\","
					+ "\"Rewind\",\"Rewind\",\"FastForward\",\"FastForward\"]' -m";
    		CommandExecutor.execEmergingCommand(command, null, null);
    	}
    	return this;
    }*/
    
    public synchronized EmergingInteract pressPlayBtn() {
    	Logger.logConsoleMessage("Press the play button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Play");
    	keyMap.put(EmergingOS.ROKU, "Play");
    	keyMap.put(EmergingOS.FIRE_TV, "KEYCODE_MEDIA_PLAY_PAUSE");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressPauseBtn() {
    	Logger.logMessage("Press the pause button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Pause");
    	keyMap.put(EmergingOS.ROKU, "Play");
    	keyMap.put(EmergingOS.FIRE_TV, "KEYCODE_MEDIA_PLAY_PAUSE");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressFastForwardBtn() {
    	Logger.logMessage("Press the fast forward button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Fast Forward");
    	keyMap.put(EmergingOS.ROKU, "Fwd");
    	keyMap.put(EmergingOS.FIRE_TV, "KEYCODE_MEDIA_FAST_FORWARD");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract pressRewindBtn() {
    	Logger.logMessage("Press the rewind button.");
    	HashMap<EmergingOS, String> keyMap = new HashMap<EmergingOS, String>();
    	keyMap.put(EmergingOS.APPLE_TV, "Rewind");
    	keyMap.put(EmergingOS.ROKU, "Rev");
    	keyMap.put(EmergingOS.FIRE_TV, "KEYCODE_MEDIA_REWIND");
    	keyCommandHandler(keyMap);
    	return this;
    }
    
    public synchronized EmergingInteract type(String inText) {
    	Logger.logMessage("Type '" + inText + "' into the on screen keyboard.");
    	keyStringCommandHandler(inText);
    	return this;
    }
    
    private String getImageFromDevice() {
    	String screenshotDir = System.getProperty("user.dir") + "/test-output/screenshots/";
    	if (GridManager.isEC2Agent()) {
    		screenshotDir = GlobalReportDir.getReportDir();
    	} else {
    		File screenshotDirLoc = new File(screenshotDir);
        	if (!screenshotDirLoc.exists()) {
        		screenshotDirLoc.mkdirs();
        	}
    	}
    	
    	if (TestRun.isAppleTV()) {
    		String imageName = "appletvscreenshot_" + RandomData.getCharacterString(40) + ".tiff";
        	String imageLocation = "/Users/admin/EmergingScreenshots/" + imageName;
        	if (machineIP == null) {
        		imageLocation = screenshotDir + imageName;
        	}
        	/*
        	EmergingUtil.commandSender(EmergingUtil.getIDeviceScreenshotPath(machineIP) + " -u " + deviceID 
    			+ " " + imageLocation, machineIP, null);
    			*/
    		if (GridManager.isQALabHub()) {
    			FileDeployer.pullFileFromNodeToGateway(GatewayIP.LAB_01, machineIP, new File(imageLocation), screenshotDir);
    			imageLocation = screenshotDir + imageName;
    		}
    		
    		// convert the tiff to png
    		File imageTiff = null;
    		File imagePNG = null;
    		try {
    			imageTiff = new File(imageLocation);
    			imagePNG = new File(imageTiff.getAbsolutePath().replace(".tiff", ".png"));
    			BufferedImage tif = ImageIO.read(imageTiff);
        		ImageIO.write(tif, "png", imagePNG);
    		} catch (Exception e) {
    			
    		}
    		return imagePNG.getAbsolutePath();
    	}
    	
    	if (TestRun.isRoku()) {
    		String[] takeScreenshot = {"curl", "-u", EmergingDriverManager.getDeviceUsername() + ":" + EmergingDriverManager.getDevicePassword(), "-v", "-F", "mysubmit=Screenshot", "-F", 
    				"'archive= '", "-F", "'passwd= '", "http://" + EmergingDriverManager.getDeviceIP() + "/plugin_inspect", "--digest"};
    		EmergingUtil.commandSender(takeScreenshot);
    		
    		String imageName = "rokuscreenshot_" + RandomData.getCharacterString(40) + ".jpg";
        	String jpegLocation = screenshotDir + imageName;
        	if (GridManager.isEC2Agent()) {
        		jpegLocation = System.getenv("EMERGING_SCREENSHOT_DIR") + "/" + imageName;
        	}
        	
        	long currentEpoch = System.currentTimeMillis() / 1000;
        	String[] screenshotRequest = {"curl", "-u", EmergingDriverManager.getDeviceUsername() + ":" 
        			+ EmergingDriverManager.getDevicePassword(), "http://" + EmergingDriverManager.getDeviceIP() 
        			+ "/pkgs/dev.jpg?time=" + String.valueOf(currentEpoch), "--digest", ">", jpegLocation};
            EmergingUtil.commandSender(screenshotRequest);
        		
        	String pngLocation = jpegLocation.replace(".jpg", ".png");
        	String[] convertCommand = {EmergingUtil.getImageMagickPath(), jpegLocation, pngLocation};
        	EmergingUtil.commandSender(convertCommand);
        	
        	String readyImage = pngLocation;
        	if (GridManager.isEC2Agent()) {
        		readyImage = FileDeployer.pullFileFromGatewayToAgent(EmergingUtil.getTargetGateway(), 
        				new File(pngLocation), GlobalReportDir.getReportDir());
        		/*
        		EmergingUtil.setTargetGateway();
        		CommandExecutor.execEmergingCommand("rm " + jpegLocation, null, null);
        		EmergingUtil.setTargetGateway();
        		CommandExecutor.execEmergingCommand("rm " + pngLocation, null, null);
        		*/
        	}
        	
        	return readyImage;
    	}
    	
    	if (TestRun.isFireTV()) {
    		String[] takeScreenshot = {EmergingDriverManager.getADBPath(), "-s", EmergingDriverManager.getDeviceId(), "shell", 
    				"screencap", "-p", "/sdcard/capture.png"};
    		EmergingUtil.commandSender(takeScreenshot);
    		
    		String imageName = "firetvscreenshot_" + RandomData.getCharacterString(40) + ".png";
        	String pngLocation = screenshotDir + imageName;
        	if (GridManager.isEC2Agent()) {
        		pngLocation = System.getenv("EMERGING_SCREENSHOT_DIR") + "/" + imageName;
        	}
        	
        	String[] pullScreenshot = {EmergingDriverManager.getADBPath(), "-s", EmergingDriverManager.getDeviceId(), "pull", 
    				"/sdcard/capture.png", pngLocation};
        	EmergingUtil.commandSender(pullScreenshot);
			
        	String readyImage = null;
        	if (GridManager.isEC2Agent()) {
        		readyImage = FileDeployer.pullFileFromGatewayToAgent(GatewayIP.LAB_01, new File(pngLocation), GlobalReportDir.getReportDir());
        		EmergingUtil.commandSender(new String[]{"rm", pngLocation});
        	}
        	
        	return readyImage;
    	}
    	
    	return new File("").getAbsolutePath();
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
    
    private String getHarmonyCLIPath() {
    	return EmergingUtil.getHarmonyCLIPath();
    }
    
    private String getNodePath() {
    	return EmergingUtil.getNodePath();
    }
    
    private void keyCommandHandler(HashMap<EmergingOS, String> keyMap) {
    	String keyEventTxt = keyMap.get(TestRun.getEmergingOS());
    	String[] command = null;
    	if (TestRun.isAppleTV()) {
    		command = new String[]{ getNodePath(), getHarmonyCLIPath(), "-l", harmonyIP, "-d", harmonyDeviceID, "-c", keyEventTxt };
    	} else if (TestRun.isRoku()) {
    		command = new String[]{"curl", "-d", "''", "'http://" + EmergingDriverManager.getDeviceIP() + ":8060/keypress/" + keyEventTxt + "'"};
    	} else if (TestRun.isFireTV()) {
    		//command = EmergingDriverManager.getADBPath() + " -s " + EmergingDriverManager.getDeviceId() + " shell input keyevent " + keyEventTxt;
    	}
    	EmergingUtil.commandSender(command);
    }
    
    private void keyStringCommandHandler(String keyStringEventTxt) {
    	String[] command = null;
    	if (TestRun.isAppleTV()) {
    		throw new RuntimeException("This method is not supported for AppleTV devices.");
    	} else if (TestRun.isRoku()) {
    		for (int i = 0, n = keyStringEventTxt.length(); i < n; i++) {
        		char c = keyStringEventTxt.charAt(i);
        		command = new String[]{"curl", "-d", "''", "'http://" + EmergingDriverManager.getDeviceIP() + ":8060/keypress/Lit_" + c + "'"};
        		EmergingUtil.commandSender(command);
        	}
    	} else if (TestRun.isFireTV()) {
    		//command = EmergingDriverManager.getADBPath() + " -s " + EmergingDriverManager.getDeviceId() + " shell input text " + keyStringEventTxt;
    		EmergingUtil.commandSender(command);
    	}
    }
    
    private String getTesseractTextFromImage(String imageFilePath) {
    	String text = CommandExecutor.execCommand(EmergingUtil.getTesseractPath() + " " + imageFilePath + " stdout -l eng", null, null);
    	text = text.replace("Info in fopenReadFromMemory: work-around: writing to a temp file", "");
    	text = text.trim();
    	return text;
    }
    
}
