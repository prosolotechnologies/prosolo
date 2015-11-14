package org.prosolo.bigdata.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void testGetFirstDayOfWeek() throws ParseException {
		Date saturday = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-08-15 UTC");
		Date monday = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-08-10 UTC");
		long saturdayDays = DateUtil.getDaysSinceEpoch(saturday);
		long mondayDays = DateUtil.getDaysSinceEpoch(monday);
		assertEquals(mondayDays, DateUtil.getFirstDayOfWeek((int) saturdayDays));
		assertEquals(mondayDays, DateUtil.getFirstDayOfWeek((int) mondayDays));
	}

}
