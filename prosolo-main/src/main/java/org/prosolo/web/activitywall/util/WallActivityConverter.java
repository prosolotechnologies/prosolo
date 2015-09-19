/**
 * 
 */
package org.prosolo.web.activitywall.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.activitywall.comments.Comment;
import org.prosolo.common.domainmodel.content.ContentType;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.activityWall.impl.util.SocialActivityConverterUtil;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.interaction.CommentingManager;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.media.util.VideoUtils;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.data.FileType;
import org.prosolo.web.activitywall.data.MediaType;
import org.prosolo.web.activitywall.data.NodeData;
import org.prosolo.web.activitywall.data.SocialActivityCommentData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.images.ImageSize;
import org.prosolo.web.util.images.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.web.activitywall.util.WallActivityConverter")
public class WallActivityConverter {
	
	private static Logger logger = Logger.getLogger(WallActivityConverter.class);
	
	@Autowired private LikeManager likeManager;
	@Autowired private DislikeManager dislikeManager;
	@Autowired private CommentingManager commentingManager;

//	public List<SocialActivityWallData> convertSocialActivityNotifications(List<SocialActivityNotification> saNotifications, 
//			User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
//		
//		List<SocialActivityWallData> wallActivities = new LinkedList<SocialActivityWallData>();
//		
//		if (saNotifications != null && !saNotifications.isEmpty()) {
//			for (SocialActivityNotification saNotification : saNotifications) {
//				SocialActivityWallData wallActivity = convertSocialActivityNotification(saNotification, loggedUser, subViewType, locale);
//				
//				if (wallActivity != null) {
//					wallActivities.add(wallActivity);
//				}
//			}
//		}
//		return wallActivities;
//	}

//	public SocialActivityWallData convertSocialActivityNotification(SocialActivityNotification saNotification, User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
//		SocialActivityWallData activityWallData = convertSocialActivity(saNotification.getSocialActivity(), loggedUser, subViewType, locale);
//		
//		if (activityWallData != null) {
//			// hashtags
//			if (saNotification.getSubViews() != null && !saNotification.getSubViews().isEmpty()) {
//				for (SocialStreamSubView subView : saNotification.getSubViews()) {
//					
//					if (subView.getType().equals(subViewType)) {
//						if (subView.getHashtags() != null) {
//							activityWallData.getHashtags().addAll(AnnotationUtil.getAsListOfTitles(subView.getHashtags()));
//						}
//					}
//				}
//			}
//			activityWallData.setLiked(saNotification.isLiked());
//			activityWallData.setDisliked(saNotification.isDisliked());
//			activityWallData.setShared(postManager.isSharedByUser(saNotification.getSocialActivity(), loggedUser));
////			activityWallData.setBookmarked(saNotification.isBookmarked());
////			activityWallData.setCreator(saNotification.isCreator());
//			activityWallData.setNotificationId(saNotification.getId());
//		}
//		return activityWallData;
//	}
 
//	public List<SocialActivityWallData> convertSocialActivities(
//			List<SocialActivity> soialActivities, User loggedUser,
//			SocialStreamSubViewType subviewType, Locale locale, boolean optionsDisabled) {
//		
//		List<SocialActivityWallData> wallActivities = new LinkedList<SocialActivityWallData>();
//		
//		if (soialActivities != null && !soialActivities.isEmpty()) {
//			for (SocialActivity socialActivity : soialActivities) {
//				SocialActivityWallData wallActivity = convertSocialActivity(socialActivity, loggedUser, subviewType, locale);
//				
//				if (wallActivity != null) {
//					wallActivity.setLiked(likeManager.isLikedByUser(socialActivity, loggedUser));
//					wallActivity.setDisliked(dislikeManager.isDislikedByUser(socialActivity, loggedUser));
//					wallActivity.setShared(postManager.isSharedByUser(socialActivity, loggedUser));
//					wallActivity.setOptionsDisabled(optionsDisabled);
//					
//					wallActivities.add(wallActivity);
//				}
//			}
//		}
//		return wallActivities;
//	}
	
//	public SocialActivityWallData convertSocialActivity(SocialActivity socialActivity, User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
//		if (socialActivity != null) {
//			socialActivity = HibernateUtil.initializeAndUnproxy(socialActivity);
//			
//			SocialActivityWallData wallActivity = new SocialActivityWallData(socialActivity);
//			
//			wallActivity.setSubViewType(subViewType);
//			
//			User actor = socialActivity.getMaker();
//			
//			logger.debug("Converting social activity " + socialActivity);
//			logger.debug("socialActivity instanceof TwitterPostSocialActivity " + (socialActivity instanceof TwitterPostSocialActivity));
//			logger.debug("actor " + actor);
//			
//			if (actor != null)
//				logger.debug("actor avatar url " + actor.getAvatarUrl());
//			
//			// it can be null for TwitterPostSocialActivity
//			if (actor != null && loggedUser != null) {
//				actor = HibernateUtil.initializeAndUnproxy(actor);
//				wallActivity.setMaker(actor.getId() == loggedUser.getId());
//			}
//			
//			
//			if (socialActivity instanceof TwitterPostSocialActivity && 
//					((TwitterPostSocialActivity) socialActivity).getUserType().equals(UserType.TWITTER_USER)) {
//				TwitterPostSocialActivity twitterPostSA = (TwitterPostSocialActivity) socialActivity;
//
//				// is anon user (user type is introduced in user class because sometimes actor is a proxy and we 
//				// can not determine whether it is of class User or AnonUser)
//				wallActivity.setAnonUser(true);
//				
//				// avatar url
//				wallActivity.setAvatarUrl(twitterPostSA.getAvatarUrl());
//				
//				PublishingServiceData publishingService = SocialActivityConverterUtil.getPublishingServiceData(
//						twitterPostSA.getServiceType(), 
//						twitterPostSA.getNickname(), 
//						twitterPostSA.getProfileUrl(),
//						locale);
//				wallActivity.getActor().setPublishingService(publishingService);
//				
//				// since user.id is used on the interface to uniquely identify a user (e.g. for tooltip), we are setting socialActivity.id as an identifier
//				wallActivity.getActor().setId(socialActivity.getId());
//				
//				wallActivity.setActivityUrl(twitterPostSA.getPostUrl());
//				
//				wallActivity.setText(twitterPostSA.getText());
//				wallActivity.setObject(new NodeData());
//				wallActivity.getObject().setShortType(getResourceType(locale, new TwitterPost()));
//			} else {
//				// avatar url
//				
//				if (actor != null)
//					wallActivity.setAvatarUrl(actor.getAvatarUrl());
//				else {
//					logger.error(actor);
//				}
//			}
//			
//			// action
//			EventType action = socialActivity.getAction();
//			
//			wallActivity.setActionName(ResourceBundleUtil.getActionName(action, locale));
//			
//			wallActivity.setLikeCount(socialActivity.getLikeCount());
//			wallActivity.setDislikeCount(socialActivity.getDislikeCount());
//			wallActivity.setShareCount(socialActivity.getShareCount());
//			
//			// userCanBeUnfollowed
//			wallActivity.setUserCanBeUnfollowed(!wallActivity.isAnonUser() && 
//												loggedUser != null && 
//												actor != null &&
//												actor.getId() != loggedUser.getId());
//			
//			// object
//			BaseEntity object = socialActivity.getObject();
//			
//			if (object != null) {
//				object = HibernateUtil.initializeAndUnproxy(object);
//				wallActivity.setObject(new NodeData(object));
//				wallActivity.getObject().setShortType(getResourceType(locale, object));
//
//				// special case when course is enrolled
//				if (action.equals(EventType.ENROLL_COURSE)) {
//					Course course = ((CourseEnrollment) object).getCourse();
//					
//					wallActivity.setObject(new NodeData(course));
//					wallActivity.getObject().setShortType(getResourceType(locale, course));
//				}
//			}
//
//			
//			// special case when resource is shared
//			if (action.equals(EventType.PostShare)) {
//				Post post = (Post) object;
//				
//				if (post.getRichContent() != null && 
//						post.getRichContent().getContentType().equals(ContentType.RESOURCE)) {
//					
//					Node sharedResource = post.getRichContent().getResource();
//					
//					if (sharedResource != null) {
//						wallActivity.getObject().setClazz(sharedResource.getClass());
//						wallActivity.getObject().setId(sharedResource.getId());
//						wallActivity.getObject().setTitle(sharedResource.getTitle());
//						wallActivity.getObject().setShortType(getResourceType(locale, sharedResource));
//					}
//				} else if (post.getReshareOf() != null) {
//					wallActivity.getObject().setShortType(getResourceType(locale, post.getReshareOf()));
//				}
//			}
//			
//			if (action.equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED) && object != null) {
//				AttachmentPreview attachPreview = new AttachmentPreview();
//				
//				attachPreview.setTitle(object.getTitle());
//				attachPreview.setDescription(object.getDescription());
//				attachPreview.setContentType(ContentType.RESOURCE);
//				attachPreview.setResource(new NodeData(object));
//				attachPreview.getResource().setShortType(getResourceType(locale, object));
//				
//				wallActivity.setAttachmentPreview(attachPreview);
//			}
//			
//			// publishing service
//			if (object instanceof Post) {
//				Post post = (Post) object;
//				post = defaultManager.merge(post);
//			}
//			
//			// affiliation
////			if (socialActivity.getObject().isReshared()) {
////				try {
////					String affiliation = ResourceBundleUtil.getMessage( 
////							"activitywall.affiliation."+action, 
////							locale);
////					wallActivity.setAffiliation(affiliation);
////				} catch (KeyNotFoundInBundleException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				}
////			}
//			boolean creator = false;
//			
//			if (actor != null && loggedUser != null) {
//				creator = loggedUser.getId() == actor.getId();
//			}
//			
//			// sharable
//			if (object instanceof Post) {
//				wallActivity.setShareable(!creator);				
//			}
//			// directMessageEnabled
//			wallActivity.setDirectMessageEnabled(!creator);
//			
//			// attachment preview
//			if (socialActivity.getRichContent() != null) {
//				wallActivity.setAttachmentPreview(createAttachmentPreview(socialActivity.getRichContent(), locale)); 
//			}
//			
//			if (action.equals(EventType.Create) || action.equals(EventType.Completion)) {
//				wallActivity.setAttachmentPreview(createAttachmentPreviewForResource(object, locale));
//				
//				if (!(object instanceof Activity)) {
//					wallActivity.setText(null);
//				}
//			}
//			
//			if (action.equals(EventType.Registered)) {
//				wallActivity.setAttachmentPreview(createAttachmentPreviewForAction(action, locale));
//			}
//			
//			// target
//			BaseEntity target = socialActivity.getTarget();
//			
//			if (target != null) {
//				target = HibernateUtil.initializeAndUnproxy(target);
//				
//				if (target instanceof Node) {
//					wallActivity.setTarget(new NodeData(target));
//					
//					String targetType = getResourceType(locale, target);
//					wallActivity.getTarget().setShortType(targetType);
//					wallActivity.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, action, object.getClass(), target.getClass()));
//				} else if (target instanceof User) {
//					wallActivity.setTargetActor(new UserData((User) target));
//					wallActivity.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, action, object.getClass(), target.getClass()));
//				}
//			}
//			
//			// comments
//			wallActivity.setComments(convertResourceComments(socialActivity, loggedUser, wallActivity));
//			
//			return wallActivity;
//		}
//		return null;
//	}

//	public static String getResourceType(Locale locale, BaseEntity resource) {
//		Class<?> clazz = null;
//		
//		try {
//			clazz = Hibernate.getClass(resource);
//		} catch (LazyInitializationException lie) {
//			if (resource instanceof HibernateProxy) {
//				clazz = resource.getClass().getSuperclass();
//			} else {
//				clazz = resource.getClass();
//			}
//		}
//		
//		return ResourceBundleUtil.getResourceType(clazz, locale);
//	}

