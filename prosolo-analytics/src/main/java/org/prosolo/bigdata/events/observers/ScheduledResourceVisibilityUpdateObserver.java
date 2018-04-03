package org.prosolo.bigdata.events.observers;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.impl.CompetenceDAOImpl;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.jobs.data.Resource;
import org.prosolo.bigdata.services.credentials.VisibilityService;
import org.prosolo.bigdata.services.credentials.impl.ResourceVisibilityServiceImpl;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.quartz.SchedulerException;

import java.util.Date;

public class ScheduledResourceVisibilityUpdateObserver implements EventObserver {
	
	private CourseDAOImpl courseDAO = new CourseDAOImpl(true);
	private CompetenceDAOImpl compDAO = new CompetenceDAOImpl();
	private VisibilityService visibilityService = new ResourceVisibilityServiceImpl();
	private static Logger logger = Logger.getLogger(ScheduledResourceVisibilityUpdateObserver.class);

	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[]{Topic.LOGS};
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[]{
				EventType.SCHEDULED_VISIBILITY_UPDATE,
				EventType.CANCEL_SCHEDULED_VISIBILITY_UPDATE
		};
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		LogEvent logEvent = (LogEvent) event;
		
		Resource resource = null;
		Date date = null;
		long resourceId = logEvent.getObjectId();
		long actorId = logEvent.getActorId();
		try {
			if(Credential1.class.getSimpleName().equals(logEvent.getObjectType())) {
				date = courseDAO.getScheduledVisibilityUpdateDate(resourceId);
				resource = Resource.CREDENTIAL;
			} else {
				date = compDAO.getScheduledVisibilityUpdateDate(resourceId);
				resource = Resource.COMPETENCE;
			}
			
			//if sheduledPublishing is set to a date, create appropriate job
			if(date != null) {
				//if job already exists, reschedule it
				if(visibilityService.visibilityUpdateJobExists(resourceId, resource)) {
					visibilityService.changeVisibilityUpdateTime(resourceId, resource, date);
					logger.info(String.format("Rescheduling job for visibility update for " + resource.name() + " : %s", resourceId));
				}
				//job does not yet exist, create one
				else {
					visibilityService.updateVisibilityAtSpecificTime(actorId, resourceId, resource, date);
					logger.info(String.format("Creating job for visibility update for " + resource.name() + " : %s", resourceId));
				}
			}
			//publish date is null, remove any scheduling jobs, if they exist
			else {
				if(visibilityService.visibilityUpdateJobExists(resourceId, resource)) {
					visibilityService.cancelVisibilityUpdate(resourceId, resource);
					logger.info(String.format("Removing job for visibility update for " + resource.name() + " : %s", resourceId));
				}
			}
			
		} catch (SchedulerException e) {
			logger.error("Scheduler error", e);
		}
	}

}
