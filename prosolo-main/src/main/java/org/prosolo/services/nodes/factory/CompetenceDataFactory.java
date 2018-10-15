package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
public class CompetenceDataFactory {
	
	public CompetenceData1 getCompetenceData(User user, CredentialCompetence1 credComp,
											 Set<CompetenceAssessmentConfig> assessmentConfig, Set<Tag> tags, boolean shouldTrackChanges) {
		if(credComp == null || credComp.getCompetence() == null) {
			return null;
		}
		CompetenceData1 comp = new CompetenceData1(false);
		Competence1 competence = credComp.getCompetence();
		comp.setVersion(competence.getVersion());
		comp.setCompetenceId(competence.getId());
		comp.setCredentialCompetenceId(credComp.getId());
		comp.setOrder(credComp.getOrder());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setPublished(competence.isPublished());
		comp.setArchived(competence.isArchived());
		comp.setType(competence.getType());
		comp.setStudentAllowedToAddActivities(competence.isStudentAllowedToAddActivities());
		comp.setDatePublished(competence.getDatePublished());
		comp.setLearningPathType(competence.getLearningPathType());

		boolean learningStagesEnabled = false;
		if (competence.getLearningStage() != null) {
			learningStagesEnabled = true;
			LearningStage ls = competence.getLearningStage();
			comp.setLearningStage(new LearningStageData(ls.getId(), ls.getTitle(), ls.getOrder(), false, false));
		}
		comp.setLearningStageEnabled(learningStagesEnabled);

		comp.setCompStatus();
		if(user != null) {
			ResourceCreator creator = new ResourceCreator(user.getId(), 
					getFullName(user.getName(), user.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120),
					user.getPosition());
			comp.setCreator(creator);
		}
		if(tags != null) {
			comp.setTags(tags);
			comp.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}

		if (assessmentConfig != null) {
			comp.setAssessmentTypes(getAssessmentConfig(assessmentConfig));
		}

		comp.getAssessmentSettings().setMaxPoints(competence.getMaxPoints());
		comp.getAssessmentSettings().setMaxPointsString(competence.getMaxPoints() > 0 ? String.valueOf(competence.getMaxPoints()) : "");
		comp.getAssessmentSettings().setGradingMode(competence.getGradingMode());
		//set rubric data
		if (competence.getRubric() != null) {
			comp.getAssessmentSettings().setRubricId(competence.getRubric().getId());
			comp.getAssessmentSettings().setRubricName(competence.getRubric().getTitle());
			comp.getAssessmentSettings().setRubricType(competence.getRubric().getRubricType());
		}
//		comp.setVisible(competence.isVisible());
//		comp.setVisibility(competence.isVisible(), competence.getScheduledPublicDate());

		comp.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if (shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}

	public List<AssessmentTypeConfig> getAssessmentConfig(Collection<CompetenceAssessmentConfig> assessmentConfig) {
		List<AssessmentTypeConfig> types = new ArrayList<>();
		for (CompetenceAssessmentConfig cac : assessmentConfig) {
			types.add(new AssessmentTypeConfig(cac.getId(), cac.getAssessmentType(), cac.isEnabled(), cac.getAssessmentType() == AssessmentType.INSTRUCTOR_ASSESSMENT));
		}
		return types;
	}
	
	public CompetenceData1 getCompetenceData(User user, Competence1 comp, Set<CompetenceAssessmentConfig> assessmentConfig,
			Set<Tag> tags, boolean shouldTrackChanges) {
		CredentialCompetence1 cc = new CredentialCompetence1();
		cc.setCompetence(comp);
		return getCompetenceData(user, cc, assessmentConfig, tags, shouldTrackChanges);
	}
	
	public CompetenceData1 getCompetenceData(User user, TargetCompetence1 tc, int order,
											 Set<CompetenceAssessmentConfig> assessmentConfig, Set<Tag> tags, Credential1 cred, boolean shouldTrackChanges) {
		Competence1 competence = tc.getCompetence();
		CompetenceData1 comp = new CompetenceData1(false);
		comp.setCompetenceId(competence.getId());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setType(competence.getType());
		comp.setTargetCompId(tc.getId());
		comp.setEnrolled(true);
		comp.setProgress(tc.getProgress());
		comp.setNextActivityToLearnId(tc.getNextActivityToLearnId());
		comp.setLearningPathType(competence.getLearningPathType());
		comp.setEvidenceSummary(tc.getEvidenceSummary());

		if (assessmentConfig != null) {
			comp.setAssessmentTypes(getAssessmentConfig(assessmentConfig));
		}

		boolean learningStagesEnabled = false;
		if (competence.getLearningStage() != null) {
			learningStagesEnabled = true;
			LearningStage ls = competence.getLearningStage();
			comp.setLearningStage(new LearningStageData(ls.getId(), ls.getTitle(), ls.getOrder(), false, false));
		}
		comp.setLearningStageEnabled(learningStagesEnabled);

		comp.getAssessmentSettings().setMaxPoints(competence.getMaxPoints());
		comp.getAssessmentSettings().setMaxPointsString(competence.getMaxPoints() > 0 ? String.valueOf(competence.getMaxPoints()) : "");
		comp.getAssessmentSettings().setGradingMode(competence.getGradingMode());
		//set rubric data
		if (competence.getRubric() != null) {
			comp.getAssessmentSettings().setRubricId(competence.getRubric().getId());
			comp.getAssessmentSettings().setRubricName(competence.getRubric().getTitle());
			comp.getAssessmentSettings().setRubricType(competence.getRubric().getRubricType());
		}

		comp.setOrder(order);
		if(user != null) {
			ResourceCreator creator = new ResourceCreator(user.getId(), 
					getFullName(user.getName(), user.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size120x120),
					user.getPosition());
			comp.setCreator(creator);
		}
		if(tags != null) {
			comp.setTags(tags);
			comp.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}
		
		if(cred != null) {
			comp.setCredentialId(cred.getId());
			comp.setCredentialTitle(cred.getTitle());
		}
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}
	
	/**
	 * If you want to create data object based on data from competence object and additionally set
	 * progress for that data this method should be called.
	 * @param createdBy
	 * @param competence
	 * @param tags
	 * @param progress
	 * @param nextActToLearnId
	 * @param shouldTrackChanges
	 * @return
	 */
	public CompetenceData1 getCompetenceDataWithProgress(User createdBy, Competence1 competence,
			Set<Tag> tags, int progress, long nextActToLearnId, boolean shouldTrackChanges) {
		CompetenceData1 comp = getCompetenceData(createdBy, competence, null, tags, shouldTrackChanges);
		comp.setProgress(progress);
		comp.setNextActivityToLearnId(nextActToLearnId);
		comp.setEnrolled(true);
		return comp;
	}
	
}
