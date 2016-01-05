package org.prosolo.services.nodes.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.prosolo.services.nodes.ActivityTimeSpentPercentileService;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.ActivityTimeSpentPercentile")
public class ActivityTimeSpentPercentileServiceImpl implements ActivityTimeSpentPercentileService {
 
	@Override
	public int getPercentileGroup(List<Long> times, long timeSpentForObservedActivity) {
		final int numberOfGroups = 5;
		
		int size = times.size();
		if (size < numberOfGroups) {
			return calculateGroupSpecific(times, timeSpentForObservedActivity);
		}
		
		double percentile = (double) 1 / numberOfGroups;
		BigDecimal bd = BigDecimal.valueOf(percentile);
		for(int i = 1; i <= numberOfGroups; i++) {
			//double currentPercentile = percentile * i;
			BigDecimal currentPercentile = bd.multiply(BigDecimal.valueOf(i));
			long upperBound = getKthPercentile(times, currentPercentile);
			if(upperBound >= timeSpentForObservedActivity) {
				return i;
			}
		}
		
		return 1;
	}
	
	//algorithm is working only when number of groups is 5
	private int calculateGroupSpecific(List<Long> times, long timeSpentForObservedActivity) {
		int size = times.size();
		switch (size) {
		case 1:
			return 3;
		case 2:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else {
				return 5;
			}
		case 3:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else if(times.get(1) == timeSpentForObservedActivity) {
				return 3;
			} else {
				return 5;
			}
		case 4:
			if (times.get(0) == timeSpentForObservedActivity) {
				return 1;
			} else if (times.get(1) == timeSpentForObservedActivity) {
				return 2;
			} else if (times.get(2) == timeSpentForObservedActivity) {
				return 3;
			} else {
				return 4;
			}
		default:
			return 1;
		}
	}

	private long getKthPercentile(List<Long> times, BigDecimal currentPercentile) {
		BigDecimal k = currentPercentile.multiply(BigDecimal.valueOf(times.size()));
		//check if k is whole number
		if(!isIntegerValue(k)) {
			k = k.setScale(0, RoundingMode.CEILING);
		}
		
		int index = k.intValueExact() - 1;
		return times.get(index);
	}
	
	private boolean isIntegerValue(BigDecimal bd) {
		  return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
	}
}
