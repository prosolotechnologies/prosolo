package org.prosolo.bigdata.api;

import org.prosolo.common.util.date.DateEpochUtil;

enum Period {
	DAY, WEEK;
	
	public Long firstDayFor(long day) {
		switch(this) {
		case WEEK:
			return DateEpochUtil.getFirstDayOfWeek((int) day);
		case DAY:
			return day;
		default:
			throw new IllegalStateException("Period '" + name() + "' is not supported");
		}
	}

}

