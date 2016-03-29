package org.prosolo.services.nodes.factory;

import java.util.Map;

import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.util.TimeUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.stereotype.Component;

@Component
public class CompetenceDataFactory {

	public CompetenceData1 getCompetenceData(User user, Competence1 competence, long credCompId, int order) {
		CompetenceData1 comp = new CompetenceData1();
		comp.setCompetenceId(competence.getId());
		comp.setCredentialCompetenceId(credCompId);
		comp.setOrder(order);
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		Map<String, Integer> durationMap = TimeUtil.getHoursAndMinutes(competence.getDuration());
		comp.setDurationString(getDurationString(durationMap.get("hours"), durationMap.get("minutes")));
		comp.setPublished(competence.isPublished());
		comp.setStatus(getPublishedStatusBasedOnPublishFlag(comp.isPublished()));
		ResourceCreator creator = new ResourceCreator(user.getId(), 
				getFullName(user.getName(), user.getLastname()),
				AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60));
		comp.setCreator(creator);
		
		return comp;
	}
	
	public CompetenceData1 getCompetenceData(User user, TargetCompetence1 competence) {
		CompetenceData1 comp = new CompetenceData1();
		comp.setCompetenceId(competence.getId());
		comp.setOrder(competence.getOrder());
		comp.setTitle(competence.getTitle());
		comp.setDescription(competence.getDescription());
		Map<String, Integer> durationMap = TimeUtil.getHoursAndMinutes(competence.getDuration());
		comp.setDurationString(getDurationString(durationMap.get("hours"), durationMap.get("minutes")));
		comp.setProgress(competence.getProgress());
		ResourceCreator creator = new ResourceCreator(user.getId(), 
				getFullName(user.getName(), user.getLastname()),
				AvatarUtils.getAvatarUrlInFormat(user.getAvatarUrl(), ImageFormat.size60x60));
		comp.setCreator(creator);
		
		return comp;
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
	
	private PublishedStatus getPublishedStatusBasedOnPublishFlag(boolean published) {
		return published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
}
