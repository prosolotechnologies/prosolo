package org.prosolo.services.reporting;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.Event;
import org.prosolo.common.event.EventObserver;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("org.prosolo.services.reporting.TwitterHashtagStatisticsObserver")
public class TwitterHashtagStatisticsObserver extends EventObserver {
	private static Logger logger = Logger.getLogger(TwitterHashtagStatisticsObserver.class);

	@Autowired
	private AnalyticalServiceCollector collector;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { EventType.HASHTAG_ENABLED, EventType.HASHTAG_DISABLED };
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] { User.class };
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("TwitterHashtagStatisticsObserver handling event");
		EventType type = event.getAction();
		logger.info("comming in event with action: " + type);
		logger.info("comming in event with actor: " + event.getActorId());
		logger.info("comming in event with object: " + event.getObject());
		logger.info("comming in event with target: " + event.getTarget());

		Map<String, String> parameters = event.getParameters();
		
		if (EventType.HASHTAG_ENABLED.equals(type)) {
			collector.enableHashtag(parameters.get("hashtag"));
		} else if (EventType.HASHTAG_DISABLED.equals(type)) {
			collector.disableHashtag(parameters.get("hashtag"));
		}
	}

}
