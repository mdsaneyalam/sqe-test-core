package com.softech.test.core.omniture;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import de.sstoehr.harreader.model.HarQueryParam;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;

import com.softech.test.core.driver.DriverManager;
import com.softech.test.core.util.Logger;

//PLEASE READ REPORTING SECTION IN THE README.MD FILE TO GET DETAILED DESCRIPTION FOR THIS UTILITY

public class OmnitureUtil {
    public static HashMap<String, Map<String, String>> dynamicExpectedValues = new HashMap<String, Map<String, String>>();
    public static Map<String, String>                  obj                   = new HashMap<String, String>();

    /**
     * Function includes all types of Omniture related validation
     * 
     * @param action
     *            describes scenario where this validation is performing and
     *            what expected values are
     * @param MobileOS
     *            String will have value if this is not Web application and will
     *            be used as part of a Final Report later
     * @param browser
     *            String will have value if this is Web application and will be
     *            used as part of a Final Report later
     * @param nameOrUrl
     *            String will have value if this is Web (url) and if application
     *            will be AppName - used as part of a Final Report later
     * @param actualParameters
     *            collected from the application from local project
     * @param sourceOmnitureFileName
     *            is a path to the file with expected values
     * @param inputmap
     *            - in cases when we should send expected run time values from
     *            the project level HashMap is used with values as in example:
     *            "v44": {"EQUAL": "Home"}
     *
     * @throws JSONException
     * @author Rostislav Alpin created May 12, 2016
     */
    public static ArrayList<String> validateOmniture(String action,
            String MobileOS, String browser, String nameOrUrl,
            List<HarQueryParam> actualParameters,
            String sourceOmnitureFileName,
            Map<String, Map<String, String>> inputmap) throws JSONException {
        Map<Integer, ArrayList<String>> data = new HashMap<Integer, ArrayList<String>>();
        int counter = 1;

        try {
            counter = validateGeneralOmnitureRules(counter, action, data, MobileOS,
                    browser, nameOrUrl, actualParameters, sourceOmnitureFileName,
                    inputmap);
            
            validateSpecificRules(action, data, counter, MobileOS, browser,
                    nameOrUrl, actualParameters, sourceOmnitureFileName,
                    inputmap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<String> reportText = convertDataToReport(data, action,
                MobileOS);
        return reportText;
    }

    private static int validateGeneralOmnitureRules(int counter, String action,
            Map<Integer, ArrayList<String>> data, String MobileOS, String browser, String nameOrUrl, List<HarQueryParam> actualParameters,
            String sourceOmnitureFileName, Map<String, Map<String, String>> inputmap) throws JSONException {
        
        validateSpecificRules("General", data, counter, MobileOS, browser,
                nameOrUrl, actualParameters, sourceOmnitureFileName, inputmap);
        return 0;
    }

    /**
     * @param data
     * @param action
     * @param MobileOS
     *            tells if this app or web
     * @return
     */
    private static ArrayList<String> convertDataToReport(
            Map<Integer, ArrayList<String>> data, String action, String MobileOS) {
        ArrayList<String> reportText = new ArrayList<String>();
        for (int key : data.keySet()) {
            ArrayList<String> reportContent = data.get(key);
            if (!(MobileOS.length() > 0)) {
                reportText.add("Omniture reporting failed for: " + "Browser - "
                        + reportContent.get(0) + "; " + "SiteURL - "
                        + reportContent.get(1) + "; " + "; Scenario - "
                        + reportContent.get(2) + "; " + "; Attribute Name - "
                        + reportContent.get(3) + "; " + "; Expected Value - "
                        + reportContent.get(4) + "; " + "; Actual Value - "
                        + reportContent.get(5) + ";");
            } else {
                reportText.add("Omniture reporting failed for: "
                        + "MobileOS - " + reportContent.get(0) + "; "
                        + "AppName - " + reportContent.get(1) + "; "
                        + "; Scenario - " + reportContent.get(2) + "; "
                        + "; Attribute Name - " + reportContent.get(3) + "; "
                        + "; Expected Value - " + reportContent.get(4) + "; "
                        + "; Actual Value - " + reportContent.get(5) + ";");
            }
        }
        return reportText;
    }

    /**
     * Validates expected attributes values against actual for specific page
     * (transactions)
     * 
     * @param action
     *            in first method - validateOmniture
     * 
     * @throws JSONException
     * @author Rostislav Alpin created May 12, 2016
     */
    private static int validateSpecificRules(String action,
            Map<Integer, ArrayList<String>> data, int counter, String MobileOS,
            String browser, String nameOrUrl,
            List<HarQueryParam> actualParameters,
            String sourceOmnitureFileName,
            Map<String, Map<String, String>> inputmap) throws JSONException {

        Map<String, String> actualMap = getActualOmniture(actualParameters);
        if (sourceOmnitureFileName.length() > 0) {
            Map<String, Map<String, String>> expected = getExpectedMap(action,
                    sourceOmnitureFileName, inputmap);
            for (String key : expected.keySet()) {
                counter = validateExpectedActual(action, data, counter,
                        MobileOS, browser, nameOrUrl, actualMap, expected, key);
            }
        }
        if (dynamicExpectedValues.size() > 0) {
            for (String key : dynamicExpectedValues.keySet()) {
                if (key == action) {
                    Map<String, String> tempMap = dynamicExpectedValues
                            .get(key);

                    String value1 = "";
                    for (String keyname : tempMap.keySet()) {

                        String value2 = actualMap.get(keyname.trim());
                        value1 = tempMap.get(keyname);
                        counter = compareStoreValues(action, data, counter,
                                keyname, value2, tempMap, value1, MobileOS,
                                browser, nameOrUrl);
                    }
                }
            }
        }
        return counter;
    }

    /**
     * @param action
     * @param data
     * @param counter
     * @param MobileOS
     * @param browser
     * @param nameOrUrl
     * @param actualMap
     * @param expected
     * @param key
     * @return
     */
    public static int validateExpectedActual(String action,
            Map<Integer, ArrayList<String>> data, int counter, String MobileOS,
            String browser, String nameOrUrl, Map<String, String> actualMap,
            Map<String, Map<String, String>> expected, String key) {
        Map<String, String> tempMap;
        String value2 = actualMap.get(key.trim());
        tempMap = expected.get(key);
        String value1 = "";
        for (String keyname : tempMap.keySet())
            value1 = tempMap.get(keyname);
        counter = compareStoreValues(action, data, counter, key, value2,
                tempMap, value1, MobileOS, browser, nameOrUrl);
        return counter;
    }

    /**
     * Method is comparing actual and expected values based on predefined rules
     * (equal, contains or regexp - match)
     * 
     * @param action
     *            - Step name
     * @param data
     *            - Object where results are stored
     * @param counter
     *            - this will be a row number in the Excel report
     * @param key
     *            - name of attribute
     * @param actual
     *            actual value
     * @param tempMap
     *            - object containing Value1 and Type of Comparison
     * @param expected
     *            - expected value
     * @return
     */
    public static int compareStoreValues(String action,
            Map<Integer, ArrayList<String>> data, int counter, String key,
            String actual, Map<String, String> tempMap, String expected,
            String MobileOS, String browser, String nameOrUrl) {

        if (actual == null) {

            actual = "Not Present";
        }
        if (tempMap.keySet().toString().toUpperCase().contains("CONTAINS")) {
            if (!actual.contains(expected)) {
                // here,final report container will be filled up
                Logger.logMessage("Test failed: Expected set is - " + key + ":"
                        + expected);
                Logger.logMessage("Actual set is - " + key + ":" + actual);
                // Store failed data into the report
                createOmnitureMemoryReport(action, data, counter, key, actual,
                        expected, MobileOS, browser, nameOrUrl);
                counter++;
            }
        } else if (tempMap.keySet().toString().toUpperCase().contains("MATCH")) {
            // Create a Pattern object
            Pattern r = Pattern.compile(expected);
            // Create matcher object.
            Matcher m = r.matcher(actual);
            if (!m.find()) {

                // here,final report container will be filled up
                Logger.logMessage("Test failed: Expected set is - " + key + ":"
                        + expected);
                Logger.logMessage("Actual set is - " + key + ":" + actual);
                // Store failed data into the report
                createOmnitureMemoryReport(action, data, counter, key, actual,
                        expected, MobileOS, browser, nameOrUrl);
                counter++;
            }

        } else if (tempMap.keySet().toString().toUpperCase().contains("MAKE")) {
            // Create a value
            String expectedString = "";
            switch (expected.toLowerCase()) {
            case "dayofweek":
                LocalDate date = LocalDate.now();
                DayOfWeek dow = date.getDayOfWeek();
                expected = dow.getDisplayName(TextStyle.FULL_STANDALONE,
                        Locale.ENGLISH);
                expectedString = expected.toString();
                break;
            case "daytime":
                org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat
                        .forPattern("h:mm a");
                DateTime expectedTime = new DateTime();
                if (expectedTime.getMinuteOfHour() >= 30) {
                    expectedTime = expectedTime.withMinuteOfHour(30);
                } else {
                    expectedTime = expectedTime.withMinuteOfHour(00);
                }
                expectedString = formatter.print(expectedTime);
                expectedString = expectedString.replaceAll("\\s+", "");
                break;
            case "dateofyear":
                Calendar now = Calendar.getInstance();
                expectedString = String.valueOf(now.get(Calendar.MONTH) + 1)
                        + "/"
                        + String.valueOf(now.get(Calendar.DAY_OF_MONTH) + "/"
                                + String.valueOf(now.get(Calendar.YEAR)));
                break;
            case "landscapeportrait":
                if ((MobileOS.length() > 0)) {
                    if (MobileOS.equalsIgnoreCase("Android")) {
                        expectedString = DriverManager.getAndroidDriver()
                                .getOrientation().toString();
                    } else {
                        expectedString = DriverManager.getIOSDriver()
                                .getOrientation().toString();
                    }
                } else {
                    expectedString = "landscape";
                }
                break;
            default:
                Logger.logMessage("Test failed: Expected set is not recognized in the Expected.json file");
            }
            if (!actual.equals(expectedString)) {
                // here,final report container will be filled up
                Logger.logMessage("Test failed: Expected set is - " + key + ":"
                        + expectedString);
                Logger.logMessage("Actual set is - " + key + ":" + actual);
                // Store failed data into the report
                createOmnitureMemoryReport(action, data, counter, key, actual,
                        expected, MobileOS, browser, nameOrUrl);
                counter++;
            }

        } else {
            if (!actual.equals(expected)) {
                // here,final report container will be filled up
                Logger.logMessage("Test failed: Expected set is - " + key + ":"
                        + expected);
                Logger.logMessage("Actual set is - " + key + ":" + actual);
                // Store failed data into the report
                createOmnitureMemoryReport(action, data, counter, key, actual,
                        expected, MobileOS, browser, nameOrUrl);
                counter++;
            }
        }
        return counter;
    }

    /**
     * Save failed data in to the object which later will be stored in the Excel
     * 
     * @param action
     *            - Step name
     * @param data
     *            - Object where results are stored
     * @param counter
     *            - this will be a row number in the Excel report
     * @param attributeName
     *            - name of attribute
     * @param actual
     *            actual value
     * @param expected
     *            expected value
     */
    public static void createOmnitureMemoryReport(String action,
            Map<Integer, ArrayList<String>> data, int counter,
            String attributeName, String actual, String expected,
            String MobileOS, String browser, String nameOrUrl) {
        ArrayList<String> reportContent = new ArrayList<String>();
        if (!(MobileOS.length() > 0)) {
            reportContent.add(browser);
            reportContent.add(nameOrUrl);
            reportContent.add(action);
            reportContent.add(attributeName);
            reportContent.add(expected);
            reportContent.add(actual);
            data.put(counter, reportContent);
        } else {
            reportContent.add(MobileOS);
            reportContent.add(nameOrUrl);
            reportContent.add(action);
            reportContent.add(attributeName);
            reportContent.add(expected);
            reportContent.add(actual);
            data.put(counter, reportContent);
        }
    }

    /**
     * Get Actual omniture request attributes parameter's values
     *
     * @return Map<String, String> which contains attributes and values
     *         collected by ProxyManager from request
     * 
     * @author Rostislav Alpin created April 29, 2016
     */

    public static Map<String, String> getActualOmniture(
            List<HarQueryParam> actualParameters) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        for (HarQueryParam entry : actualParameters) {
            String name = entry.getName();
            String value = entry.getValue();
            if (value.length() == 0) {
                continue;
            }
            map.put(name, value);
            Logger.logConsoleMessage("Attribute " + name + " and value is "
                    + value);
        }

        return map;
    }

    /**
     * Get Expected omniture request attributes parameter's values
     *
     * @param action
     *            action tells which set of data collect for current execution
     * @return Map<String, Object> which contains attributes and values
     *         predefined based on test cases for each specific step recognized
     *         by parameter in the test scenario
     * 
     * @author Rostislav Alpin created April 29, 2016
     */

    public static Map<String, Map<String, String>> getExpectedMap(
            String action, String sourceOmnitureFileName,
            Map<String, Map<String, String>> inputmap) {
        Map<String, Map<String, String>> finalmap = new HashMap<String, Map<String, String>>();
        JSONObject expectedJSON = null;
        try {
            expectedJSON = getElementJSON(action, sourceOmnitureFileName);
        } catch (Throwable e) {
            Assert.fail(
                    "Couldn't pull out JSONObject from OmnitureSource file.", e);
        }
        Map<String, Map<String, String>> map = null;
        try {
            // map = fromJsontoMap(expectedJSON);
            map = fromJsontoMapStringObject(expectedJSON);
        } catch (JSONException e) {
            Assert.fail(
                    "Couldn't convert JSONObject into the Map<String, String>. ",
                    e);
        }
        if (inputmap != null) {
            finalmap.putAll(inputmap);
        }
        finalmap.putAll(map);
        return finalmap;
    }

    /**
     * Get specified JSON node which contains pairs attributes and values
     *
     * @param action
     *            action tells which node of data collect for current execution
     * @return JSONObject which contains requested data
     * 
     * @author Rostislav Alpin created April 29, 2016
     */

    private static JSONObject getElementJSON(String action,
            String sourceOmnitureFileName) {
        // String elementFileName = UrlConstants.PATH_TO_OMNITURE_SOURCE;

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(
                    sourceOmnitureFileName));
            jsonObject = (JSONObject) jsonObject.get(action);
        } catch (FileNotFoundException e) {
            Assert.fail("No Element File found.", e);
        } catch (IOException e) {
            Assert.fail("Failed to query for action '" + action, e);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            Assert.fail("Unable to parse File's element to JSONObject '", e);
        }
        return jsonObject;
    }

    /**
     * Converts JsonObject to Map<String, String>
     *
     * @param object
     *            which should be converted
     * @return Map<String, Object>
     * 
     * @author Rostislav Alpin created April 29, 2016
     */

    @SuppressWarnings("unchecked")
    public static Map<String, String> fromJsontoMap(JSONObject object)
            throws JSONException {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> keys = (Iterator<String>) object.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, object.get(key).toString());
        }
        return map;
    }

    /**
     * Converts JsonObject to Map<String, Object>
     *
     * @param object
     *            which should be converted
     * @return Map<String, Object>
     * 
     * @author Rostislav Alpin created April 29, 2016
     */

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> fromJsontoMapStringObject(
            JSONObject object) throws JSONException {
        Map<String, String> stringMap = new HashMap<String, String>();
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        Iterator<String> keys = (Iterator<String>) object.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (object.get(key) instanceof JSONObject) {
                stringMap = fromJsontoMap((JSONObject) object.get(key));
                map.put(key, stringMap);
            } else {
                Assert.fail("Source Data doesn't contains JSON Node");
            }

        }
        return map;
    }

    public static synchronized HashMap<String, Map<String, String>> buildDynamicMap(
            String actionName, String name, String value, boolean... flag) {

        boolean lastelement = (boolean) (flag.length > 0 ? flag[0] : false);
        if (lastelement) {

            String[] temp = value.split("/");
            if (temp[temp.length - 1].contains("!")) {
                temp[temp.length - 1] = temp[temp.length - 1].toString()
                        .replace("!", "");
                value = temp[temp.length - 2].toString()
                        + temp[temp.length - 1].toString();
            }

            else {
                value = temp[temp.length - 1].toString();
            }
        }
        if(dynamicExpectedValues.get(actionName)==null) {
            obj = new HashMap<String, String>();
            obj.put(name, value);
        }
        else {
            obj = dynamicExpectedValues.get(actionName);
            obj.put(name, value);
        }
        dynamicExpectedValues.put(actionName, obj);
        return dynamicExpectedValues;
    }

}
