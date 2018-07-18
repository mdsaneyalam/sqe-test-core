package com.softech.test.core.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class RandomData {
	
	// TODO - this class should be greatly expanded upon and cleaned up as needed. Java doc'ing also needed.
	// possibility of importing 3rd party java randomization test library???
    public RandomData() {

    }

    public static synchronized String getCharacterString(int maximumLength) {
        // TODO - add a loop that creates a string as long as needed and not
        // just the standard UUID length
        String charString = UUID.randomUUID().toString() + UUID.randomUUID().toString() 
                + UUID.randomUUID().toString() + UUID.randomUUID().toString() + UUID.randomUUID().toString();
        charString = charString.replace("-", "").substring(0, maximumLength);
        return charString;
    }

    public static synchronized String getDateOfBirth(int startYear, int endYear) {
        GregorianCalendar gc = new GregorianCalendar();
        int year = randBetween(startYear, endYear);
        gc.set(Calendar.YEAR, year);
        int dayOfYear = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        gc.set(Calendar.DAY_OF_YEAR, dayOfYear);

        String month = Integer.toString(gc.get(Calendar.MONTH));
        String yearS = Integer.toString(gc.get(Calendar.YEAR));
        // TODO - If random day of month is greater than 28 but month is a month that doesn't have
        // 28 days then registration methods will fail. Below is a short/easy work around for now
        String day = "";
        Integer dayPre = gc.get(Calendar.DAY_OF_MONTH);
        if (dayPre > 28) {
            day = "28";
        } else {
            day = Integer.toString(dayPre);
        }
        
        String dob = month.replace("0", "1") + "-" + day + "-" + yearS;
        return dob;
    }

    public static synchronized Integer getInteger(int minValue, int maxValue) {
        java.util.Random rand = new java.util.Random();
        int randomNum = rand.nextInt((maxValue - minValue) + 1) + minValue;
        return randomNum;
    }

    public static int randBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }
    
    /**
     * @author Jitendra Khare created March 17, 2017
     * To be used with Randomdata.isSorted method
     *
     */
    public enum SortType{
        ASCENDING(1),
        DESCENDING(-1);

        private final int value;

        SortType(final int sortType) {
            value = sortType;
        }
    }
    
    /**
	 * @author Jitendra Khare created March 17, 2017
	 * @param list - List to be checked for sorting
	 * @param sortType {@link SortType}
	 * @return true- If list is sorted in expected order
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(List<T> list,SortType sortType) {
	    List<T> copy = new ArrayList<T>(list);
	    if(sortType.value==SortType.ASCENDING.value){
		    Collections.sort(copy);
		    return copy.equals(list);
	    }else{
	    	Collections.sort(copy);
	    	Collections.reverse(copy);
		    return copy.equals(list);
	    }
	}

}
