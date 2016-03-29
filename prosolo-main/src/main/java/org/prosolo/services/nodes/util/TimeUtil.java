package org.prosolo.services.nodes.util;

import java.util.HashMap;
import java.util.Map;

public class TimeUtil {

	public static Map<String, Integer> getHoursAndMinutes(long timeInMinutes) {
		int minutesInHour = 60;
		int hours = (int) (timeInMinutes / minutesInHour);
		int minutes = (int) (timeInMinutes % minutesInHour);
		Map<String, Integer> res = new HashMap<>();
		res.put("hours", hours);
		res.put("minutes", minutes);
		return res;
	}
}
