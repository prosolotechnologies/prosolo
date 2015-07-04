package org.prosolo.util.date;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
 

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
	
	public static void main(String[] args) {
		Calendar cal = new GregorianCalendar();
		
		//cal.set(2015, 11, 31);
		
		System.out.println(getWeekBeginningDate(cal.getTime()));
		System.out.println(getNextWeekBeginningDate(cal.getTime()));
		
	}
}
