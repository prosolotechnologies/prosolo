package org.prosolo.services.logging;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Service("org.prosolo.services.logging.LoggingEventsObserver")
public class LoggingEventsObserver implements EventObserver {
	protected static Logger logger = Logger.getLogger(LoggingEventsObserver.class);
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private LoggingService loggingService;

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		String objectType = "";
		long objectId = 0;
		String objectTitle = "";

		BaseEntity object = event.getObject();
		if (object != null) {
			object = HibernateUtil.initializeAndUnproxy(object);
			
			objectType = object.getClass().getSimpleName();
			objectId = object.getId();
			objectTitle = object.getTitle();
		}

		String targetType = "";
		long targetId = 0;

		BaseEntity target = event.getTarget();
		if (target != null) {
			target = HibernateUtil.initializeAndUnproxy(target);
			
			targetType = target.getClass().getSimpleName();
			targetId = target.getId();
		}

		String reasonType = "";
		long reasonId = 0;

		if (event.getReason() != null) {
			reasonType = event.getReason().getClass().getSimpleName();
			reasonId = event.getReason().getId();
		}
		
		String ipAddress = null;
		
		if (event.getActor() != null) {
			HttpSession httpSession = applicationBean.getUserSession(event
					.getActor().getId());
			

			if (httpSession != null) {
				LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
						.getAttribute("loggeduser");
				
				if(!loggedUserBean.isInitialized()) {
					loggedUserBean.initializeSessionData(httpSession);
				}
				
				//LoggedUserBean loggedUserBean = ServiceLocator.getInstance().getService(LoggedUserBean.class);
				ipAddress = loggedUserBean.getIpAddress();
			}
		} else {
			logger.debug("Event without actor:"+event.getAction().name()+" "+event.getObject().getClass().getName());
		}

		try {
			loggingService.logEventObserved(event.getAction(), event.getActor(),
					objectType, objectId, objectTitle, targetType, targetId,
					reasonType, reasonId, event.getParameters(), null, ipAddress);
		} catch (LoggingException e) {
			logger.error(e);
		}
	}

	@Override
	public EventType[] getSupportedEvents() {
		return null;
	}

}
