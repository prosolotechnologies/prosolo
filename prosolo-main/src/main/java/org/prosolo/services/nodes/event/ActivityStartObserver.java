package org.prosolo.services.nodes.event;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.goals.LearnBean;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.goals.cache.GoalDataCache;
import org.prosolo.web.goals.cache.LearningGoalPageDataCache;
import org.prosolo.web.home.SuggestedLearningBean;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.event.ActivityStartObserver")
public class ActivityStartObserver implements EventObserver {

	@Inject
	private ActivityStartEventProcessorFactory activityStartEventProcessorFactory;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] {
			EventType.SERVICEUSE,
			EventType.NAVIGATE
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		activityStartEventProcessorFactory.getActivityStartEventProcessor(event).updateActivity();
	}

	

}
