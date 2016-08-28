package org.prosolo.bigdata.events.observers;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.GenericDAO;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.prosolo.bigdata.events.pojo.DefaultEvent;
import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.services.credentials.PublishingService;
import org.prosolo.bigdata.services.credentials.impl.PublishingServiceImpl;
import org.prosolo.bigdata.streaming.Topic;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.quartz.SchedulerException;

public class ScheduledCredentialObserver implements EventObserver {
	
	private GenericDAO courseDAO = new CourseDAOImpl();
	private PublishingService publishingService = new PublishingServiceImpl();
	private static Logger logger = Logger.getLogger(ScheduledCredentialObserver.class);

	@Override
	public Topic[] getSupportedTopics() {
		return new Topic[]{Topic.LOGS};
	}

	@Override
	public EventType[] getSupportedTypes() {
		return new EventType[]{EventType.Create_Draft, EventType.Edit, EventType.Edit_Draft};
	}

	@Override
	public void handleEvent(DefaultEvent event) {
		LogEvent logEvent = (LogEvent) event;
		if(Credential1.class.getSimpleName().equals(logEvent.getObjectType())) {
			try {
				Credential1 cred = courseDAO.load(Credential1.class, logEvent.getObjectId());
				//if sheduledPublishing is set to a date, create appropriate job
				if(cred.getScheduledPublishDate() != null) {
					//if job already exists, reschedule it
					if(publishingService.publishJobExists(cred.getId())) {
						publishingService.updatePublishingCredentialAtSpecificTime(cred.getId(), cred.getScheduledPublishDate());
						logger.info(String.format("Rescheduling job for publishing credential : %s", cred.getId()));
					}
					//job does not yet exist, create one
					else {
						publishingService.publishCredentialAtSpecificTime(cred.getId(), cred.getScheduledPublishDate());
						logger.info(String.format("Creating job for publishing credential : %s", cred.getId()));
					}
				}
				//publish date is null, remove any scheduling jobs, if they exist
				else {
					if(publishingService.publishJobExists(cred.getId())) {
						publishingService.deletePublishingCredential(cred.getId());
						logger.info(String.format("Removing job for publishing credential : %s", cred.getId()));
					}
				}
				
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Could not retrieve credential to be scheduled", e);
			} catch (SchedulerException e) {
				logger.error("Scheduler error", e);
			}
		}
	
	}

}
