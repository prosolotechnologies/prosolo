package org.prosolo.bigdata.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.EventMatcher;
import org.prosolo.bigdata.session.impl.LearningEventSummary.Milestone;

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
	private boolean isMilestoneEvent = false;
	//used for milestones
	private MilestoneType type;
	private String name;
	
	@Override
	public boolean eventMatches(LogEvent event) {
		return patternList.stream().map(s -> s.match(event)).reduce(true,(a,s) -> a && s);
	}
	
	@Override
	public boolean eventMatchesPattern(LogEvent event, int patternNumber) {
		return patternList.get(patternNumber).match(event);
	}


	
	public static LearningEventMatcher fromJSONString(String jsonString, List<LearningEventSummary.Milestone> milestoneTypes) throws ParseException {
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
		if(isMilestone(ler.getId(), milestoneTypes)) {
			Milestone matchingMilestone = getMilestoneById(ler.getId(), milestoneTypes);
			if(matchingMilestone != null) {
				ler.setMilestoneEvent(true);
				ler.setType(matchingMilestone.getType());
				ler.setName(matchingMilestone.getName());
			}
		}
		return ler;
	}
	
	private static Milestone getMilestoneById(String ruleId, List<LearningEventSummary.Milestone> milestoneTypes) {
		for(LearningEventSummary.Milestone mil : milestoneTypes) {
			if(mil.getId().equals(ruleId)) {
				return mil;
			}
		}
		return null;
	}
	
	private static boolean isMilestone(String ruleId,List<LearningEventSummary.Milestone> milestoneTypes) {
		return milestoneTypes.stream().map(mil -> mil.getId().equals(ruleId)).reduce(true,(acc,val) -> acc || val);
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
	
	public boolean isMilestoneEvent() {
		return isMilestoneEvent;
	}

	public void setMilestoneEvent(boolean isMilestoneEvent) {
		this.isMilestoneEvent = isMilestoneEvent;
	}

	@Override
	public String toString() {
		return "LearningEventRule [id=" + id + ", description=" + description + ", process=" + process
				+ ", patternList=" + patternList + "]";
	}
	
	public MilestoneType getType() {
		return type;
	}

	public void setType(MilestoneType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}





	public static class EventPattern {
		
		public static final String EVENT_TYPE_KEY = "eventType";
		public static final String OBJECT_TYPE_KEY = "objectType";
		public static final String TARGET_TYPE_KEY = "targetType";
		public static final String LINK_KEY ="link";
		public static final String CONTEXT_KEY = "context";
		public static final String ACTION_KEY = "action";
		private Map<String,Pattern> patterns;
		

		public boolean match(LogEvent event) {
			boolean matches = true;
			for(Map.Entry<String, Pattern> patternEntry : patterns.entrySet()) {
				if(matches) {
					matches = matchProperty(event, patternEntry);
				}
				else break;
			}
			return matches;
		}
		
		private boolean matchProperty(LogEvent event, Entry<String, Pattern> e) {
			// TODO better way?
			switch(e.getKey()) {
				case EVENT_TYPE_KEY : {
					if(event.getEventType() == null) return false;
					else return e.getValue().matcher(event.getEventType().toString()).matches();
				}
				case OBJECT_TYPE_KEY : {
					if(event.getObjectType() == null) return false;
					else return e.getValue().matcher(event.getObjectType()).matches();
				}
				case TARGET_TYPE_KEY : {
					if(event.getTargetType() == null) return false;
					else return e.getValue().matcher(event.getTargetType()).matches();
				}
				case LINK_KEY : {
					if(event.getLink() == null) return false;
					else return e.getValue().matcher(event.getLink()).matches();
				}
				case CONTEXT_KEY : {
					if(event.getParameters() == null || event.getParameters().get("context") == null) return false;
					else {
						return e.getValue().matcher(event.getParameters().get("context").getAsString()).matches();
					}
				}
				case ACTION_KEY : {
					if(event.getParameters() == null || event.getParameters().get("action") == null) return false;
					else {
						return e.getValue().matcher(event.getParameters().get("action").getAsString()).matches();
					}
				}
				default : return false;
			}
		}

		public EventPattern(Map<String, String> patternStrings) {
			super();
			patterns = new HashMap<>(patternStrings.size());
			//prepare patterns to be used later
			for(Map.Entry<String,String> entry : patternStrings.entrySet()) {
				patterns.put(entry.getKey(), Pattern.compile(entry.getValue()));
			}
		}

		@Override
		public String toString() {
			return "EventPattern [patterns=" + patterns + "]";
		}
		
		public Map<String,Pattern> getRaw() {
			return Collections.unmodifiableMap(patterns);
		}
		
		
	}
	
}
