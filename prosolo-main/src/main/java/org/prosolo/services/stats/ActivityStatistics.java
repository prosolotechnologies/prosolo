/**
 * 
 */
package org.prosolo.services.stats;

import org.prosolo.domainmodel.activities.Activity;

/**
 * @author Nikola Milikic
 *
 */
public interface ActivityStatistics {

	int getNumberOfLikes(Activity activity);

	int getNumberOfDislikes(Activity activity);

	int getNumberOfOngoingGoalsWithActivity(Activity activity);

	int getNumberOfCompletedGoalsWithActivity(Activity activity);

}
