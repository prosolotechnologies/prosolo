package org.prosolo.bigdata.api;

import org.prosolo.bigdata.utils.DateUtil;

enum Period {
	DAY, WEEK;
	
	public Long firstDayFor(long day) {
		switch(this) {
		case WEEK:
			return DateUtil.getFirstDayOfWeek((int) day);
		case DAY:
			return day;
		default:
			throw new IllegalStateException("Period '" + name() + "' is not supported");
		}
	}

}

