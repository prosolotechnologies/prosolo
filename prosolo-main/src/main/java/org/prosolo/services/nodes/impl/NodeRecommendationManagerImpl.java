/**
 * 
 */
package org.prosolo.services.nodes.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.NodeRecommendationManager;
import org.prosolo.web.home.data.RecommendationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.nodes.NodeRecommendationManager")
public class NodeRecommendationManagerImpl extends AbstractManagerImpl implements NodeRecommendationManager {
	
	private static final long serialVersionUID = -4943315453841928072L;
	
	private static Logger logger = Logger.getLogger(NodeRecommendationManagerImpl.class);
	
	@Autowired private EventFactory eventFactory;
	
	@Override
	@Transactional
	public Recommendation sendRecommendation(User maker, long receiverId, Node resource, RecommendationType type) throws ResourceCouldNotBeLoadedException {
		User receiver = loadResource(User.class, receiverId);
		
		return sendRecommendation(maker, receiver, resource, type);
	}
	
	@Override
	@Transactional
	public Recommendation sendRecommendation(User maker, User receiver, Node resource, RecommendationType type) {
		Recommendation recommendation = null;
		if (resource != null) {
			recommendation = new Recommendation(type);
			recommendation.setDateCreated(new Date());
			recommendation.setMaker(maker);
			recommendation.setRecommendedTo(receiver);
			recommendation.setRecommendedResource(resource);
			recommendation = saveEntity(recommendation);
			try {
				eventFactory.generateEvent(EventType.Create_recommendation,
						maker, recommendation);
			} catch (EventException e) {
				logger.error(e);
			}
		}

		return recommendation;
	}
	
	public void dismissRecommendation(RecommendationData recommendationData, User user){
		Recommendation recommendation=null;
		
		if(recommendationData.getRecommendationType().equals(RecommendationType.USER)){
			recommendation=recommendationData.getRecommendation();
			recommendation.setDismissed(true);
			recommendation=saveEntity(recommendation);
		}else if(recommendationData.getRecommendationType().equals(RecommendationType.SYSTEM)){
			recommendation=new Recommendation(RecommendationType.SYSTEM);
			recommendation.setDateCreated(new Date());
			recommendation.setDismissed(true);
			recommendation.setRecommendedResource((Node) recommendationData.getResource());
			recommendation.setRecommendedTo(user);
			recommendation=saveEntity(recommendation);
		}
		try{
			eventFactory.generateEvent(EventType.DISMISS_RECOMMENDATION, user, recommendation);
		}catch(EventException e){
			logger.error(e.getLocalizedMessage());
		}
	 
	}
}
