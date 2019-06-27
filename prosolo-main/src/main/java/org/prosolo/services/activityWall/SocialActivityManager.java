package org.prosolo.services.activityWall;

import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.data.Result;
import org.prosolo.services.interaction.data.CommentData;

import java.util.*;

public interface SocialActivityManager {

    List<SocialActivityData1> getSocialActivities(long userId, Filter filter, int offset,
                                                  int limit, long previousId, Date previousDate, Locale locale) throws DbConnectionException;

    SocialActivity1 saveNewSocialActivity(SocialActivity1 socialActivity, Session session)
            throws DbConnectionException;

    SocialActivityData1 getSocialActivity(long id, Class<? extends SocialActivity1> clazz,
                                          long userId, Locale locale, Session session) throws DbConnectionException;

    PostSocialActivity1 createNewPost(SocialActivityData1 postData, UserContextData context) throws DbConnectionException;

    Result<PostSocialActivity1> createNewPostAndGetEvents(SocialActivityData1 postData, UserContextData context) throws DbConnectionException;

    PostSocialActivity1 updatePost(long postId, String newText,
                                   UserContextData context) throws DbConnectionException;

    Result<PostSocialActivity1> updatePostAndGetEvents(long postId, String newText,
                                                       UserContextData context) throws DbConnectionException;

    Comment1 saveSocialActivityComment(long socialActivityId, CommentData data,
                                       CommentedResourceType resource, UserContextData context) throws DbConnectionException;

    Result<Comment1> saveSocialActivityCommentAndGetEvents(long socialActivityId, CommentData data,
                                                   CommentedResourceType resource, UserContextData context)
            throws DbConnectionException;

    void updateSocialActivityComment(long id, CommentData data, UserContextData context)
            throws DbConnectionException;

    void likeSocialActivity(long socialActivityId, UserContextData context)
            throws DbConnectionException;

    Result<Void> likeSocialActivityAndGetEvents(long socialActivityId, UserContextData context)
            throws DbConnectionException;

    void unlikeSocialActivity(long socialActivityId, UserContextData context)
            throws DbConnectionException;

    Result<Void> unlikeSocialActivityAndGetEvents(long socialActivityId, UserContextData context)
            throws DbConnectionException;

    PostReshareSocialActivity sharePost(String text, long originalPostId,
                                        UserContextData context) throws DbConnectionException;

    Result<PostReshareSocialActivity> sharePostAndGetEvents(String text, long originalPostId, UserContextData context)
            throws DbConnectionException;

    Optional<SocialActivityData1> getSocialActivityById(long socialActivityId, long userId, Locale locale)
            throws DbConnectionException;

    Set<Long> getUsersInMyNetwork(long userId);

    void saveUnitWelcomePostSocialActivityIfNotExists(long unitId, Session session) throws DbConnectionException;

    void deleteUnitWelcomePostSocialActivityIfExists(long unitId, Session session) throws DbConnectionException;

    /**
     * Returns an id of the social activity actor.
     *
     * @param socialActivityId id of the social activity
     * @return
     */
    long getSocialActivityActorId(long socialActivityId);
}
