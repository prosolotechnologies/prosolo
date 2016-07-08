package org.prosolo.recommendation;

import java.util.List;

import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.workflow.LearningPlan;

public interface LearningPlanRecommendation {

	List<LearningPlan> recommendLearningPlans(long userId, long targetCompId, int limit);

	boolean hasAppendedPlans(LearningPlan learningPlan, TargetCompetence selectedCompetence);

	List<LearningPlan> getAppendedPlans(LearningPlan selectedPlan, TargetCompetence selectedComp);

	List<LearningPlan> recommendLearningPlansForCompetence(User user, Competence selectedComp, int limit);

	boolean hasAppendedPlansForCompetence(LearningPlan plan, long competenceId);

	List<LearningPlan> getAppendedPlansForCompetence(LearningPlan selectedPlan, Competence selectedComp);

}
