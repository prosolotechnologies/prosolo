package org.prosolo.services.indexing.utils;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.impl.CredentialManagerImpl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ElasticsearchUtil {

	private static Logger logger = Logger.getLogger(ElasticsearchUtil.class);

	private static final DateFormat df = getDateFormatter();

	/**
	 * 
	 * @param date
	 * @throws {@link NullPointerException} if date is null
	 */
	public static String getDateStringRepresentation(Date date) {
		if(date == null) {
			throw new NullPointerException();
		}
		return df.format(date);
	}

	public static Date parseDate(String date) {
		try {
			return date == null ? null : df.parse(date);
		} catch (ParseException e) {
			logger.error(e);
			return null;
		}
	}

	private static DateFormat getDateFormatter() {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df;
	}
}
