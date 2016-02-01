package org.prosolo.bigdata.session.impl;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.EventMatcher;
import org.prosolo.bigdata.session.EventMatcherDao;


/**
 * @author Nikola Maric
 *
 */
public class LearningEventsMatcherDaoImpl implements EventMatcherDao<LogEvent> {
	
	private static Logger logger = Logger.getLogger(LearningEventsMatcherDaoImpl.class);
	private static List<EventMatcher<LogEvent>> matchers;
	
	public static class LearningEventsMatcherDaoImplHolder {
		public static final LearningEventsMatcherDaoImpl INSTANCE = new LearningEventsMatcherDaoImpl();
	}
	public static LearningEventsMatcherDaoImpl getInstance() {
		return LearningEventsMatcherDaoImplHolder.INSTANCE;
	}

	//fetch match rules when class is initialized (or maybe lazy initialize it upon first request?)
	static {
		//TODO move location to config file and read location from there
		String fileLocation = "src/main/resources/config/learningEventsMatchRules.json";
		initializeListOfMatchers(fileLocation);
	}

	@Override
	public List<EventMatcher<LogEvent>> getEventMatchers() {
		return matchers;
	}
	
	private static void initializeListOfMatchers(String path) {
		try {
			JSONArray array = (JSONArray) new JSONParser().parse(new FileReader(path));
			matchers = new ArrayList<>(array.size());
			for(int i = 0; i < array.size(); i++) {
				matchers.add(LearningEventMatcher.fromJSONString(array.get(i).toString()));
			}
		} catch (IOException | ParseException e) {
			logger.error("Failed to parse rules for matching learning events", e);
		}
	}
	
}
