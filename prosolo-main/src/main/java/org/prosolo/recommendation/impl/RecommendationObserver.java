package org.prosolo.recommendation.impl;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.event.Event;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.messaging.SessionMessageDistributer;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.web.ApplicationBean;
import org.prosolo.web.home.SuggestedLearningBean;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.util.RecommendationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author Zoran Jeremic 2013-05-25
 */
@Service("org.prosolo.recommendation.RecommendationObserver")
public class RecommendationObserver implements EventObserver {
	
	private static Logger logger = Logger .getLogger(RecommendationObserver.class.getName());
	
	@Autowired private ApplicationBean applicationBean;
	@Autowired private RecommendationConverter recommendationConverter;
	@Autowired private DefaultManager defaultManager;
	@Autowired private SessionMessageDistributer messageDistributer;
	
	@Override
	public EventType[] getSupportedEvents() {
		return new EventType[] { 
				 EventType.Create_recommendation,
				 EventType.DISMISS_RECOMMENDATION
		};
	}

	@Override
	public Class<? extends BaseEntity>[] getResourceClasses() {
		return null;
	}

	@Override
	public void handleEvent(Event event) {
		Session session = (Session) defaultManager.getPersistence().openSession();
	
		// event=(Event) session.get(Event.class, event.getId());
		try{
			if (event.getAction().equals( EventType.Create_recommendation)){
				Recommendation recommendation = (Recommendation) event.getObject();
				User receiver = recommendation.getRecommendedTo();
				
			if(CommonSettings.getInstance().config.rabbitMQConfig.distributed){
					messageDistributer.distributeMessage(ServiceType.ADDSUGGESTEDBYCOLLEAGUES, receiver.getId(), recommendation.getId(), null, null);
			}else{
				HttpSession httpSession = applicationBean.getUserSession(receiver.getId());
				
				if (httpSession != null) {
					SuggestedLearningBean suggestedLearningBean = (SuggestedLearningBean) httpSession.getAttribute("suggestedLearningBean");
					RecommendationData rData = recommendationConverter.convertRecommendationToRecommendedData(recommendation, session);
					suggestedLearningBean.addSuggestedByColleagues(rData);
				} 
			}
			} else if (event.getAction().equals(EventType.DISMISS_RECOMMENDATION)){
				
			}
			
			session.flush();
		} catch (Exception e) {
			logger.error("Exception in handling message", e);
		} finally {
			HibernateUtil.close(session);
		}
	}
}

