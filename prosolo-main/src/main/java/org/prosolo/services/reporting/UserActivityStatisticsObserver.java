package org.prosolo.services.reporting;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Deprecated
@Service("org.prosolo.services.reporting.UserActivityStatisticsObserver")
public class UserActivityStatisticsObserver extends EventObserver {

	protected static Logger logger = Logger
			.getLogger(UserActivityStatisticsObserver.class);

	@Autowired
	private AnalyticalServiceCollector collector;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { EventType.Registered, EventType.LOGIN, EventType.NAVIGATE, EventType.SELECT_GOAL,
				EventType.SELECT_COMPETENCE };
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { User.class};
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("UserActivityStatisticsObserver handling event");
		logger.info("comming in event with action: " + event.getAction());
		logger.info("comming in event with actor: " + event.getActorId());
		logger.info("comming in event with object: " + event.getObject());
		logger.info("comming in event with target: " + event.getTarget());
		
		EventType eventType = event.getAction();
		Map<String, String> parameters = event.getParameters();
		long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
		long userId = event.getActorId();
		
		collector.increaseUserEventCount(eventType, parameters, daysSinceEpoch);
		collector.increaseEventCount(userId, eventType, parameters, daysSinceEpoch);
	}

}
