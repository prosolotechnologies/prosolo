package org.prosolo.services.activityWall.factory;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activitywall.*;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.ContentType1;
import org.prosolo.common.domainmodel.content.ImageSize;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.notifications.ResourceType;
import org.prosolo.services.activityWall.impl.data.ObjectData;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.activityWall.impl.data.SocialActivityType;
import org.prosolo.services.interaction.data.CommentsData;
import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.nodes.data.ActivityType;
import org.prosolo.services.user.data.UserData;
import org.prosolo.services.user.data.UserType;
import org.prosolo.services.nodes.data.statusWall.AttachmentPreview;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Date;
import java.util.Locale;

@Component
public class SocialActivityDataFactory {

    private static final Logger logger = Logger.getLogger(SocialActivityDataFactory.class);

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
            String postRichContentTitle,
            String postRichContentDescription,
            String postRichContentContentType,
            String postRichContentImageUrl,
            String postRichContentLink,
            String postRichContentEmbedId,
            String postRichContentImageSize,
            BigInteger postObjectId,
            String postObjectText,
            String postObjectRichContentTitle,
            String postObjectRichContentDescription,
            String postObjectRichContentContentType,
            String postObjectRichContentImageUrl,
            String postObjectRichContentLink,
            String postObjectRichContentEmbedId,
            String postObjectRichContentImageSize,
            BigInteger postObjectActorId,
            String postObjectActorName,
            String postObjectActorLastName,
            String postObjectActorAvatar,
            Date postObjectDateCreated,
            BigInteger credObjectId,
            String credObjectTitle,
            BigInteger credObjectDuration,
            BigInteger credObjectActorId,
            String credObjectActorName,
            String credObjectActorLastname,
            String credObjectDescription,
//not used currently
//			BigInteger commentObjectId,
//			String commentObjectComment,
//			BigInteger compTargetId,
//			String compTargetTitle,
//			BigInteger actTargetId,
//			String actTargetTitle,
//			BigInteger actTargetCompId,
//			String actTargetDType,
//			String actTargetUrlType,
//			BigInteger actObjectId,
//			String actObjectTitle,
//			BigInteger actObjectDuration,
//			String actObjectType,
//			BigInteger actObjectActorId,
//			String actObjectActorName,
//			String actObjectActorLastname,
//			String actObjectDescription,
//			String actObjectDType,
//			String actObjectUrlType,
//			BigInteger actObjectCompId,
            BigInteger compObjectId,
            String compObjectTitle,
            BigInteger compObjectDuration,
            String compObjectType,
            BigInteger compObjectActorId,
            String compObjectActorName,
            String compObjectActorLastname,
            String compObjectDescription,
            String unitWelcomeMessage,
            boolean liked,
            BigInteger commentsNumber,
            Locale locale) {
        SocialActivityData1 sad = new SocialActivityData1();
        sad.setId(id.longValue());
        sad.setDateCreated(dateCreated);
        sad.setLastAction(lastAction);
        sad.setCommentsDisabled(commentsDisabled.charValue() == 'T');

        if (!dType.equals(TwitterPostSocialActivity1.class.getSimpleName()) && !dType.equals(UnitWelcomePostSocialActivity.class.getSimpleName())) {
            sad.setText(text);
        }
        sad.setLikeCount(likeCount);
        sad.setLiked(liked);
        CommentsData cd = new CommentsData(CommentedResourceType.SocialActivity, id.longValue());
        cd.setNumberOfComments(commentsNumber.intValue());
        sad.setComments(cd);

        AttachmentPreview ap = null;
        ObjectData obj = null;
        ObjectData target = null;

        if (actorId != null) {
            User user = new User();
            user.setId(actorId.longValue());
            user.setName(actorName);
            user.setLastname(actorLastname);
            user.setAvatarUrl(actorAvatarUrl);
            sad.setActor(new UserData(user));
        }
        if (dType.equals(TwitterPostSocialActivity1.class.getSimpleName())) {
            //twitter post
            sad.setType(SocialActivityType.Twitter_Post);
            //if(twitterUserType.intValue() == UserType.TWITTER_USER.ordinal()) {
            if (sad.getActor() == null) {
                sad.setActor(new UserData(0, twitterActorName, null, twitterActorAvatar, null, null, true));
                sad.getActor().setType(UserType.TWITTER_USER);
            }
            ap = richContentFactory.getAttachmentPreviewForTwitterPost(twitterActorNick, twitterProfileUrl,
                    text, twitterPostUrl);
            //}
        } else if (dType.equals(PostSocialActivity1.class.getSimpleName())) {
            //post
            sad.setType(SocialActivityType.Post);

            if (postRichContentContentType != null) {
                RichContent1 rc = new RichContent1();
                rc.setTitle(postRichContentTitle);
                rc.setDescription(postRichContentDescription);
                rc.setContentType(ContentType1.valueOf(postRichContentContentType));
                rc.setImageUrl(postRichContentImageUrl);

                if (rc.getContentType() == ContentType1.LINK && rc.getImageUrl() != null) {
                    rc.setImageSize(ImageSize.valueOf(postRichContentImageSize));
                }
                rc.setLink(postRichContentLink);
                rc.setEmbedId(postRichContentEmbedId);

                try {
                    ap = richContentFactory.getAttachmentPreview(rc);
                } catch (LinkParserException e) {
                    logger.error(e);
                }
            }
        } else if (dType.equals(PostReshareSocialActivity.class.getSimpleName())) {
            //post reshare
            sad.setType(SocialActivityType.Post_Reshare);
            obj = objectFactory.getObjectData(postObjectId.longValue(), null,
                    ResourceType.PostSocialActivity, postObjectActorId.longValue(), postObjectActorName,
                    postObjectActorLastName, locale);
            SocialActivityData1 originalPost = new SocialActivityData1();
            originalPost.setId(postObjectId.longValue());
            originalPost.setDateCreated(postObjectDateCreated);
            originalPost.setActor(new UserData(postObjectActorId.longValue(), postObjectActorName,
                    postObjectActorLastName, postObjectActorAvatar, null, null, false));
            originalPost.setText(postObjectText);

            if (postObjectRichContentContentType != null) {
                RichContent1 rc = new RichContent1();
                rc.setTitle(postObjectRichContentTitle);
                rc.setDescription(postObjectRichContentDescription);
                rc.setContentType(ContentType1.valueOf(postObjectRichContentContentType));
                rc.setImageUrl(postObjectRichContentImageUrl);

                if (rc.getContentType() == ContentType1.LINK && rc.getImageUrl() != null) {
                    rc.setImageSize(ImageSize.valueOf(postObjectRichContentImageSize));
                }
                rc.setLink(postObjectRichContentLink);
                rc.setEmbedId(postObjectRichContentEmbedId);

                try {
                    AttachmentPreview attach = richContentFactory.getAttachmentPreview(rc);
                    originalPost.setAttachmentPreview(attach);
                } catch (LinkParserException e) {
                    logger.error(e);
                }
            }
            sad.setOriginalSocialActivity(originalPost);
        } else if (dType.equals(CredentialEnrollSocialActivity.class.getSimpleName())) {
            //credential enroll
            sad.setType(SocialActivityType.Enroll_Credential);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Credential, 0, null, null, locale);
            ap = richContentFactory.getAttachmentPreviewForCredential(credObjectId.longValue(),
                    credObjectDuration.longValue(), credObjectTitle, credObjectDescription,
                    LearningResourceType.UNIVERSITY_CREATED, credObjectActorName,
                    credObjectActorLastname);
        } else if (dType.equals(CredentialCompleteSocialActivity.class.getSimpleName())) {
            //credential complete
            sad.setType(SocialActivityType.Learning_Completion);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Credential, 0, null, null, locale);
            ap = richContentFactory.getAttachmentPreviewForCredential(credObjectId.longValue(),
                    credObjectDuration.longValue(), credObjectTitle, credObjectDescription,
                    LearningResourceType.UNIVERSITY_CREATED, credObjectActorName,
                    credObjectActorLastname);
        } else if (dType.equals(CompetenceCompleteSocialActivity.class.getSimpleName())) {
            //competence complete
            sad.setType(SocialActivityType.Learning_Completion);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Competence, 0, null, null, locale);

            ap = richContentFactory.getAttachmentPreviewForCompetence(compObjectId.longValue(),
                    compObjectDuration.longValue(), compObjectTitle, compObjectDescription,
                    LearningResourceType.valueOf(compObjectType), compObjectActorName,
                    compObjectActorLastname, 0);
        } else if (dType.equals(UnitWelcomePostSocialActivity.class.getSimpleName())) {
            sad.setType(SocialActivityType.Unit_Welcome_Post);
            //set welcome message as social activity text
            sad.setText(unitWelcomeMessage);
        }
        //these social activity types are not used currently
