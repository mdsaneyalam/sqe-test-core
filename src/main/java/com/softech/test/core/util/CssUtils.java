package com.softech.test.core.util;

import org.openqa.selenium.WebElement;

/**
 * Class for fetching CSS properties of UI element
 * 
 * @author Jitendra Khare
 *
 */
public class CssUtils {

	private final static String FONT_SIZE = "font-size";
	private final static String FONT_FAMILY = "font-family";
	private final static String FONT_WEIGHT = "font-weight";
	private final static String TEXT_ALIGN = "text-align";
	private final static String COLOR = "color";
	private final static String TEXT_TRANSFORM = "text-transform";
	private final static String VERTICAL_ALIGN = "vertical-align";
	private final static String TEXT_DECORATION = "text-decoration";
	private final static String BOLD_CONST1 = "bold";
	private final static String BOLD_CONST2 = "700";
	private final static String UNDERLINE_CONST = "underline";
	private final static String UPPERCASE_CONST = "uppercase";
	private final static String LOWERCASE_CONST = "lowercase";

	/**
	 * Return font size
	 * 
	 * @author Jitendra Khare created February 21, 2017 
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Font Size
	 */
	public static String getFontSize(WebElement we) {
		return we.getCssValue(FONT_SIZE);
	}

	/**
	 * Return font family name
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Font Family
	 */
	public static String getFontFamily(WebElement we) {
		return we.getCssValue(FONT_FAMILY);
	}

	/**
	 * Return font weight 
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Font Weight
	 */
	public static String getFontWeight(WebElement we) {
		return we.getCssValue(FONT_WEIGHT);
	}

	/**
	 * Return horizontal text alignment
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Text Alignment
	 */
	public static String getHorizontalTextAlignment(WebElement we) {
		return we.getCssValue(TEXT_ALIGN);
	}

	/**
	 * Return vertical text alignment
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Vertical Text Alignment
	 */
	public static String getVerticalTextAlignment(WebElement we) {
		return we.getCssValue(VERTICAL_ALIGN);
	}

	/**
	 * Return text decoration
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} text decoration
	 */
	public static String getTextDecoration(WebElement we) {
		return we.getCssValue(TEXT_DECORATION);
	}

	/**
	 * Verify if text is bold
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link boolean} true if BOLD
	 */
	public static boolean isBold(WebElement we) {
		if (getFontWeight(we).equals(BOLD_CONST1) || getFontWeight(we).equals(BOLD_CONST2)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Verify if text is in uppercase
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link boolean} true if uppercase
	 */
	public static boolean isUpperCase(WebElement we) {
		if (we.getCssValue(TEXT_TRANSFORM).equals(UPPERCASE_CONST)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Verify if text is in lowercase
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link boolean} true if lowercase
	 */
	public static boolean isLowerCase(WebElement we) {
		if (we.getCssValue(TEXT_TRANSFORM).equals(LOWERCASE_CONST)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Verify if text is underlined
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link boolean} true if UNDERLINED
	 */
	public static boolean isUnderlined(WebElement we) {
		if (getTextDecoration(we).equals(UNDERLINE_CONST)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Return WebElement color
	 * 
	 * @author Jitendra Khare created February 21, 2017
	 * @param {@link
	 * 			WebElement} UI element
	 * @return {@link String} Font Weight
	 */
	public static String getColor(WebElement we) {
		return we.getCssValue(COLOR);
	}

}
