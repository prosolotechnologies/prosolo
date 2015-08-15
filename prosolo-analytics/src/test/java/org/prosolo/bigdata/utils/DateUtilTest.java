package org.prosolo.bigdata.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateUtilTest {
	
	@Test
	public void testGetFirstDayOfWeek() throws ParseException {
		Date saturday = new SimpleDateFormat("yyyy-MM-dd").parse("2015-08-15");
		Date monday = new SimpleDateFormat("yyyy-MM-dd").parse("2015-08-10");
		assertEquals(DateUtil.getDaysSinceEpoch(monday), DateUtil.getFirstDayOfWeek(DateUtil.getDaysSinceEpoch(saturday)));
	}
	
	@Test
	public void testGetFirstDayOfMonth() throws ParseException {
		Date fifthJuly = new SimpleDateFormat("yyyy-MM-dd").parse("2015-07-05");
		Date firstJuly = new SimpleDateFormat("yyyy-MM-dd").parse("2015-07-01");
		assertEquals(DateUtil.getDaysSinceEpoch(firstJuly), DateUtil.getFirstDayOfMonth(DateUtil.getDaysSinceEpoch(fifthJuly)));
	}

}