	public List<SocialActivityCommentData> convertResourceComments(BaseEntity resource, User loggedUser, SocialActivityData wallData) {
		List<SocialActivityCommentData> wallActivities = new LinkedList<SocialActivityCommentData>();
		List<Comment> comments = commentingManager.getComments(resource);
		
		if (comments != null && !comments.isEmpty()) {
			for (Comment comment : comments) {
				wallActivities.add(new SocialActivityCommentData(comment, likeManager.likeCount(comment), likeManager.isLikedByUser(comment, loggedUser), wallData));
			}
		}
		return wallActivities;
	}
	
	public List<SocialActivityCommentData> convertResourceComments(long socialActivityId, User loggedUser, SocialActivityData wallData) {
		List<SocialActivityCommentData> wallActivities = new LinkedList<SocialActivityCommentData>();
		List<Comment> comments = commentingManager.getCommentsForSocialActivity(socialActivityId);
		
		if (comments != null && !comments.isEmpty()) {
			for (Comment comment : comments) {
				wallActivities.add(new SocialActivityCommentData(comment, likeManager.likeCount(comment), likeManager.isLikedByUser(comment, loggedUser), wallData));
			}
		}
		return wallActivities;
	}
	
	public static AttachmentPreview createAttachmentPreview(String title, String description, String link,
		String imageUrl, ContentType contentType, Locale locale) {
	
		if (contentType == null) {
			contentType = ContentType.LINK;
		}
		
		AttachmentPreview attachmentPreview = new AttachmentPreview();
		attachmentPreview.setTitle(title);
		attachmentPreview.setDescription(description);
		attachmentPreview.setLink(link);
		attachmentPreview.setContentType(contentType);
	
		if (imageUrl != null) {
			attachmentPreview.setImage(imageUrl);
			attachmentPreview.getImages().add(imageUrl);
			attachmentPreview.setSelectedImageIndex(0);
		}
		
		if (contentType.equals(ContentType.LINK)) {
			attachmentPreview.setTitle(title);
			attachmentPreview.setDescription(description);
		} else if (contentType.equals(ContentType.UPLOAD)) {
			attachmentPreview.setUploadTitle(title);
			attachmentPreview.setUploadDescription(description);
			
			// image is uplaoded
			if (attachmentPreview.getLink() == null && attachmentPreview.getImage() != null) {
				attachmentPreview.setLink(attachmentPreview.getImage());
			}
		}
		
		attachmentPreview = initializeAttachmentPreview(attachmentPreview, locale);
		return attachmentPreview;
	}
	
