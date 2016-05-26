package org.prosolo.bigdata.session.impl;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.EventMatcherDao;

/**
 * @author Nikola Maric
 *
 */
public class LearningEventsMatcherDaoImpl implements EventMatcherDao<LogEvent> {

	private static Logger logger = Logger.getLogger(LearningEventsMatcherDaoImpl.class);
	private static List<LearningEventMatcher> matchers;
	
	public static class LearningEventsMatcherDaoImplHolder {
		public static final LearningEventsMatcherDaoImpl INSTANCE = new LearningEventsMatcherDaoImpl();
	}

	public static LearningEventsMatcherDaoImpl getInstance() {
		return LearningEventsMatcherDaoImplHolder.INSTANCE;
	}

	// fetch match rules when class is initialized (or maybe lazy initialize it
	// upon first request?)
	static {
		// TODO move location to config file and read location from there
		String learningEventsFile = "config/learningEventsMatchRules.json";
		String milestoneTypeFile = "config/learningEventsMilestoneIds.json";
		initializeListOfMatchers(learningEventsFile, milestoneTypeFile);
	}

	@Override
	public List<LearningEventMatcher> getEventMatchers() {
		return matchers;
	}

	private static void initializeListOfMatchers(String learningEventsFile, String milestoneTypesFile) {
		try (InputStream milestonesStream = LearningEventsMatcherDaoImpl.class.getClassLoader()
				.getResourceAsStream(milestoneTypesFile);
				InputStream learningEventsStreamStream = LearningEventsMatcherDaoImpl.class.getClassLoader()
						.getResourceAsStream(learningEventsFile)) {

			JSONArray learningEventRules = (JSONArray) new JSONParser().parse(new InputStreamReader(learningEventsStreamStream));
			JSONArray milestoneTypes = (JSONArray) new JSONParser().parse(new InputStreamReader(milestonesStream));
			// JSONArray is a raw sub type of ArrayList. Methods it inherits are
			// raw, all their type parameter
			// uses are erased and reduced to Object. So no ugly casting in
			// streams, using just foreach loop.
			List<LearningEventSummary.Milestone> milestoneTypesList = new ArrayList<>(milestoneTypes.size());
			for (Object milestoneType : milestoneTypes) {
				JSONObject jsonMilestone = (JSONObject) milestoneType;
				LearningEventSummary.Milestone milestone = new LearningEventSummary.Milestone();
				milestone.setId(jsonMilestone.get("id").toString());
				milestone.setType(MilestoneType.valueOf(jsonMilestone.get("type").toString()));
				milestone.setName(jsonMilestone.get("name").toString());
				milestoneTypesList.add(milestone);
			}
			matchers = new ArrayList<>(learningEventRules.size());
			for (int i = 0; i < learningEventRules.size(); i++) {
				matchers.add(
						LearningEventMatcher.fromJSONString(learningEventRules.get(i).toString(), milestoneTypesList));
			}
			logger.info(String.format("Initialized %d event matchers", matchers.size()));
		} catch (Exception e) {
			logger.error("Failed to parse rules for matching learning events", e);
		}
	}

}
