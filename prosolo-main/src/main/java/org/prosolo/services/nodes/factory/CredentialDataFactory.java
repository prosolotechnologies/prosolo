package org.prosolo.services.nodes.factory;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class CredentialDataFactory {
	
	@Inject
	private CompetenceDataFactory compFactory;

	public CredentialData getCredentialData(User createdBy, Credential1 credential) {
		CredentialData cred = new CredentialData();
		cred.setId(credential.getId());
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		cred.setTags(credential.getTags());
		cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(cred.getTags()));
		cred.setHashtags(credential.getHashtags());
		cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(cred.getHashtags()));
		cred.setType(credential.getType());
		cred.setPublished(credential.isPublished());
		cred.setStatus(getPublishedStatusBasedOnPublishFlag(cred.isPublished()));
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		Map<String, Integer> durationMap = TimeUtil.getHoursAndMinutes(credential.getDuration());
		cred.setDurationString(getDurationString(durationMap.get("hours"), durationMap.get("minutes")));
		if(createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size60x60));
			cred.setCreator(creator);
		}
		return cred;
	}
	
	public CredentialData getCredentialData(User createdBy, long credId, TargetCredential1 credential) {
		CredentialData cred = new CredentialData();
		cred.setId(credId);
		cred.setTitle(credential.getTitle());
		cred.setDescription(credential.getDescription());
		cred.setTags(credential.getTags());
		cred.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(cred.getTags()));
		cred.setHashtags(credential.getHashtags());
		cred.setHashtagsString(AnnotationUtil.getAnnotationsAsSortedCSV(cred.getHashtags()));
		cred.setType(credential.getCredentialType());
		cred.setMandatoryFlow(credential.isCompetenceOrderMandatory());
		Map<String, Integer> durationMap = TimeUtil.getHoursAndMinutes(credential.getDuration());
		cred.setDurationString(getDurationString(durationMap.get("hours"), durationMap.get("minutes")));
		if(createdBy != null) {
			ResourceCreator creator = new ResourceCreator(createdBy.getId(), 
					getFullName(createdBy.getName(), createdBy.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(createdBy.getAvatarUrl(), ImageFormat.size60x60));
			cred.setCreator(creator);
		}
		cred.setEnrolled(true);
		cred.setTargetCredId(credential.getId());
		cred.setProgress(credential.getProgress());
		return cred;
	}
	
	public CredentialData getFullCredentialData(TargetCredential1 targetCred) {
		CredentialData cd = getCredentialData(targetCred.getCreatedBy(), 
				targetCred.getCredential().getId(), targetCred);
		List<TargetCompetence1> targetComps = targetCred.getTargetCompetences();
		if(targetComps != null) {
			for(TargetCompetence1 tc : targetComps) {
				CompetenceData1 compData = compFactory.getFullCompetenceData(tc);
				cd.getCompetences().add(compData);
			}
		}
		return cd;
	}
	
	private PublishedStatus getPublishedStatusBasedOnPublishFlag(boolean published) {
		return published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}
	
	private String getDurationString(int durationHours, int durationMinutes) {
		String duration = durationHours != 0 ? durationHours + " hours " : "";
		if(duration.isEmpty()) {
			duration = durationMinutes + " minutes";
		} else if(durationMinutes != 0) {
			duration += durationMinutes + " minutes";
		}
		
		return duration;
	}

}