	public static AttachmentPreview initializeAttachmentPreview(AttachmentPreview attachmentPreview, Locale locale) {
		if (VideoUtils.isEmbedableVideo(attachmentPreview.getLink())) {
			String embedLink = VideoUtils.convertEmbedingLinkForYouTubeVideos(attachmentPreview, attachmentPreview.getLink());
			
			if (embedLink != null && embedLink.length() > 0) {
				attachmentPreview.setMediaType(MediaType.VIDEO);
				attachmentPreview.setEmbedingLink(embedLink);
			} 
		}
		if (SlideShareUtils.isSlideSharePresentation(attachmentPreview.getLink())) {
			String embedLink = SlideShareUtils.convertSlideShareURLToEmbededUrl(attachmentPreview.getLink());
			
			if (embedLink != null && embedLink.length() > 10) {
				attachmentPreview.setMediaType(MediaType.SLIDESHARE);
				attachmentPreview.setEmbedingLink(embedLink);
			}
		}
		if (attachmentPreview.getImage() != null && attachmentPreview.getImage().length() > 0) {
		} else {
			FileType fileType = FileType.getFileType(attachmentPreview.getLink());
			
			// if image is the content
			if (ImageUtility.showImage(fileType)) {
				attachmentPreview.setImage(attachmentPreview.getLink());
			} else if (attachmentPreview.getContentType() != null) {
				
				if (attachmentPreview.getContentType().equals(ContentType.UPLOAD) || 
						!fileType.equals(FileType._BLANK)) {
					
					if (attachmentPreview.getContentType().equals(ContentType.UPLOAD)) {
						attachmentPreview.setContentType(ContentType.UPLOAD);
					}
					attachmentPreview.setImage(ImageUtil.getFileTypeIcon(fileType, ImageSize.size0x100));
				}
			}
		}
		
		/// TODO Nikola if shared resource
//			if (richContent.getResource() != null) {
//				Node sharedResource = richContent.getResource();
//				attachPreview.setResource(new NodeData(sharedResource));
//				attachPreview.getResource().setShortType(getObjectType(locale, sharedResource));
//			}
		return attachmentPreview;
	}
	
//	public static AttachmentPreview createAttachmentPreview(RichContent richContent, Locale locale) {
//		if (richContent != null) {
//			if (richContent.getResource() != null) {
//				return createAttachmentPreviewForResource(richContent.getResource(), locale);
//			}
//			AttachmentPreview attachPreview = new AttachmentPreview();
//			
//			attachPreview.setTitle(richContent.getTitle());
//			attachPreview.setDescription(richContent.getDescription());
//			attachPreview.setLink(richContent.getLink());
//			attachPreview.setContentType(richContent.getContentType());
//			
//			if (VideoUtils.isEmbedableVideo(richContent.getLink())) {
//				 VideoUtils.convertEmbedingLinkForYouTubeVideos(attachPreview, richContent.getLink());
//				
////				if (embedLink != null && embedLink.length() > 0) {
////					attachPreview.setMediaType(MediaType.VIDEO);
////					attachPreview.setEmbedingLink(embedLink);
////				} 
//			}
//			if (SlideShareUtils.isSlideSharePresentation(richContent.getLink())) {
//				String embedLink = SlideShareUtils.convertSlideShareURLToEmbededUrl(richContent.getLink());
//				
//				if (embedLink != null && embedLink.length() > 10) {
//					attachPreview.setMediaType(MediaType.SLIDESHARE);
//					attachPreview.setEmbedingLink(embedLink);
//				}
//			}
//			if (richContent.getImageUrl() != null && richContent.getImageUrl().length() > 0) {
//				attachPreview.setImage(richContent.getImageUrl());
//			} else {
//				FileType fileType = FileType.getFileType(richContent.getLink());
//				
//				// if image is the content
//				if (ImageUtility.showImage(fileType)) {
//					attachPreview.setImage(richContent.getLink());
//				} else if (richContent.getContentType() != null) {
//
//					if (richContent.getContentType().equals(ContentType.UPLOAD) || 
//							!fileType.equals(FileType._BLANK)) {
//						
//						if (richContent.getContentType().equals(ContentType.UPLOAD)) {
//							attachPreview.setContentType(ContentType.UPLOAD);
//						}
//						attachPreview.setImage(ImageUtil.getFileTypeIcon(fileType, ImageSize.size0x50));
//					}
//				}
//			}
//			/// if shared resource
//			if (richContent.getResource() != null) {
//				Node sharedResource = richContent.getResource();
//				attachPreview.setResource(new NodeData(sharedResource));
//				attachPreview.getResource().setShortType(getResourceType(locale, sharedResource));
//			}
//			return attachPreview;
//		}
//		return new AttachmentPreview();
//	}
	
//	public static AttachmentPreview createAttachmentPreviewForResource(BaseEntity object, Locale locale) {
//		if (object instanceof Activity) {
//			return createAttachmentPreviewForActivity((Activity) object, locale);
//		} else if (object instanceof TargetActivity) {
//			TargetActivity tActivity = (TargetActivity) object;
//			
//			return createAttachmentPreviewForActivity(tActivity.getActivity(), locale);
//		} else {
//			AttachmentPreview attachPreview = new AttachmentPreview();
//			
//			BaseEntity res = HibernateUtil.initializeAndUnproxy(object);
//			
//			if (res instanceof TargetCompetence) {
//				TargetCompetence tc = (TargetCompetence) res;
//			
//				attachPreview.setTitle(tc.getCompetence().getTitle());
//				attachPreview.setDescription(tc.getCompetence().getDescription());
//			} else {
//				attachPreview.setTitle(res.getTitle());
//				attachPreview.setDescription(res.getDescription());
//			}
//			
//			attachPreview.setContentType(ContentType.RESOURCE);
//			attachPreview.setResource(new NodeData(res));
//			attachPreview.getResource().setShortType(getResourceType(locale, res));
//			return attachPreview;
//		}
//	}

