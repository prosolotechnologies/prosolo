package org.prosolo.services.nodes.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
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
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.media.util.MediaDataException;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.ActivityResultData;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.util.url.URLUtil;
import org.springframework.stereotype.Component;

@Component
public class ActivityDataFactory {
	
	private static final Logger logger = Logger.getLogger(ActivityDataFactory.class);
	
	public ActivityData getActivityData(CompetenceActivity1 competenceActivity, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges) throws MediaDataException {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}
		ActivityData data = new ActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		data.setCompetenceActivityId(competenceActivity.getId());
		data.setActivityId(activity.getId());
		data.setOrder(competenceActivity.getOrder());
		data.setTitle(activity.getTitle());
		data.setDescription(activity.getDescription());
		data.setDurationHours((int) (activity.getDuration() / 60));
		data.setDurationMinutes((int) (activity.getDuration() % 60));
		data.calculateDurationString();
		data.setPublished(activity.isPublished());
		data.setMaxPointsString(activity.getMaxPoints() > 0 ? String.valueOf(activity.getMaxPoints()) : "");
		data.setStudentCanSeeOtherResponses(activity.isStudentCanSeeOtherResponses());
		data.setStudentCanEditResponse(activity.isStudentCanEditResponse());
		data.setActivityStatus();
		data.getResultData().setResultType(getResultType(activity.getResultType()));
		data.setDateCreated(activity.getDateCreated());
		data.setType(activity.getType());
		data.setCreatorId(activity.getCreatedBy().getId());
		data.setVisibleForUnenrolledStudents(activity.isVisibleForUnenrolledStudents());
		data.setDifficulty(activity.getDifficulty());
		data.setAutograde(activity.isAutograde());
		
