package org.prosolo.bigdata.utils;
/**
@author Zoran Jeremic May 23, 2015
 *
 */

public class DateUtil {
	public static long getDaysSinceEpoch(){
		 return System.currentTimeMillis()/86400000;
	}
}

