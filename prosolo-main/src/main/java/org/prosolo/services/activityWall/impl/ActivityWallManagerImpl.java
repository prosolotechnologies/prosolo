/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.SocialStreamSubViewType;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.Post;
import org.prosolo.common.domainmodel.interfacesettings.FilterType;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.activityWall.ActivityWallFactory;
import org.prosolo.services.activityWall.ActivityWallManager;
import org.prosolo.services.activityWall.filters.CourseFilter;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData;
import org.prosolo.services.activityWall.impl.util.SocialActivityDataResultTransformer;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nikola Milikic
 *
 */
@Service("org.prosolo.services.activitywall.ActivityWallManager")
public class ActivityWallManagerImpl extends AbstractManagerImpl implements ActivityWallManager {
	
	private static final long serialVersionUID = 803424506923574836L;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ActivityWallManager.class);

	@Autowired private ActivityWallFactory activityWallFactory;
	@Autowired private TagManager tagManager;
	
//	@Override
//	@Transactional (readOnly = true)
//	@Deprecated
//	public List<SocialActivityNotification> getUserSocialEvents(User user, FilterType filter, int offset, int limit, boolean loadOneMore, VisibilityType visibility) {
//		StringBuffer query = new StringBuffer();
//		query.append(
//			"SELECT DISTINCT activityNotification " +
//			"FROM SocialActivityNotification activityNotification " +
//			"LEFT JOIN activityNotification.user user " +
//			"LEFT JOIN activityNotification.subViews subView " +
//			"LEFT JOIN activityNotification.socialActivity socialActivity " +
//			"WHERE user = :user " +
//				"AND activityNotification.hidden = :hidden " +
//				"AND socialActivity.deleted = :deleted " +
//				"AND socialActivity.action NOT IN ('Comment') " +
//				"AND subView.type = :subViewType "
//		);
//		
//		if (visibility != null) {
//			query.append(
//				"AND socialActivity.visibility = :visibility "
//			);
//		}
//		
//		switch (filter) {
//			case ALL:
//				query.append("AND socialActivity.class != TwitterPostSocialActivity ");
//				break;
//			case MY_ACTIVITIES:
//				query.append(
//					"AND socialActivity.maker = :user " +
//					"AND socialActivity.class != TwitterPostSocialActivity ");
//				break;
//			case MY_NETWORK:
//				query.append(
//					"AND socialActivity.maker != :user " +
//					"AND NOT EXISTS (FROM TwitterPost post " +
//								"WHERE socialActivity.postObject = post) " +
//					"AND socialActivity.class != TwitterPostSocialActivity "
//				);
//				break;
//			case TWITTER:
//				query.append("AND socialActivity.class = TwitterPostSocialActivity ");
//				break;
//			case GOALS:
//				query.append("AND exists (FROM LearningGoal lg " +
//						"					WHERE socialActivity.nodeObject = lg) " +
//						"	AND socialActivity.maker = :user ");
//				break;
//			default:
//				break;
//		}
//		
//		query.append(
//			"ORDER BY socialActivity.lastAction desc"
//		);
//		Query q =  persistence.currentManager().createQuery(query.toString())
//			.setEntity("user", user)
//			.setBoolean("hidden", false)
//			.setBoolean("deleted", false)
//			.setString("subViewType", SocialStreamSubViewType.STATUS_WALL.name())
//			.setParameterList("excludedEvents", new EventType[]{EventType.Comment});
//		
//		if (visibility != null) {
//			q.setString("visibility", visibility.name());
//		}
//		
//		if (offset >= 0) {
//			q.setFirstResult(offset);
//		}
//		if (limit > 0) {
//			q.setMaxResults(loadOneMore ? limit+1 : limit);
//		}
//		
//		@SuppressWarnings("unchecked")
//		List<SocialActivityNotification> activityNotifications =  q.list();
//		return activityNotifications;
//	}
	
	
	/**
	 * Retrieves {@link SocialActivity} instances for a given user and their filter. Method will return limit+1 number of instances if available; that is 
	 * user do determine whether there are more instances to load. 
	 * 
	 * @version 0.5
	 */
	@Override
	@Transactional (readOnly = true)
	public List<SocialActivityData> getSocialActivities(long user, Filter filter, int offset, int limit) {
		switch (filter.getFilterType()) {
			case MY_ACTIVITIES:
				return getUserSocialActivities(user, offset, limit, null);
			case MY_NETWORK:
				return getMyNetworkSocialActivities(user, offset, limit);
			case TWITTER:
				return getTwitterSocialActivities(user, offset, limit);
			case ALL_PROSOLO:
				return getAllProSoloSocialActivities(user, offset, limit);
			case ALL:
				return getAllSocialActivities(user, offset, limit);
			case COURSE:
				return getCourseSocialActivities(user, offset, limit, ((CourseFilter) filter).getCourseId());
			default:
				return new ArrayList<SocialActivityData>();
		}
	}
	
	private List<SocialActivityData> getUserSocialActivities(long user, int offset, int limit, VisibilityType visibility) {
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +
				
				"WHERE sa.maker = :maker \n" +		// user is maker of a social activity
				
				((visibility != null) ? 
						"	AND sa.visibility = '"+visibility+"' \n" : "") +
						
				"	AND sa.deleted = false \n" +
				"	AND (config.id IS NULL OR " +
				"		(config.user = :maker AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
				.setLong("maker", user)
				.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
				.setInteger("offset", offset)
				.setLong("logged_user", user)
				.setString("liked_annotation_type", AnnotationType.Like.name())
				.setString("disliked_annotation_type", AnnotationType.Dislike.name())
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		for (SocialActivityData object : result) {
			System.out.println(object);
		}
		
		return result;
	}
	
	@Override
	public List<SocialActivityData> getUserPublicSocialEvents(long user, int offset, int limit) {
		return getUserSocialActivities(user, offset, limit, VisibilityType.PUBLIC);
	}
	
	private List<SocialActivityData> getMyNetworkSocialActivities(long user, int offset, int limit) {
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +
				
				"WHERE sa.maker IN ( \n" +		// user follows maker of a social activity 
				"					SELECT followed_entity.followed_user \n" +
				"					FROM followed_entity \n" +
				"					INNER JOIN user ON followed_entity.user = user.id \n" +
				"					WHERE user.id = :userId \n" +
				"				) \n" +
				"	AND sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				"	AND sa.deleted = false \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
				.setLong("userId", user)
				.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
				.setInteger("offset", offset)
				.setLong("logged_user", user)
				.setString("liked_annotation_type", AnnotationType.Like.name())
				.setString("disliked_annotation_type", AnnotationType.Dislike.name())
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		return result;
	}
	
	private List<SocialActivityData> getTwitterSocialActivities(long user, int offset, int limit) {
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN social_activity_hashtags AS sa_tag \n" +
				"		ON sa.id = sa_tag.social_activity \n" +
				"	LEFT JOIN tag AS tag \n" +
				"		ON sa_tag.hashtags = tag.id \n" +
				"	LEFT JOIN user_topic_preference_preferred_hashtags_tag AS pref_tag \n" +
				"		ON tag.id = pref_tag.preferred_hashtags \n" +
				"	LEFT JOIN user_preference AS pref \n" +
				"		ON pref_tag.user_preference = pref.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +

				"WHERE sa.dtype = 'TwitterPostSocialActivity' \n" +		// it is social activity of type TwitterPostSocialActivity
				"	AND sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				"	AND sa.deleted = false \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND pref.dtype = 'TopicPreference' \n" +
				"	AND pref.user = :userId \n" +		// user follows Twitter hashtag that is contained in a social activity
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
				.setLong("userId", user)
				.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
				.setInteger("offset", offset)
				.setLong("logged_user", user)
				.setString("liked_annotation_type", AnnotationType.Like.name())
				.setString("disliked_annotation_type", AnnotationType.Dislike.name())
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		return result;
	}
	
	private List<SocialActivityData> getAllProSoloSocialActivities(long user, int offset, int limit) {
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +
						
				"WHERE sa.dtype != 'TwitterPostSocialActivity' \n" +		// exclude social activities of type TwitterPostSocialActivity
				"	AND sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				"	AND sa.deleted = false \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
				.setLong("userId", user)
				.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
				.setInteger("offset", offset)
				.setLong("logged_user", user)
				.setString("liked_annotation_type", AnnotationType.Like.name())
				.setString("disliked_annotation_type", AnnotationType.Dislike.name())
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		return result;
	}

	private List<SocialActivityData> getAllSocialActivities(long user, int offset, int limit) {
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +
					
				"WHERE sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				"	AND sa.deleted = false \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
				.setLong("userId", user)
				.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
				.setInteger("offset", offset)
				.setLong("logged_user", user)
				.setString("liked_annotation_type", AnnotationType.Like.name())
				.setString("disliked_annotation_type", AnnotationType.Dislike.name())
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		return result;
	}
	
	private List<SocialActivityData> getCourseSocialActivities(long user, int offset, int limit, long courseId) {
		List<Long> courseRelatedResources = getResourcesRelatedToCourse(courseId);
		
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +
				"	LEFT JOIN social_activity_hashtags AS sa_hashtags \n" +
				"		ON sa_hashtags.social_activity = sa.id \n" +
				"	LEFT JOIN tag AS tag \n" +
				"		ON sa_hashtags.hashtags = tag.id \n" +

				"WHERE sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				"	AND sa.deleted = false \n" +	// exclude social activities marked as deleted
				"	AND (sa.node_object IN (:relatedResources) OR \n" +
				"			sa.node_target IN (:relatedResources) OR \n" +
				"			sa.goal_target IN (:relatedResources) OR " +
				"			sa.course_enrollment_object IN (SELECT course_enrollment.id \n" +
				"											FROM course_enrollment \n" +
				"											WHERE course_enrollment.course = :courseId) OR \n" +
				"			tag.id IN (SELECT course_tags.tags \n" +
				"						FROM course_tags \n" +
				"						WHERE course_tags.course = :courseId)) \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = persistence.currentManager().createSQLQuery(query)
			.setLong("userId", user)
			.setLong("courseId", courseId)
			.setParameterList("relatedResources", courseRelatedResources)
			.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
			.setInteger("offset", offset)
			.setLong("logged_user", user)
			.setString("liked_annotation_type", AnnotationType.Like.name())
			.setString("disliked_annotation_type", AnnotationType.Dislike.name())
			.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
			.list();
		
		return result;
	}
	
	@Override
	public List<SocialActivityData> getLearningGoalSocialActivities(long user, int offset, int limit, long goalId, long filterMaker) {
		List<BigInteger> goalRelatedResources = getResourcesRelatedToLearnignGoal(goalId);
		
		if (goalRelatedResources.isEmpty()) {
			return new ArrayList<SocialActivityData>();
		}
		
		String query = 
				getSelectPart() +
				"FROM social_activity sa \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"	LEFT JOIN node AS node_object \n" +
				"		ON sa.node_object = node_object.id \n" +
				"	LEFT JOIN post AS post_object \n" +
				"		ON sa.post_object = post_object.id \n" +
				"	LEFT JOIN node AS node_target \n" +
				"		ON sa.node_target = node_target.id \n" +
				"	LEFT JOIN node AS goal_target \n" +
				"		ON sa.goal_target = goal_target.id \n" +
				"	LEFT JOIN user AS sa_maker \n" +
				"		ON sa.maker = sa_maker.id \n" +
				"	LEFT JOIN course_enrollment AS course_enrollment \n" +
				"		ON sa.course_enrollment_object = course_enrollment.id \n" +
				"	LEFT JOIN course AS course \n" +
				"		ON course_enrollment.course = course.id \n" +
				"	LEFT JOIN rich_content AS rich_content \n" +
				"		ON sa.rich_content = rich_content.id \n" +
				"	LEFT JOIN annotation AS annotation \n" +
				"		ON annotation.social_activity = sa.id \n" +

				"WHERE sa.visibility != 'PRIVATE' \n" + 	// exclude private social activities
				
				((filterMaker > 0) ? "AND sa.maker = :maker \n" : "") +		// filterUser, if set, is the maker of a social activity
				
				"	AND sa.deleted = false \n" +	// exclude social activities marked as deleted
				"	AND (sa.node_object IN (:relatedResources) OR \n" +
				"			sa.node_target IN (:relatedResources) OR \n" +
				"			sa.goal_target IN (:relatedResources)) \n" +
				"	AND (config.id IS NULL OR \n" +
				"		(config.user = :userId AND config.hidden = 'F')) \n" +
				"	AND (annotation.maker IS NULL OR " +
				"			annotation.maker = :logged_user) \n" +
				"	AND sa.action NOT IN ('Comment') " +
				"ORDER BY sa.last_action DESC \n" +
				"LIMIT :limit \n" +
				"OFFSET :offset";
		
		Query q = persistence.currentManager().createSQLQuery(query)
			.setLong("userId", user)
			.setParameterList("relatedResources", goalRelatedResources)
			.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
			.setInteger("offset", offset)
			.setLong("logged_user", user)
			.setString("liked_annotation_type", AnnotationType.Like.name())
			.setString("disliked_annotation_type", AnnotationType.Dislike.name());
		
		if (filterMaker > 0) {
			q.setLong("maker", filterMaker);
		}
		
		@SuppressWarnings("unchecked")
		List<SocialActivityData> result = q
				.setResultTransformer(SocialActivityDataResultTransformer.getConstructor())
				.list();
		
		return result;
	}
	
	/**
	 * @return
	 *
	 * @version 0.5
	 */
	private String getSelectPart() {
		return 
				"SELECT DISTINCT " + 
				"sa.id AS sa_id, " +
				"sa.dtype AS sa_dtype, " +
				"sa.created AS sa_created, " +
				"sa.last_action AS sa_lastAction, " +
				"sa.updated AS sa_updated, " +
				"sa.maker AS sa_makerId, " +
				"sa_maker.name AS sa_maker_name, " +
				"sa_maker.lastname AS sa_maker_lastname, " +
				"sa_maker.avatar_url AS sa_maker_avatar_url, " +
				"sa_maker.position AS sa_maker_position, " +
				"sa_maker.profile_url AS sa_maker_profile_url, " +
				"sa.post_object AS sa_post_object, " +
				"post_object.dtype AS post_object_dtype, " +
				"sa.node_object AS sa_node_object, " +
				"node_object.dtype AS node_object_dtype, " +
				"node_object.title AS node_object_title, " +
				"node_object.description AS node_object_description, " +
				"sa.course_enrollment_object AS sa_course_enrollment_object, " +
				"course.id AS course_id, " +
				"course.title AS course_title, " +
				"course.description AS course_description, " +
				"sa.user_object AS sa_user_object, " +
				"sa.action AS sa_action, " +
				"sa.node_target AS sa_node_target, " +
				"node_target.dtype AS node_target_dtype, " +
				"node_target.title AS node_target_title, " +
				"sa.goal_target AS sa_goal_target, " +
				"goal_target.title AS goal_target_title, " +
				"sa.user_target AS sa_user_target, " +
				"config.id AS config_id, " +
				"sa.comments_disabled AS sa_comments_disabled, " +
				"sa.text AS sa_text, " +
				"sa.like_count AS sa_like_count, " +
				"sa.dislike_count AS sa_dislike_count, " +
				"sa.share_count AS sa_share_count, " +
				"sa.visibility AS sa_visibility, " +
				"sa.twitter_poster_name AS twitter_poster_name, " +
				"sa.twitter_poster_nickname AS twitter_poster_nickname, " +
				"sa.twitter_poster_profile_url AS twitter_poster_profile_url, " +
				"sa.twitter_poster_avatar_url AS twitter_poster_avatar_url, " +
				"sa.twitter_post_url AS twitter_post_url, " +
				"sa.twitter_user_type AS twitter_user_type, " +
				"rich_content.title AS post_rich_content_title, " +
				"rich_content.description AS post_rich_content_description, " +
				"rich_content.content_type AS post_rich_content_content_type, " +
				"rich_content.image_url AS post_rich_content_image_url, " +
				"rich_content.link AS post_rich_content_link, " +
				"IF (annotation.annotation_type = :liked_annotation_type, true, false) AS liked, " +
				"IF (annotation.annotation_type = :disliked_annotation_type, true, false) AS disliked ";
//				"sa.rich_content AS sa_rich_content, " +
//				"sa.node AS sa_node, " +
//				"sa.social_activity AS sa_social_activity, " +
//				"sa.name AS sa_name, " +
//				"sa.nickname AS sa_nickname, " +
//				"sa.post_link AS sa_post_link, " +
//				"sa.profile_url AS sa_profile_url, " +
//				"sa.service_type AS sa_service_type, " +
//				"sa.user_type AS sa_user_type, " +
//				"sa.dtype AS sa_dtype " +
	}


	private List<BigInteger> getResourcesRelatedToLearnignGoal(long goalId) {
		String queryRelatedResources = 
				"SELECT DISTINCT targetGoal.id \n" + 
				"FROM node AS targetGoal \n" +
				"WHERE targetGoal.learning_goal = :goalId \n" +
				"	AND targetGoal.id IS NOT NULL \n" +
				
				"UNION \n" +
				
				"SELECT DISTINCT targetComp1.target_competences \n" + 
				"FROM node AS targetGoal1 \n" +
				"LEFT JOIN user_learning_goal_target_competence AS targetComp1 \n" +
				"	ON targetGoal1.id = targetComp1.node \n" +
				"WHERE targetGoal1.learning_goal = :goalId " +
				"	AND targetComp1.target_competences IS NOT NULL \n" +
				
				"UNION \n" +
		
				"SELECT DISTINCT targetActivity2.target_activities \n" + 
				"FROM node AS targetGoal2 \n" +
				"LEFT JOIN user_learning_goal_target_competence AS targetComp2 \n" +
				"	ON targetGoal2.id = targetComp2.node \n" +
				"LEFT JOIN node_target_activities AS targetActivity2 \n" +
				"	ON targetComp2.target_competences = targetActivity2.node \n" +
				"WHERE targetGoal2.learning_goal = :goalId \n" +
				"	AND targetActivity2.target_activities IS NOT NULL";
		
		@SuppressWarnings("unchecked")
		List<BigInteger> relatedResources = persistence.currentManager().createSQLQuery(queryRelatedResources)
			.setLong("goalId", goalId)
			.list();
		
		relatedResources.add(new BigInteger(String.valueOf(goalId)));
		
		return relatedResources;
	}
	
	private List<Long> getResourcesRelatedToCourse(long courseId) {
		String queryRelatedResources = 
				"SELECT DISTINCT targetGoal.id \n" + 
				"FROM course_enrollment AS course_enrollment \n" +
				"LEFT JOIN node AS targetGoal \n" +
				"	ON course_enrollment.target_goal = targetGoal.id \n" +
				"WHERE course_enrollment.course = :courseId \n" +
				"	AND targetGoal.id IS NOT NULL \n" +
						
				"UNION \n" +
				
				"SELECT DISTINCT targetComp1.target_competences \n" +
				"FROM course_enrollment AS course_enrollment1 \n" +
				"LEFT JOIN node AS targetGoal1 \n" +
				"	ON course_enrollment1.target_goal = targetGoal1.id \n" +
				"LEFT JOIN user_learning_goal_target_competence AS targetComp1 \n" +
				"	ON targetGoal1.id = targetComp1.node \n" +
				"WHERE course_enrollment1.course = :courseId \n" +
				"	AND targetComp1.target_competences IS NOT NULL \n" +
				
				"UNION \n" +
				
				"SELECT DISTINCT targetActivity2.target_activities \n" + 
				"FROM course_enrollment AS course_enrollment2 \n" +
				"LEFT JOIN node AS targetGoal2 \n" +
				"	ON course_enrollment2.target_goal = targetGoal2.id \n" +
				"LEFT JOIN user_learning_goal_target_competence AS targetComp2 \n" +
				"	ON targetGoal2.id = targetComp2.node \n" +
				"LEFT JOIN node_target_activities AS targetActivity2 \n" +
				"	ON targetComp2.target_competences = targetActivity2.node \n" +
				"WHERE course_enrollment2.course = :courseId \n" +
				"	AND targetActivity2.target_activities IS NOT NULL";
		
		@SuppressWarnings("unchecked")
		List<Long> relatedResources = persistence.currentManager().createSQLQuery(queryRelatedResources)
			.setLong("courseId", courseId)
			.list();
		
		return relatedResources;
	}
	