		if(links != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();
			for(ResourceLink rl : links) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setIdParamName(rl.getIdParameterName());
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityLinks.add(rlData);
			}
			data.setLinks(activityLinks);
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
			data.setFiles(activityFiles);
		}
		
		data.setCompetenceId(competenceActivity.getCompetence().getId());
		
		populateTypeSpecificData(data, activity);

		data.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			data.startObservingChanges();
		}
		
		return data;
	}
	
	public ActivityResultType getResultType(org.prosolo.common.domainmodel.credential.ActivityResultType resultType) {
		if(resultType == null) {
			return ActivityResultType.NONE;
		}
		switch(resultType) {
			case FILE_UPLOAD:
				return ActivityResultType.FILE_UPLOAD;
			case TEXT:
				return ActivityResultType.TEXT;
			default:
				return ActivityResultType.NONE;
		}
	}
	
	public org.prosolo.common.domainmodel.credential.ActivityResultType getResultType(
			ActivityResultType resultType) {
		if(resultType == null) {
			return org.prosolo.common.domainmodel.credential.ActivityResultType.NONE;
		}
		switch(resultType) {
			case FILE_UPLOAD:
				return org.prosolo.common.domainmodel.credential.ActivityResultType.FILE_UPLOAD;
			case TEXT:
				return org.prosolo.common.domainmodel.credential.ActivityResultType.TEXT;
			default:
				return org.prosolo.common.domainmodel.credential.ActivityResultType.NONE;
		}
	}

	public ActivityData getActivityData(Activity1 act, long compId, int order, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges) throws MediaDataException {
		CompetenceActivity1 ca = new CompetenceActivity1();
		ca.setActivity(act);
		Competence1 comp = new Competence1();
		comp.setId(compId);
		ca.setCompetence(comp);
		ca.setOrder(order);
		return getActivityData(ca, links, files, shouldTrackChanges);
	}
	
	private void populateTypeSpecificData(ActivityData act, Activity1 activity) throws MediaDataException {
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
						act.setEmbedId(URLUtil.getYoutubeEmbedId(urlAct.getUrl()));
					} catch(Exception e) {
						e.printStackTrace();
					}
					if(urlAct.getCaptions() != null) {
						List<ResourceLinkData> captions = new ArrayList<>();
						for(ResourceLink rl : urlAct.getCaptions()) {
							ResourceLinkData rlData = new ResourceLinkData();
							rlData.setId(rl.getId());
							rlData.setLinkName(rl.getLinkName());
							rlData.setFetchedTitle(rl.getUrl().substring(rl.getUrl().lastIndexOf("/") + 1));
							rlData.setUrl(rl.getUrl());
							rlData.setStatus(ObjectStatus.UP_TO_DATE);
							captions.add(rlData);
						}
						act.setCaptions(captions);
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
			act.setOpenInNewWindow(extAct.isOpenInNewWindow());
			act.setScoreCalculation(extAct.getScoreCalculation());
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
		act.setType(activity.getType());
		act.setAutograde(activity.isAutograde());
		
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


	/**
	 * 
	 * @param targetActivity
	 * @param links
	 * @param files
	 * @param shouldTrackChanges
	 * @param isManager did request come from manage section
	 * @return
	 */
	public ActivityData getActivityData(TargetActivity1 targetActivity, Set<ResourceLink> links,
			Set<ResourceLink> files, boolean shouldTrackChanges, boolean isManager) {
		if (targetActivity == null) {
			return null;
		}
		ActivityData data = new ActivityData(false);
		data.setActivityId(targetActivity.getActivity().getId());
		data.setTargetActivityId(targetActivity.getId());
		data.setOrder(targetActivity.getOrder());
		data.setTitle(targetActivity.getTitle());
		data.setDescription(targetActivity.getDescription());
		data.setDurationHours((int) (targetActivity.getDuration() / 60));
		data.setDurationMinutes((int) (targetActivity.getDuration() % 60));
		data.calculateDurationString();
		data.setCompleted(targetActivity.isCompleted());
		data.setEnrolled(true);
		data.setType(targetActivity.getLearningResourceType());
		data.setResultData(getActivityResultData(targetActivity, isManager));
		data.setCreatorId(targetActivity.getCreatedBy().getId());
		data.setMaxPointsString(String.valueOf(targetActivity.getActivity().getMaxPoints()));
		data.setStudentCanEditResponse(targetActivity.getActivity().isStudentCanEditResponse());
		data.setStudentCanSeeOtherResponses(targetActivity.getActivity().isStudentCanSeeOtherResponses());
		
		data.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if (shouldTrackChanges) {
			data.startObservingChanges();
		}
		
		if (links != null) {
			List<ResourceLinkData> activityLinks = new ArrayList<>();
			for (ResourceLink rl : links) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setIdParamName(rl.getIdParameterName());
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityLinks.add(rlData);
			}
			data.setLinks(activityLinks);
		}
		
		if (files != null) {
			List<ResourceLinkData> activityFiles = new ArrayList<>();
			for (ResourceLink rl : files) {
				ResourceLinkData rlData = new ResourceLinkData();
				rlData.setId(rl.getId());
				rlData.setLinkName(rl.getLinkName());
				rlData.setUrl(rl.getUrl());
				rlData.setFetchedTitle(rl.getUrl().substring(rl.getUrl().lastIndexOf("/") + 1));
				rlData.setStatus(ObjectStatus.UP_TO_DATE);
				activityFiles.add(rlData);
			}
			data.setFiles(activityFiles);
		}
		
		//or add targetCompetenceId to activitydata
		data.setCompetenceId(targetActivity.getTargetCompetence().getId());
		data.setCompetenceName(targetActivity.getTargetCompetence().getTitle());
		populateTypeSpecificData(data, targetActivity);

		data.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if (shouldTrackChanges) {
			data.startObservingChanges();
		}
		
		return data;
	}
	
	private ActivityResultData getActivityResultData(TargetActivity1 activity, boolean isManager) {
		// TODO: Stefan - all code up to the last calling getActivityResultData() is not needed
		ActivityResultData ard = new ActivityResultData(false);
		ard.setTargetActivityId(activity.getId());
		ard.setResultType(getResultType(activity.getResultType()));
		ard.setResult(activity.getResult());
		if(ard.getResult() != null && !ard.getResult().isEmpty() 
				&& ard.getResultType() == ActivityResultType.FILE_UPLOAD) {
			ard.setAssignmentTitle(ard.getResult().substring(ard.getResult().lastIndexOf("/") + 1));
		}
		ard.setResultPostDate(activity.getResultPostDate());
		return getActivityResultData(activity.getId(), activity.getResultType(), activity.getResult(), 
				activity.getResultPostDate(), null, 0, false, isManager);
	}
	
	public ActivityResultData getActivityResultData(long targetActivityId, 
			org.prosolo.common.domainmodel.credential.ActivityResultType resType, String result, 
			Date postDate, User user, int commentsNumber, boolean isInstructor, boolean isManager) {
		ActivityResultData ard = new ActivityResultData(false);
		ard.setResultType(getResultType(resType));
		ard.setResult(result);
		if(ard.getResult() != null && !ard.getResult().isEmpty() 
				&& ard.getResultType() == ActivityResultType.FILE_UPLOAD) {
			ard.setAssignmentTitle(ard.getResult().substring(ard.getResult().lastIndexOf("/") + 1));
		}
		ard.setResultPostDate(postDate);
		if(user != null) {
			ard.setUser(new UserData(user));
		}
		ard.setTargetActivityId(targetActivityId);
		CommentsData commData = new CommentsData(CommentedResourceType.ActivityResult, 
				targetActivityId, isInstructor, isManager);
		commData.setNumberOfComments(commentsNumber);
		ard.setResultComments(commData);
		return ard;
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
						act.setEmbedId(URLUtil.getYoutubeEmbedId(urlAct.getUrl()));
					} catch(Exception e) {
						e.printStackTrace();
					}
					if(urlAct.getCaptions() != null) {
						List<ResourceLinkData> captions = new ArrayList<>();
						for(ResourceLink rl : urlAct.getCaptions()) {
							ResourceLinkData rlData = new ResourceLinkData();
							rlData.setId(rl.getId());
							rlData.setLinkName(rl.getLinkName());
							rlData.setUrl(rl.getUrl());
							rlData.setFetchedTitle(rl.getUrl().substring(rl.getUrl().lastIndexOf("/") + 1));
							rlData.setStatus(ObjectStatus.UP_TO_DATE);
							captions.add(rlData);
						}
						act.setCaptions(captions);
					}
					break;
				case Slides:
					act.setActivityType(ActivityType.SLIDESHARE);
					try {
						act.setEmbedId(SlideShareUtils.convertSlideShareURLToEmbededUrl(urlAct.getUrl(), null).getEmbedLink());
					} catch (MediaDataException e) {
						logger.error(e);
					}
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
			act.setOpenInNewWindow(extAct.isOpenInNewWindow());
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

	private void populateCommonData(Activity1 activity, ActivityData data) {
		activity.setId(data.getActivityId());
		activity.setTitle(data.getTitle());
		activity.setDescription(data.getDescription());
		activity.setDuration(data.getDurationHours() * 60 + data.getDurationMinutes());
		activity.setPublished(data.isPublished());
		activity.setResultType(getResultType(data.getResultData().getResultType()));
		activity.setDateCreated(data.getDateCreated());
		activity.setType(data.getType());
		activity.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
		activity.setStudentCanEditResponse(data.isStudentCanEditResponse());
		activity.setDifficulty(data.getDifficulty());
		activity.setAutograde(data.isAutograde());
	}
	
	public Activity1 getActivityFromActivityData(ActivityData activityData) {
		if (activityData == null) {
			return null;
		}
		return createActivityBasedOnType(activityData);
	}

	private Activity1 createActivityBasedOnType(ActivityData activityData) {
		switch(activityData.getActivityType()) {
			case TEXT:
				TextActivity1 ta = new TextActivity1();
				populateCommonData(ta, activityData);
				ta.setText(activityData.getText());
				return ta;
			case VIDEO:
			case SLIDESHARE:
				UrlActivity1 urlAct = new UrlActivity1();
				if(activityData.getActivityType() == ActivityType.VIDEO) {
					urlAct.setUrlType(UrlActivityType.Video);
				} else {
					urlAct.setUrlType(UrlActivityType.Slides);
				}
				populateCommonData(urlAct, activityData);
				urlAct.setUrl(activityData.getLink());
				urlAct.setLinkName(activityData.getLinkName());
				return urlAct;
			case EXTERNAL_TOOL:
				ExternalToolActivity1 extAct = new ExternalToolActivity1();
				populateCommonData(extAct, activityData);
				extAct.setLaunchUrl(activityData.getLaunchUrl());
				extAct.setSharedSecret(activityData.getSharedSecret());
				extAct.setConsumerKey(activityData.getConsumerKey());
				extAct.setAcceptGrades(activityData.isAcceptGrades());
				extAct.setOpenInNewWindow(activityData.isOpenInNewWindow());
				extAct.setScoreCalculation(activityData.getScoreCalculation());
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
