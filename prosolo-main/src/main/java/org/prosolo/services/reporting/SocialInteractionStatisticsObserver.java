package org.prosolo.services.reporting;

import static org.prosolo.common.domainmodel.activities.events.EventType.Comment;

import java.util.Map;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.AnalyticalServiceCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.reporting.SocialInteractionStatisticsObserver")
public class SocialInteractionStatisticsObserver implements EventObserver {
	
	protected static Logger logger = Logger.getLogger(SocialInteractionStatisticsObserver.class);
	
	@Autowired
	private AnalyticalServiceCollector collector;

	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { Comment };
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("SocialInteractionStatisticsObserver handling event");
		logger.info("comming in event with action: " + event.getAction());
		logger.info("comming in event with actor: " + event.getActor());
		logger.info("comming in event with object: " + event.getObject());
		logger.info("comming in event with target: " + event.getTarget());
		
		long source = event.getActor().getId();
		
		long target = ((SocialActivity) event.getTarget()).getTarget().getId();
		
		if (source == target) return;
		
		collector.increaseSocialInteractionCount(source, target);
	}

}
