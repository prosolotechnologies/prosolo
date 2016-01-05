package org.prosolo.news;


import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.featuredNews.LearningGoalFeaturedNews;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.ChangeVisibilityEvent;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.interaction.FeaturedNewsManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.home.FeaturedNewsBean;
import org.prosolo.web.home.data.FeaturedNewsData;
import org.prosolo.web.home.util.FeaturedNewsConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.news.FeaturedNewsObserver")
public class FeaturedNewsObserver extends EventObserver  {
	private static Logger logger = Logger .getLogger(FeaturedNewsObserver.class.getName());
	@Autowired private FeaturedNewsManager featuredNewsManager;
	@Autowired private ApplicationBean applicationBean;
	@Autowired private DefaultManager defaultManager;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[]{
			EventType.ChangeVisibility
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		 Session session = (Session) defaultManager.getPersistence().openSession();
//		 event = (Event) session.get(Event.class, event.getId());
		try{
		 if (event instanceof ChangeVisibilityEvent) {

			VisibilityType visType = ((ChangeVisibilityEvent) event).getNewVisibility();
			BaseEntity object = event.getObject();
			
			if (visType == VisibilityType.PUBLIC && object instanceof LearningGoal) {
				LearningGoalFeaturedNews featuredNews = featuredNewsManager.createPublicFeaturedNewsForEvent(event, session);
				Map<Long, HttpSession> httpsessions = applicationBean.getAllHttpSessions();
				Collection<HttpSession> sessions = httpsessions.values();
				FeaturedNewsData fNewsData = FeaturedNewsConverter.convertFeaturedNewsToFeaturedNewsData(featuredNews);
				for (HttpSession httpSession : sessions) {
					if (httpSession != null) {
						FeaturedNewsBean featuredNewsBean = (FeaturedNewsBean) httpSession.getAttribute("featuredNewsBean");
						LoggedUserBean loggedUserBean=(LoggedUserBean) httpSession.getAttribute("loggeduser");
						if(loggedUserBean.getUser().getId()!=fNewsData.getActor().getId()){
							if (featuredNewsBean != null) {
								featuredNewsBean.addFeaturedNewsData(fNewsData);
							}
						}
					}
				}
			} else if (visType == VisibilityType.PRIVATE) {

			}
		}
		session.flush();
		}catch(Exception e){
			logger.error("Exception in handling message",e);
		}finally{
			HibernateUtil.close(session);
		}
	}

}
