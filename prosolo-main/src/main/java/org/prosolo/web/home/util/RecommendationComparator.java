/**
 * 
 */
package org.prosolo.web.home.util;

import java.util.Comparator;

import org.prosolo.domainmodel.activities.Recommendation;

/**
 * @author "Nikola Milikic"
 *
 */
public class RecommendationComparator implements Comparator<Recommendation> {

	@Override
	public int compare(Recommendation o1, Recommendation o2) {
		return new RecommendedNodeComparator().compare(o1.getRecommendedResource(), o2.getRecommendedResource());
	}

}
