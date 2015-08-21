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
		Date sunday = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-08-09 UTC");
		assertEquals(DateUtil.getDaysSinceEpoch(sunday), DateUtil.getFirstDayOfWeek(DateUtil.getDaysSinceEpoch(saturday)));
		assertEquals(DateUtil.getDaysSinceEpoch(sunday), DateUtil.getFirstDayOfWeek(DateUtil.getDaysSinceEpoch(sunday)));
	}
	
	@Test
	public void testGetFirstDayOfMonth() throws ParseException {
		Date fifthJuly = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-07-05 UTC");
		Date firstJuly = new SimpleDateFormat("yyyy-MM-dd Z").parse("2015-07-01 UTC");
		assertEquals(DateUtil.getDaysSinceEpoch(firstJuly), DateUtil.getFirstDayOfMonth(DateUtil.getDaysSinceEpoch(fifthJuly)));
		assertEquals(DateUtil.getDaysSinceEpoch(firstJuly), DateUtil.getFirstDayOfMonth(DateUtil.getDaysSinceEpoch(firstJuly)));
	}

}
