package org.prosolo.services.logging;

import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.context.ContextJsonParserService;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.event.context.LearningContext;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic Dec 29, 2013
 */
@Service("org.prosolo.services.logging.LoggingEventsObserver")
public class LoggingEventsObserver extends EventObserver {
	protected static Logger logger = Logger.getLogger(LoggingEventsObserver.class);
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private LoggingService loggingService;
	@Inject private ContextJsonParserService contextJsonParserService;

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		logger.info("LoggingEvent handling event action: " + event.getAction());
		logger.info("LoggingEvent handling event actor: " + event.getActor());
		logger.info("LoggingEvent handling event object: " + event.getObject());
		logger.info("LoggingEvent handling event target: " + event.getTarget());
		logger.info("LoggingEvent handling event page:"+event.getPage());
		logger.info("LoggingEvent handling event context:"+event.getContext());
		logger.info("LoggingEvent handling event service:"+event.getService());
		String objectType = "";
		long objectId = 0;
		String objectTitle = "";
		
		try {
			//adding for migration to new context approach
			LearningContext learningContext = contextJsonParserService.
					parseCustomContextString(event.getPage(), event.getContext(), event.getService());
			
			BaseEntity object = event.getObject();
			if (object != null) {
				object = HibernateUtil.initializeAndUnproxy(object);
				
				objectType = object.getClass().getSimpleName();
				objectId = object.getId();
				objectTitle = object.getTitle();
			} else {
				Map<String, String> params = event.getParameters();
				if(params != null) {
					objectType = event.getParameters().get("objectType");
				}
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
			
			Map<String, String> params = event.getParameters();
			if(params != null && params.containsKey("ip")) {
				ipAddress = event.getParameters().get("ip");
			} else if (event.getActor() != null) {
				HttpSession httpSession = applicationBean.getUserSession(event
						.getActor().getId());
				
	
				if (httpSession != null) {
					LoggedUserBean loggedUserBean = (LoggedUserBean) httpSession
							.getAttribute("loggeduser");
					
					if(loggedUserBean != null) {
						if(!loggedUserBean.isInitialized()) {
							loggedUserBean.initializeSessionData(httpSession);
						}
					
						//LoggedUserBean loggedUserBean = ServiceLocator.getInstance().getService(LoggedUserBean.class);
						ipAddress = loggedUserBean.getIpAddress();
					} else {
						Map<String, Object> userData = (Map<String, Object>) httpSession.getAttribute("user");
						if(userData != null){
							ipAddress = (String) userData.get("ipAddress");
						}
					}
				}
			} else {
				logger.debug("Event without actor:"+event.getAction().name()+" "+event.getObject().getClass().getName());
			}
	
			try {
				loggingService.logEventObserved(event.getAction(), event.getActor(),
						objectType, objectId, objectTitle, targetType, targetId,
						reasonType, reasonId, event.getParameters(), ipAddress, learningContext);
			} catch (LoggingException e) {
				logger.error(e);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public EventType[] getSupportedEvents() {
		return null;
	}

}
