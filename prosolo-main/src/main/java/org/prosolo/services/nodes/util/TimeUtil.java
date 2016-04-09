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
	
	public static String getHoursAndMinutesInString(long timeInMinutes) {
		Map<String, Integer> durationMap = TimeUtil.getHoursAndMinutes(timeInMinutes);
		int hours = durationMap.get("hours");
		int minutes = durationMap.get("minutes");
		String duration = hours != 0 ? hours + " hours " : "";
		if(duration.isEmpty()) {
			duration = minutes + " minutes";
		} else if(minutes != 0) {
			duration += minutes + " minutes";
		}
		
		return duration;
	}
}
