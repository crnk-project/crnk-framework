package io.crnk.core.engine.internal.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringUtils {

	public static final String EMPTY = "";

	private StringUtils() {
	}

	public static String join(String delimiter, Iterable<?> stringsIterable) {
		List<String> strings = new LinkedList<>();
		Iterator<?> iterator = stringsIterable.iterator();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			if (obj == null) {
				strings.add(null);
			}
			else {
				strings.add(obj.toString());
			}
		}

		StringBuilder ab = new StringBuilder();
		for (int i = 0; i < strings.size(); i++) {
			ab.append(strings.get(i));
			if (i != strings.size() - 1) {
				ab.append(delimiter);
			}
		}
		return ab.toString();
	}

	/**
	 * <p>Checks if a String is whitespace, empty ("") or null.</p>
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("crnk")     = false
	 * StringUtils.isBlank("  crnk  ") = false
	 * </pre>
	 *
	 * @param value the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 */
	public static boolean isBlank(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((!Character.isWhitespace(value.charAt(i)))) {
				return false;
			}
		}
		return true;
	}

	public static String emptyToNull(String value) {
		if (value.length() == 0) {
			return null;
		}
		return value;
	}

	/**
	 * Utility method to take a string and convert it to normal Java variable
	 * name capitalization.  This normally means converting the first
	 * character from upper case to lower case, but in the (unusual) special
	 * case when there is more than one character and both the first and
	 * second characters are upper case, we leave it alone.
	 * <p>
	 * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays
	 * as "URL".
	 * <p>
	 * This follows Java beans specification section 8.8 Capitalization of inferred names.
	 *
	 * @param name The string to be decapitalized.
	 * @return The decapitalized version of the string.
	 */
	public static String decapitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		char chars[] = name.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
}
