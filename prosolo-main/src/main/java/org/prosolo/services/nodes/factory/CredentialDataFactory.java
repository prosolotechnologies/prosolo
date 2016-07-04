package org.prosolo.services.nodes.factory;

import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class CredentialDataFactory {

	public CredentialData getCredentialData(User createdBy, Credential1 credential, Set<Tag> tags,
			Set<Tag> hashtags, boolean shouldTrackChanges) {
		if(credential == null) {
			return null;
		}
		CredentialData cred = new CredentialData(false);
		cred.setId(credential.getId());
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		if(tags != null) {
			cred.setTags(credential.getTags());
			cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getTags()));
		}
		if(hashtags != null) {
			cred.setHashtags(credential.getHashtags());
			cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getHashtags()));
		}
		cred.setType(credential.getType());
		cred.setPublished(credential.isPublished());
		cred.setCredentialStatus();
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		cred.setDraft(credential.isDraft());
		cred.setHasDraft(credential.isHasDraft());
		cred.setDuration(credential.getDuration());
		cred.calculateDurationString();
		if(createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size60x60));
			cred.setCreator(creator);
		}
		cred.setStudentsCanAddCompetences(credential.isStudentsCanAddCompetences());
		cred.setAutomaticallyAssingStudents(!credential.isManuallyAssignStudents());
		cred.setDefaultNumberOfStudentsPerInstructor(credential.getDefaultNumberOfStudentsPerInstructor());

		if(shouldTrackChanges) {
			cred.startObservingChanges();
		}
		return cred;
	}
	
	public CredentialData getCredentialData(User createdBy, TargetCredential1 credential,
			Set<Tag> tags, Set<Tag> hashtags, boolean shouldTrackChanges) {
		if(credential == null) {
			return null;
		}
		CredentialData cred = new CredentialData(false);
		cred.setId(credential.getCredential().getId());
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		if(tags != null) {
			cred.setTags(credential.getTags());
			cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getTags()));
		}
		if(hashtags != null) {
			cred.setHashtags(credential.getHashtags());
			cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getHashtags()));
		}
		cred.setType(credential.getCredentialType());
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		cred.setDuration(credential.getDuration());
		cred.calculateDurationString();
		if(createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size60x60));
			cred.setCreator(creator);
		}
		cred.setStudentsCanAddCompetences(credential.isStudentsCanAddCompetences());
		cred.setEnrolled(true);
		cred.setTargetCredId(credential.getId());
		cred.setProgress(credential.getProgress());
		cred.setNextCompetenceToLearnId(credential.getNextCompetenceToLearnId());
		cred.setNextActivityToLearnId(credential.getNextActivityToLearnId());
		if(credential.getInstructor() != null && credential.getInstructor().getUser() != null) {
			cred.setInstructorPresent(true);
			cred.setInstructorId(credential.getInstructor().getUser().getId());
			cred.setInstructorAvatarUrl(
					AvatarUtils.getAvatarUrlInFormat(credential.getInstructor().getUser().getAvatarUrl(),
					ImageFormat.size60x60));
			cred.setInstructorFullName(credential.getInstructor().getUser().getName()
					+ " " 
					+ credential.getInstructor().getUser().getLastname());
		}
		if(shouldTrackChanges) {
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
			long nextCompToLearnId, long nextActToLearnId) {
		CredentialData cred = getCredentialData(createdBy, credential, tags, hashtags, shouldTrackChanges);
		cred.setProgress(progress);
		cred.setNextCompetenceToLearnId(nextCompToLearnId);
		cred.setNextActivityToLearnId(nextActToLearnId);
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
