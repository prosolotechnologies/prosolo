package org.prosolo.services.es;

import java.util.Collection;
import java.util.List;

import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.services.general.AbstractManager;

public interface MoreNodesLikeThis extends AbstractManager {

	List<Competence> getCompetencesForUserAndLearningGoal(String inputText,
			Collection<Competence> ignoredCompetences, int limit);

	List<Node> getSuggestedResourcesForUser(String sr,
			Collection<Node> ignoreResources, int limit);



	List<Activity> getSuggestedActivitiesForCompetence(String likeText,
			Collection<Long> ignoredActivities, long competenceId, int limit);

}
