package org.prosolo.bigdata.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Zoran Jeremic May 23, 2015
 *
 */
public class DateUtil {
	
	private static long milliseconds(long day) {
		return day * 86400000;
	}
	
	private static long days(long milliseconds) {
		return milliseconds / 86400000;
	}
	
	public static long getDaysSinceEpoch() {
		return days(System.currentTimeMillis());
	}

	public static long getDaysSinceEpoch(Date date) {
		return days(date.getTime());
	}
	public static long getDaysSinceEpoch(long timestamp){
		return days(timestamp);
	}
	
	public static long getWeeksSinceEpoch() {
		return getDaysSinceEpoch() / 7;
	}
	
	public static long getWeeksSinceEpoch(Date date) {
		return getDaysSinceEpoch(date) / 7;
	}
	
	public static long getFirstDayOfWeek(long day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds(day));
		calendar.set(Calendar.DAY_OF_WEEK, 1);
		return days(calendar.getTimeInMillis());
	}
	
	public static long getFirstDayOfMonth(long day) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds(day));
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return days(calendar.getTimeInMillis());
	}

}
