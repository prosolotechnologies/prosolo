package org.prosolo.services.nodes.factory;

import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
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
		comp.setDraft(competence.isDraft());
		comp.setPublished(competence.isPublished());
		comp.setCompStatus();
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

//		if(activities != null) {
//			for(Activity1 act : activities) {
//				BasicActivityData bad = activityFactory.getActivityData(act, true);
//				comp.getActivities().add(bad);
//			}
//		}
		comp.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	public CompetenceData1 getCompetenceData(User user, Competence1 comp, 
			Set<Tag> tags, boolean shouldTrackChanges) {
		CredentialCompetence1 cc = new CredentialCompetence1();
		cc.setCompetence(comp);
		return getCompetenceData(user, cc, tags, shouldTrackChanges);
	}
	
	public CompetenceData1 getCompetenceData(User user, TargetCompetence1 competence, 
			Set<Tag> tags, boolean shouldTrackChanges) {
		CompetenceData1 comp = new CompetenceData1(false);
		comp.setCompetenceId(competence.getCompetence().getId());
		comp.setOrder(competence.getOrder());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		comp.setDuration(competence.getDuration());
		comp.setTargetCompId(competence.getId());
		comp.setEnrolled(true);
		comp.setProgress(competence.getProgress());
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
		
//		if(activities != null) {
//			for(TargetActivity1 act : activities) {
//				BasicActivityData bad = activityFactory.getActivityData(act, true);
//				comp.getActivities().add(bad);
//			}
//		}
		
		if(shouldTrackChanges) {
			comp.startObservingChanges();
		}
		
		return comp;
	}
	
	private String getFullName(String name, String lastName) {
		return name + (lastName != null ? " " + lastName : "");
	}
	
}
