package org.prosolo.services.nodes.factory;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.credential.visitor.ActivityVisitor;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.ActivityResultType;
import org.prosolo.services.util.url.URLUtil;
import org.prosolo.util.nodes.AnnotationUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class ActivityDataFactory {
	
	private static final Logger logger = Logger.getLogger(ActivityDataFactory.class);
	
	public ActivityData getActivityData(CompetenceActivity1 competenceActivity, Set<ResourceLink> links,
										Set<ResourceLink> files, Set<Tag> tags, boolean shouldTrackChanges) throws LinkParserException {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}
		ActivityData data = new ActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		data.setVersion(activity.getVersion());
		data.setCompetenceActivityId(competenceActivity.getId());
		data.setActivityId(activity.getId());
		data.setOrder(competenceActivity.getOrder());
		data.setTitle(activity.getTitle());
		data.setDescription(activity.getDescription());
		data.setDurationHours((int) (activity.getDuration() / 60));
		data.setDurationMinutes((int) (activity.getDuration() % 60));
		data.calculateDurationString();
		data.setStudentCanSeeOtherResponses(activity.isStudentCanSeeOtherResponses());
		data.setStudentCanEditResponse(activity.isStudentCanEditResponse());
		data.getResultData().setResultType(getResultType(activity.getResultType()));
		data.setDateCreated(activity.getDateCreated());
		data.setType(activity.getType());
		data.setCreatorId(activity.getCreatedBy().getId());
		data.setVisibleForUnenrolledStudents(activity.isVisibleForUnenrolledStudents());
		data.setDifficulty(activity.getDifficulty());
		if (tags != null) {
			data.setTags(tags);
			data.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}
		
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

		data.getAssessmentSettings().setMaxPoints(activity.getMaxPoints());
		data.getAssessmentSettings().setMaxPointsString(activity.getMaxPoints() > 0 ? String.valueOf(activity.getMaxPoints()) : "");
		data.getAssessmentSettings().setGradingMode(activity.getGradingMode());
		//set rubric data
		if (activity.getRubric() != null) {
			data.getAssessmentSettings().setRubricId(activity.getRubric().getId());
			data.setRubricVisibility(activity.getRubricVisibility());
			data.getAssessmentSettings().setRubricName(activity.getRubric().getTitle());
			data.getAssessmentSettings().setRubricType(activity.getRubric().getRubricType());
		}
		
		data.setCompetenceId(competenceActivity.getCompetence().getId());
		
		data.setOncePublished(competenceActivity.getCompetence().getDatePublished() != null);
		
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
			Set<ResourceLink> files, boolean shouldTrackChanges) throws LinkParserException {
		CompetenceActivity1 ca = new CompetenceActivity1();
		ca.setActivity(act);
		Competence1 comp = new Competence1();
		comp.setId(compId);
		ca.setCompetence(comp);
		ca.setOrder(order);
		return getActivityData(ca, links, files, null, shouldTrackChanges);
	}

	private void populateTypeSpecificData(ActivityData act, Activity1 activity) {
		activity.accept(new ActivityVisitor() {
			
			@Override
			public void visit(ExternalToolActivity1 activity) {
				act.setActivityType(ActivityType.EXTERNAL_TOOL);
				act.setLaunchUrl(activity.getLaunchUrl());
				act.setSharedSecret(activity.getSharedSecret());
				act.setConsumerKey(activity.getConsumerKey());
				act.setAcceptGrades(activity.isAcceptGrades());
				act.setOpenInNewWindow(activity.isOpenInNewWindow());
				act.setScoreCalculation(activity.getScoreCalculation());
			}
			
			@Override
			public void visit(UrlActivity1 activity) {
				switch(activity.getUrlType()) {
					case Video:
						act.setActivityType(ActivityType.VIDEO);
						try {
							act.setEmbedId(URLUtil.getYoutubeEmbedId(activity.getUrl()));
						} catch(Exception e) {
							e.printStackTrace();
						}
						if(activity.getCaptions() != null) {
							List<ResourceLinkData> captions = new ArrayList<>();
							for(ResourceLink rl : activity.getCaptions()) {
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
						act.setVideoLink(activity.getUrl());
						break;
					case Slides:
						act.setActivityType(ActivityType.SLIDESHARE);
						try {
							act.setEmbedId(SlideShareUtils.convertSlideShareURLToEmbededUrl(activity.getUrl(), null)
									.getEmbedLink());
						} catch (LinkParserException e) {
							logger.error(e);
						}
						act.setSlidesLink(activity.getUrl());
						break;
				}
				act.setLinkName(activity.getLinkName());
			}
			
			@Override
			public void visit(TextActivity1 activity) {
				act.setActivityType(ActivityType.TEXT);
				act.setText(activity.getText());
			}
		});
	}
	
	public ActivityData getBasicActivityData(CompetenceActivity1 competenceActivity, 
			boolean shouldTrackChanges) {
		if(competenceActivity == null || competenceActivity.getActivity() == null) {
			return null;
		}

		ActivityData act = new ActivityData(false);
		Activity1 activity = competenceActivity.getActivity();
		act.setVersion(activity.getVersion());
		act.setCompetenceActivityId(competenceActivity.getId());
		act.setActivityId(activity.getId());
		act.setOrder(competenceActivity.getOrder());
		act.setTitle(activity.getTitle());		
		act.setDurationHours((int) (activity.getDuration() / 60));
		act.setDurationMinutes((int) (activity.getDuration() % 60));
		act.calculateDurationString();
		act.setType(activity.getType());
		act.getResultData().setResultType(getResultType(activity.getResultType()));

		act.getAssessmentSettings().setMaxPoints(activity.getMaxPoints());
		act.getAssessmentSettings().setGradingMode(activity.getGradingMode());
		//set rubric data
		if (activity.getRubric() != null) {
			act.getAssessmentSettings().setRubricId(activity.getRubric().getId());
			act.setRubricVisibility(activity.getRubricVisibility());
			act.getAssessmentSettings().setRubricName(activity.getRubric().getTitle());
			act.getAssessmentSettings().setRubricType(activity.getRubric().getRubricType());
		}
		
		act.setActivityType(getActivityType(activity));

		setBasicTypeSpecificData(activity, act);

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}

		return act;
	}

	private void setBasicTypeSpecificData(Activity1 activity, ActivityData act) {
		activity.accept(new ActivityVisitor() {

			@Override
			public void visit(ExternalToolActivity1 activity) {
				act.setAcceptGrades(activity.isAcceptGrades());
			}

			@Override
			public void visit(UrlActivity1 activity) { }

			@Override
			public void visit(TextActivity1 activity) { }
		});
	}

	/**
	 * 
	 * @param activity
	 * @return
	 * @throws NullPointerException if {@code activity} is null
	 */
	public ActivityType getActivityType(Activity1 activity) {
		if(activity == null) {
			throw new NullPointerException();
		}
		
		class ActivityTypeVisitor implements ActivityVisitor {
			
			private ActivityType type;
			
			@Override
			public void visit(TextActivity1 activity) {
				type = ActivityType.TEXT;
			}

			@Override
			public void visit(UrlActivity1 activity) {
				switch(activity.getUrlType()) {
					case Video:
						type = ActivityType.VIDEO;
						break;
					case Slides:
						type = ActivityType.SLIDESHARE;
						break;
				}
			}

			@Override
			public void visit(ExternalToolActivity1 activity) {
				type = ActivityType.EXTERNAL_TOOL;
			}
		}
		
		ActivityTypeVisitor typeVisitor = new ActivityTypeVisitor();
		activity.accept(typeVisitor);
		return typeVisitor.type;
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
	 * @throws NullPointerException if {@code targetActivity} or {@code targetActivity.getActivity()} is null
	 * 
	 */
	public ActivityData getActivityData(TargetActivity1 targetActivity, Set<ResourceLink> links,
			Set<ResourceLink> files, Set<Tag> tags, boolean shouldTrackChanges, int order, boolean isManager) {
		if (targetActivity == null || targetActivity.getActivity() == null) {
			throw new NullPointerException();
		}
		Activity1 activity = targetActivity.getActivity();
		ActivityData data = new ActivityData(false);
		data.setActivityId(activity.getId());
		data.setTargetActivityId(targetActivity.getId());
		data.setOrder(order);
		data.setCommonScore(targetActivity.getCommonScore());
		data.setTitle(activity.getTitle());
		data.setDescription(activity.getDescription());
		data.setDurationHours((int) (activity.getDuration() / 60));
		data.setDurationMinutes((int) (activity.getDuration() % 60));
		data.calculateDurationString();
		data.setCompleted(targetActivity.isCompleted());
		data.setEnrolled(true);
		data.setType(activity.getType());
		data.setResultData(getActivityResultData(targetActivity, isManager));
		data.setCreatorId(activity.getCreatedBy().getId());
		data.setStudentCanEditResponse(activity.isStudentCanEditResponse());
		data.setStudentCanSeeOtherResponses(activity.isStudentCanSeeOtherResponses());
		if (tags != null) {
			data.setTags(tags);
			data.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(tags));
		}

		data.getAssessmentSettings().setMaxPoints(activity.getMaxPoints());
		data.getAssessmentSettings().setMaxPointsString(String.valueOf(activity.getMaxPoints()));
		data.getAssessmentSettings().setGradingMode(activity.getGradingMode());
		//set rubric data
		if (activity.getRubric() != null) {
			data.getAssessmentSettings().setRubricId(activity.getRubric().getId());
			data.setRubricVisibility(activity.getRubricVisibility());
			data.getAssessmentSettings().setRubricName(activity.getRubric().getTitle());
			data.getAssessmentSettings().setRubricType(activity.getRubric().getRubricType());
		}

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
		data.setCompetenceName(targetActivity.getTargetCompetence().getCompetence().getTitle());
		populateTypeSpecificData(data, targetActivity.getActivity());

		data.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if (shouldTrackChanges) {
			data.startObservingChanges();
		}
		
		return data;
	}
	
	private ActivityResultData getActivityResultData(TargetActivity1 activity, boolean isManager) {
		return getActivityResultData(activity.getId(), activity.getActivity().getResultType(), activity.getResult(), 
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
	
	public ActivityData getBasicActivityData(TargetActivity1 activity, boolean shouldTrackChanges) {
		if(activity == null) {
			return null;
		}
		ActivityData act = new ActivityData(false);
		Activity1 activ = activity.getActivity();
		act.setActivityId(activ.getId());
		act.setTargetActivityId(activity.getId());
		act.setCommonScore(activity.getCommonScore());
		act.setTitle(activ.getTitle());
		act.setCompleted(activity.isCompleted());
		act.setEnrolled(true);
		act.setDurationHours((int) (activ.getDuration() / 60));
		act.setDurationMinutes((int) (activ.getDuration() % 60));
		act.calculateDurationString();
		act.getResultData().setResultType(getResultType(activ.getResultType()));
		act.getResultData().setResult(activity.getResult());
		act.setTargetCompetenceId(activity.getTargetCompetence().getId());

		act.getAssessmentSettings().setMaxPoints(activ.getMaxPoints());
		act.getAssessmentSettings().setGradingMode(activ.getGradingMode());
		//set rubric data
		if (activ.getRubric() != null) {
			act.getAssessmentSettings().setRubricId(activ.getRubric().getId());
			act.setRubricVisibility(activ.getRubricVisibility());
			act.getAssessmentSettings().setRubricName(activ.getRubric().getTitle());
			act.getAssessmentSettings().setRubricType(activ.getRubric().getRubricType());
		}

		setBasicTypeSpecificData(activ, act);

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		act.setActivityType(getActivityType(activ));

		act.setObjectStatus(ObjectStatus.UP_TO_DATE);
		
		if(shouldTrackChanges) {
			act.startObservingChanges();
		}
		
		return act;
	}

	private void populateCommonData(Activity1 activity, ActivityData data) {
		activity.setId(data.getActivityId());
		activity.setTitle(data.getTitle());
		activity.setDescription(data.getDescription());
		activity.setDuration(data.getDurationHours() * 60 + data.getDurationMinutes());
		activity.setResultType(getResultType(data.getResultData().getResultType()));
		activity.setDateCreated(data.getDateCreated());
		activity.setType(data.getType());
		activity.setStudentCanSeeOtherResponses(data.isStudentCanSeeOtherResponses());
		activity.setStudentCanEditResponse(data.isStudentCanEditResponse());
		activity.setDifficulty(data.getDifficulty());
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
					urlAct.setUrl(activityData.getVideoLink());
				} else {
					urlAct.setUrlType(UrlActivityType.Slides);
					urlAct.setUrl(activityData.getSlidesLink());
				}
				populateCommonData(urlAct, activityData);
				urlAct.setLinkName(activityData.getLinkName());
				return urlAct;
			case EXTERNAL_TOOL:
				ExternalToolActivity1 extAct = new ExternalToolActivity1();
				populateCommonData(extAct, activityData);
				extAct.setLaunchUrl(activityData.getLaunchUrl());
				extAct.setSharedSecret(activityData.getSharedSecret());
				extAct.setConsumerKey(activityData.getConsumerKey());
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