//	@Override
//	@Transactional (readOnly = true)
//	public SocialActivityNotification getSocialActivityNotification(SocialActivity socialActivity, User user) {
//		String query = 
//			"SELECT activityNotification " +
//			"FROM SocialActivityNotification activityNotification " +
//			"LEFT JOIN activityNotification.socialActivity socialActivity " +
//			"LEFT JOIN activityNotification.user user " +
//			"WHERE socialActivity = :socialActivity " +
//				"AND user = :user";
//		
//		@SuppressWarnings("unchecked")
//		List<SocialActivityNotification> result = getPersistence().currentManager().createQuery(query)
//				.setEntity("socialActivity", socialActivity)
//				.setEntity("user", user)
//				.list();
//		
//		
//		if (result != null && !result.isEmpty()) {
//			return result.iterator().next();
//		}
//		return null;
//	}
	
	@Override
	@Transactional (readOnly = true)
	public List<User> getUsersSubscribedToSocialActivity(SocialActivity socialActivity) {
		return getUsersSubscribedToSocialActivity(socialActivity, getPersistence().currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	// TODO Nikola: needs to be rewritten
	public List<User> getUsersSubscribedToSocialActivity(SocialActivity socialActivity, Session session) {
		String query = 
			"SELECT DISTINCT user " +
			"FROM SocialActivityNotification activityNotification " +
			"LEFT JOIN activityNotification.user user " +
			"LEFT JOIN activityNotification.socialActivity socialActivity " +
			"WHERE socialActivity = :socialActivity ";
		
		@SuppressWarnings("unchecked")
		List<User> result = session.createQuery(query)
				.setEntity("socialActivity", socialActivity)
				.list();
		
		
		if (result != null) {
			return result;
		}
		return new ArrayList<User>();
	}
	
	@Override
	@Transactional (readOnly = true)
	// TODO Nikola
	public Date getLastActivityForGoal(User user, TargetLearningGoal targetGoal, Session session) {
		String query =
			"SELECT DISTINCT socialActivity.dateCreated " +
			"FROM SocialActivityNotification activityNotification " +
			"LEFT JOIN activityNotification.user user " +
			"LEFT JOIN activityNotification.subViews subView " +
			"LEFT JOIN activityNotification.socialActivity socialActivity " +
			"WHERE user = :user " +
				"AND socialActivity.deleted = :deleted " +
				"AND socialActivity.action NOT IN (:excludedEvents) " +
				"AND subView.type = :subViewType " +
				"AND :targetGoal IN elements(subView.relatedResources)  " +
				"AND socialActivity.maker = :user " +
			"ORDER BY socialActivity.dateCreated desc ";

		Date date = (Date) session.createQuery(query.toString())
				.setEntity("user", user)
				.setBoolean("deleted", false)
				.setParameterList("excludedEvents", new EventType[]{EventType.Comment})
				.setString("subViewType", SocialStreamSubViewType.GOAL_WALL.name())
				.setEntity("targetGoal", targetGoal)
				.setMaxResults(1)
				.uniqueResult();
		
		return date;
	}
	
	@Override
	@Transactional (readOnly = false)
	public ArrayCount searchPublicSocialActivitiesWithHashtag(
			String searchQuery, int offset, int limit, boolean loadOneMore, boolean calculateCount, boolean excludeTweets) {
		
		Tag hashtag = tagManager.getOrCreateTag(searchQuery);
		
		if (hashtag == null) {
			return new ArrayCount();
		}
		
		return getPublicSocialActivities(offset, limit, FilterType.ALL, loadOneMore, hashtag, calculateCount, excludeTweets);
	}

	@Override
	@Transactional (readOnly = true)
	@Deprecated
	// TODO Nikola
	public ArrayCount getPublicSocialActivities(int offset, int limit, FilterType filter,
			boolean loadOneMore, Tag hashtag, boolean calculateCount, boolean excludeTweets) {
		
		ArrayCount arrayCount = new ArrayCount();
		
		StringBuffer query = new StringBuffer();
		query.append( 
			"SELECT DISTINCT socialActivity " +
			"FROM SocialActivity socialActivity " +
			"WHERE socialActivity.deleted = :deleted " +
				"AND socialActivity.visibility = :visibility " +
				"AND socialActivity.maker != NULL " +
				"AND socialActivity.action NOT IN (:excludedEvents) "
		);
		
		if (hashtag != null) {
			query.append( 
				"AND :hashtag IN elements(socialActivity.hashtags) "
			);
		}
		
		if (filter.equals(FilterType.ALL_PROSOLO)) {
			query.append( 
				"AND socialActivity.class != TwitterPostSocialActivity " +
				"AND NOT EXISTS (FROM TwitterPost post " +
					"WHERE socialActivity.postObject = post) "
			);
		}
		
		if (excludeTweets) {
			query.append( 
				"AND socialActivity.class != TwitterPostSocialActivity "
			);
		}
		
		query.append( 
			"ORDER BY socialActivity.lastAction desc "
		);

		Query q = persistence.currentManager().createQuery(query.toString())
			.setBoolean("deleted", false)
			.setString("visibility", VisibilityType.PUBLIC.name())
			.setParameterList("excludedEvents", new EventType[]{EventType.Comment});
		
		if (hashtag != null) {
			q.setEntity("hashtag", hashtag);
		}
		
		@SuppressWarnings("unchecked")
		List<SocialActivity> activityNotifications =  q
			.setFirstResult(offset)
			.setMaxResults(loadOneMore ? limit+1 : limit)
			.list();
		
		arrayCount.array = activityNotifications;
		
		if (calculateCount) {
			StringBuffer query1 = new StringBuffer();
			
			query1.append(
				"SELECT COUNT(DISTINCT socialActivity) " +
				"FROM SocialActivity socialActivity " +
				"WHERE socialActivity.deleted = :deleted " +
					"AND socialActivity.visibility = :visibility " +
					"AND socialActivity.maker != NULL " +
					"AND socialActivity.action NOT IN (:excludedEvents) "
			);
			
			if (hashtag != null) {
				query1.append(
					"AND :hashtag IN elements(socialActivity.hashtags) "
				);
			}
			
			Query q1 = persistence.currentManager().createQuery(query1.toString())
				.setBoolean("deleted", false)
				.setString("visibility", VisibilityType.PUBLIC.name())
				.setParameterList("excludedEvents", new EventType[]{EventType.Comment});
			
			if (hashtag != null) {
				q1.setEntity("hashtag", hashtag);
			}
			
			arrayCount.count =  (Long) q1.uniqueResult();
		}
		
		return arrayCount;
	}
	
	@Override
	@Transactional (readOnly = true)
	public SocialActivity getSocialActivityOfPost(Post originalPost) {
		return getSocialActivityOfPost(originalPost, getPersistence().currentManager());
	}
		
	@Override
	@Transactional (readOnly = true)
	public SocialActivity getSocialActivityOfPost(Post originalPost, Session session) {
		String query = 
			"SELECT socialActivity " +
			"FROM PostSocialActivity socialActivity " +
			"WHERE socialActivity.postObject = :post ";
		
		@SuppressWarnings("unchecked")
		List<SocialActivity> result = session.createQuery(query)
				.setEntity("post", originalPost)
				.list();
		
		if (result != null && !result.isEmpty()) {
			return result.iterator().next();
		}
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public Set<Long> getUsersInMyNetwork(long userId){
		Set<Long> myNetwork = new TreeSet<Long>();
		
		String query = 
			"SELECT DISTINCT followedUser.id " +  
			"FROM FollowedUserEntity fUser " + 
			"LEFT JOIN fUser.followedUser followedUser "+				
			"WHERE fUser.user = :userId " ;
		
		@SuppressWarnings("unchecked")
		List<Long> users = persistence.currentManager().createQuery(query)
			.setLong("userId", userId)
			.list();
		
		if (users != null && !users.isEmpty()) {
			myNetwork.addAll(users);			
		}
		return myNetwork;
	}
	
	public class ArrayCount {
		private List<?> array;
		private long count;

		public List<?> getArray() {
			return array;
		}

		public void setArray(List<?> array) {
			this.array = array;
		}

		public long getCount() {
			return count;
		}

		public void setCount(long count) {
			this.count = count;
		}
	}
}
