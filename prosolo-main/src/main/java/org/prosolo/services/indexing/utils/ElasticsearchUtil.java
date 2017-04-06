package org.prosolo.services.indexing.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ElasticsearchUtil {

	private static final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
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
}
