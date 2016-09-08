package org.prosolo.services.interaction.impl;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.home.SuggestedLearningBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
@author Zoran Jeremic Nov 30, 2013
 */
@Service("org.prosolo.services.interaction.CourseInteractionObserver")
public class CourseInteractionObserver extends EventObserver {
	private static Logger logger = Logger.getLogger(CourseInteractionObserver.class);
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				EventType.ENROLL_COURSE
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
		
		try {
			if (event.getAction().equals(EventType.ENROLL_COURSE)) {
				HttpSession httpSession = applicationBean.getUserSession(event.getActorId());
				
				if (httpSession != null) {
//					SuggestedLearningBean suggestedLearningBean = (SuggestedLearningBean) httpSession
//							.getAttribute("suggestedLearningBean");
//					suggestedLearningBean.loadSuggestedByCourse();
				}
			}
			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}
}
