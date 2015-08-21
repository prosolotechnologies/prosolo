/**
 * 
 */
package org.prosolo.web.goals.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.ExternalToolActivity;
import org.prosolo.common.domainmodel.activities.ResourceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.UploadAssignmentActivity;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.RichContent;
import org.prosolo.common.domainmodel.outcomes.Outcome;
import org.prosolo.common.domainmodel.outcomes.SimpleOutcome;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.interaction.CommentingManager;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.web.activitywall.data.ActivityWallData;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.activitywall.util.WallActivityConverter;
import org.prosolo.web.competences.data.ActivityType;
import org.prosolo.web.goals.cache.ActionDisabledReason;
import org.prosolo.web.goals.cache.CompetenceDataCache;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.nodes.ActivityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.goals.util.CompWallActivityConverter")
public class CompWallActivityConverter {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CompWallActivityConverter.class);
	
	@Autowired private DislikeManager dislikeManager;
	@Autowired private LikeManager likeManager;
	@Autowired private CommentingManager commentingManager;
	@Autowired private WallActivityConverter wallActivityConverter;
	@Autowired private CompetenceManager competenceManager;
	
	public List<ActivityWallData> convertToActivityInstances(CompetenceDataCache compData, List<TargetActivity> targetActivities, 
			User loggedUser, boolean detailed, boolean unsavedActivity, Locale locale) {
		
		List<ActivityWallData> wallActivities = new ArrayList<ActivityWallData>();
		
		if (targetActivities != null && !targetActivities.isEmpty()) {
			for (TargetActivity targetAct : targetActivities) {
				ActivityWallData wallActivity = convertTargetActivityToActivityWallData(compData, targetAct, loggedUser, locale, detailed, unsavedActivity);
				
				if (wallActivity != null) {
					wallActivities.add(wallActivity);
				}
			}
		}
		
		return wallActivities;
	}

	/**
	 * @param compData 
	 * @param targetActivity
	 * @param loggedUser 
	 * @param locale 
	 * @param detailed 
	 * @return
	 */
	public ActivityWallData convertTargetActivityToActivityWallData(CompetenceDataCache compData, TargetActivity targetActivity, User loggedUser, Locale locale, boolean detailed, boolean unsavedActivity) {
		if (targetActivity != null) {
			Activity activity = targetActivity.getActivity();
			activity = HibernateUtil.initializeAndUnproxy(activity);

			ActivityWallData wallActivity = convertActivityToActivityWallData(activity, loggedUser, locale, detailed, unsavedActivity);
			wallActivity.setId(targetActivity.getId());
			wallActivity.setCompData(compData);
			
			wallActivity.setObject(new NodeData(targetActivity));
			wallActivity.getObject().setShortType(ResourceBundleUtil.getResourceType(targetActivity.getClass(), locale));
			
			wallActivity.setDateCreated(targetActivity.getDateCreated());
			wallActivity.setActivity(new NodeData(targetActivity.getActivity()));
			wallActivity.setCompleted(targetActivity.isCompleted());
			wallActivity.setDateCompleted(DateUtil.getPrettyDate(targetActivity.getDateCompleted()));
			wallActivity.setActionName(ActivityUtil.getActivityTitle(activity));
			
			AttachmentPreview attachmentPreview = wallActivity.getAttachmentPreview();
			
			if (attachmentPreview != null) {
				attachmentPreview.setUploadedAssignmentLink(targetActivity.getAssignmentLink());
				attachmentPreview.setUploadedAssignmentTitle(targetActivity.getAssignmentTitle());
			}
			if (!targetActivity.getOutcomes().isEmpty()) {
				Outcome outcome = targetActivity.getOutcomes().get(targetActivity.getOutcomes().size() - 1);
				wallActivity.setResult(((SimpleOutcome) outcome).getResult());
			}
				
			return wallActivity;
		}
		return null;
	}
	
	public List<ActivityWallData> convertCompetenceActivities(List<CompetenceActivity> compActivities, User user, boolean detailed, boolean unsavedActivity) {
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		
		List<ActivityWallData> wallActivities = new ArrayList<ActivityWallData>();
		
		if (compActivities != null && !compActivities.isEmpty()) {
			for (CompetenceActivity compAct : compActivities) {
				ActivityWallData wallActivity = convertActivityToActivityWallData(compAct.getActivity(), user, locale, detailed, unsavedActivity);
				
				if (wallActivity != null) {
					wallActivity.setPosition(compAct.getActivityPosition());
					wallActivities.add(wallActivity);
				}
			}
		}
		
		return wallActivities;
	}

	public List<ActivityWallData> convertActivities(List<Activity> activities, User user, Locale locale, boolean detailed, boolean unsavedActivity) {
		List<ActivityWallData> wallActivities = new ArrayList<ActivityWallData>();
		
		if (activities != null && !activities.isEmpty()) {
			int index = 0;
			
			for (Activity act : activities) {
				ActivityWallData wallActivity = convertActivityToActivityWallData(act, user, locale, detailed, unsavedActivity);
				
				if (wallActivity != null) {
					wallActivity.setPosition(index++);
					wallActivities.add(wallActivity);
				}
			}
		}
		return wallActivities;
	}
	
	public ActivityWallData convertActivityToActivityWallData(Activity activity, User loggedUser, Locale locale, boolean detailed,
			boolean unsavedActivity) {
		ActivityWallData wallActivity = new ActivityWallData();
		
		if (activity instanceof UploadAssignmentActivity) {
			wallActivity.setCanNotBeMarkedAsCompleted(ActionDisabledReason.COMPLETION_DISABLED_FOR_UPLOAD_ACTIVITY);
			wallActivity.setActivityType(ActivityType.ASSIGNMENTUPLOAD);
		}else if(activity instanceof ExternalToolActivity){
			wallActivity.setActivityType(ActivityType.EXTERNALTOOL);
			if(((ExternalToolActivity) activity).isAcceptGrades()){
				wallActivity.setAcceptGrades(true);
			}
		}else if(activity instanceof ResourceActivity){
			wallActivity.setActivityType(ActivityType.RESOURCE);
		}
		
		wallActivity.setActivity(new NodeData(activity));
			
		wallActivity.setObject(new NodeData(activity));
		String objectType = ResourceBundleUtil.getResourceType(Hibernate.getClass(activity), locale);
		wallActivity.getObject().setShortType(objectType);
		
		// this is to avoid accessing comments of an unsaved activity instance. 
		// In opposite, an error would occur: object references an unsaved transient instance
		if (!unsavedActivity) {
			wallActivity.setComments(wallActivityConverter.convertResourceComments(activity, loggedUser, wallActivity));
		
			if (detailed) {
				wallActivity.setLikeCount(likeManager.likeCount(activity));
				wallActivity.setLiked(likeManager.isLikedByUser(activity, loggedUser));
				
				wallActivity.setDislikeCount(dislikeManager.dislikeCount(activity));
				wallActivity.setDisliked(dislikeManager.isDislikedByUser(activity, loggedUser));
				
				// TODO: set share count
				wallActivity.setShareCount(0);
				
				// TODO: set bookmark count
//				wallActivity.setBookmarkCount(0);
			}
		}
		
		// attachment preview
		if (activity instanceof ResourceActivity) {
			RichContent richContent = ((ResourceActivity) activity).getRichContent();
			
			if (richContent != null) {
				AttachmentPreview attachPreview = WallActivityConverter.createAttachmentPreview(
						richContent.getTitle(), 
						richContent.getDescription(), 
						richContent.getLink(), 
						richContent.getImageUrl(), 
						richContent.getContentType(),
						locale);
				wallActivity.setAttachmentPreview(attachPreview);
			}
		} else if (activity instanceof UploadAssignmentActivity) {
			UploadAssignmentActivity uplaodActivity = (UploadAssignmentActivity) activity;
			AttachmentPreview attachPreview = WallActivityConverter.createAttachmentPreview(
					uplaodActivity.getTitle(), 
					uplaodActivity.getDescription(), 
					null, 
					null, 
					ContentType.UPLOAD_ASSIGNMENT,
					locale);
			wallActivity.setAttachmentPreview(attachPreview);
		}else if(activity instanceof ExternalToolActivity){
			wallActivity.setActivity(new NodeData(activity));
		}
		wallActivity.setComments(wallActivityConverter.convertResourceComments(activity, loggedUser, wallActivity));
		return wallActivity;
	}

