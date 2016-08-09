package org.prosolo.services.nodes.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolActivity1;
import org.prosolo.common.domainmodel.credential.ExternalToolTargetActivity1;
import org.prosolo.common.domainmodel.credential.ResourceLink;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TextActivity1;
import org.prosolo.common.domainmodel.credential.TextTargetActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivity1;
import org.prosolo.common.domainmodel.credential.UrlActivityType;
import org.prosolo.common.domainmodel.credential.UrlTargetActivity1;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.web.competences.validator.YoutubeLinkValidator;
import org.springframework.stereotype.Component;

@Component
public class ActivityDataFactory {

	public ActivityData getActivityData(CompetenceActivity1 competenceActivity, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges) {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}
		ActivityData act = new ActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		act.setCompetenceActivityId(competenceActivity.getId());
		act.setActivityId(activity.getId());
		act.setOrder(competenceActivity.getOrder());
		act.setTitle(activity.getTitle());
		act.setDescription(activity.getDescription());
		act.setDurationHours((int) (activity.getDuration() / 60));
		act.setDurationMinutes((int) (activity.getDuration() % 60));
		act.calculateDurationString();
		act.setPublished(activity.isPublished());
		act.setDraft(activity.isDraft());
		act.setHasDraft(activity.isHasDraft());
		act.setActivityStatus();
		act.setUploadAssignment(activity.isUploadAssignment());
		act.setDateCreated(activity.getDateCreated());
		act.setType(activity.getType());
		act.setCreatorId(activity.getCreatedBy().getId());
		
