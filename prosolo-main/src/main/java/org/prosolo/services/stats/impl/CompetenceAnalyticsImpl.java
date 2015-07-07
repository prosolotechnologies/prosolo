package org.prosolo.services.stats.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.stats.CompetenceAnalytics;
import org.springframework.stereotype.Service;


/**
 * @author Zoran
 *
 */
@Service("org.prosolo.services.stats.CompetenceAnalytics")
public class CompetenceAnalyticsImpl  extends AbstractManagerImpl implements CompetenceAnalytics{
	
	private static final long serialVersionUID = -5972383353715407684L;
	private static Logger logger = Logger.getLogger(CompetenceAnalytics.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<User> getUsersUsingCompetence(long competenceId){
		String queryActiveUsers =
				"SELECT DISTINCT user " +
				"FROM User user "+
				"LEFT JOIN user.learningGoals lGoal "+
				"LEFT JOIN lGoal.targetCompetences tCompetence "+
				"LEFT JOIN tCompetence.competence competence "+
				"WHERE competence.id = :competenceId";
		
		List<User> users1 = (List<User>) persistence.currentManager().createQuery(queryActiveUsers)
				.setLong("competenceId", competenceId)
				.list();
		
	//	logger.debug("hb query:" + queryActiveUsers);
		
//		String queryAcquiredUsers = 
//				"SELECT DISTINCT user "	+ 
//				"FROM User user " + 
//				"LEFT JOIN user.portfolio portfolio " + 
//				"LEFT JOIN portfolio.competences achievedCompetence " + 
//				"LEFT JOIN achievedCompetence.competence competence " + 
//				"WHERE competence.id = :competenceId";
		
		String queryAcquiredUsers = 
				"SELECT DISTINCT user "	+ 
				"FROM Portfolio portfolio " + 
				"LEFT JOIN portfolio.user user " + 
				"LEFT JOIN portfolio.competences achievedCompetence " + 
				"LEFT JOIN achievedCompetence.competence competence " + 
				"WHERE competence.id = :competenceId "
				+ "AND portfolio.id=user.id";
		
		logger.debug("hb query:" + queryAcquiredUsers);
		
		List<User> users2 = (List<User>) persistence.currentManager().createQuery(queryAcquiredUsers)
				.setLong("competenceId", competenceId)
				.list();

		Set<User> users = new HashSet<User>(users1);
		users.addAll(users2);

		return users;
	}
	
	@Override
	public int getNumberOfUsersAchievingCompetence(long competenceId){
		String query="" +
			"SELECT cast(COUNT(DISTINCT user) as int) " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals lGoal " +
			"LEFT JOIN lGoal.targetCompetences tCompetence " +
			"LEFT JOIN tCompetence.competence competence " +
			"WHERE competence.id = :competenceId";
		
	  	return (Integer) persistence.currentManager().createQuery(query)
	  			.setLong("competenceId", competenceId)
	  			.uniqueResult();
	}
	
	@Override
	public int getTimeToAchieveCompetence(long competenceId) {
		String query1 = 
				"SELECT tCompetence.completedDay, tCompetence.dateCreated  " +
				"FROM User user " +
				"LEFT JOIN user.learningGoals goal " +
				"LEFT JOIN goal.targetCompetences tCompetence " +
				"LEFT JOIN tCompetence.competence competence " +
				"WHERE tCompetence.completed = :completed " +
					"AND competence.id = :competenceId";
		
		@SuppressWarnings("unchecked")
		List<Object> dates1 = persistence.currentManager().createQuery(query1)
				.setBoolean("completed", true)
				.setLong("competenceId", competenceId)
				.list();
		
		String query = 
				"SELECT tCompetence.completedDay, tCompetence.dateCreated  " +
				"FROM Portfolio portfolio " +
			//	"LEFT JOIN portfolio.user user " +
				"LEFT JOIN portfolio.competences achievedCompetence " +
				"LEFT JOIN achievedCompetence.targetCompetence tCompetence " +
				"LEFT JOIN tCompetence.competence competence " +
				"WHERE competence.id = :competenceId";
		
	//	logger.debug("hb query:" + query);
		
		@SuppressWarnings("unchecked")
		List<Object> dates2 = persistence.currentManager().createQuery(query)
				.setLong("competenceId", competenceId)
				.list();
		
		dates1.addAll(dates2);
		Iterator<Object> it = dates1.iterator();
		long secondsSum = 0;
		
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();

			Date d1 = (Date) o[0];
			Date d2 = (Date) o[1];
			
			if (d1 != null && d2 != null) {
				long diff = d1.getTime() - d2.getTime();

				secondsSum = secondsSum + diff;
			}
		}
		
		if (dates1.size() == 0)
			return 0;
		
		return (int) (secondsSum / dates1.size());
	}

}
