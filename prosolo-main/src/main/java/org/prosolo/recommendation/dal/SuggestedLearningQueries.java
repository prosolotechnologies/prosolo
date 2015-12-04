package org.prosolo.recommendation.dal;

import java.util.List;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.LearningPlan;

public interface SuggestedLearningQueries {
	
	List<Recommendation> findSuggestedLearningResourcesByCollegues(User user, RecommendationType recType,
			int page, int limit);

	List<RichContent> findRichContentForLearningGoal(LearningGoal goal);

	List<LearningPlan> findAllLearningPlansForCompetence(Competence competence);

	List<TargetActivity> loadAllActivitiesForLearningPlanByMaker(User user, LearningPlan plan);

	boolean checkIfLearningPlanHasActivities(User user, LearningPlan plan);

	List<Activity> loadActivitiesForLearningPlan(LearningPlan plan);

	int findNumberOfSuggestedLearningResourcesByCollegues(User user,
			RecommendationType recType);

	List<Node> findDismissedRecommendedResources(User user);

	/**
	 * Retrieves a collection of recommendations for a given resource.
	 * 
	 * @param userId
	 * @param resourceId - Id of a resource (subclass of {@see Node} class)
	 * @return
	 *
	 * @version 0.5
	 */
	List<Recommendation> findSuggestedLearningResourcesForResource(long userId,
			long resourceId);

	List<Activity> findAllAppendedActivitiesForCompetence(Competence competence, List<Activity> ignoredActivities);

}