		if(links != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();
			for(ResourceLink rl : links) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityLinks.add(rlData);
			}
			act.setLinks(activityLinks);
		}
		
		if(files != null) {
			List<ResourceLinkData> activityFiles = new ArrayList<>();
			for(ResourceLink rl : files) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setFetchedTitle(rl.getUrl().substring(rl.getUrl().lastIndexOf("/") + 1));
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityFiles.add(rlData);
			}
			act.setFiles(activityFiles);
		}
		
		act.setCompetenceId(competenceActivity.getCompetence().getId());
		
		populateTypeSpecificData(act, activity);

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}
	
	public ActivityData getActivityData(Activity1 act, long compId, int order, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges) {
		CompetenceActivity1 ca = new CompetenceActivity1();
		ca.setActivity(act);
		Competence1 comp = new Competence1();
		comp.setId(compId);
		ca.setCompetence(comp);
		ca.setOrder(order);
		return getActivityData(ca, links, files, shouldTrackChanges);
	}
	
	private void populateTypeSpecificData(ActivityData act, Activity1 activity) {
		if(activity instanceof TextActivity1) {
			TextActivity1 ta = (TextActivity1) activity;
			act.setActivityType(ActivityType.TEXT);
			act.setText(ta.getText());
		} else if(activity instanceof UrlActivity1) {
			UrlActivity1 urlAct = (UrlActivity1) activity;
			switch(urlAct.getUrlType()) {
				case Video:
					act.setActivityType(ActivityType.VIDEO);
					try {
						act.setEmbedId((String) new YoutubeLinkValidator(null)
								.performValidation(urlAct.getUrl(), null));
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case Slides:
					act.setActivityType(ActivityType.SLIDESHARE);
					act.setEmbedId(SlideShareUtils.convertSlideShareURLToEmbededUrl(urlAct.getUrl(), null)
							.getEmbedLink());
					break;
			}
			act.setLink(urlAct.getUrl());
			act.setLinkName(urlAct.getLinkName());
		} else if(activity instanceof ExternalToolActivity1) {
			ExternalToolActivity1 extAct = (ExternalToolActivity1) activity;
			act.setActivityType(ActivityType.EXTERNAL_TOOL);
			act.setLaunchUrl(extAct.getLaunchUrl());
			act.setSharedSecret(extAct.getSharedSecret());
			act.setConsumerKey(extAct.getConsumerKey());
			act.setAcceptGrades(extAct.isAcceptGrades());
		}
	}
	
	public ActivityData getBasicActivityData(CompetenceActivity1 competenceActivity, 
			boolean shouldTrackChanges) {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}

		ActivityData act = new ActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		act.setCompetenceActivityId(competenceActivity.getId());
		act.setActivityId(activity.getId());
		act.setOrder(competenceActivity.getOrder());
		act.setTitle(activity.getTitle());		
		act.setDurationHours((int) (activity.getDuration() / 60));
		act.setDurationMinutes((int) (activity.getDuration() % 60));
		act.calculateDurationString();
		act.setPublished(activity.isPublished());
		act.setDraft(activity.isDraft());
		act.setHasDraft(activity.isHasDraft());
		act.setType(activity.getType());
		
		act.setActivityType(getActivityType(activity));
		
		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}

		return act;
	}

	public ActivityType getActivityType(Activity1 activity) {
		if(activity instanceof TextActivity1) {
			return ActivityType.TEXT;
		} else if(activity instanceof UrlActivity1) {
			UrlActivity1 urlAct = (UrlActivity1) activity;
			switch(urlAct.getUrlType()) {
				case Video:
					return ActivityType.VIDEO;
				case Slides:
					return ActivityType.SLIDESHARE;
			}
		} else if(activity instanceof ExternalToolActivity1) {
			return ActivityType.EXTERNAL_TOOL;
		}
		
		return null;
	}
	
	/**
	 * returns activity type for String values for activity type and url type for UrlActivity1 from db
	 * @param activityDType
	 * @param urlType
	 * @return
	 */
	public ActivityType getActivityType(String activityDType, String urlType) {
		if(TextActivity1.class.getSimpleName().equals(activityDType)) {
			return ActivityType.TEXT;
		} else if(UrlActivity1.class.getSimpleName().equals(activityDType)) {
			if(UrlActivityType.Video.name().equals(urlType)) {
				return ActivityType.VIDEO;
			} else {
				return ActivityType.SLIDESHARE;
			}
		} else if(ExternalToolActivity1.class.getSimpleName().equals(activityDType)) {
			return ActivityType.EXTERNAL_TOOL;
		}
		
		return null;
	}


	public ActivityData getActivityData(TargetActivity1 activity, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges) {
		if(activity == null) {
			return null;
		}
		ActivityData act = new ActivityData(false);
		act.setActivityId(activity.getActivity().getId());
		act.setTargetActivityId(activity.getId());
		act.setOrder(activity.getOrder());
		act.setTitle(activity.getTitle());
		act.setDescription(activity.getDescription());
		act.setDurationHours((int) (activity.getDuration() / 60));
		act.setDurationMinutes((int) (activity.getDuration() % 60));
		act.calculateDurationString();
		act.setUploadAssignment(activity.isUploadAssignment());
		act.setCompleted(activity.isCompleted());
		act.setEnrolled(true);
		act.setAssignmentLink(activity.getAssignmentLink());
		act.setAssignmentTitle(activity.getAssignmentTitle());
		act.setCreatorId(activity.getCreatedBy().getId());
		
		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		if(links != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();
			for(ResourceLink rl : links) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityLinks.add(rlData);
			}
			act.setLinks(activityLinks);
		}
		
		if(files != null) {
			List<ResourceLinkData> activityFiles = new ArrayList<>();
			for(ResourceLink rl : files) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setFetchedTitle(rl.getUrl().substring(rl.getUrl().lastIndexOf("/") + 1));
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityFiles.add(rlData);
			}
			act.setFiles(activityFiles);
		}
		
		//or add targetCompetenceId to activitydata
		act.setCompetenceId(activity.getTargetCompetence().getId());
		act.setCompetenceName(activity.getTargetCompetence().getTitle());
		populateTypeSpecificData(act, activity);

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}
	
	private void populateTypeSpecificData(ActivityData act, TargetActivity1 activity) {
		if(activity instanceof TextTargetActivity1) {
			TextTargetActivity1 ta = (TextTargetActivity1) activity;
			act.setActivityType(ActivityType.TEXT);
			act.setText(ta.getText());
		} else if(activity instanceof UrlTargetActivity1) {
			UrlTargetActivity1 urlAct = (UrlTargetActivity1) activity;
			switch(urlAct.getType()) {
				case Video:
					act.setActivityType(ActivityType.VIDEO);
					try {
						act.setEmbedId((String) new YoutubeLinkValidator(null)
								.performValidation(urlAct.getUrl(), null));
					} catch(Exception e) {
						e.printStackTrace();
					}
					break;
				case Slides:
					act.setActivityType(ActivityType.SLIDESHARE);
					act.setEmbedId(SlideShareUtils.convertSlideShareURLToEmbededUrl(urlAct.getUrl(), 
							null).getEmbedLink());
					break;
			}
			act.setLink(urlAct.getUrl());
			act.setLinkName(urlAct.getLinkName());
		} else if(activity instanceof ExternalToolTargetActivity1) {
			ExternalToolTargetActivity1 extAct = (ExternalToolTargetActivity1) activity;
			act.setActivityType(ActivityType.EXTERNAL_TOOL);
			act.setLaunchUrl(extAct.getLaunchUrl());
			act.setSharedSecret(extAct.getSharedSecret());
			act.setConsumerKey(extAct.getConsumerKey());
		}
	}
	
	public ActivityData getBasicActivityData(TargetActivity1 activity, boolean shouldTrackChanges) {
		if(activity == null) {
			return null;
		}
		ActivityData act = new ActivityData(false);
		act.setActivityId(activity.getActivity().getId());
		act.setTargetActivityId(activity.getId());
		act.setTitle(activity.getTitle());
		act.setCompleted(activity.isCompleted());
		act.setEnrolled(true);
		act.setDurationHours((int) (activity.getDuration() / 60));
		act.setDurationMinutes((int) (activity.getDuration() % 60));
		act.calculateDurationString();
		
		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		act.setActivityType(determineActivityType(activity));

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}

	private ActivityType determineActivityType(TargetActivity1 activity) {
		if(activity instanceof TextTargetActivity1) {
			return ActivityType.TEXT;
		} else if(activity instanceof UrlTargetActivity1) {
			UrlTargetActivity1 urlAct = (UrlTargetActivity1) activity;
			switch(urlAct.getType()) {
				case Video:
					return ActivityType.VIDEO;
				case Slides:
					return ActivityType.SLIDESHARE;
			}
		} else if(activity instanceof ExternalToolTargetActivity1) {
			return ActivityType.EXTERNAL_TOOL;
		}
		
		return null;
	}

	private void populateCommonData(Activity1 act, ActivityData activity) {
		act.setId(activity.getActivityId());
		act.setTitle(activity.getTitle());
		act.setDescription(activity.getDescription());
		act.setDuration(activity.getDurationHours() * 60 + activity.getDurationMinutes());
		act.setPublished(activity.isPublished());
		act.setUploadAssignment(activity.isUploadAssignment());
		act.setDateCreated(activity.getDateCreated());
		act.setType(activity.getType());
	}
	
	public Activity1 getActivityFromActivityData(ActivityData activity) {
		if(activity == null) {
			return null;
		}
		return createActivityBasedOnType(activity);
	}

	private Activity1 createActivityBasedOnType(ActivityData activity) {
		switch(activity.getActivityType()) {
			case TEXT:
				TextActivity1 ta = new TextActivity1();
				populateCommonData(ta, activity);
				ta.setText(activity.getText());
				return ta;
			case VIDEO:
			case SLIDESHARE:
				UrlActivity1 urlAct = new UrlActivity1();
				if(activity.getActivityType() == ActivityType.VIDEO) {
					urlAct.setUrlType(UrlActivityType.Video);
				} else {
					urlAct.setUrlType(UrlActivityType.Slides);
				}
				populateCommonData(urlAct, activity);
				urlAct.setUrl(activity.getLink());
				urlAct.setLinkName(activity.getLinkName());
				return urlAct;
			case EXTERNAL_TOOL:
				ExternalToolActivity1 extAct = new ExternalToolActivity1();
				populateCommonData(extAct, activity);
				extAct.setLaunchUrl(activity.getLaunchUrl());
				extAct.setSharedSecret(activity.getSharedSecret());
				extAct.setConsumerKey(activity.getConsumerKey());
				extAct.setAcceptGrades(activity.isAcceptGrades());
				return extAct;
			default: 
				return null;
		}
	}
	
	public Activity1 getObjectForActivityType(ActivityType type) {
		switch(type) {
			case TEXT:
				return new TextActivity1();
			case VIDEO:
				UrlActivity1 actVideo = new UrlActivity1();
				actVideo.setUrlType(UrlActivityType.Video);
				return actVideo;
			case SLIDESHARE:
				UrlActivity1 actSlides = new UrlActivity1();
				actSlides.setUrlType(UrlActivityType.Slides);
				return actSlides;
			case EXTERNAL_TOOL:
				return new ExternalToolActivity1();
			default:
				return null;
		}
	}
	
}
