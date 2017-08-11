package org.prosolo.bigdata.utils;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.prosolo.common.util.date.DateEpochUtil;

public class DateEpochUtilTest {

	@Test
	public void testGetFirstDayOfWeek() throws ParseException {
		Date saturday = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-08-15 UTC");
		Date monday = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-08-10 UTC");
		long saturdayDays = DateEpochUtil.getDaysSinceEpoch(saturday);
		long mondayDays = DateEpochUtil.getDaysSinceEpoch(monday);
		assertEquals(mondayDays, DateEpochUtil.getFirstDayOfWeek((int) saturdayDays));
		assertEquals(mondayDays, DateEpochUtil.getFirstDayOfWeek((int) mondayDays));
	}

}