//	private List<ActivityCommentData> convertSocialActivityComments(Activity activity, User loggedUser, ActivityWallData wallData) {
//		List<ActivityCommentData> wallActivities = new ArrayList<ActivityCommentData>();
//	 	List<Comment> comments = commentingManager.getComments(activity);
//
//	 	if (comments != null && !comments.isEmpty()) {
//			for (Comment comment : comments) {
//				ActivityCommentData wallActivityDataComment = new ActivityCommentData(comment, likeManager.likeCount(comment), likeManager.isLikedByUser(comment, loggedUser), wallData);
//				wallActivities.add(wallActivityDataComment);
//			}
//		}
//		
//		return wallActivities;
//	}
	
	public List<ActivityWallData> generateCompWallActivitiesData(TargetCompetence targetCompetence, Locale locale) {
		if (targetCompetence != null) {
			targetCompetence = competenceManager.merge(targetCompetence);
			List<TargetActivity> compActivities = targetCompetence.getTargetActivities();
			
			return convertToActivityWallData(compActivities, locale);
		} 
		return null;
	}
	
	public List<ActivityWallData> generateCompWallActivitiesData(ExternalCredit externalCredit, Locale locale) {
		if (externalCredit != null) {
			externalCredit = competenceManager.merge(externalCredit);
			List<TargetActivity> exCreditActivities = externalCredit.getTargetActivities();
			
			return convertToActivityWallData(exCreditActivities, locale);
		} 
		return null;
	}

	public List<ActivityWallData> convertToActivityWallData(List<TargetActivity> activities, Locale locale) {
		List<ActivityWallData> wallActivities = new ArrayList<ActivityWallData>();
		
		if (activities != null && !activities.isEmpty()) {
			for (TargetActivity act : activities) {
				ActivityWallData wallActivity = convertTargetActivityToActivityWallData(
						null,
						act,
						act.getMaker(), 
						locale, 
						false, 
						false);
				
				if (wallActivity != null) {
					wallActivities.add(wallActivity);
				}
			}
		}
		return wallActivities;
	}

}
