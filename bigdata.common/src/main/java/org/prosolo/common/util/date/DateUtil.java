package org.prosolo.common.util.date;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;
 
 

/**
 * @author Nikola Milikic
 * 
 */
public class DateUtil {
	
	private static Logger logger = Logger.getLogger(DateUtil.class);
	
	private static final long ONE_HOUR = 60 * 60 * 1000L;
	public static final String DATE = "dd.MM.yyyy";
	public static final String DASH_DATE = "yyyy-MM-dd";
	public static final String DATE_EN="MMM dd, yyyy";
	public static final String HOUR_DATE_EN="h:mm a 'on' MMM dd, yyyy"; // 13:45 on Oct 15, 2015 
	public static final String TIME_DATE = "HH:mm - dd.MM.yyyy";
	public static final String SQL_TIME_DATE = "yyyy-MM-dd HH:mm:ss";
	
	public static final SimpleDateFormat sdfHourDateEn = new SimpleDateFormat(HOUR_DATE_EN);
	
	static {
		DateFormatSymbols symbols = new DateFormatSymbols();
		symbols.setAmPmStrings(new String[] { "am", "pm" });
		sdfHourDateEn.setDateFormatSymbols(symbols);
	}


	public static long daysBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime() + ONE_HOUR) / (ONE_HOUR * 24));
	}

	public static long hoursBetween(Date d1, Date d2) {
		return ((d2.getTime() - d1.getTime()) / (ONE_HOUR));
	}

	public static String getPrettyDate(Date date) {
		return getPrettyDate(date, DateUtil.DATE);
	}
	public static String getPrettyDateEn(Date date) {
		return getPrettyDate(date, DateUtil.DATE_EN);
	}
	
	public static String getPrettyTimeDate(Date date) {
		return getPrettyDate(date, DateUtil.TIME_DATE);
	}

	public static String getPrettyDate(Date date, String format) {
		if (date != null) {
			return new SimpleDateFormat(format).format(date);
		} else {
			return "";
		}
	}

	public static String getTimeBetween(Date d1, Date d2) {
		// Calculate difference in milliseconds
		long diff = d2.getTime() - d1.getTime();

		// Calculate difference in seconds
		long diffSeconds = diff / 1000;

		// Calculate difference in minutes
		long diffMinutes = diff / (60 * 1000);

		// Calculate difference in hours
		long diffHours = diff / (60 * 60 * 1000);

		// Calculate difference in days
		long diffDays = diff / (24 * 60 * 60 * 1000);

		if (diffDays == 1) {
			return "1 day";
		} else if (diffDays > 1) {
			return String.valueOf(diffDays) + " days";
		} else if (diffHours == 1) {
			return "1 hour";
		} else if (diffHours > 1) {
			return String.valueOf(diffHours) + " hours";
		} else if (diffMinutes == 1) {
			return "1 minute";
		} else if (diffMinutes > 1) {
			return String.valueOf(diffMinutes) + " minutes";
		} else if (diffSeconds == 1) {
			return "1 second";
		} else {
			return String.valueOf(diffSeconds) + " seconds";
		}
	}

	enum TimePeriod {
		DAY, HOUR, MINUTE, SECOND
	}

	public enum TimePeriodExtensionType {
		AGO, PERIOD
	}
	
	private static HashMap<TimePeriod, String> timeAgo = new HashMap<TimePeriod, String>();
	private static HashMap<TimePeriod, String> timePeriod = new HashMap<TimePeriod, String>();
	static {
		timeAgo.put(TimePeriod.DAY, "d ago");
		timeAgo.put(TimePeriod.HOUR, "h ago");
		timeAgo.put(TimePeriod.MINUTE, "m ago");
		timeAgo.put(TimePeriod.SECOND, "s ago");
		
		timePeriod.put(TimePeriod.DAY, " days");
		timePeriod.put(TimePeriod.HOUR, " hours");
		timePeriod.put(TimePeriod.MINUTE, " minutes");
		timePeriod.put(TimePeriod.SECOND, " seconds");
	}

	public static String getTimeExtension(TimePeriod period, TimePeriodExtensionType type) {
		if (type.equals(TimePeriodExtensionType.AGO)) {
			return timeAgo.get(period);
		} else {
			return timePeriod.get(period);
		}
	}

	/**
	 * long diff in miliseconds
	 */
	public static String getTimeDuration(long diff, TimePeriodExtensionType type) {
		// Calculate difference in seconds
		long diffSeconds = diff / 1000;
		// Calculate difference in minutes
		long diffMinutes = diff / (60 * 1000);
		// Calculate difference in hours
		long diffHours = diff / (60 * 60 * 1000);
		// Calculate difference in days
		long diffDays = diff / (24 * 60 * 60 * 1000);

		if (diffDays > 0) {
			return String.valueOf(diffDays)	+ getTimeExtension(TimePeriod.DAY, type);
		} else if (diffHours > 0) {
			return String.valueOf(diffHours) + getTimeExtension(TimePeriod.HOUR, type);
		} else if (diffMinutes > 0) {
			return String.valueOf(diffMinutes) + getTimeExtension(TimePeriod.MINUTE, type);
		} else if (diffSeconds >= 0) {
			return String.valueOf(diffSeconds) + getTimeExtension(TimePeriod.SECOND, type);
		} else {
			return null;
		}
	}
	
	public static String getTimeAgoFromNow(Date d1) {
		String suffix = " ago";
		Date d2 = new Date();
		long diff = d2.getTime() - d1.getTime();
		
		// Calculate difference in days
		long diffDays = diff / (24 * 60 * 60 * 1000);
		
		if (diffDays > 0) {
			// TODO: if there is locale, it should be set here
			return sdfHourDateEn.format(d1);
		}

		// Calculate difference in hours
		long diffHours = diff / (60 * 60 * 1000);
		
		if (diffHours > 0) {
			if (diffHours == 1) {
				return String.valueOf(diffHours) + " hr" + suffix;
			}
			return String.valueOf(diffHours) + " hrs" + suffix;
		}
		
		// Calculate difference in minutes
		long diffMinutes = diff / (60 * 1000);
		
		if (diffMinutes > 0) {
			if (diffMinutes == 1) {
				return String.valueOf(diffMinutes) + " min" + suffix;
			}
			return String.valueOf(diffMinutes) + " mins" + suffix;
		}
		
		// Calculate difference in seconds
		long diffSeconds = diff / 1000;
		
		if (diffSeconds >= 0) {
			return String.valueOf(diffSeconds) + " sec" + suffix;
		}
		return "";
	}

	public static String getTimeDuration(Long diffSeconds) {
		return getTimeDuration(diffSeconds, TimePeriodExtensionType.PERIOD);
	}
	
	public static long getSecondsDifference(Date start, Date end){
		long seconds=(start.getTime()-end.getTime())/1000;
		return seconds;
	}
	
	public static long getDayDifference(Date start, Date end) {
		FAQCalendar faqStart = new FAQCalendar(start.getTime());
		FAQCalendar faqEnd = new FAQCalendar(end.getTime());
		return faqStart.diffDayPeriods(faqEnd);
	}

	public static String getDayName(Date d) {
		DateFormat f = new SimpleDateFormat("EEEE");
		
		try {
			return f.format(d);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return "";
		}
	}

	public static Date getTimeFrameMarginDate(TimeFrame timeFrame) {
		int calendarField = -1;

		switch (timeFrame) {
			case WEEK:
				calendarField = Calendar.WEEK_OF_YEAR;
				break;
			case MONTH:
				calendarField = Calendar.MONTH;
				break;
			case YEAR:
				calendarField = Calendar.YEAR;
				break;
			case OVERALL:
				return null;
			default:
				return null;
		}

		GregorianCalendar cal = new GregorianCalendar();
		cal.add(calendarField, -1);

		return cal.getTime();
	}
	
	public static String createUpdateTime(Date lastUpdate) {
		String updateTime = null;
		Date now = new Date();
		
		if (DateUtil.daysBetween(lastUpdate, now) == 0) {
			updateTime = DateUtil.getTimeAgoFromNow(lastUpdate);
		} else {
			updateTime = DateUtil.getPrettyDate(lastUpdate);
		}
		return updateTime;
	}
	
	public static long getDaysSinceEpoch(){
		 return System.currentTimeMillis()/86400000;
	}

	public static Date parseDashDate(String dateString) {
		return parseDate(dateString, DASH_DATE);
	}
	
	public static Date parseDate(String dateString, String format) {
		if (dateString != null && !dateString.isEmpty()) {
			DateFormat formater = new SimpleDateFormat(format);
			
			try {
				return formater.parse(dateString);
			} catch (ParseException e) {
				logger.error(e);
			}
		}
		return null;
	}
	
	public static Date getDayBeginningDateTime(Date date) {
		return new LocalDate(date).toDateTimeAtStartOfDay().toDate();
	}
	
	public static Date getNextDay(Date date) {
		return new LocalDate(date).toDateTimeAtStartOfDay().plusDays(1).toDate();
	}

	public static Date getWeekBeginningDate(Date date) {
		LocalDate weekBegin = new LocalDate(date).withDayOfWeek(DateTimeConstants.MONDAY);
		return weekBegin.toDateTimeAtStartOfDay().toDate();
	}
	
	public static Date getNextWeekBeginningDate(Date date) {
		LocalDate weekBegin = new LocalDate(date).withDayOfWeek(DateTimeConstants.MONDAY);
		return weekBegin.toDateTimeAtStartOfDay().plusWeeks(1).toDate();
	}
	
	public static Date getMonthBeginningDate(Date date) {
		LocalDate monthBegin = new LocalDate(date).withDayOfMonth(1);
		
		return monthBegin.toDateTimeAtStartOfDay().toDate();
	}
	
	public static Date getNextMonthBeginningDate(Date date) {
		LocalDate monthBegin = new LocalDate(date).withDayOfMonth(1);
		
		return monthBegin.toDateTimeAtStartOfDay().plusMonths(1).toDate();
	}
	
	/**
	 * Returns string representation of a date. Example of a string returned: 
	 * Dec 15, 2016 at 12:00 AM
	 * @param date
	 * @return
	 */
	public static String parseDateWithShortMonthName(Date date) {
		if(date == null) {
			return null;
		}
	 
	    DateFormat dfFr = new SimpleDateFormat(
	        "MMM dd, yyyy 'at' hh:mm a");

	    return dfFr.format(date);
	}

	public static String formatDate(Date date, String format) {
		if (date == null || format == null) {
			return null;
		}

		DateFormat dfFr = new SimpleDateFormat(format);

		return dfFr.format(date);
	}

	//java 8 date time
	
	public static Date toDate(LocalDateTime ldt) {
		ZoneId zone = ZoneId.systemDefault();
		return toDate(ldt, zone);
	}
	
	public static Date toDate(LocalDateTime ldt, ZoneId zone) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return Date.from(ldt.atZone(zone).toInstant());
	}
	
	public static LocalDateTime toLocalDateTime(Date date) {
		ZoneId zone = ZoneId.systemDefault();
		return toLocalDateTime(date, zone);
	}
	
	public static LocalDateTime toLocalDateTime(Date date, ZoneId zone) {
		if(date == null) {
			throw new NullPointerException();
		}
		return date.toInstant().atZone(zone).toLocalDateTime();
	}
	
	public static LocalDateTime yesterday(LocalDateTime ldt) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return ldt.minusDays(1);
	}
	
	public static LocalDateTime weekAgo(LocalDateTime ldt) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return ldt.minusWeeks(1);
	}
	
	public static LocalDateTime getWeekAgoDayStartDateTime(LocalDateTime ldt) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return getDayBeginningDateTime(ldt.minusWeeks(1));
	}
	
	public static LocalDateTime getNDaysAgoDayStartDateTime(LocalDateTime ldt, int daysAgo) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return getDayBeginningDateTime(ldt.minusDays(daysAgo));
	}
	
	public static LocalDateTime getYesterdayEndDateTime(LocalDateTime ldt) {
		return getDayEndingDateTime(yesterday(ldt));
	}
	
	public static LocalDateTime getYesterdayBeginningDateTime(LocalDateTime ldt) {
		return getDayBeginningDateTime(yesterday(ldt));
	}
	
	public static LocalDateTime getDayBeginningDateTime(LocalDateTime ldt) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return ldt.toLocalDate().atStartOfDay();
	}
	
	public static LocalDateTime getDayEndingDateTime(LocalDateTime ldt) {
		if(ldt == null) {
			throw new NullPointerException();
		}
		return ldt.with(LocalTime.MAX);
	}
	
	public static boolean isDayOfWeek(LocalDateTime ldt, DayOfWeek day) {
		if(ldt == null || day == null) {
			throw new NullPointerException();
		}
		return ldt.getDayOfWeek() == day;
	}
	
	public static void main(String[] args) {
		Calendar cal = new GregorianCalendar();
		
		//cal.set(2015, 11, 31);
		
		System.out.println(getWeekBeginningDate(cal.getTime()));
		System.out.println(getNextWeekBeginningDate(cal.getTime()));
		Date date = new GregorianCalendar(2016, 0, 15).getTime();
		System.out.println(parseDateWithShortMonthName(date));
		
		Date now = new Date();
		System.out.println("Date " + now);
		LocalDateTime ldt = LocalDateTime.now();
		System.out.println("Local date " + ldt);
		System.out.println("Yesterday local date " +yesterday(ldt));
		System.out.println("Week ago local date " + weekAgo(ldt));
		System.out.println("Local date to date " + toDate(ldt));
		System.out.println("Date to local date " + toLocalDateTime(now));
		System.out.println("Day beginning " + getDayBeginningDateTime(ldt));
		System.out.println("Day ending " + getDayEndingDateTime(ldt));
		System.out.println("Yesterday beginning " + getYesterdayBeginningDateTime(ldt));
		System.out.println("Yesterday ending " + getYesterdayEndDateTime(ldt));
		System.out.println("Week ago start " + getWeekAgoDayStartDateTime(ldt));
		System.out.println("Is monday " + isDayOfWeek(ldt, DayOfWeek.MONDAY));
		System.out.println("Is friday " + isDayOfWeek(ldt, DayOfWeek.FRIDAY));
		System.out.println("6 days ago " + getNDaysAgoDayStartDateTime(ldt, 6));


		OffsetDateTime odt = LocalDateTime.now().atOffset(ZoneOffset.ofTotalSeconds(-120 * 60));


		System.out.println("DATE WITH OFFSET: " + odt);
		System.out.println("DATE WITH OFFSET TO INSTANT: " + odt.toInstant());
		System.out.println("Offset date time converted to java.util.Date: " + Date.from(odt.toInstant()));

		System.out.println("INSTANT NOW: " + Instant.now());

		System.out.println("NOW AT ZONE: " + LocalDateTime.now().atZone(TimeZone.getTimeZone("UTC").toZoneId()));
	}
}
