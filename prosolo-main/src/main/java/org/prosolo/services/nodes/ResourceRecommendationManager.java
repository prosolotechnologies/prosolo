/**
 * @author Zoran Jeremic 
 */
package org.prosolo.services.nodes;

import java.util.Collection;

import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.services.general.AbstractManager;

public interface ResourceRecommendationManager extends AbstractManager {
	
	Collection<Recommendation> getRecommendationsOfResource(Node resource);
}
