/**
 * @author Zoran Jeremic 
 */
package org.prosolo.services.nodes.impl;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Recommendation;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.ResourceRecommendationManager;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.nodes.ResourceRecommendationManager")
public class ResourceRecommendationManagerImpl extends AbstractManagerImpl implements ResourceRecommendationManager {

	private static final long serialVersionUID = 8884702628706083276L;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResourceRecommendationManagerImpl.class);

	@Override
	public Collection<Recommendation> getRecommendationsOfResource(Node resource) {
		String query = 
				"SELECT DISTINCT rec " +
				"FROM Recommendation rec " +
				"WHERE rec.recommendedResource = :resource " +
					"AND rec.accepted = :accepted " +
					"AND rec.rejected = :rejected";
		
		
		
		@SuppressWarnings("unchecked")
		List<Recommendation> result = persistence.currentManager().createQuery(query).
				setEntity("resource", resource).
				setBoolean("accepted", false).
				setBoolean("rejected", false).
				list();
		
		if (result != null && !result.isEmpty()) {
			return result;
		}

		return null;
	}

}
