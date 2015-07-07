/**
 * 
 */
package org.prosolo.services.nodes;

import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.web.home.data.RecommendationData;

/**
 * @author "Nikola Milikic"
 * 
 */
public interface NodeRecommendationManager {

	Recommendation sendRecommendation(User maker, long receiverId,
			Node resource, RecommendationType type)
			throws ResourceCouldNotBeLoadedException;
	
	Recommendation sendRecommendation(User maker, User receiver,
			Node resource, RecommendationType type);

	public void dismissRecommendation(RecommendationData recommendationData,
			User user);

}