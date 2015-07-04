package org.prosolo.recommendation.dal;

import java.util.List;

import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.Recommendation;
import org.prosolo.domainmodel.activities.RecommendationType;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.content.RichContent;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.domainmodel.workflow.LearningPlan;

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

	List<Recommendation> findSuggestedLearningResourcesForResource(User user,
			Node resource);

	List<Activity> findAllAppendedActivitiesForCompetence(Competence competence, List<Activity> ignoredActivities);

 

}
