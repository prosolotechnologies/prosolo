package org.prosolo.services.nodes.observers.learningResources;

import java.util.Locale;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.interfacesettings.UserSettings;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.observers.LearningResourceChangeObserver")
public class LearningResourceChangeObserver extends EventObserver {

	private static Logger logger = Logger.getLogger(LearningResourceChangeObserver.class.getName());

	@Inject private LearningResourceChangeProcessorFactory processorFactory;

	@Inject @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.Edit
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return new Class[] {
				Credential1.class,
				Competence1.class,
				Activity1.class
		};
	}

	public void handleEvent(Event event) {
		logger.info("LearningResourceChangeObserver started");
		
		try {
			LearningResourceChangeProcessor processor = processorFactory.getProcessor(event);
			if(processor != null) {
				processor.process();
				logger.info("LearningResourceChangeObserver finished");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public Locale getLocale(UserSettings userSettings) {
		if (userSettings!= null && userSettings.getLocaleSettings() != null) {
			return userSettings.getLocaleSettings().createLocale();
		} else {
			return new Locale("en", "US");
		}
	}

}
