/**
 * 
 */
package org.prosolo.web.portfolio.util;

import java.util.Comparator;

import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;

/**
 * @author "Nikola Milikic"
 *
 */
public class AchievedCompCompletedDateComparator implements Comparator<AchievedCompetence> {
	
	@Override
	public int compare(AchievedCompetence c1, AchievedCompetence c2) {
		if (c1.getCompletedDate() != null && c2.getCompletedDate() != null) {
			if (c1.getCompletedDate().after(c2.getCompletedDate())) {
				return -1;
			} else {
				return 1;
			}
		}
		return c1.getTitle().compareToIgnoreCase(c2.getTitle());
	}
	
}