	public static AttachmentPreview createAttachmentPreviewForResource(NodeData resource, Locale locale) {
		AttachmentPreview attachmentPreview = new AttachmentPreview();
		
		attachmentPreview.setContentType(ContentType.RESOURCE);
		attachmentPreview.setTitle(resource.getTitle());
		attachmentPreview.setDescription(resource.getDescription());
		attachmentPreview.setResource(resource);
		attachmentPreview.getResource().setShortType(ResourceBundleUtil.getResourceType(resource.getClazz(), locale));
		
		if ((Activity.class.isAssignableFrom(resource.getClazz())
						|| TargetActivity.class.isAssignableFrom(resource.getClazz()))) {
			attachmentPreview = initializeAttachmentPreview(attachmentPreview, locale);
		}
		return attachmentPreview;
	}
	
//	@Deprecated
//	public static AttachmentPreview createAttachmentPreviewForActivity(Activity activity, Locale locale) {
//		if (activity != null) {
//			if (activity instanceof ResourceActivity) {
//				return WallActivityConverter.createAttachmentPreview(((ResourceActivity) activity).getRichContent(), locale);
//			} else if (activity instanceof UploadAssignmentActivity) {
//				return WallActivityConverter.createAttachmentPreview((UploadAssignmentActivity) activity, locale);
//			}
//		}
//		return null;
//	}
	
//	@Deprecated
//	public static AttachmentPreview initializeAttachmentPreviewForActivity(AttachmentPreview attachmentPreview, NodeData activity, Locale locale) {
//		if (activity != null) {
//			if (activity.equals(ResourceActivity.class)) {
//				return createAttachmentPreview(((ResourceActivity) activity).getRichContent(), locale);
//			} else if (activity.equals(UploadAssignmentActivity.class)) {
//				attachmentPreview.setMaxFilesNumber(uploadActivity.getMaxFilesNumber());
//				attachmentPreview.setDuration(uploadActivity.getDuration());
//				attachmentPreview.setVisibleToEveryone(uploadActivity.isVisibleToEveryone());
//				
//				// TODO Nikola
////				attachPreview.setResource(new NodeData(uploadActivity));
////				attachPreview.getResource().setShortType(getObjectType(locale, uploadActivity));
//				attachmentPreview.setContentType(ContentType.UPLOAD_ASSIGNMENT);
//		
//				return attachmentPreview;
//			}
//		}
//		return null;
//	}
	
