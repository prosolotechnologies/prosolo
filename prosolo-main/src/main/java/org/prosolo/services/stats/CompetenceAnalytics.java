/**
 * @author zoran
 */
package org.prosolo.services.stats;

import java.util.Collection;

import org.prosolo.common.domainmodel.user.User;

/**
 * @author zoran
 *
 */
public interface CompetenceAnalytics {

	Collection<User> getUsersUsingCompetence(long competenceId);

	int getNumberOfUsersAchievingCompetence(long competenceId);

	int getTimeToAchieveCompetence(long competenceId);

}
