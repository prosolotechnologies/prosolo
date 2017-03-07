package org.prosolo.services.nodes.factory;

import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
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
		comp.setPublished(competence.isPublished());
		comp.setArchived(competence.isArchived());
		comp.setType(competence.getType());
		comp.setStudentAllowedToAddActivities(competence.isStudentAllowedToAddActivities());
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
//		comp.setVisible(competence.isVisible());
//		comp.setVisibility(competence.isVisible(), competence.getScheduledPublicDate());

		comp.setDatePublished(competence.getDatePublished());
		comp.setCanBeEdited(competence.isCanBeEdited());
		
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
	
	public CompetenceData1 getCompetenceData(User user, TargetCompetence1 tc, 
			Set<Tag> tags, Credential1 cred, boolean shouldTrackChanges) {
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
		CompetenceData1 comp = getCompetenceData(createdBy, competence, tags, shouldTrackChanges);
		comp.setProgress(progress);
		comp.setNextActivityToLearnId(nextActToLearnId);
		comp.setEnrolled(true);
		return comp;
	}
	
}
