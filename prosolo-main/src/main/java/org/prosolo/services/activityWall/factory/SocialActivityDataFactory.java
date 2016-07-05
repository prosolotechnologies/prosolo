package org.prosolo.services.activityWall.factory;

import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.activitywall.ActivityCommentSocialActivity;
import org.prosolo.common.domainmodel.activitywall.ActivityCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CompetenceCommentSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CredentialCompleteSocialActivity;
import org.prosolo.common.domainmodel.activitywall.CredentialEnrollSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.user.UserType;
import org.prosolo.common.domainmodel.user.notifications.ObjectType;
import org.prosolo.services.activityWall.impl.data.ObjectData;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;
import org.prosolo.services.activityWall.impl.data.UserData;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.nodes.data.activity.attachmentPreview.AttachmentPreview1;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

@Component
public class SocialActivityDataFactory {

	@Inject private RichContentDataFactory richContentFactory;
	@Inject private ObjectDataFactory objectFactory;
	@Inject private ActivityDataFactory activityFactory;
	
	public SocialActivityData1 getSocialActivityData(
			BigInteger id,
			String dType,
			Date dateCreated,
			Date lastAction,
			Character commentsDisabled,
			String text,
			Integer likeCount,
			BigInteger actorId,
			String actorName,
			String actorLastname,
			String actorAvatarUrl,
			String twitterActorName,
			String twitterActorNick,
			String twitterProfileUrl,
			String twitterActorAvatar,
			String twitterPostUrl,
			Integer twitterUserType,
			Integer shareCount,
			String postRichContentTitle,
			String postRichContentDescription,
			String postRichContentContentType,
			String postRichContentImageUrl,
			String postRichContentLink,
			String postRichContentImageSize,
			BigInteger postObjectId,
			String postObjectText,
			String postObjectRichContentTitle,
			String postObjectRichContentDescription,
			String postObjectRichContentContentType,
			String postObjectRichContentImageUrl,
			String postObjectRichContentLink,
			String postObjectRichContentImageSize,
			BigInteger postObjectActorId,
			String postObjectActorName,
			String postObjectActorLastName,
			BigInteger credObjectId,
			String credObjectTitle,
			BigInteger credObjectDuration,
			String credObjectType,
			BigInteger credObjectActorId,
			String credObjectActorName,
			String credObjectActorLastname,
			String credObjectDescription,
			BigInteger commentObjectId,
			String commentObjectComment,
			BigInteger compTargetId,
			String compTargetTitle,
			BigInteger actTargetId,
			String actTargetTitle,
			BigInteger actObjectId,
			String actObjectTitle,
			BigInteger actObjectDuration,
			String actObjectType,
			BigInteger actObjectActorId,
			String actObjectActorName,
			String actObjectActorLastname,
			String actObjectDescription,
			String actObjectDType,
			String actObjectUrlType,
			BigInteger actObjectCompId,
			BigInteger actObjectCredId,
			Integer liked,
			Locale locale) {
		SocialActivityData1 sad = new SocialActivityData1();
		sad.setId(id.longValue());
		sad.setDateCreated(dateCreated);
		sad.setLastAction(lastAction);
		sad.setCommentsDisabled(commentsDisabled.charValue() == 'F');
		sad.setText(text);
		sad.setLikeCount(likeCount);
		sad.setLiked(liked == 1);
		
		AttachmentPreview1 ap = null;
		ObjectData obj = null;
		ObjectData target = null;
		
		if(actorId != null) {
			sad.setActor(new UserData(actorId.longValue(), actorName, actorLastname, actorAvatarUrl, false));
		}
		if(dType.equals(TwitterPostSocialActivity1.class.getSimpleName())) {
			//twitter post
			sad.setType(SocialActivityType.Twitter_Post);
			//TODO check if we have twitter user data when user type is regular
			if(twitterUserType.intValue() == UserType.TWITTER_USER.ordinal()) {
				sad.setActor(new UserData(twitterActorNick, twitterActorName, twitterActorAvatar, 
						twitterProfileUrl));
			}

			sad.setTwitterPostUrl(twitterPostUrl);
		} else if(dType.equals(PostSocialActivity1.class.getSimpleName())) {
			//post
			sad.setType(SocialActivityType.Post);
			if(shareCount != null) {
				sad.setShareCount(shareCount);
			}
			if(postRichContentContentType != null) {
				RichContent1 rc = new RichContent1();
				rc.setTitle(postRichContentTitle);
				rc.setDescription(postRichContentDescription);
				rc.setContentType(ContentType1.valueOf(postRichContentContentType));
				rc.setImageUrl(postRichContentImageUrl);
				if(rc.getContentType() == ContentType1.LINK && rc.getImageUrl() != null) {
					rc.setImageSize(ImageSize.valueOf(postRichContentImageSize));
				}
				rc.setLink(postRichContentLink);
				
				ap = richContentFactory.getAttachmentPreview(rc);
			}
		} else if(dType.equals(PostReshareSocialActivity.class.getSimpleName())) {
			//post reshare
			sad.setType(SocialActivityType.Post_Reshare);
			obj = objectFactory.getObjectData(postObjectId.longValue(), postObjectText, 
					ObjectType.PostSocialActivity, postObjectActorId.longValue(), postObjectActorName, 
					postObjectActorLastName, locale);
			if(postObjectRichContentContentType != null) {
				RichContent1 rc = new RichContent1();
				rc.setTitle(postObjectRichContentTitle);
				rc.setDescription(postObjectRichContentDescription);
				rc.setContentType(ContentType1.valueOf(postObjectRichContentContentType));
				rc.setImageUrl(postObjectRichContentImageUrl);
				if(rc.getContentType() == ContentType1.LINK && rc.getImageUrl() != null) {
					rc.setImageSize(ImageSize.valueOf(postObjectRichContentImageSize));
				}
				rc.setLink(postObjectRichContentLink);
				
				ap = richContentFactory.getAttachmentPreview(rc);
			}
		} else if(dType.equals(CredentialEnrollSocialActivity.class.getSimpleName())) {
			//credential enroll
			sad.setType(SocialActivityType.Enroll_Credential);
			obj = objectFactory.getObjectData(0, null, 
					ObjectType.Credential, 0, null, null, locale);
			ap = richContentFactory.getAttachmentPreviewForCredential(credObjectId.longValue(), 
					credObjectDuration.longValue(), credObjectTitle, credObjectDescription, 
					LearningResourceType.valueOf(credObjectType), credObjectActorName, 
					credObjectActorLastname);
		} else if(dType.equals(CredentialCompleteSocialActivity.class.getSimpleName())) {
			//credential complete
			sad.setType(SocialActivityType.Learning_Completion);
			obj = objectFactory.getObjectData(0, null, 
					ObjectType.Credential, 0, null, null, locale);
			ap = richContentFactory.getAttachmentPreviewForCredential(credObjectId.longValue(), 
					credObjectDuration.longValue(), credObjectTitle, credObjectDescription, 
					LearningResourceType.valueOf(credObjectType), credObjectActorName, 
					credObjectActorLastname);
		} else if(dType.equals(CompetenceCommentSocialActivity.class.getSimpleName())) {
			//competence comment
			sad.setType(SocialActivityType.Comment);
			obj = objectFactory.getObjectData(commentObjectId.longValue(), commentObjectComment, 
					ObjectType.Comment, 0, null, null, locale);
			target = objectFactory.getObjectData(compTargetId.longValue(), compTargetTitle, 
					ObjectType.Competence, 0, null, null, locale);
		} else if(dType.equals(ActivityCommentSocialActivity.class.getSimpleName())) {
			//activity comment
			sad.setType(SocialActivityType.Comment);
			obj = objectFactory.getObjectData(commentObjectId.longValue(), commentObjectComment, 
					ObjectType.Comment, 0, null, null, locale);
			target = objectFactory.getObjectData(actTargetId.longValue(), actTargetTitle, 
					ObjectType.Activity, 0, null, null, locale);
		} else if(dType.equals(ActivityCompleteSocialActivity.class.getSimpleName())) {
			//activity complete
			sad.setType(SocialActivityType.Learning_Completion);
			obj = objectFactory.getObjectData(0, null, 
					ObjectType.Activity, 0, null, null, locale);
			
			ActivityType actType = activityFactory.getActivityType(actObjectDType, actObjectUrlType);
			ap = richContentFactory.getAttachmentPreviewForActivity(actObjectId.longValue(), 
					actObjectDuration.longValue(), actObjectTitle, actObjectDescription, 
					LearningResourceType.valueOf(actObjectType), actType, actObjectActorName, 
					actObjectActorLastname, actObjectCompId.longValue(), actObjectCredId.longValue());
		}
		
		sad.setPredicate(ResourceBundleUtil.getActionName(sad.getType().name(), locale));
		if(obj != null && target != null) {
			sad.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, sad.getType(), 
					obj.getType(), target.getType()));
		}
		
		sad.setAttachmentPreview(ap);
		sad.setObject(obj);
		sad.setTarget(target);
		
		return sad;
	}
	
	/**
	 * this method requires all needed relationships initialized
	 * @param act
	 * @param liked
	 * @param locale
	 * @return
	 */
	public SocialActivityData1 getSocialActivityData(SocialActivity1 act, boolean liked, Locale locale) {
		if(act == null) {
			return null;
		}
		SocialActivityData1 sad = new SocialActivityData1();
		sad.setId(act.getId());
		sad.setDateCreated(act.getDateCreated());
		sad.setLastAction(act.getLastAction());
		sad.setCommentsDisabled(act.isCommentsDisabled());
		sad.setText(act.getText());
		sad.setLikeCount(act.getLikeCount());
		sad.setLiked(liked);
		
		AttachmentPreview1 ap = null;
		ObjectData obj = null;
		ObjectData target = null;
		
		sad.setActor(new UserData(act.getActor()));
		
		if(act instanceof TwitterPostSocialActivity1) {
			//twitter post
			TwitterPostSocialActivity1 tpAct = (TwitterPostSocialActivity1) act;
			sad.setType(SocialActivityType.Twitter_Post);
			//TODO check if we have twitter user data when user type is regular
			if(tpAct.getUserType() == UserType.TWITTER_USER) {
				sad.setActor(new UserData(tpAct.getNickname(), tpAct.getName(), tpAct.getAvatarUrl(), 
						tpAct.getProfileUrl()));
			}

			sad.setTwitterPostUrl(tpAct.getPostUrl());
		} else if(act instanceof PostSocialActivity1) {
			//post
			PostSocialActivity1 pAct = (PostSocialActivity1) act;
			sad.setType(SocialActivityType.Post);
			sad.setShareCount(pAct.getShareCount());

			if(pAct.getRichContent() != null) {
				ap = richContentFactory.getAttachmentPreview(pAct.getRichContent());
			}
		} else if(act instanceof PostReshareSocialActivity) {
			//post reshare
			PostReshareSocialActivity prAct = (PostReshareSocialActivity) act;
			PostSocialActivity1 psa = prAct.getPostObject();
			sad.setType(SocialActivityType.Post_Reshare);
			obj = objectFactory.getObjectData(psa.getId(), psa.getText(), 
					ObjectType.PostSocialActivity, psa.getActor().getId(), psa.getActor().getName(), 
					psa.getActor().getLastname(), locale);
			if(psa.getRichContent() != null) {
				ap = richContentFactory.getAttachmentPreview(psa.getRichContent());
			}
		} else if(act instanceof CredentialEnrollSocialActivity) {
			//credential enroll
			CredentialEnrollSocialActivity ceAct = (CredentialEnrollSocialActivity) act;
			Credential1 cred = ceAct.getCredentialObject();
			sad.setType(SocialActivityType.Enroll_Credential);
			obj = objectFactory.getObjectData(cred.getId(), cred.getTitle(), 
					ObjectType.Credential, 0, null, null, locale);
			ap = richContentFactory.getAttachmentPreviewForCredential(cred.getId(), 
					cred.getDuration(), cred.getTitle(), cred.getDescription(), 
					cred.getType(), cred.getCreatedBy().getName(), 
					cred.getCreatedBy().getName());
		} else if(act instanceof CredentialCompleteSocialActivity) {
			//credential complete
			CredentialCompleteSocialActivity ccAct = (CredentialCompleteSocialActivity) act;
			Credential1 cred = ccAct.getCredentialObject();
			sad.setType(SocialActivityType.Learning_Completion);
			obj = objectFactory.getObjectData(cred.getId(), cred.getTitle(), 
					ObjectType.Credential, 0, null, null, locale);
			ap = richContentFactory.getAttachmentPreviewForCredential(cred.getId(), 
					cred.getDuration(), cred.getTitle(), cred.getDescription(), 
					cred.getType(), cred.getCreatedBy().getName(), 
					cred.getCreatedBy().getName());
		} else if(act instanceof CompetenceCommentSocialActivity) {
			//competence comment
			CompetenceCommentSocialActivity ccAct = (CompetenceCommentSocialActivity) act;
			Comment1 comment = ccAct.getCommentObject();
			Competence1 comp = ccAct.getCompetenceTarget();
			sad.setType(SocialActivityType.Comment);
			obj = objectFactory.getObjectData(comment.getId(), comment.getDescription(), 
					ObjectType.Comment, 0, null, null, locale);
			target = objectFactory.getObjectData(comp.getId(), comp.getTitle(), 
					ObjectType.Competence, 0, null, null, locale);
		} else if(act instanceof ActivityCommentSocialActivity) {
			//activity comment
			ActivityCommentSocialActivity acAct = (ActivityCommentSocialActivity) act;
			Comment1 comment = acAct.getCommentObject();
			Activity1 activity = acAct.getActivityTarget();
			sad.setType(SocialActivityType.Comment);
			obj = objectFactory.getObjectData(comment.getId(), comment.getDescription(), 
					ObjectType.Comment, 0, null, null, locale);
			target = objectFactory.getObjectData(activity.getId(), activity.getTitle(), 
					ObjectType.Activity, 0, null, null, locale);
		} else if(act instanceof ActivityCompleteSocialActivity) {
			//activity complete
			ActivityCompleteSocialActivity acAct = (ActivityCompleteSocialActivity) act;
			TargetActivity1 tAct = (TargetActivity1) acAct.getTargetActivityObject();
			Activity1 activity = tAct.getActivity();
			sad.setType(SocialActivityType.Learning_Completion);
			obj = objectFactory.getObjectData(0, null, 
					ObjectType.Activity, 0, null, null, locale);
			
			ActivityType actType = activityFactory.getActivityType(activity);
			ap = richContentFactory.getAttachmentPreviewForActivity(activity.getId(), 
					activity.getDuration(), activity.getTitle(), activity.getDescription(), 
					activity.getType(), actType, activity.getCreatedBy().getName(), 
					activity.getCreatedBy().getLastname(), 
					tAct.getTargetCompetence().getCompetence().getId(), 
					tAct.getTargetCompetence().getTargetCredential().getCredential().getId());
		}
		
		sad.setPredicate(ResourceBundleUtil.getActionName(sad.getType().name(), locale));
		if(obj != null && target != null) {
			sad.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, sad.getType(), 
					obj.getType(), target.getType()));
		}
		
		sad.setAttachmentPreview(ap);
		sad.setObject(obj);
		sad.setTarget(target);
		
		return sad;
	}
}