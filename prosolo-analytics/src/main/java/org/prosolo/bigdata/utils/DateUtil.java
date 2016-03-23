package org.prosolo.bigdata.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;

/**
 * @author Zoran Jeremic May 23, 2015
 *
 */
public class DateUtil {

	private static MutableDateTime epoch() {
		return new MutableDateTime(0, DateTimeZone.UTC);
	}

	private static MutableDateTime utc(int days) {
		MutableDateTime result = epoch();
		result.addDays(days);
		return result;
	}

	private static int days(MutableDateTime date) {
		return Days.daysBetween(epoch(), date).getDays();
	}

	public static long getDaysSinceEpoch() {
		return days(new MutableDateTime(System.currentTimeMillis(), DateTimeZone.UTC));
	}

	public static long getDaysSinceEpoch(Date date) {
		return days(new MutableDateTime(date, DateTimeZone.UTC));
	}

	public static long getDaysSinceEpoch(long timestamp) {
		return days(new MutableDateTime(timestamp, DateTimeZone.UTC));
	}

	public static long getTimeSinceEpoch(int day) {
		return utc(day).getMillis();
	}

	public static long getFirstDayOfWeek(int day) {
		// First day of week is Monday.
		MutableDateTime firstDay = utc(day);
		firstDay.setDayOfWeek(1);
		return days(firstDay);
	}
	
	public static long parseDaysSinceEpoch(String dateString, String format) throws ParseException {
		return getDaysSinceEpoch(new SimpleDateFormat(format).parse(dateString));
	}

}