	private AttachmentPreview createAttachmentPreviewForAction(EventType action, Locale locale) {
		AttachmentPreview attachPreview = new AttachmentPreview();
		attachPreview.setTitle("joined ProSolo");
		attachPreview.setAction(action);
		attachPreview.setContentType(ContentType.NONE);
		return attachPreview;
	}
	
	/**
	 * METHODS AFTER STATUS WALL REFACTORING
	 */
	public List<SocialActivityData> initializeSocialActivitiesData(
			List<SocialActivityData> soialActivitiesData, User loggedUser,
			SocialStreamSubViewType subviewType, Locale locale, boolean optionsDisabled) {
		
		List<SocialActivityData> wallActivities = new LinkedList<SocialActivityData>();
		
		if (soialActivitiesData != null && !soialActivitiesData.isEmpty()) {
			for (SocialActivityData socialActivityData : soialActivitiesData) {
				SocialActivityData initializedSocialActivityData = initiailizeSocialActivityData(socialActivityData, loggedUser, subviewType, locale);
				
				if (initializedSocialActivityData != null) {
//					wallActivity.setLiked(likeManager.isLikedByUser(socialActivity.getSocialActivity().getId(), loggedUser));
//					wallActivity.setDisliked(dislikeManager.isDislikedByUser(socialActivity, loggedUser));
//					wallActivity.setShared(postManager.isSharedByUser(socialActivity, loggedUser));
//					initializedSocialActivityData.setOptionsDisabled(optionsDisabled);
					
					wallActivities.add(initializedSocialActivityData);
				}
			}
		}
		return wallActivities;
	}
	
