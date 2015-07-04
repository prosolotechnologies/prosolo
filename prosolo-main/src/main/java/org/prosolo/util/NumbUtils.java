package org.prosolo.util;

/**
 * @author Zoran Jeremic
 * @date May 1, 2012
 */

public class NumbUtils {

	public static int getNumberOfPages(Integer resNumber, int PAGE_SIZE) {
		Double val = (double) (resNumber.doubleValue() / PAGE_SIZE);
		return (int) Math.ceil(val);
	}

}
