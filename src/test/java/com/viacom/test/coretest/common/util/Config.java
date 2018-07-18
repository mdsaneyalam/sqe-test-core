package com.viacom.test.coretest.common.util;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.testng.Assert;
import org.w3c.dom.Document;

public class Config {

    private static String SYSTEM_TEST_PROP = "system.test.";
    private static String USER_DIR_PROP = "user.dir";
    
    public static String getFilePath(final String parameterName) {
        String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
        if (parameterValue != null) {
        	return System.getProperty(USER_DIR_PROP) + parameterValue.replace("/", File.separator);
        }
        String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
        System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
        return System.getProperty(USER_DIR_PROP) +  propFromXML.replace("/", File.separator);
    }

    public static Integer getInt(final String parameterName) {
        String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
        if (parameterValue != null) {
            return Integer.parseInt(parameterValue);
        }
        String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
        System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
        return Integer.parseInt(propFromXML);
    }
    
    public static Boolean getBoolean(final String parameterName) {
        String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
        if (parameterValue != null) {
            return Boolean.valueOf(parameterValue);
        }
        String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
        System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
        return Boolean.valueOf(propFromXML);
    }

    public static String getString(final String parameterName) {
        String parameterValue = System.getProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase());
        if (parameterValue != null) {
            return parameterValue;
        }
        String propFromXML = getXPathValueFromFile(getConfigFileLocation(), getParameterValue(parameterName));
        System.setProperty(SYSTEM_TEST_PROP + parameterName.toLowerCase(), propFromXML);
        return propFromXML;
    }

    public static String getConfigFileLocation() {
        String fileLoc = System.getProperty(USER_DIR_PROP) + "/src/test/resources/TestNGSuiteConfig.xml";
        return fileLoc.replace("/", File.separator);
    }

    private static String getParameterValue(String parameterName) {
        return "//parameter[@name='" + parameterName + "']/@value";
    }
    
    private static String getXPathValueFromFile(final String fileLocation, final String xpathQuery) {
        String value = null;
        try {
            File file = new File(fileLocation);
            DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = docBuilder.parse(file);
            XPathFactory xpathFact = XPathFactory.newInstance();
            XPath xpath = xpathFact.newXPath();
            value = (String) xpath.evaluate(xpathQuery, xmlDoc, XPathConstants.STRING);
        } catch (Exception e) {
            Assert.fail("Failed to retrieve configuration value from Config File at '" + fileLocation 
                    + "' with xpath query '" + xpathQuery + "'.", e);
        }
        return value;
    }

}