	public SocialActivityData initiailizeSocialActivityData(SocialActivityData socialActivityData, User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
		if (socialActivityData != null) {
			socialActivityData = HibernateUtil.initializeAndUnproxy(socialActivityData);
			
			socialActivityData.setSubViewType(subViewType);
			
			UserData actor = socialActivityData.getActor();
			
			logger.debug("Converting social activity " + socialActivityData);
			logger.debug("actor " + actor);
			
			if (actor != null)
				logger.debug("actor avatar url " + actor.getAvatarUrl());
			
			// it can be null for TwitterPostSocialActivity
			if (actor != null && loggedUser != null) {
				actor = HibernateUtil.initializeAndUnproxy(actor);
				socialActivityData.setMaker(actor.getId() == loggedUser.getId());
			}
			
			
			if (socialActivityData.getSocialActivity().getClazz().equals(TwitterPostSocialActivity.class) 
					&& socialActivityData.getTwitterUserType() != null
					&& socialActivityData.getTwitterUserType().equals(UserType.TWITTER_USER)) {

				ServiceType serviceType = socialActivityData.getPublishingService().getServiceType();
				socialActivityData.getPublishingService().setName(SocialActivityConverterUtil.getPublishingService(locale, serviceType));
			}
			
			// action
			EventType action = socialActivityData.getAction();
			
			socialActivityData.setActionName(ResourceBundleUtil.getActionName(action, locale));
			
			socialActivityData.setLikeCount(socialActivityData.getLikeCount());
			socialActivityData.setDislikeCount(socialActivityData.getDislikeCount());
			socialActivityData.setShareCount(socialActivityData.getShareCount());
			
			// userCanBeUnfollowed
			socialActivityData.setUserCanBeUnfollowed(!socialActivityData.isAnonUser() && 
												loggedUser != null && 
												actor != null &&
												actor.getId() != loggedUser.getId());
			
			// object
			NodeData object = socialActivityData.getObject();
			
			if (object != null) {
				socialActivityData.getObject().setShortType(ResourceBundleUtil.getResourceType(object.getClazz(), locale));
			}

			
			// special case when resource is shared
			if (action.equals(EventType.PostShare)) {
				// TODO: Nikola
//				Post post = (Post) object;
//				
//				if (post.getRichContent() != null && 
//						post.getRichContent().getContentType().equals(ContentType.RESOURCE)) {
//					
//					Node sharedResource = post.getRichContent().getResource();
//					
//					if (sharedResource != null) {
//						socialActivityData.getObject().setClazz(sharedResource.getClass());
//						socialActivityData.getObject().setId(sharedResource.getId());
//						socialActivityData.getObject().setTitle(sharedResource.getTitle());
//						socialActivityData.getObject().setShortType(getObjectType(locale, sharedResource));
//					}
//				} else if (post.getReshareOf() != null) {
//					socialActivityData.getObject().setShortType(getObjectType(locale, post.getReshareOf()));
//				}
			}
			
			boolean creator = false;
			
			if (actor != null && loggedUser != null) {
				creator = loggedUser.getId() == actor.getId();
			}
			
			// sharable
			if (object != null && object.getClazz().isAssignableFrom(Post.class)) {
				socialActivityData.setShareable(!creator);				
			}
			
			// target
			NodeData target = socialActivityData.getTarget();
			
			if (target != null) {
				
				// special case when adding GoalNot to TargetLearningGoal. In that case, we do not want to display target title nor relation to target
//				if (subViewType.equals(SocialStreamSubViewType.GOAL_WALL) && object.getClazz().equals(GoalNote.class)) {
//					target.setTitle(null);
//				} else {
					if (Node.class.isAssignableFrom(target.getClazz())) {
						socialActivityData.getTarget().setShortType(ResourceBundleUtil.getResourceType(target.getClazz(), locale));
					}
	
					socialActivityData.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, action, object.getClazz(), target.getClazz()));
//				} 
			}
			
