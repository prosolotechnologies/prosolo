package org.prosolo.web.portfolio.util;

import java.util.Comparator;

import org.prosolo.web.data.GoalData;

public class UncompletedGoalDescComparator implements Comparator<GoalData> {

	@Override
	public int compare(GoalData data1, GoalData data2) {
		if (data1.getLastActivity() != null) {
			if (data2.getLastActivity() != null) {
				return data1.getLastActivity().before(data2.getLastActivity()) ? 1 : -1;
			} else {
				return 1;
			}
		}
		// here last activity of data1 is null
		if (data2.getLastActivity() != null) {
			return 1;
		}
		return data1.getTitle().compareToIgnoreCase(data2.getTitle());
	}
	
}