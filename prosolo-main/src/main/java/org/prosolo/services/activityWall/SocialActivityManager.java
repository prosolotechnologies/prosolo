package org.prosolo.services.activityWall;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activitywall.PostReshareSocialActivity;
import org.prosolo.common.domainmodel.activitywall.PostSocialActivity1;
import org.prosolo.common.domainmodel.activitywall.SocialActivity1;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.interaction.data.CommentData;

public interface SocialActivityManager {
	
	List<SocialActivityData1> getSocialActivities(long userId, Filter filter, int offset, 
			int limit, long previousId, Date previousDate, Locale locale) throws DbConnectionException;
	
	SocialActivity1 saveNewSocialActivity(SocialActivity1 socialActivity, Session session) 
			throws DbConnectionException;
	
	SocialActivityData1 getSocialActivity(long id, Class<? extends SocialActivity1> clazz, 
			long userId, Locale locale, Session session) throws DbConnectionException;
	
	PostSocialActivity1 createNewPost(long userId, SocialActivityData1 postData,
			LearningContextData context) throws DbConnectionException;
	
	PostSocialActivity1 updatePost(long userId, long postId, String newText, 
			LearningContextData context) throws DbConnectionException;
	
	Comment1 saveSocialActivityComment(long socialActivityId, CommentData data, long userId, 
			CommentedResourceType resource, LearningContextData context) throws DbConnectionException;
	
	void updateSocialActivityComment(long id, CommentData data, long userId, 
			LearningContextData context) throws DbConnectionException;
	
	void likeSocialActivity(long userId, long socialActivityId, LearningContextData context) 
			throws DbConnectionException;
	
	void unlikeSocialActivity(long userId, long socialActivityId, LearningContextData context) 
			throws DbConnectionException;
	
	PostReshareSocialActivity sharePost(long userId, String text, long originalPostId,
			LearningContextData context) throws DbConnectionException;

}
