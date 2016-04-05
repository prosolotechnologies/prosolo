package org.prosolo.services.nodes.factory;

import java.util.List;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class CredentialDataFactory {
	
	@Inject
	private CompetenceDataFactory compFactory;

	public CredentialData getCredentialData(User createdBy, Credential1 credential, 
			boolean shouldTrackChanges) {
		if(credential == null) {
			return null;
		}
		CredentialData cred = new CredentialData(false);
		cred.setId(credential.getId());
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		cred.setTags(credential.getTags());
		cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getTags()));
		cred.setHashtags(credential.getHashtags());
		cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getHashtags()));
		cred.setType(credential.getType());
		cred.setPublished(credential.isPublished());
		cred.setCredentialStatus();
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		cred.setDraft(credential.isDraft());
		cred.setDuration(credential.getDuration());
		cred.calculateDurationString();
		if(createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size60x60));
			cred.setCreator(creator);
		}
		cred.setStudentsCanAddCompetences(credential.isStudentsCanAddCompetences());
		cred.setManuallyAssingStudents(credential.isManuallyAssignStudents());
		cred.setDefaultNumberOfStudentsPerInstructor(credential.getDefaultNumberOfStudentsPerInstructor());
		if(shouldTrackChanges) {
			cred.startObservingChanges();
		}
		return cred;
	}
	
	public CredentialData getCredentialData(User createdBy, long credId, TargetCredential1 credential,
			boolean shouldTrackChanges) {
		if(credential == null) {
			return null;
		}
		CredentialData cred = new CredentialData(false);
		cred.setId(credId);
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		cred.setTags(credential.getTags());
		cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getTags()));
		cred.setHashtags(credential.getHashtags());
		cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(credential.getHashtags()));
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
		
		if(shouldTrackChanges) {
			cred.startObservingChanges();
		}
		return cred;
	}
	
	public CredentialData getFullCredentialData(TargetCredential1 targetCred, boolean shouldTrackChanges) {
		CredentialData cd = getCredentialData(targetCred.getCreatedBy(), 
				targetCred.getCredential().getId(), targetCred, shouldTrackChanges);
		List<TargetCompetence1> targetComps = targetCred.getTargetCompetences();
		if(targetComps != null) {
			for(TargetCompetence1 tc : targetComps) {
				CompetenceData1 compData = compFactory.getFullCompetenceData(tc, shouldTrackChanges);
				cd.getCompetences().add(compData);
			}
		}

		return cd;
	}
	
	public CredentialData getFullCredentialData(Credential1 cred, User creator, 
			List<CredentialCompetence1> comps, boolean shouldTrackChanges) {
		CredentialData cd = getCredentialData(creator, cred, shouldTrackChanges);
		if(comps != null) {
			for(CredentialCompetence1 cc : comps) {
				CompetenceData1 compData = compFactory.getCompetenceData(null, cc, null, 
						shouldTrackChanges);
				cd.getCompetences().add(compData);
			}
		}

		return cd;
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}

}