			// attachment preview
			
			if (socialActivityData.getAttachmentPreview() != null) {
				socialActivityData.setAttachmentPreview(initializeAttachmentPreview(socialActivityData.getAttachmentPreview(), locale));
			}
			
			if (action.equals(EventType.JOIN_GOAL_INVITATION_ACCEPTED) && object != null) {
				AttachmentPreview attachPreview = new AttachmentPreview();
				
				attachPreview.setTitle(object.getTitle());
				attachPreview.setDescription(object.getDescription());
				attachPreview.setContentType(ContentType.RESOURCE);
				
				// TODO: Nikola
//				attachPreview.setResource(new NodeData(object));
//				attachPreview.getResource().setShortType(getObjectType(locale, object));
				
				socialActivityData.setAttachmentPreview(attachPreview);
			}
			
			if (action.equals(EventType.Registered)) {
				socialActivityData.setAttachmentPreview(createAttachmentPreviewForAction(action, locale));
			}
			
			// if there is no attachment preview and object is Node
			if (socialActivityData.getAttachmentPreview() == null 
					&& object != null 
					&& Node.class.isAssignableFrom(object.getClazz())) {
				socialActivityData.setAttachmentPreview(createAttachmentPreviewForResource(object, locale));
			}
			
			// comments
			socialActivityData.setComments(convertResourceComments(socialActivityData.getSocialActivity().getId(), loggedUser, socialActivityData));
			
			return socialActivityData;
		}
		return null;
	}

	public List<SocialActivityData> convertSocialActivities(List<SocialActivity> socialActivities, User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
		List<SocialActivityData> socialActivitiesData = new ArrayList<SocialActivityData>();
		
		for (SocialActivity socialActivity : socialActivities) {
			socialActivitiesData.add(convertSocialActivityToSocialActivityData(socialActivity, loggedUser, subViewType, locale));
		}
		return socialActivitiesData;
	}
	
	public SocialActivityData convertSocialActivityToSocialActivityData(SocialActivity socialActivity, User loggedUser, SocialStreamSubViewType subViewType, Locale locale) {
		SocialActivityData socialActivityData = new SocialActivityData(socialActivity);
		
		// initializing liked and disliked
		socialActivityData.setLiked(likeManager.isLikedByUser(socialActivity, loggedUser));
		socialActivityData.setDisliked(dislikeManager.isDislikedByUser(socialActivity, loggedUser));
		
		return initiailizeSocialActivityData(socialActivityData, loggedUser, subViewType, locale);
	}
}
