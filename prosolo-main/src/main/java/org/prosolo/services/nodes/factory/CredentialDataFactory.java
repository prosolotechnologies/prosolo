package org.prosolo.services.nodes.factory;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialAssessmentConfig;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CredentialDataFactory {

	@Inject private CredentialDeliveryStatusFactory deliveryStatusFactory;
	
	public CredentialData getCredentialData(User createdBy, Credential1 credential,
											Set<CredentialAssessmentConfig> assessmentConfig, Set<Tag> tags,
											Set<Tag> hashtags, boolean shouldTrackChanges) {
		if (credential == null) {
			return null;
		}
		CredentialData cred = new CredentialData(false);
		cred.setVersion(credential.getVersion());
		cred.setId(credential.getId());
		cred.setOrganizationId(credential.getOrganization().getId());
		cred.setType(credential.getType());
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		cred.setArchived(credential.isArchived());
		if (assessmentConfig != null) {
			List<AssessmentTypeConfig> types = new ArrayList<>();
			for (CredentialAssessmentConfig cac : assessmentConfig) {
				types.add(new AssessmentTypeConfig(cac.getId(), cac.getAssessmentType(), cac.isEnabled(), cac.getAssessmentType() == AssessmentType.INSTRUCTOR_ASSESSMENT));
			}
			cred.setAssessmentTypes(types);
		}

		if (tags != null) {
			cred.setTags(credential.getTags());
			cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getTags()));
		}
		if (hashtags != null) {
			cred.setHashtags(credential.getHashtags());
			cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getHashtags()));
		}
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		cred.setDuration(credential.getDuration());
		cred.calculateDurationString();
		if (createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size120x120),
					createdBy.getPosition());
			cred.setCreator(creator);
		}
		cred.setAutomaticallyAssingStudents(!credential.isManuallyAssignStudents());
		cred.setDefaultNumberOfStudentsPerInstructor(credential.getDefaultNumberOfStudentsPerInstructor());

		boolean learningStagesEnabled = false;
		if (credential.getLearningStage() != null) {
			learningStagesEnabled = true;
			LearningStage ls = credential.getLearningStage();
			cred.setLearningStage(new LearningStageData(ls.getId(), ls.getTitle(), ls.getOrder(), false, false));
			cred.setFirstLearningStageCredentialId(
					credential.getFirstLearningStageCredential() == null
							? credential.getId()
							: credential.getFirstLearningStageCredential().getId());
		}
		cred.setLearningStageEnabled(learningStagesEnabled);

		if (credential.getType() == CredentialType.Delivery) {
			cred.setDeliveryOfId(credential.getDeliveryOf().getId());
			cred.setDeliveryOfTitle(credential.getDeliveryOf().getTitle());
			cred.setDeliveryStartTime(DateUtil.getMillisFromDate(credential.getDeliveryStart()));
			cred.setDeliveryEndTime(DateUtil.getMillisFromDate(credential.getDeliveryEnd()));
			cred.setDeliveryStatus(deliveryStatusFactory.getDeliveryStatus(
					credential.getDeliveryStart(), credential.getDeliveryEnd()));
		}

		cred.getAssessmentSettings().setMaxPoints(credential.getMaxPoints());
		cred.getAssessmentSettings().setMaxPointsString(credential.getMaxPoints() > 0 ? String.valueOf(credential.getMaxPoints()) : "");
		cred.getAssessmentSettings().setGradingMode(credential.getGradingMode());
		//set rubric data
		if (credential.getRubric() != null) {
			cred.getAssessmentSettings().setRubricId(credential.getRubric().getId());
			cred.getAssessmentSettings().setRubricName(credential.getRubric().getTitle());
			cred.getAssessmentSettings().setRubricType(credential.getRubric().getRubricType());
		}
		
		if (shouldTrackChanges) {
			cred.startObservingChanges();
		}
		return cred;
	}
	
	public CredentialData getCredentialData(User createdBy, TargetCredential1 credential,
			Set<Tag> tags, Set<Tag> hashtags, boolean shouldTrackChanges) {
		if (credential == null || credential.getCredential() == null) {
			return null;
		}
		Credential1 c = credential.getCredential();
		//get credential specific data
		CredentialData cred = getCredentialData(createdBy, c, null, tags, hashtags, false);
		
		//set target credential specific data
		cred.setEnrolled(true);
		cred.setTargetCredId(credential.getId());
		cred.setProgress(credential.getProgress());
		cred.setNextCompetenceToLearnId(credential.getNextCompetenceToLearnId());
		
		if (credential.getInstructor() != null && credential.getInstructor().getUser() != null) {
			cred.setInstructorPresent(true);
			cred.setInstructorId(credential.getInstructor().getUser().getId());
			cred.setInstructorAvatarUrl(
					AvatarUtils.getAvatarUrlInFormat(credential.getInstructor().getUser().getAvatarUrl(),
					ImageFormat.size120x120));
			cred.setInstructorFullName(credential.getInstructor().getUser().getName()
					+ " " 
					+ credential.getInstructor().getUser().getLastname());
		}
		if (shouldTrackChanges) {
			cred.startObservingChanges();
		}
		return cred;
	}
	
	/**
	 * If you want to create data object based on data from Credential object and additionally set
	 * progress for that data this method should be called. Use this method only when user is enrolled
	 * but you want data from original credential to be shown.
	 * @param createdBy
	 * @param credential
	 * @param tags
	 * @param hashtags
	 * @param shouldTrackChanges
	 * @param progress
	 * @return
	 */
	public CredentialData getCredentialDataWithProgress(User createdBy, Credential1 credential,
			Set<Tag> tags, Set<Tag> hashtags, boolean shouldTrackChanges, int progress,
			long nextCompToLearnId) {
		CredentialData cred = getCredentialData(createdBy, credential, null, tags, hashtags, shouldTrackChanges);
		cred.setProgress(progress);
		cred.setNextCompetenceToLearnId(nextCompToLearnId);
		cred.setEnrolled(true);
		return cred;
	}
	
//	public CredentialData getFullCredentialData(TargetCredential1 targetCred, boolean shouldTrackChanges) {
//		CredentialData cd = getCredentialData(targetCred.getCreatedBy(), 
//				targetCred.getCredential().getId(), targetCred, shouldTrackChanges);
//		List<TargetCompetence1> targetComps = targetCred.getTargetCompetences();
//		if(targetComps != null) {
//			for(TargetCompetence1 tc : targetComps) {
//				CompetenceData1 compData = compFactory.getCompetenceData(null, tc, null, tc, shouldTrackChanges)(tc, shouldTrackChanges);
//				cd.getCompetences().add(compData);
//			}
//		}
//
//		return cd;
//	}
	
//	public CredentialData getFullCredentialData(Credential1 cred, User creator, 
//			List<CredentialCompetence1> comps, boolean shouldTrackChanges) {
//		CredentialData cd = getCredentialData(creator, cred, shouldTrackChanges);
//		if(comps != null) {
//			for(CredentialCompetence1 cc : comps) {
//				CompetenceData1 compData = compFactory.getCompetenceData(null, cc, null, 
//						shouldTrackChanges);
//				cd.getCompetences().add(compData);
//			}
//		}
//
//		return cd;
//	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}

}