//		else if(dType.equals(CompetenceCommentSocialActivity.class.getSimpleName())) {
//			//competence comment
//			sad.setType(SocialActivityType.Comment);
//			obj = objectFactory.getObjectData(0, null,
//					ResourceType.Comment, 0, null, null, locale);
//			target = objectFactory.getObjectData(0, null,
//					ResourceType.Competence, 0, null, null, locale);
//			ap = richContentFactory.getAttachmentPreviewForComment(commentObjectId.longValue(),
//					target.getType(), compTargetTitle, commentObjectComment, compTargetId.longValue(), 0, null);
//		} else if(dType.equals(ActivityCommentSocialActivity.class.getSimpleName())) {
//			//activity comment
//			sad.setType(SocialActivityType.Comment);
//			obj = objectFactory.getObjectData(0, null,
//					ResourceType.Comment, 0, null, null, locale);
//			target = objectFactory.getObjectData(0, null,
//					ResourceType.Activity, 0, null, null, locale);
//			ActivityType actType = activityFactory.getActivityType(actTargetDType, actTargetUrlType);
//			ap = richContentFactory.getAttachmentPreviewForComment(commentObjectId.longValue(),
//					target.getType(), actTargetTitle, commentObjectComment, actTargetCompId.longValue(),
//					actTargetId.longValue(), actType);
//		} else if(dType.equals(ActivityCompleteSocialActivity.class.getSimpleName())) {
//			//activity complete
//			sad.setType(SocialActivityType.Learning_Completion);
//			obj = objectFactory.getObjectData(0, null,
//					ResourceType.Activity, 0, null, null, locale);
//
//			ActivityType actType = activityFactory.getActivityType(actObjectDType, actObjectUrlType);
//			ap = richContentFactory.getAttachmentPreviewForActivity(actObjectId.longValue(),
//					actObjectDuration.longValue(), actObjectTitle, actObjectDescription,
//					LearningResourceType.valueOf(actObjectType), actType, actObjectActorName,
//					actObjectActorLastname, actObjectCompId.longValue(), 0);
//		}

        sad.setPredicate(ResourceBundleUtil.getActionName(sad.getType().name(), locale));
        if (obj != null && target != null) {
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
     *
     * @param act
     * @param liked
     * @param locale
     * @return
     */
    public SocialActivityData1 getSocialActivityData(SocialActivity1 act, boolean liked, Locale locale) {
        if (act == null) {
            return null;
        }
        SocialActivityData1 sad = new SocialActivityData1();
        sad.setId(act.getId());
        sad.setDateCreated(act.getDateCreated());
        sad.setLastAction(act.getLastAction());
        sad.setCommentsDisabled(act.isCommentsDisabled());
        if (!(act instanceof TwitterPostSocialActivity1)) {
            sad.setText(act.getText());
        }
        sad.setLikeCount(act.getLikeCount());
        sad.setLiked(liked);

        AttachmentPreview ap = null;
        ObjectData obj = null;
        ObjectData target = null;

        sad.setActor(new UserData(act.getActor()));

        if (act instanceof TwitterPostSocialActivity1) {
            //twitter post
            TwitterPostSocialActivity1 tpAct = (TwitterPostSocialActivity1) act;
            sad.setType(SocialActivityType.Twitter_Post);
            ap = richContentFactory.getAttachmentPreviewForTwitterPost(tpAct.getNickname(),
                    tpAct.getProfileUrl(), tpAct.getText(), tpAct.getPostUrl());
        } else if (act instanceof PostSocialActivity1) {
            //post
            PostSocialActivity1 pAct = (PostSocialActivity1) act;
            sad.setType(SocialActivityType.Post);

            if (pAct.getRichContent() != null) {
                try {
                    ap = richContentFactory.getAttachmentPreview(pAct.getRichContent());
                } catch (LinkParserException e) {
                    logger.error(e);
                }
            }
        } else if (act instanceof PostReshareSocialActivity) {
            //post reshare
            PostReshareSocialActivity prAct = (PostReshareSocialActivity) act;
            PostSocialActivity1 psa = prAct.getPostObject();
            sad.setType(SocialActivityType.Post_Reshare);

            obj = objectFactory.getObjectData(psa.getId(), null,
                    ResourceType.PostSocialActivity, psa.getActor().getId(), psa.getActor().getName(),
                    psa.getActor().getLastname(), locale);

            SocialActivityData1 originalPost = new SocialActivityData1();
            originalPost.setId(psa.getId());
            originalPost.setDateCreated(psa.getDateCreated());
            originalPost.setActor(new UserData(psa.getActor()));
            originalPost.setText(psa.getText());
            if (psa.getRichContent() != null) {
                try {
                    AttachmentPreview attach = richContentFactory.getAttachmentPreview(psa.getRichContent());
                    originalPost.setAttachmentPreview(attach);
                } catch (LinkParserException e) {
                    logger.error(e);
                }
            }
            sad.setOriginalSocialActivity(originalPost);
        } else if (act instanceof CredentialEnrollSocialActivity) {
            //credential enroll
            CredentialEnrollSocialActivity ceAct = (CredentialEnrollSocialActivity) act;
            Credential1 cred = ceAct.getCredentialObject();
            sad.setType(SocialActivityType.Enroll_Credential);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Credential, 0, null, null, locale);
            ap = richContentFactory.getAttachmentPreviewForCredential(cred.getId(),
                    cred.getDuration(), cred.getTitle(), cred.getDescription(),
                    LearningResourceType.UNIVERSITY_CREATED,
                    cred.getCreatedBy().getName(), cred.getCreatedBy().getName());
        } else if (act instanceof CredentialCompleteSocialActivity) {
            //credential complete
            CredentialCompleteSocialActivity ccAct = (CredentialCompleteSocialActivity) act;
            Credential1 cred = ccAct.getCredentialObject();
            sad.setType(SocialActivityType.Learning_Completion);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Credential, 0, null, null, locale);
            ap = richContentFactory.getAttachmentPreviewForCredential(cred.getId(),
                    cred.getDuration(), cred.getTitle(), cred.getDescription(),
                    LearningResourceType.UNIVERSITY_CREATED,
                    cred.getCreatedBy().getName(),
                    cred.getCreatedBy().getName());
        } else if (act instanceof CompetenceCompleteSocialActivity) {
            //competence complete
            CompetenceCompleteSocialActivity ccAct = (CompetenceCompleteSocialActivity) act;
            Competence1 comp = ccAct.getTargetCompetenceObject().getCompetence();
            sad.setType(SocialActivityType.Learning_Completion);

            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Competence, 0, null, null, locale);

            Credential1 credential = ccAct.getCredential();

            // TODO: remove ternary operator when reading cred id once all SocialActivity data is migrated
            ap = richContentFactory.getAttachmentPreviewForCompetence(comp.getId(),
                    comp.getDuration(), comp.getTitle(), comp.getDescription(),
                    comp.getType(), comp.getCreatedBy().getName(),
                    comp.getCreatedBy().getName(), credential != null ? credential.getId() : 0);
        } else if (act instanceof CompetenceCommentSocialActivity) {
            //competence comment
            CompetenceCommentSocialActivity ccAct = (CompetenceCommentSocialActivity) act;
            Comment1 comment = ccAct.getCommentObject();
            Competence1 comp = ccAct.getCompetenceTarget();
            sad.setType(SocialActivityType.Comment);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Comment, 0, null, null, locale);
            target = objectFactory.getObjectData(0, null,
                    ResourceType.Competence, 0, null, null, locale);
            ap = richContentFactory.getAttachmentPreviewForComment(comment.getId(),
                    target.getType(), comp.getTitle(), comment.getDescription(), comp.getId(), 0, null);
        } else if (act instanceof ActivityCommentSocialActivity) {
            //activity comment
            ActivityCommentSocialActivity acAct = (ActivityCommentSocialActivity) act;
            Comment1 comment = acAct.getCommentObject();
            Activity1 activity = acAct.getActivityTarget();
            sad.setType(SocialActivityType.Comment);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Comment, 0, null, null, locale);
            target = objectFactory.getObjectData(0, null,
                    ResourceType.Activity, 0, null, null, locale);
            ActivityType actType = activityFactory.getActivityType(activity);
            //TODO pass competenceId when you find the way to retrieve it
            ap = richContentFactory.getAttachmentPreviewForComment(comment.getId(),
                    target.getType(), activity.getTitle(), comment.getDescription(), 0, activity.getId(),
                    actType);
        } else if (act instanceof ActivityCompleteSocialActivity) {
            //activity complete
            ActivityCompleteSocialActivity acAct = (ActivityCompleteSocialActivity) act;
            TargetActivity1 tAct = (TargetActivity1) acAct.getTargetActivityObject();
            Activity1 activity = tAct.getActivity();
            sad.setType(SocialActivityType.Learning_Completion);
            obj = objectFactory.getObjectData(0, null,
                    ResourceType.Activity, 0, null, null, locale);

            ActivityType actType = activityFactory.getActivityType(activity);
            ap = richContentFactory.getAttachmentPreviewForActivity(activity.getId(),
                    activity.getDuration(), activity.getTitle(), activity.getDescription(),
                    activity.getType(), actType, activity.getCreatedBy().getName(),
                    activity.getCreatedBy().getLastname(),
                    tAct.getTargetCompetence().getCompetence().getId(),
                    0);
        }

        sad.setPredicate(ResourceBundleUtil.getActionName(sad.getType().name(), locale));
        if (obj != null && target != null) {
            sad.setRelationToTarget(ResourceBundleUtil.getRelationBetweenResources(locale, sad.getType(),
                    obj.getType(), target.getType()));
        }

        sad.setAttachmentPreview(ap);
        sad.setObject(obj);
        sad.setTarget(target);

        return sad;
    }
}
