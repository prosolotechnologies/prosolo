package org.prosolo.bigdata.session.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.EventMatcher;

/**
 * @author Nikola Maric
 * 
 * Holder for event patterns, with additional information
 *
 */
public class LearningEventMatcher implements EventMatcher<LogEvent> {
	
	private String id;
	private String description;
	private String process;
	private List<EventPattern> patternList;
	
	@Override
	public boolean eventMatches(LogEvent event) {
		for(int i = 0; i < patternList.size(); i++) {
			if(!patternList.get(i).match(event)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean eventMatchesPattern(LogEvent event, int patternNumber) {
		return patternList.get(patternNumber).match(event);
	}


	
	public static LearningEventMatcher fromJSONString(String jsonString) throws ParseException {
		JSONObject json = (JSONObject) new JSONParser().parse(jsonString);
		LearningEventMatcher ler = new LearningEventMatcher();
		ler.setId(json.get("id").toString());
		ler.setDescription(json.get("description").toString());
		ler.setProcess(json.get("process").toString());
		JSONArray array = (JSONArray) json.get("patterns");
		if(array != null && array.size() > 0) {
			List<EventPattern> eventPatternList = new ArrayList<>();
			for(int i =0; i < array.size(); i++) {
				JSONObject pattern = (JSONObject) array.get(i);
				Map<String,String> patternMap = new HashMap<>();
				for(Object key : pattern.keySet()) {
					patternMap.put(key.toString(), pattern.get(key).toString());
				}
				EventPattern p = new EventPattern(patternMap);
				eventPatternList.add(p);
			}
			ler.setPatternList(eventPatternList);
		}
		return ler;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProcess() {
		return process;
	}



	public void setProcess(String process) {
		this.process = process;
	}

	public List<EventPattern> getPatternList() {
		return patternList;
	}

	public void setPatternList(List<EventPattern> patternList) {
		this.patternList = patternList;
	}
	
	@Override
	public String toString() {
		return "LearningEventRule [id=" + id + ", description=" + description + ", process=" + process
				+ ", patternList=" + patternList + "]";
	}



	public static class EventPattern {
		
		private Map<String,String> patterns;
		

		public boolean match(LogEvent event) {
			//TODO how to do this properly (we have string values and java objects)
			return false;
		}
		
		public EventPattern(Map<String, String> patterns) {
			super();
			this.patterns = patterns;
		}

		public Map<String, String> getPatterns() {
			return patterns;
		}

		public void setPatterns(Map<String, String> patterns) {
			this.patterns = patterns;
		}


		@Override
		public String toString() {
			return "EventPattern [patterns=" + patterns + "]";
		}
		
		
	}

}
