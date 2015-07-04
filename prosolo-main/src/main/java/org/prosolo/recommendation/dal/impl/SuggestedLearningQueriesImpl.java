package org.prosolo.recommendation.dal.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.Recommendation;
import org.prosolo.domainmodel.activities.RecommendationType;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.content.RichContent;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.LearningPlan;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.recommendation.dal.SuggestedLearningQueries")
@Transactional
public class SuggestedLearningQueriesImpl extends AbstractManagerImpl implements SuggestedLearningQueries{

	private static final long serialVersionUID = -6331499276053447274L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SuggestedLearningQueries.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Recommendation> findSuggestedLearningResourcesByCollegues(User user, RecommendationType recType, int page, int limit) {
		String query=
			"SELECT DISTINCT recommendation " +
			"FROM Recommendation recommendation "+
			"JOIN FETCH recommendation.recommendedTo recommendedTo "+
				" WHERE recommendedTo.id = :user " +
				" AND recommendation.recommendationType = :rType "+
				" AND recommendation.dismissed != :dismissed";
			List<Recommendation> recommendations= (List<Recommendation>) persistence.currentManager().createQuery(query)
			.setLong("user", user.getId())
			.setString("rType", recType.toString())
			.setBoolean("dismissed", true)
			.setFirstResult(page * limit)
			.setMaxResults(limit)
			.list();
			return recommendations;
	}
	@Override
	public List<Recommendation> findSuggestedLearningResourcesForResource(User user, Node resource){
		String query=
				"SELECT DISTINCT recommendation " +
				"FROM Recommendation recommendation "+
				//"JOIN FETCH recommendation.recommendedTo recommendedTo "+
					" WHERE recommendation.recommendedTo = :user " +
					" AND recommendation.recommendedResource = :resource "+
					" AND recommendation.dismissed != :dismissed";
				@SuppressWarnings("unchecked")
				List<Recommendation> recommendations= (List<Recommendation>) persistence.currentManager().createQuery(query)
				.setEntity("user", user)
				.setEntity("resource", resource)
				.setBoolean("dismissed", true)
				.list();
				return recommendations;
	}
	@Override
	public int findNumberOfSuggestedLearningResourcesByCollegues(User user, RecommendationType recType) {
		String query=
			"SELECT DISTINCT cast(COUNT(recommendation) as int) " +
			"FROM Recommendation recommendation "+
			"JOIN recommendation.recommendedTo recommendedTo "+
			"WHERE recommendedTo = :user " +
				"AND recommendation.recommendationType = :rType "+
				"AND recommendation.dismissed != :dismissed";
		int number= (Integer) persistence.currentManager().createQuery(query)
				.setString("rType", recType.toString())
				.setBoolean("dismissed", true)
				.setEntity("user", user)
				.uniqueResult();
		return number;
	}
	
	@Override
	public List<RichContent> findRichContentForLearningGoal(LearningGoal goal){
		String query = 
			"SELECT DISTINCT richContent " +
			"FROM GoalNote goalNote " +
			"LEFT JOIN goalNote.goal lGoal " +
			"LEFT JOIN goalNote.richContent richContent " +
	    	"WHERE lGoal = :goal";
		
		@SuppressWarnings("unchecked")
		List<RichContent> richContents = (List<RichContent>) persistence.currentManager().createQuery(query)
			.setEntity("goal", goal)
			.list();
		
		return richContents;
	}
	
	@Override
	public List<LearningPlan> findAllLearningPlansForCompetence(Competence competence){
		String query =
			"SELECT DISTINCT tCompetence " +
			"FROM TargetCompetence tCompetence " +
			"LEFT JOIN tCompetence.competence competence " +	
			"WHERE competence = :competence";
				 
		@SuppressWarnings("unchecked")
		List<TargetCompetence> tCompetences = persistence.currentManager().createQuery(query)
			.setEntity("competence", competence)
			.list();
		List<LearningPlan> lPlans=new ArrayList<LearningPlan>();		
		for (TargetCompetence tComp : tCompetences) {
			List<TargetActivity> tActivities = tComp.getTargetActivities();
			List<Activity> activities = new ArrayList<Activity>();
			
			for (TargetActivity ta : tActivities) {
				activities.add(ta.getActivity());
			}			
			LearningPlan lPlan = new LearningPlan();
			lPlan.setActivities(activities);
			lPlans.add(lPlan);
		}		
		return lPlans; 
	}
	
	@Override
	public List<Activity> findAllAppendedActivitiesForCompetence(Competence competence, List<Activity> ignoredActivities){
		String query =
			"SELECT DISTINCT activity " +
			"FROM TargetCompetence tCompetence " +
			"LEFT JOIN tCompetence.competence competence " +	
			"LEFT JOIN tCompetence.targetActivities tActivity "+
			"LEFT JOIN tActivity.activity activity "+
			"WHERE competence = :competence AND activity NOT IN (:ignoredActivities)";
		 
		@SuppressWarnings("unchecked")
		List<Activity> appendedActivities = persistence.currentManager().createQuery(query)
			.setEntity("competence", competence)
			.setParameterList("ignoredActivities",ignoredActivities)
			.list();
		 	
		
	 	
		return appendedActivities;
	}

	
	@Override
	public List<TargetActivity> loadAllActivitiesForLearningPlanByMaker(User user, LearningPlan plan){
		String query =
				"SELECT DISTINCT activity " +
				"FROM LearningPlan lPlan " +
				"LEFT JOIN lPlan.targetActivities activity " +	
				"WHERE lPlan = :plan " +
					"AND activity.maker = :user";
		
		@SuppressWarnings("unchecked")
		List<TargetActivity> activities = persistence.currentManager().createQuery(query)
			.setEntity("plan", plan)
			.setEntity("user", user)
			.list();
		
		return activities;
	}
	
	@Override
	public List<Activity> loadActivitiesForLearningPlan(LearningPlan plan){
		String query =
			"SELECT DISTINCT activity " +
			"FROM LearningPlan lPlan " +
			"LEFT JOIN lPlan.activities activity " +	
			"WHERE lPlan = :plan";
		
		@SuppressWarnings("unchecked")
		List<Activity> activities = (List<Activity>) persistence.currentManager().createQuery(query)
			.setEntity("plan", plan)
			.list();

		return activities;
	}
	
	@Override
	public boolean checkIfLearningPlanHasActivities(User user, LearningPlan plan){
		String query = 
			"SELECT cast(COUNT(DISTINCT activity) as int) " +
			"FROM LearningPlan lPlan "+
			"LEFT JOIN lPlan.activities activity "+	
			"WHERE lPlan = :plan " +
				"AND activity.maker = :maker";
		
		Integer actNumber = (Integer) persistence.currentManager().createQuery(query)
				.setEntity("plan", plan)
				.setEntity("maker", user)
				.uniqueResult();
		
		return actNumber > 0;
	}
	@Override
	public List<Node> findDismissedRecommendedResources(User user) {
		String query=
			"SELECT DISTINCT resource " +
			"FROM Recommendation recommendation "+
			"JOIN recommendation.recommendedTo recommendedTo "+
			"JOIN recommendation.recommendedResource resource "+
				" WHERE recommendedTo = :user " +
				" AND recommendation.dismissed = :dismissed";
		
		@SuppressWarnings("unchecked")
		List<Node> resources = (List<Node>) persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setBoolean("dismissed", true)
			.list();
		
		return resources;
	}

}
