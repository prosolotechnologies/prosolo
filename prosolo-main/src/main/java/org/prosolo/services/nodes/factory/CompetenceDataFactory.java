package org.prosolo.services.nodes.factory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.BasicActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class CompetenceDataFactory {
	
	public CompetenceData1 getCompetenceData(User user, CredentialCompetence1 credComp, 
			Set<Tag> tags, boolean shouldTrackChanges) {
		if(credComp == null || credComp.getCompetence() == null) {
			return null;
		}
		CompetenceData1 comp = new CompetenceData1(false);
		Competence1 competence = credComp.getCompetence();
		comp.setCompetenceId(competence.getId());
		comp.setCredentialCompetenceId(credComp.getId());
		comp.setOrder(credComp.getOrder());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setPublished(competence.isPublished());
		comp.setStatus(getPublishedStatusBasedOnPublishFlag(comp.isPublished()));
		if(user != null) {
			ResourceCreator creator = new ResourceCreator(user.getId(), 
					getFullName(user.getName(), user.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60));
			comp.setCreator(creator);
		}
		if(tags != null) {
			comp.setTags(tags);
			comp.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}
		comp.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	public CompetenceData1 getCompetenceData(User user, TargetCompetence1 competence,
			boolean shouldTrackChanges) {
		CompetenceData1 comp = new CompetenceData1(false);
		comp.setCompetenceId(competence.getId());
		comp.setOrder(competence.getOrder());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setProgress(competence.getProgress());
		if(user != null) {
			ResourceCreator creator = new ResourceCreator(user.getId(), 
					getFullName(user.getName(), user.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60));
			comp.setCreator(creator);
		}
//		if(tags != null) {
//			comp.setTags(tags);
//			comp.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
//		}
		
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	public CompetenceData1 getFullCompetenceData(TargetCompetence1 targetComp, 
			boolean shouldTrackChanges) {
		CompetenceData1 cd = getCompetenceData(targetComp.getCreatedBy(), targetComp, shouldTrackChanges);
		List<TargetActivity1> activities = targetComp.getTargetActivities();
		if(activities != null) {
			for(TargetActivity1 ta : activities) {
				//TODO add mapping when activity model is implemented
				BasicActivityData bad = new BasicActivityData();
				cd.getActivities().add(bad);
			}
		}
		return cd;
	}
	
	public CompetenceData1 getCompetenceData(User user, Competence1 competence, 
			Set<Tag> tags, boolean shouldTrackChanges) {
		if(competence == null) {
			return null;
		}
		CompetenceData1 comp = new CompetenceData1(false);
		comp.setCompetenceId(competence.getId());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setPublished(competence.isPublished());
		comp.setStatus(getPublishedStatusBasedOnPublishFlag(comp.isPublished()));
		if(user != null) {
			ResourceCreator creator = new ResourceCreator(user.getId(), 
					getFullName(user.getName(), user.getLastname()),
					AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60));
			comp.setCreator(creator);
		}
		if(tags != null) {
			comp.setTags(tags);
			comp.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}
		comp.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}
	
	private PublishedStatus getPublishedStatusBasedOnPublishFlag(boolean published) {
		return published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
}
