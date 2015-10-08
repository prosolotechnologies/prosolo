package org.prosolo.bigdata.api;

import org.prosolo.bigdata.utils.DateUtil;

enum Period {
	DAY, WEEK, MONTH;
	
	public Long firstDayFor(long day) {
		switch(this) {
		case WEEK:
			return DateUtil.getFirstDayOfWeek(day);
		case MONTH:
			return DateUtil.getFirstDayOfMonth(day);
		case DAY:
			return day;
		default:
			throw new IllegalStateException("Period '" + name() + "' is not supported");
		}
	}

}

