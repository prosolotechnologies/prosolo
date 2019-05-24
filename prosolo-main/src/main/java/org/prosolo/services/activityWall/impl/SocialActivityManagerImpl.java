/**
 * 
 */
package org.prosolo.services.activityWall.impl;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.activitywall.*;
import org.prosolo.common.domainmodel.annotation.AnnotatedResource;
import org.prosolo.common.domainmodel.annotation.AnnotationType;
import org.prosolo.common.domainmodel.comment.Comment1;
import org.prosolo.common.domainmodel.content.RichContent1;
import org.prosolo.common.domainmodel.credential.CommentedResourceType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.activityWall.SocialActivityManager;
import org.prosolo.services.activityWall.factory.RichContentDataFactory;
import org.prosolo.services.activityWall.factory.SocialActivityDataFactory;
import org.prosolo.services.activityWall.filters.Filter;
import org.prosolo.services.activityWall.impl.data.SocialActivityData1;
import org.prosolo.services.annotation.Annotation1Manager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.CommentManager;
import org.prosolo.services.interaction.data.CommentData;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service("org.prosolo.services.activitywall.SocialActivityManager")
public class SocialActivityManagerImpl extends AbstractManagerImpl implements SocialActivityManager {

	private static final long serialVersionUID = 8656308195928025188L;
	
	private static Logger logger = Logger.getLogger(SocialActivityManagerImpl.class);

	@Inject private SocialActivityDataFactory socialActivityFactory;
	@Inject private Annotation1Manager annotationManager;
	@Inject private EventFactory eventFactory;
	@Inject private RichContentDataFactory richContentFactory;
	@Inject private ResourceFactory resourceFactory;
	@Inject private CommentManager commentManager;
	@Inject private UrlIdEncoder idEncoder;
	@Inject private ThreadPoolTaskExecutor taskExecutor;
	@Inject private CredentialManager credentialManager;
	@Inject private SocialActivityManager self;
	
