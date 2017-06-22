package org.prosolo.recommendation.impl;

import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.recommendation.RecommendationObserver")
public class RecommendationObserver 
	//extends EventObserver 
{
	
//	private static Logger logger = Logger .getLogger(RecommendationObserver.class.getName());
//	
//	@Autowired private ApplicationBean applicationBean;
//	@Autowired private RecommendationConverter recommendationConverter;
//	@Autowired private DefaultManager defaultManager;
//	@Autowired private SessionMessageDistributer messageDistributer;
//	
//	@Override
//	public EventType[] getSupportedEvents() {
//		return new EventType[] { 
//				 EventType.Create_recommendation,
//				 EventType.DISMISS_RECOMMENDATION
//		};
//	}
//
//	@Override
//	public Class<? extends BaseEntity>[] getResourceClasses() {
//		return null;
//	}
//
//	@Override
//	public void handleEvent(Event event) {
//		Session session = (Session) defaultManager.getPersistence().openSession();
//	
//		// event=(Event) session.get(Event.class, event.getId());
//		try{
//			if (event.getAction().equals( EventType.Create_recommendation)){
//				Recommendation recommendation = (Recommendation) event.getObject();
//				User receiver = recommendation.getRecommendedTo();
//				
//			if(CommonSettings.getInstance().config.rabbitMQConfig.distributed){
//					messageDistributer.distributeMessage(ServiceType.ADD_SUGGESTED_BY_COLLEAGUES, receiver.getId(), recommendation.getId(), null, null);
//			}else{
//				HttpSession httpSession = applicationBean.getUserSession(receiver.getId());
//				
//				if (httpSession != null) {
//					SuggestedLearningBean suggestedLearningBean = (SuggestedLearningBean) httpSession.getAttribute("suggestedLearningBean");
//					RecommendationData rData = recommendationConverter.convertRecommendationToRecommendedData(recommendation, session);
//					suggestedLearningBean.addSuggestedByColleagues(rData);
//				} 
//			}
//			} else if (event.getAction().equals(EventType.DISMISS_RECOMMENDATION)){
//				
//			}
//			
//			session.flush();
//		} catch (Exception e) {
//			logger.error("Exception in handling message", e);
//		} finally {
//			HibernateUtil.close(session);
//		}
//	}
}