	/**
	 * Retrieves {@link SocialActivity1} instances for a given user and their filter. Method will return limit+1 number of instances if available; that is 
	 * user to determine whether there are more instances to load. 
	 * 
	 * @version 0.5
	 */
	@Override
	@Transactional (readOnly = true)
	public List<SocialActivityData1> getSocialActivities(long userId, Filter filter, int offset, 
			int limit, long previousId, Date previousDate, Locale locale) throws DbConnectionException {
		try {
			switch (filter.getFilterType()) {
				case MY_ACTIVITIES:
					return getUserSocialActivities(userId, offset, limit, previousId, previousDate, locale);
				case MY_NETWORK:
					return getMyNetworkSocialActivities(userId, offset, limit, previousId, previousDate, locale);
				case TWITTER:
					return getTwitterSocialActivities(userId, offset, limit, previousId, previousDate, locale);
				case ALL_PROSOLO:
					return getAllProSoloSocialActivities(userId, offset, limit, previousId, previousDate, locale);
				case ALL:
					return getAllSocialActivities(userId, offset, limit, previousId, previousDate, locale);
				default:
					return new ArrayList<>();
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activities");
		}
	}
	
	private List<SocialActivityData1> getUserSocialActivities(long user, int offset, int limit,
			long previousId, Date previousDate, Locale locale) {
		String specificCondition = "AND sa.actor = :userId \n ";
		Query q = createQueryWithCommonParametersSet(user, limit, offset, specificCondition, previousId, 
				previousDate, false, false, locale);
		if (q != null) {
			@SuppressWarnings("unchecked")
			List<SocialActivityData1> res = q.list();
			return res;
		}
		return new ArrayList<>();
	}
		
	private List<SocialActivityData1> getMyNetworkSocialActivities(long user, int offset, int limit,
			long previousId, Date previousDate, Locale locale) {
		String specificCondition = "AND sa.actor IN ( \n" + 
				"					SELECT fe.followed_user \n" +
				"					FROM followed_entity AS fe \n" +
				"					WHERE fe.user = :userId \n" +
				"				) \n";
		Query q = createQueryWithCommonParametersSet(user, limit, offset, specificCondition, previousId, 
				previousDate, false, false, locale);
		if (q != null) {
			@SuppressWarnings("unchecked")
			List<SocialActivityData1> res = q.list();
			return res;
		}
		return new ArrayList<>();
	}
	
	private List<SocialActivityData1> getTwitterSocialActivities(long user, int offset, int limit,
			long previousId, Date previousDate, Locale locale) {
		String specificCondition = "AND sa.dtype = :twitterPostDType \n ";
		Query q = createQueryWithCommonParametersSet(user, limit, offset, specificCondition, previousId,
				previousDate, false, false, locale);
		if (q != null) {
			q.setParameter("twitterPostDType", TwitterPostSocialActivity1.class.getSimpleName());
			@SuppressWarnings("unchecked")
			List<SocialActivityData1> res = q.list();
			return res;
		}
		return new ArrayList<>();
	}
	
	private List<SocialActivityData1> getAllProSoloSocialActivities(long user, int offset, int limit,
			long previousId, Date previousDate, Locale locale) {
		String specificCondition = "AND sa.dtype != :twitterPostDType \n ";
		Query q = createQueryWithCommonParametersSet(user, limit, offset, specificCondition, previousId,
				previousDate, false, false, locale);
		if (q != null) {
			q.setParameter("twitterPostDType", TwitterPostSocialActivity1.class.getSimpleName());
			@SuppressWarnings("unchecked")
			List<SocialActivityData1> res = q.list();
			return res;
		}
		return new ArrayList<>();
	}

	private List<SocialActivityData1> getAllSocialActivities(long user, int offset, int limit,
			long previousId, Date previousDate, Locale locale) {
		Query q = createQueryWithCommonParametersSet(user, limit, offset, "", previousId, 
				previousDate, false, false, locale);
		if (q != null) {
			@SuppressWarnings("unchecked")
			List<SocialActivityData1> res = q.list();
			return res;
		}
		return new ArrayList<>();
	}
	
	private String getSelectPart() {
		return 
				"SELECT " + 
				"sa.id AS sa_id, " +
				"sa.dtype AS sa_dtype, " +
				"sa.created AS sa_created, " +
				"sa.last_action AS sa_lastAction, " +
				"sa.comments_disabled AS sa_comments_disabled, " +
				"sa.text AS sa_text, " +
				"sa.like_count AS sa_like_count, " +
				"sa.actor AS actorId, " +
				"actor.name AS actorName, " +
				"actor.lastname AS actorLastName, " +
				"actor.avatar_url AS actorAvatar, " +
				//twitter post social activity
				"sa.twitter_poster_name AS twitterName, " +
				"sa.twitter_poster_nickname AS twitterNick, " +
				"sa.twitter_poster_profile_url AS twitterProfileUrl, " +
				"sa.twitter_poster_avatar_url AS twitterPosterAvatar, " +
				"sa.twitter_post_url AS twitterPostUrl, " +
				"sa.twitter_user_type AS twitterUserType, " +
				//post social activity
				"sa.rich_content_title AS richContentTitle, " +
				"sa.rich_content_description AS richContentDescription, " +
				"sa.rich_content_content_type AS richContentType, " +
				"sa.rich_content_image_url AS richContentImageUrl, " +
				"sa.rich_content_link AS richContentLink, " +
				"sa.rich_content_embed_id AS richContentEmbedId, " +
				"sa.rich_content_image_size AS richContentImageSize, " +
				//post reshare 
				"sa.post_object AS postObjectId, " +
				"postObject.text AS postObjectText, " +
				"postObject.rich_content_title AS postObjectRichContentTitle, " +
				"postObject.rich_content_description AS postObjectRichContentDescription, " +
				"postObject.rich_content_content_type AS postObjectRichContentType, " +
				"postObject.rich_content_image_url AS postObjectRichContentImageUrl, " +
				"postObject.rich_content_link AS postObjectRichContentLink, " +
				"postObject.rich_content_embed_id AS postObjectRichContentEmbedId, " +
				"postObject.rich_content_image_size AS postObjectRichContentImageSize, " +
				"postObject.actor AS postObjectActorId, " +
				"postObjectActor.name AS postObjectActorName, " +
				"postObjectActor.lastname AS postObjectActorLastName, " +
				"postObjectActor.avatar_url AS postObjectActorAvatar, " +
				"postObject.created AS postObjectDateCreated, " +
				//credential enroll and credential complete social activity
				"sa.credential_object AS credObjectId, " +
				"credObject.title AS credObjectTitle, " +
				"credObject.duration AS credObjectDuration, " +
				"credObject.created_by AS credObjectActorId, " +
				"credObjectActor.name AS credObjectActorName, " +
				"credObjectActor.lastname AS credObjectActorLastname, " +
				"credObject.description AS credObjectDescription, " +
				"sa.parent_credential AS parentCredentialId, " +
				//comment social activity (competence and activity)
						//types of social activity not used currently
					//"sa.comment_object AS commentObjectId, " +
					//"commentObject.description AS commentObjectComment, " +
				//competence comment social activity
					//"sa.competence_target AS compTargetId, " +
					//"compTarget.title AS compTargetTitle, " +
				//activity comment social activity
					//"sa.activity_target AS actTargetId, " +
					//"actTarget.title AS actTargetTitle, " +
					//"compActivity.competence AS actTargetCompId, " +
					//"actTarget.dtype AS actTargetDType, " +
					//"actTarget.url_type AS actTargetUrlType, " +
				//activity complete
//					"tActObject.activity AS actObjectId, " +
//					"actObject.title AS actObjectTitle, " +
//					"actObject.duration AS actObjectDuration, " +
//					"actObject.type AS actObjectType, " +
//					"actObject.created_by AS actObjectActorId, " +
//					"actObjectActor.name AS actObjectActorName, " +
//					"actObjectActor.lastname AS actObjectActorLastname, " +
//					"actObject.description AS actObjectDescription, " +
//					"actObject.dtype AS actObjectDType, " +
//					"actObject.url_type AS actObjectUrlType, " +
//					"tComp.competence AS actObjectCompetenceId, " +
				//"tCred.credential AS actObjectCredentialId, " +
				//competence complete
				"tCompObject.competence AS compObjectId, " +
				"compObject.title AS compObjectTitle, " +
				"compObject.duration AS compObjectDuration, " +
				"compObject.type AS compObjectType, " +
				"compObject.created_by AS compObjectActorId, " +
				"compObjectActor.name AS compObjectActorName, " +
				"compObjectActor.lastname AS compObjectActorLastname, " +
				"compObject.description AS compObjectDescription, " +
				//unit welcome post
				"unit.welcome_message as unitWelcomeMessage, " +
				
				"annotation.id is NOT NULL AS liked, \n " +
				" (SELECT COUNT(comm.id) FROM comment1 comm \n" +
						" WHERE comm.commented_resource_id = sa.id \n" +
						" AND comm.resource_type = :commentResourceType \n " +
						" AND comm.parent_comment IS NULL) AS commentsNumber ";
	}
	
	private String getTablesString(String specificPartOfTheCondition, long previousId, Date previousDate,
			boolean queryById, boolean shouldReturnHidden, List<Long> credentialIds) {
		String q =
				"FROM social_activity1 sa \n" +
				"	LEFT JOIN user AS actor \n" +
				"		ON sa.actor = actor.id \n" +
				"	LEFT JOIN social_activity_config AS config \n" +
				"		ON config.social_activity = sa.id \n" +
				"       AND config.user = :userId " +
				//post reshare social activity
				"	LEFT JOIN social_activity1 AS postObject \n" +
				"		ON sa.dType = :postReshareDType \n" +
				"       AND sa.post_object = postObject.id \n " +
				"	LEFT JOIN user AS postObjectActor " +
				"       ON postObject.actor = postObjectActor.id \n " +
				//credential enroll and credential complete social activity
				"	LEFT JOIN credential1 AS credObject \n" +
				"		ON (sa.dType = :credEnrollDType \n " +
				"       OR sa.dType = :credCompleteDType) \n " +
				"		AND sa.credential_object = credObject.id \n " +
				"   LEFT JOIN user AS credObjectActor " +
				"       ON credObject.created_by = credObjectActor.id " +
				//competence complete social activity
				"	LEFT JOIN target_competence1 AS tCompObject \n" +
				"		ON sa.dType = :compCompleteDType \n " +
				"		AND sa.target_competence_object = tCompObject.id \n " +
				"   LEFT JOIN competence1 compObject " +
				"       ON tCompObject.competence = compObject.id " +
				"   LEFT JOIN user AS compObjectActor " +
				"       ON compObject.created_by = compObjectActor.id " +
				//comment social activity (competence and activity)
//				"	LEFT JOIN comment1 AS commentObject \n" +
//				"		ON (sa.dType = :competenceCommentDType " +
//				"       OR sa.dType = :activityCommentDType) \n " +
//				"       AND sa.comment_object = commentObject.id \n " +
				//competence comment social activity
//				"	LEFT JOIN competence1 AS compTarget \n" +
//				"		ON sa.dType = :competenceCommentDType \n" +
//				"       AND sa.competence_target = compTarget.id \n " +
				//activity comment social activity
//				"	LEFT JOIN activity1 AS actTarget \n" +
//				"		ON sa.dType = :activityCommentDType \n" +
//				"       AND sa.activity_target = actTarget.id \n " +
//				"   LEFT JOIN (competence_activity1 compActivity \n " +
//						"   INNER JOIN competence1 AS actTargetCompetence \n " +
//						"       ON compActivity.competence = actTargetCompetence.id) \n " +
//				"       ON actTarget.id = compActivity.activity \n " +
				//activity complete social activity
//				"	LEFT JOIN target_activity1 AS tActObject \n" +
//				"		ON sa.dType = :activityCompleteDType \n" +
//				"       AND sa.target_activity_object = tActObject.id \n " +
//				"   LEFT JOIN activity1 actObject " +
//				"       ON tActObject.activity = actObject.id " +
//				"   LEFT JOIN target_competence1 tComp " +
//				"       ON tActObject.target_competence = tComp.id " +
//				"   LEFT JOIN user AS actObjectActor " +
//				"       ON actObject.created_by = actObjectActor.id " +
				//unit welcome post
				" 	LEFT JOIN unit AS unit \n " +
						"ON sa.dType = :unitWelcomePostDType \n " +
						"AND sa.unit = unit.id \n " +

				"	LEFT JOIN annotation1 AS annotation \n" +
				"		ON annotation.annotated_resource_id = sa.id \n" +
				"       AND annotation.annotated_resource = :annotatedResource " +
				"		AND annotation.annotation_type = :annotationType " +
				"		AND annotation.maker = :userId " +
						
				"WHERE sa.deleted = :saDeleted \n" +
				//these social activity types are not used currently
				"AND sa.dType != :competenceCommentDType " +
				"AND sa.dType != :activityCommentDType " +
				"AND sa.dType != :activityCompleteDType ";
			
		if (!shouldReturnHidden) {
			q += " AND config.id IS NULL \n";
		}

		if (!queryById) {
			String commonCondition = "actor.id = :userId OR (sa.dType = :unitWelcomePostDType AND EXISTS " +
				"(SELECT urm.id FROM unit_role_membership urm INNER JOIN role r ON (urm.role = r.id) where urm.unit = unit.id AND urm.user = :userId AND r.title = :studentRoleTitle))";
			if (!credentialIds.isEmpty()) {
				q += "AND (" + commonCondition + " OR EXISTS " +
						"(SELECT cred.id from target_credential1 cred WHERE cred.user = actor.id AND cred.credential IN (:credentialIds))) ";
			} else {
				q += "AND " + commonCondition + " ";
			}
		}

		if (!queryById) {
			if(previousDate != null && previousId > 0) {
				q += "AND sa.last_action <= :date \n " +
					 "AND NOT (sa.last_action = :date AND sa.id >= :previousId) ";
			}
		}
		
		return q + specificPartOfTheCondition +
			(queryById ? "" 
					   : "ORDER BY sa.last_action DESC, sa.id DESC \n" +
						 "LIMIT :limit \n" +
						 "OFFSET :offset");
	}
	
	private Query createQueryWithCommonParametersSet(long userId, int limit, int offset,
			String specificCondition, long previousId, Date previousDate, boolean queryById, 
			boolean shouldReturnHidden, Locale locale) {
		List<Long> deliveriesUserIsLearning = null;
		if (!queryById) {
			deliveriesUserIsLearning = credentialManager.getIdsOfUncompletedDeliveries(userId);
		}
		String query = getSelectPart() + getTablesString(specificCondition, previousId, previousDate,
				queryById, shouldReturnHidden, deliveriesUserIsLearning);

		Query q = persistence.currentManager().createSQLQuery(query)
			.setLong("userId", userId)
			.setString("postReshareDType", PostReshareSocialActivity.class.getSimpleName())
			.setString("credEnrollDType", CredentialEnrollSocialActivity.class.getSimpleName())
			.setString("credCompleteDType", CredentialCompleteSocialActivity.class.getSimpleName())
			.setString("competenceCommentDType", CompetenceCommentSocialActivity.class.getSimpleName())
			.setString("activityCommentDType", ActivityCommentSocialActivity.class.getSimpleName())
			.setString("activityCompleteDType", ActivityCompleteSocialActivity.class.getSimpleName())
			.setString("compCompleteDType", CompetenceCompleteSocialActivity.class.getSimpleName())
			.setString("unitWelcomePostDType", UnitWelcomePostSocialActivity.class.getSimpleName())
			.setString("annotatedResource", AnnotatedResource.SocialActivity.name())
			.setString("annotationType", AnnotationType.Like.name())
			//.setBoolean("boolFalse", false)
			.setCharacter("saDeleted", 'F')
			.setString("commentResourceType", CommentedResourceType.SocialActivity.name());

		if (!queryById) {
			q.setString("studentRoleTitle", SystemRoleNames.USER);
			if (!deliveriesUserIsLearning.isEmpty()) {
				q.setParameterList("credentialIds", deliveriesUserIsLearning);
			}
		}

		q.setResultTransformer(new ResultTransformer() {
			private static final long serialVersionUID = 3421375509302043275L;

			@Override
			public Object transformTuple(Object[] tuple, String[] aliases) {
				return socialActivityFactory.getSocialActivityData(
						(BigInteger) tuple[0],
						(String) tuple[1],
						(Date) tuple[2],
						(Date) tuple[3],
						(Character) tuple[4],
						(String) tuple[5],
						(Integer) tuple[6],
						(BigInteger) tuple[7],
						(String) tuple[8],
						(String) tuple[9],
						(String) tuple[10],
						(String) tuple[11],
						(String) tuple[12],
						(String) tuple[13],
						(String) tuple[14],
						(String) tuple[15],
						(Integer) tuple[16],
						(String) tuple[17],
						(String) tuple[18],
						(String) tuple[19],
						(String) tuple[20],
						(String) tuple[21],
						(String) tuple[22],
						(String) tuple[23],
						(BigInteger) tuple[24],
						(String) tuple[25],
						(String) tuple[26],
						(String) tuple[27],
						(String) tuple[28],
						(String) tuple[29],
						(String) tuple[30],
						(String) tuple[31],
						(String) tuple[32],
						(BigInteger) tuple[33],
						(String) tuple[34],
						(String) tuple[35],
						(String) tuple[36],
						(Date) tuple[37],
						(BigInteger) tuple[38],
						(String) tuple[39],
						(BigInteger) tuple[40],
						(BigInteger) tuple[41],
						(String) tuple[42],
						(String) tuple[43],
						(String) tuple[44],
						(BigInteger) tuple[45],
//not used social activity types
//						(BigInteger) tuple[45],
//						(String) tuple[46],
//						(BigInteger) tuple[47],
//						(String) tuple[48],
//						(BigInteger) tuple[49],
//						(String) tuple[50],
//						(BigInteger) tuple[51],
//						(String) tuple[52],
//						(String) tuple [53],
//						(BigInteger) tuple[54],
//						(String) tuple[55],
//						(BigInteger) tuple[56],
//						(String) tuple[57],
//						(BigInteger) tuple[58],
//						(String) tuple[59],
//						(String) tuple[60],
//						(String) tuple[61],
//						(String) tuple[62],
//						(String) tuple[63],
//						(BigInteger) tuple[64],
						(BigInteger) tuple[46],
						(String) tuple[47],
						(BigInteger) tuple[48],
						(String) tuple[49],
						(BigInteger) tuple[50],
						(String) tuple[51],
						(String) tuple[52],
						(String) tuple[53],
						(String) tuple[54],
						1 == Integer.valueOf(tuple[55].toString()),	//Sometimes Integer is returned and sometimes BigInteger
						(BigInteger) tuple[56],
						locale);
			}

			@SuppressWarnings("rawtypes")
			@Override
			public List transformList(List collection) {return collection;}
		});
		
		if(!queryById && previousDate != null && previousId > 0) {
			q.setTimestamp("date", previousDate);
			q.setLong("previousId", previousId);
		}
		if(!queryById) {
			q.setInteger("limit", limit + 1) // +1 because it always loads one extra in order to inform whether there are more to load
			 .setInteger("offset", offset);
		}
		
		return q;
	}
	
	@Override
	@Transactional(readOnly = false)
	public SocialActivity1 saveNewSocialActivity(SocialActivity1 socialActivity, Session session) 
			throws DbConnectionException {
		try {
			session.saveOrUpdate(socialActivity);
			return socialActivity;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving social activity");
		}
	}

	@Override
	@Transactional
	public void saveUnitWelcomePostSocialActivityIfNotExists(long unitId, Session session) throws DbConnectionException {
		try {
			Optional<UnitWelcomePostSocialActivity> sa = getUnitWelcomePostSocialActivityIfExists(unitId, session);
			if (!sa.isPresent()) {
				UnitWelcomePostSocialActivity unitWelcomePostSocialActivity = new UnitWelcomePostSocialActivity();
				Date now = new Date();
				unitWelcomePostSocialActivity.setDateCreated(now);
				unitWelcomePostSocialActivity.setLastAction(now);
				unitWelcomePostSocialActivity.setUnit((Unit) session.load(Unit.class, unitId));
				saveEntity(unitWelcomePostSocialActivity, session);
			}
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving the unit welcome post social activity");
		}
	}

	@Override
	@Transactional
	public void deleteUnitWelcomePostSocialActivityIfExists(long unitId, Session session) throws DbConnectionException {
		try {
			Optional<UnitWelcomePostSocialActivity> sa = getUnitWelcomePostSocialActivityIfExists(unitId, session);
			if (sa.isPresent()) {
				session.delete(sa.get());
			}
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error deleting the unit welcome post social activity");
		}
	}

	private Optional<UnitWelcomePostSocialActivity> getUnitWelcomePostSocialActivityIfExists(long unitId, Session session) throws DbConnectionException {
		try {
			String query = "SELECT sa " +
					"FROM UnitWelcomePostSocialActivity sa " +
					"WHERE sa.unit.id = :unitId";
			UnitWelcomePostSocialActivity sa = (UnitWelcomePostSocialActivity) session.createQuery(query)
					.setLong("unitId", unitId)
					.uniqueResult();

			return Optional.ofNullable(sa);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getPostSocialActivity(long id, Session session) throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM PostSocialActivity1 sa " +
						   "INNER JOIN fetch sa.actor " +
						   "WHERE sa.id = :id";
			PostSocialActivity1 sa = (PostSocialActivity1) session.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}

	private SocialActivity1 getTwitterPostSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM TwitterPostSocialActivity1 sa " +
						   "INNER JOIN fetch sa.actor " +
						   "WHERE sa.id = :id";
			TwitterPostSocialActivity1 sa = (TwitterPostSocialActivity1) session.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getPostReshareSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM PostReshareSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.postObject obj " +
						   "INNER JOIN fetch obj.actor " +
						   "WHERE sa.id = :id";
			PostReshareSocialActivity sa = (PostReshareSocialActivity) session.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getCredentialEnrollSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM CredentialEnrollSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.credentialObject obj " +
						   "INNER JOIN fetch obj.createdBy " +
						   "WHERE sa.id = :id";
			CredentialEnrollSocialActivity sa = (CredentialEnrollSocialActivity) session.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getCompetenceCommentSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM CompetenceCommentSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.commentObject obj " +
						   "INNER JOIN fetch sa.competenceTarget target " +
						   "WHERE sa.id = :id";
			CompetenceCommentSocialActivity sa = (CompetenceCommentSocialActivity) session
					.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getActivityCommentSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM ActivityCommentSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.commentObject obj " +
						   "INNER JOIN fetch sa.activityTarget target " +
						   "WHERE sa.id = :id";
			ActivityCommentSocialActivity sa = (ActivityCommentSocialActivity) session
					.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getCredentialCompleteSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM CredentialCompleteSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.credentialObject obj " +
						   "INNER JOIN fetch obj.createdBy " +
						   "WHERE sa.id = :id";
			CredentialCompleteSocialActivity sa = (CredentialCompleteSocialActivity) session
					.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getCompetenceCompleteSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM CompetenceCompleteSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.targetCompetenceObject obj " +
						   "INNER JOIN fetch obj.competence comp " +
						   "INNER JOIN fetch comp.createdBy " +
						   "WHERE sa.id = :id";
			CompetenceCompleteSocialActivity sa = (CompetenceCompleteSocialActivity) session
					.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	private SocialActivity1 getActivityCompleteSocialActivity(long id, Session session)
			throws DbConnectionException {
		try {
			String query = "SELECT sa " +
						   "FROM ActivityCompleteSocialActivity sa " +
						   "INNER JOIN fetch sa.actor " +
						   "INNER JOIN fetch sa.targetActivityObject obj " +
						   "INNER JOIN fetch obj.activity act " +
						   "INNER JOIN fetch obj.targetCompetence tComp " +
						   "INNER JOIN fetch act.createdBy " +
						   "WHERE sa.id = :id";
			
			ActivityCompleteSocialActivity sa = (ActivityCompleteSocialActivity) session
					.createQuery(query)
					.setLong("id", id)
					.uniqueResult();
			return sa;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error retrieving social activity");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public SocialActivityData1 getSocialActivity(long id, Class<? extends SocialActivity1> clazz, 
			long userId, Locale locale, Session session) throws DbConnectionException {
		SocialActivity1 sa = null;
		if(clazz == PostSocialActivity1.class) {
			sa = getPostSocialActivity(id, session);
		} else if(clazz == TwitterPostSocialActivity1.class) {
			sa = getTwitterPostSocialActivity(id, session);
		} else if(clazz == PostReshareSocialActivity.class) {
			sa = getPostReshareSocialActivity(id, session);
		} else if(clazz == CredentialEnrollSocialActivity.class) {
			sa = getCredentialEnrollSocialActivity(id, session);
		} else if(clazz == CredentialCompleteSocialActivity.class) {
			sa = getCredentialCompleteSocialActivity(id, session);
		} else if(clazz == CompetenceCommentSocialActivity.class) {
			sa = getCompetenceCommentSocialActivity(id, session);
		} else if(clazz == ActivityCommentSocialActivity.class) {
			sa = getActivityCommentSocialActivity(id, session);
		} else if(clazz == ActivityCompleteSocialActivity.class) {
			sa = getActivityCompleteSocialActivity(id, session);
		} else if(clazz == CompetenceCompleteSocialActivity.class) {
			sa = getCompetenceCompleteSocialActivity(id, session);
		}
		
		boolean liked = hasUserLikedSocialActivity(userId, id);
		
		SocialActivityData1 sad = socialActivityFactory.getSocialActivityData(sa, liked, locale);
		
		return sad;
	}
	
	private boolean hasUserLikedSocialActivity(long userId, long resourceId) {
		return annotationManager.hasUserAnnotatedResource(userId, resourceId, AnnotationType.Like, 
				AnnotatedResource.SocialActivity);
	}
	
	@Override
	//nt
	public PostSocialActivity1 createNewPost(SocialActivityData1 postData,
			UserContextData context) throws DbConnectionException {
		Result<PostSocialActivity1> res = self.createNewPostAndGetEvents(postData, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<PostSocialActivity1> createNewPostAndGetEvents(SocialActivityData1 postData,
																 UserContextData context) throws DbConnectionException {
		try {
			Result<PostSocialActivity1> result = new Result<>();
			RichContent1 richContent = richContentFactory.getRichContent(postData.getAttachmentPreview());

			PostSocialActivity1 post = resourceFactory.createNewPost(context.getActorId(), postData.getText(), richContent);

			// generate events for links and files indexing
//			if (richContent != null && richContent.getContentType() != null) {
//				switch (richContent.getContentType()) {
//					case LINK:
//						result.appendEvent(eventFactory.generateEventData(EventType.LinkAdded, context, post, null, null, null));
//					case FILE:
//						result.appendEvent(eventFactory.generateEventData(EventType.FileUploaded, context, post, null, null, null));
//					default:
//						break;
//				}
//			}

			// generate Post event
			result.appendEvent(eventFactory.generateEventData(EventType.Post, context, post, null, null, null));

			// generate MENTIONED event
			List<Long> mentionedUsers = getMentionedUsers(postData.getText());

			if (!mentionedUsers.isEmpty()) {
				for (long mentionedUserId : mentionedUsers) {
					User mentionedUser = (User) persistence.currentManager().load(User.class, mentionedUserId);

					result.appendEvent(eventFactory.generateEventData(EventType.MENTIONED, context, mentionedUser, post, null, null));
				}
			}

			result.setResult(post);

			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving post");
		}
	}

	private List<Long> getMentionedUsers(String postText) {
		List<Long> userIds = new ArrayList<>();
		
		Pattern rulePattern = Pattern.compile("<a data-id=\"(?<userId>[a-zA-Z0-9]+)\"");
		
		Matcher ruleMatch = rulePattern.matcher(postText);

		while (ruleMatch.find()) {
			String userId = ruleMatch.group("userId");
			
			userIds.add(idEncoder.decodeId(userId));
		}
		return userIds;
	}
	
	@Override
	@Transactional
	public PostReshareSocialActivity sharePost(String text, long originalPostId, UserContextData context)
			throws DbConnectionException {
		try {
			PostReshareSocialActivity postShare = resourceFactory.sharePost(context.getActorId(), text, originalPostId);

			eventFactory.generateEvent(EventType.PostShare, context, postShare, null, null, null);
			
			return postShare;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error sharing post");
		}
	}
	
	@Override
	@Transactional
	public PostSocialActivity1 updatePost(long postId, String newText,
			UserContextData context) throws DbConnectionException {
		try {
			PostSocialActivity1 post = resourceFactory.updatePost(postId, newText);

			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("newText", newText);
			
			eventFactory.generateEvent(EventType.PostUpdate, context, post, null, null, parameters);

			
			return post;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error("Error", e);
			throw new DbConnectionException("Error saving post");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Comment1 saveSocialActivityComment(long socialActivityId, CommentData data, 
			CommentedResourceType resource, UserContextData context)
					throws DbConnectionException {
		try {
			Comment1 comment = commentManager.saveNewComment(data, resource, context);
			updateLastActionDate(socialActivityId, comment.getPostDate());
			return comment;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving comment");
		}
		
	}
	
	private void updateLastActionDate(long socialActivityId, Date newDate) throws DbConnectionException {
		SocialActivity1 sa = (SocialActivity1) persistence.currentManager()
				.load(SocialActivity1.class, socialActivityId);
		sa.setLastAction(newDate);
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateSocialActivityComment(long id, CommentData data, UserContextData context)
			throws DbConnectionException {
		try {
			commentManager.updateComment(data, context);
			updateLastActionDate(id, new Date());
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error updating comment");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void likeSocialActivity(long socialActivityId, UserContextData context)
			throws DbConnectionException {
		try {
			annotationManager.createAnnotation(context.getActorId(), socialActivityId, AnnotatedResource.SocialActivity,
					AnnotationType.Like);
			String query = "UPDATE SocialActivity1 sa " +
						   "SET sa.likeCount = sa.likeCount + 1 " +
						   "WHERE sa.id = :id";
			persistence.currentManager()
				.createQuery(query)
				.setLong("id", socialActivityId)
				.executeUpdate();
			
			//to avoid retrieving data from database
			SocialActivity1 sa = new SocialActivity1();
			sa.setId(socialActivityId);
			
			eventFactory.generateEvent(EventType.Like, context, sa, null,null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving social activity like");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void unlikeSocialActivity(long socialActivityId, UserContextData context)
			throws DbConnectionException {
		try {
			annotationManager.deleteAnnotation(context.getActorId(), socialActivityId, AnnotatedResource.SocialActivity,
					AnnotationType.Like);
			String query = "UPDATE SocialActivity1 sa " +
					   "SET sa.likeCount = sa.likeCount - 1 " +
					   "WHERE sa.id = :id";
			persistence.currentManager()
				.createQuery(query)
				.setLong("id", socialActivityId)
				.executeUpdate();
			
			//to avoid retrieving data from database
			SocialActivity1 sa = new SocialActivity1();
			sa.setId(socialActivityId);
			
			eventFactory.generateEvent(EventType.RemoveLike, context, sa, null, null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error saving social activity like");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<SocialActivityData1> getSocialActivityById(long socialActivityId, long userId, Locale locale)
			throws DbConnectionException {
		try {
			String specificCondition = "AND sa.id = :saId \n ";

			SocialActivityData1 res = (SocialActivityData1) createQueryWithCommonParametersSet(userId, 0, 0, specificCondition, 0L,
					null, true, true, locale).
					setParameter("saId", socialActivityId).
					setMaxResults(1).
					uniqueResult();

			return res == null ? Optional.empty() : Optional.of(res);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving social activity.");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Set<Long> getUsersInMyNetwork(long userId) {
		Set<Long> myNetwork = new HashSet<Long>();

		String query = 
				"SELECT DISTINCT followedUser.id " +
				"FROM FollowedUserEntity fUser " +
				"LEFT JOIN fUser.followedUser followedUser " +
				"WHERE fUser.user = :userId ";

		@SuppressWarnings("unchecked")
		List<Long> users = persistence.currentManager().createQuery(query)
				.setLong("userId", userId)
				.list();

		if (users != null && !users.isEmpty()) {
			myNetwork.addAll(users);
		}
		return myNetwork;
	}
	
}
