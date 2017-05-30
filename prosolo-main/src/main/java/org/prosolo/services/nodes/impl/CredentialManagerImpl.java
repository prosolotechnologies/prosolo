package org.prosolo.services.nodes.impl;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.*;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventObserver;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.impl.NodeChangeObserver;
import org.prosolo.services.indexing.utils.ElasticsearchUtil;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.nodes.data.resourceAccess.*;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.CredentialDataFactory;
import org.prosolo.services.nodes.factory.CredentialInstructorDataFactory;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service("org.prosolo.services.nodes.CredentialManager")
public class CredentialManagerImpl extends AbstractManagerImpl implements CredentialManager {

	private static final long serialVersionUID = -2783669846949034832L;

	private static Logger logger = Logger.getLogger(CredentialManagerImpl.class);
	
	@Inject
	private EventFactory eventFactory;
	@Inject
	private ResourceFactory resourceFactory;
	@Inject
	private TagManager tagManager;
	@Inject
	private Competence1Manager compManager;
	@Inject
	private CredentialDataFactory credentialFactory;
	@Inject
	private CompetenceDataFactory competenceFactory;
	@Inject
	private CredentialInstructorManager credInstructorManager;
	@Inject
	private FeedSourceManager feedSourceManager;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private CredentialInstructorDataFactory credInstructorFactory;
	@Inject
	private UserGroupManager userGroupManager;
	@Inject
	private ResourceAccessFactory resourceAccessFactory;
	//self inject for better control of transaction bondaries
	@Inject private CredentialManager credManager;
	@Inject private UserDataFactory userDataFactory;
	
	@Override
	@Transactional(readOnly = false)
	public Credential1 saveNewCredential(CredentialData data, long creatorId, LearningContextData context) 
			throws DbConnectionException {
		Credential1 cred = null;
		try {
			cred = resourceFactory.createCredential(data.getTitle(), data.getDescription(),
					data.getTagsString(), data.getHashtagsString(), creatorId, data.isMandatoryFlow(), 
					data.getDuration(), !data.isAutomaticallyAssingStudents(), data.getCompetences());
			
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			eventFactory.generateEvent(EventType.Create, creatorId, cred, null, page, lContext,
					service, null);
			Set<Tag> hashtags = cred.getHashtags();
			if(!hashtags.isEmpty()) {
				Map<String, String> params = new HashMap<>();
				String csv = StringUtil.convertTagsToCSV(hashtags);
				params.put("newhashtags", csv);
				params.put("oldhashtags", "");
				eventFactory.generateEvent(EventType.UPDATE_HASHTAGS, creatorId, cred, null, page, 
						lContext, service, params);
			}

			return cred;
		} catch(DbConnectionException dce) {
			logger.error(dce);
			dce.printStackTrace();
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving credential");
		} 
	}

	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public Credential1 deleteCredential(long credId, long userId) throws DbConnectionException {
		try {
			if(credId > 0) {
				Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
				cred.setDeleted(true);
				
//				if(data.isDraft()) {
//					Credential1 draftVersion = (Credential1) persistence.currentManager()
//							.load(Credential1.class, data.getId());
//					cred.setDraftVersion(null);
//					delete(draftVersion);
//					//eventFactory.generateEvent(EventType.Delete_Draft, user, draftVersion);
//				}
				
				deleteCredentialCompetences(credId);
	
//				/*
//				 * if credential was once published delete event is generated
//				 */
//				if(data.isPublished() || data.isDraft()) {
//					Map<String, String> params = null;
//					if(data.isDraft()) {
//						params = new HashMap<>();
//						params.put("draftVersionId", data.getId() + "");
//					}
//					eventFactory.generateEvent(EventType.Delete, userId, cred, null, params);
//				}
				eventFactory.generateEvent(EventType.Delete, userId, cred, null);
//				/*
//				 * if credential is draft and it was never published delete_draft event
//				 * is generated
//				 */
//				else {
//					eventFactory.generateEvent(EventType.Delete_Draft, userId, cred);
//				}
				
				return cred;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential");
		}
	}
	
	//non transactional
	@Override
	public void deleteDelivery(long deliveryId, long actorId) throws DbConnectionException, StaleDataException, 
			DataIntegrityViolationException, EventException {
		//self invocation so spring can intercept the call and start transaction
		Result<Void> res = credManager.deleteDeliveryAndGetEvents(deliveryId, actorId);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Result<Void> deleteDeliveryAndGetEvents(long deliveryId, long actorId) throws DbConnectionException,
			DataIntegrityViolationException, StaleDataException {
		try {
			Result<Void> res = new Result<>();
			if(deliveryId > 0) {
				/*
				 * get ids of all competencies in a credential so right event can be fired for each competency.
				 * resource visibility change event for each competency must be generated here because later
				 * reference between delivery and competencies will be lost
				 */
				List<Long> compIds = getIdsOfAllCompetencesInACredential(deliveryId, persistence.currentManager());
				for (long id : compIds) {
					Competence1 comp = new Competence1();
					comp.setId(id);
					res.addEvent(eventFactory.generateEventData(
							EventType.RESOURCE_VISIBILITY_CHANGE, actorId, comp, null, null, null));
				}
				/*
				 * get ids of user groups added to delivery not including default groups and only groups with
				 * learn privilege will be returned. That is because user group removed from resource event
				 * should be generated and this event does not have to be generated for default groups and
				 * groups with edit privilege (because edit privilege in delivery is inherited from original
				 * credential). user group removed from resource event for delivery must be generated here because 
				 * later reference between delivery and user groups will be lost
				 */
				List<Long> userGroupIds = userGroupManager.getIdsOfUserGroupsAddedToCredential(deliveryId, false, 
						UserGroupPrivilege.Learn, persistence.currentManager());
				for (long id : userGroupIds) {
					UserGroup ug = new UserGroup();
					ug.setId(id);
					Credential1 del = new Credential1();
					del.setId(deliveryId);
					res.addEvent(eventFactory.generateEventData(
							EventType.USER_GROUP_REMOVED_FROM_RESOURCE, actorId, ug, del, null, null));
				}
				Credential1 del = new Credential1();
				del.setId(deliveryId);
				res.addEvent(eventFactory.generateEventData(EventType.Delete, actorId, del, null, null, null));
			
				//delete delivery from database
				deleteById(Credential1.class, deliveryId, persistence.currentManager());
			}
			//to force evential exceptions on commit so they can be caught
			persistence.flush();
			return res;
		} catch (HibernateOptimisticLockingFailureException e) {
				e.printStackTrace();
				logger.error(e);
				throw new StaleDataException("Credential edited in the meantime");
		} catch (DataIntegrityViolationException div) {
			logger.error(div);
			div.printStackTrace();
			throw div;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential delivery");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialData getCredentialDataWithProgressIfExists(long credentialId, long userId) 
					throws DbConnectionException {
		CredentialData credData = null;
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			String query = "SELECT DISTINCT cred, creator, targetCred.progress, bookmark.id, targetCred.nextCompetenceToLearnId " +
						   "FROM Credential1 cred " +
						   "INNER JOIN cred.createdBy creator " +
						   "LEFT JOIN cred.targetCredentials targetCred " + 
						   "WITH targetCred.user.id = :user " +
						   "LEFT JOIN cred.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE cred.id = :credId " +
						   "AND cred.type = :type";

			//only delivery is considered because user can only enroll delivery
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("user", user.getId())
					.setLong("credId", credentialId)
					.setString("type", CredentialType.Delivery.name())
					.uniqueResult();

			if (res != null) {
				Credential1 cred = (Credential1) res[0];
				User creator = (User) res[1];
				Integer paramProgress = (Integer) res[2];
				Long paramBookmarkId = (Long) res[3];
				Long nextCompId = (Long) res[4];
				if(paramProgress != null) {
					credData = credentialFactory.getCredentialDataWithProgress(creator, cred, null, 
							null, false, paramProgress.intValue(), nextCompId.longValue());
				} else {
					credData = credentialFactory.getCredentialData(creator, cred, null, null, false);
				}
				if(paramBookmarkId != null) {
					credData.setBookmarkedByCurrentUser(true);
				}
				
				return credData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CredentialData getDraftVersionCredentialDataWithProgressIfExists(long originalVersionId, 
//			long userId) throws DbConnectionException {
//		CredentialData credData = null;
//		try {
//			User user = (User) persistence.currentManager().load(User.class, userId);
//			String query = "SELECT draftCred, targetCred.progress, bookmark.id, targetCred.nextCompetenceToLearnId, targetCred.nextActivityToLearnId " +
//						   "FROM Credential1 cred " + 
//						   "LEFT JOIN cred.draftVersion draftCred " +
//						   "LEFT JOIN cred.targetCredentials targetCred " + 
//						   "WITH targetCred.user.id = :user " +
//						   "LEFT JOIN cred.bookmarks bookmark " +
//						   "WITH bookmark.user.id = :user " +
//						   "WHERE cred.id = :credId";
//
//			Object[] res = (Object[]) persistence.currentManager()
//					.createQuery(query)
//					.setLong("user", user.getId())
//					.setLong("credId", originalVersionId)
//					.uniqueResult();
//
//			if (res != null) {
//				Credential1 cred = (Credential1) res[0];
//				Integer paramProgress = (Integer) res[1];
//				Long paramBookmarkId = (Long) res[2];
//				Long nextCompId = (Long) res[3];
//				Long nextActId = (Long) res[4];
//				
//				if(paramProgress != null) {
//					credData = credentialFactory.getCredentialDataWithProgress(null, cred, 
//							null, null, false, paramProgress.intValue(), nextCompId.longValue(),
//							nextActId.longValue());
//				} else {
//					credData = credentialFactory.getCredentialData(null, cred, 
//							null, null, false);
//				}
//				if(paramBookmarkId != null) {
//					credData.setBookmarkedByCurrentUser(true);
//				}
//				
//				
//				/*
//				 * id of original credential version is set
//				 */
//				credData.setId(originalVersionId);
//				return credData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading credential data");
//		}
//	}
	
	@Override
	@Transactional(readOnly = true)
	public CredentialData getBasicCredentialData(long credentialId, long userId) 
					throws DbConnectionException {
		return getBasicCredentialData(credentialId, userId, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CredentialData getBasicCredentialData(long credentialId, long userId, CredentialType type) 
					throws DbConnectionException {
		CredentialData credData = null;
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			StringBuilder query =  new StringBuilder(
						   "SELECT cred, creator, bookmark.id " +
						   "FROM Credential1 cred " + 
						   "INNER JOIN cred.createdBy creator " +
						   "LEFT JOIN cred.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE cred.id = :credId ");
			
			if (type != null) {
				query.append("AND cred.type = :type");
			}

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("user", user.getId())
					.setLong("credId", credentialId);
			
			if (type != null) {
				q.setString("type", type.name());
			}
			
			Object[] res = (Object[]) q.uniqueResult();

			if (res != null) {
				Credential1 cred = (Credential1) res[0];
				User creator = (User) res[1];
				Long paramBookmarkId = (Long) res[2];

				credData = credentialFactory.getCredentialData(creator, cred, null, null, false);

				if(paramBookmarkId != null) {
					credData.setBookmarkedByCurrentUser(true);
				}
				
				return credData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CredentialData getDraftVersionBasicCredentialData(long originalVersionId, 
//			long userId) throws DbConnectionException {
//		CredentialData credData = null;
//		try {
//			User user = (User) persistence.currentManager().load(User.class, userId);
//			String query = "SELECT draftCred, bookmark.id " +
//						   "FROM Credential1 cred " + 
//						   "LEFT JOIN cred.draftVersion draftCred " +
//						   "LEFT JOIN cred.bookmarks bookmark " +
//						   "WITH bookmark.user.id = :user " +
//						   "WHERE cred.id = :credId";
//
//			Object[] res = (Object[]) persistence.currentManager()
//					.createQuery(query)
//					.setLong("user", user.getId())
//					.setLong("credId", originalVersionId)
//					.uniqueResult();
//
//			if (res != null) {
//				Credential1 cred = (Credential1) res[0];
//				Long paramBookmarkId = (Long) res[1];
//		
//				credData = credentialFactory.getCredentialData(null, cred, 
//						null, null, false);
//
//				if(paramBookmarkId != null) {
//					credData.setBookmarkedByCurrentUser(true);
//				}
//				
//				/*
//				 * id of original credential version is set
//				 */
//				credData.setId(originalVersionId);
//				return credData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading credential data");
//		}
//	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CredentialData> getFullTargetCredentialOrCredentialData(long credentialId, 
			long userId) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		CredentialData credData = null;
		try {
			credData = getTargetCredentialData(credentialId, userId, true);
			if (credData == null) {
				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.USER)
						.addPrivilege(UserGroupPrivilege.Learn);
				return getCredentialData(credentialId, true, true, userId, req);
			}
			
			/* if user is aleardy learning credential, he doesn't need any of the privileges;
			 * we just need to determine which privileges he has (can he edit or instruct a competence)
			 */
			ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.USER);
			ResourceAccessData access = getResourceAccessData(credentialId, userId, req);
			return RestrictedAccessResult.of(credData, access);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialData getTargetCredentialData(long credentialId, long userId, 
			boolean loadCompetences) throws DbConnectionException {
		CredentialData credData = null;
		try {
			TargetCredential1 res = getTargetCredential(credentialId, userId, true, true, true);

			if (res != null) {
				credData = credentialFactory.getCredentialData(res.getCredential().getCreatedBy(), 
						res, res.getCredential().getTags(), res.getCredential().getHashtags(), false);
				
				if (credData != null && loadCompetences) {
					List<CompetenceData1> targetCompData = compManager
							.getUserCompetencesForCredential(credentialId, userId, true);
					credData.setCompetences(targetCompData);
				}
				return credData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public TargetCredential1 getTargetCredential(long credentialId, long userId, 
			boolean loadCreator, boolean loadTags, boolean loadInstructor) throws DbConnectionException {
		User user = (User) persistence.currentManager().load(User.class, userId);

		StringBuilder queryBuilder = new StringBuilder(
				"SELECT targetCred " +
				"FROM TargetCredential1 targetCred " +
				"INNER JOIN fetch targetCred.credential cred ");
		if (loadCreator) {
			queryBuilder.append("INNER JOIN fetch cred.createdBy user ");
		}
		if (loadTags) {
			queryBuilder.append("LEFT JOIN fetch cred.tags tags " +
					   		    "LEFT JOIN fetch cred.hashtags hashtags ");
		}
		if (loadInstructor) {
			queryBuilder.append("LEFT JOIN fetch targetCred.instructor inst " +
								"LEFT JOIN fetch inst.user ");
		}
		queryBuilder.append("WHERE cred.id = :credId " +
				   			"AND targetCred.user = :student");

		TargetCredential1 res = (TargetCredential1) persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", credentialId)
				.setEntity("student", user)
				.uniqueResult();

		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CredentialData> getCredentialDataForManagerView(long credentialId, 
			long userId) throws ResourceNotFoundException, DbConnectionException {
		ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
				.addPrivilege(UserGroupPrivilege.Edit)
				.addPrivilege(UserGroupPrivilege.Instruct);
		return getCredentialDataForView(credentialId, userId, req);
	}
	
	@Transactional(readOnly = true)
	private RestrictedAccessResult<CredentialData> getCredentialDataForView(long credentialId, 
			long userId, ResourceAccessRequirements req) throws ResourceNotFoundException, DbConnectionException {
		return getCredentialData(credentialId, true, true, userId, req);
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CredentialData> getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences, long userId, ResourceAccessRequirements req) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			if(req == null) {
				throw new IllegalArgumentException();
			}
			Credential1 cred = getCredential(credentialId, loadCreatorData, userId);
			
			if(cred == null) {
				throw new ResourceNotFoundException();
			}
			
			User createdBy = loadCreatorData ? cred.getCreatedBy() : null;
			CredentialData credData = credentialFactory.getCredentialData(createdBy, cred, cred.getTags(), 
					cred.getHashtags(), true);
			
			if(loadCompetences) {
				//if user sent a request, we should always return enrolled competencies if he is enrolled
				if (req.getAccessMode() == AccessMode.USER) {
					credData.setCompetences(compManager.getUserCompetencesForCredential(credentialId, userId, false));
				} else {
					/*
					 * always include not published competences
					 */
					credData.setCompetences(compManager.getCredentialCompetencesData(
							credentialId, false, false, false, true, userId));
				}
			}
			
			ResourceAccessData access = getResourceAccessData(credentialId, userId, req);
			return RestrictedAccessResult.of(credData, access);
		} catch (ResourceNotFoundException|IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CredentialData> getCredentialForEdit(long credId, long userId) 
			throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			//credential can be edited only from manage section
			ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
					.addPrivilege(UserGroupPrivilege.Edit);
			RestrictedAccessResult<CredentialData> res = getCredentialData(credId, true, true, userId, 
					req);
			
			return res;
		} catch (ResourceNotFoundException|IllegalArgumentException|DbConnectionException e) {
			throw e;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	/**
	 * Returns credential with specified id. 
	 *
	 * @param credentialId
	 * @param loadCreatorData
	 * @param userId
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private Credential1 getCredential(long credentialId, boolean loadCreatorData, long userId) 
			throws DbConnectionException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT cred FROM Credential1 cred ");

			if(loadCreatorData) {
				builder.append("INNER JOIN fetch cred.createdBy user ");
			}
			builder.append("LEFT JOIN fetch cred.tags tags "); 
			builder.append("LEFT JOIN fetch cred.hashtags hashtags ");
			builder.append("WHERE cred.id = :credentialId AND cred.deleted = :deleted ");
				
			logger.info("GET CREDENTIAL DATA QUERY: " + builder.toString());
			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false);
			
			Credential1 cred = (Credential1) q.uniqueResult();

			return cred;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	
//	@Override
//	@Transactional(readOnly = true)
//	public CredentialData getCredentialDataForEdit(long credentialId, long creatorId, 
//			boolean loadCompetences) throws DbConnectionException {
//		return getCurrentVersionOfCredentialBasedOnRole(credentialId, creatorId, false, 
//				loadCompetences, Role.User);
//	}
//	
//	@Transactional(readOnly = true)
//	private CredentialData getCurrentVersionOfCredentialBasedOnRole(long credentialId, long creatorId, 
//			boolean loadCreator, boolean loadCompetences, Role role) throws DbConnectionException {
//		try {	
//			StringBuilder commonQueryBuilder = new StringBuilder("SELECT cred " +
//					   "FROM Credential1 cred " + 
//					   "LEFT JOIN fetch cred.tags tags " +
//					   "LEFT JOIN fetch cred.hashtags hashtags ");
//			
//			if(loadCreator) {
//				commonQueryBuilder.append("INNER JOIN fetch cred.createdBy ");
//			}
//			
//			StringBuilder queryBuilder = new StringBuilder(commonQueryBuilder.toString() + 
//					"WHERE cred.id = :credentialId " +
//					"AND cred.deleted = :deleted " +
//					"AND cred.draft = :draft ");
//			
//			if(role == Role.User) {
//				queryBuilder.append("AND cred.type = :type " +
//									"AND cred.createdBy.id = :user");
//			} else {
//				queryBuilder.append("AND cred.type = :type");
//			}
//						   
//			Query q = persistence.currentManager()
//					.createQuery(queryBuilder.toString())
//					.setLong("credentialId", credentialId)
//					.setBoolean("deleted", false)
//					.setBoolean("draft", false);
//			
//			if(role == Role.User) {
//				q.setParameter("type", LearningResourceType.USER_CREATED);
//				q.setLong("user", creatorId);
//			} else {
//				q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
//			}
//					
//			Credential1 res = (Credential1) q.uniqueResult();
//			
//			if(res != null) {
//				CredentialData credData = null;
//				if(res.isHasDraft()) {
//					String query2 = commonQueryBuilder.toString() + 
//							" WHERE cred = :draftVersion";
//					Credential1 draftCred = (Credential1) persistence.currentManager()
//							.createQuery(query2)
//							.setEntity("draftVersion", res.getDraftVersion())
//							.uniqueResult();
//					if(draftCred != null) {
//						User creator = loadCreator ? draftCred.getCreatedBy() : null;
//						credData = credentialFactory.getCredentialData(creator, draftCred, 
//								draftCred.getTags(), draftCred.getHashtags(), true);
//					}	
//				} else {
//					User creator = loadCreator ? res.getCreatedBy() : null;
//					credData = credentialFactory.getCredentialData(creator, res, res.getTags(),
//							res.getHashtags(), true);
//				}
//				if(credData != null && loadCompetences) {
//					List<CompetenceData1> compsData = compManager.getCredentialCompetencesData(
//							credData.getId(), true, false, false, true);
//					credData.setCompetences(compsData);
//				}
//				return credData;
//			}
//			
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading credential data");
//		}
//	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Credential1 updateCredential(CredentialData data, long userId, LearningContextData context) 
			throws DbConnectionException, StaleDataException, IllegalDataStateException {
		try {
			Result<Credential1> res = resourceFactory.updateCredential(data, userId);
			Credential1 cred = res.getResult();
			
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			
			for(EventData ev : res.getEvents()) {
				ev.setPage(page);
				ev.setContext(lContext);
				ev.setService(service);
				eventFactory.generateEvent(ev);
			}
			
			fireEditEvent(data, userId, cred, 0, page, lContext, service);
			if(data.isHashtagsStringChanged()) {
				Map<String, String> params = new HashMap<>();
				params.put("newhashtags", data.getHashtagsString());
				params.put("oldhashtags", data.getOldHashtags());
				eventFactory.generateEvent(EventType.UPDATE_HASHTAGS, userId, cred, null, page, 
						lContext, service, params);
			}
			/* 
			 * flushing to force lock timeout exception so it can be catched here. 
			 * It is rethrown as StaleDataException.
			 */
			persistence.currentManager().flush();
		    return cred;
		} catch(StaleDataException e) {
			logger.error(e);
			throw e;
		} catch(HibernateOptimisticLockingFailureException e) {
			e.printStackTrace();
			logger.error(e);
			throw new StaleDataException("Credential edited in the meantime");
		} catch (IllegalDataStateException idse) {
			logger.error(idse);
			throw idse;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential");
		}
	}
	
	private long getRecalculatedDuration(long credId) {
		String query = "SELECT sum(c.duration) FROM CredentialCompetence1 cc " +
					   "INNER JOIN cc.competence c " +
					   "WHERE cc.credential.id = :credId";
		Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.uniqueResult();
		
		return res != null ? res : 0;
	}
	
	private void fireEditEvent(CredentialData data, long userId, 
			Credential1 cred, long originalVersionId, String page, String context,
			String service) throws EventException {   
	    Map<String, String> params = new HashMap<>();
	    CredentialChangeTracker changeTracker = new CredentialChangeTracker(
	    		data.isTitleChanged(), data.isDescriptionChanged(), false,
	    		data.isTagsStringChanged(), data.isHashtagsStringChanged(), 
	    		data.isMandatoryFlowChanged());
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    eventFactory.generateEvent(EventType.Edit, userId, cred, null, page, context, service, params);
	}
	
	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Result<Credential1> updateCredentialData(CredentialData data, long userId) throws StaleDataException,
			IllegalDataStateException {
		Result<Credential1> res = new Result<>();
		Credential1 credToUpdate = (Credential1) persistence.currentManager()
				.load(Credential1.class, data.getId());
		
		/* this check is needed to find out if credential is changed from the moment credential data
		 * is loaded for edit to the moment update request is sent
		 */
		if (credToUpdate.getVersion() != data.getVersion()) {
			throw new StaleDataException("Credential edited in the meantime");
		}
		
		/*
		 * if it is a delivery and end date is before start throw exception
		 */
		if (data.getType() == CredentialType.Delivery
				&& data.getDeliveryStart() != null && data.getDeliveryEnd() != null 
				&& data.getDeliveryStart().after(data.getDeliveryEnd())) {
			throw new IllegalDataStateException("Delivery cannot be ended before it starts");
		}

		//group of attributes that can be changed on delivery and original credential
		credToUpdate.setTitle(data.getTitle());
		credToUpdate.setDescription(data.getDescription());
		credToUpdate.setCompetenceOrderMandatory(data.isMandatoryFlow());
		if(data.isTagsStringChanged()) {
    		credToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getTagsString())));		     
    	}
    	if(data.isHashtagsStringChanged()) {
    		credToUpdate.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getHashtagsString())));
    	}
		
    	//this group of attributes can be changed only for original credential and not for delivery
    	if(data.getType() == CredentialType.Original) {
			credToUpdate.setManuallyAssignStudents(!data.isAutomaticallyAssingStudents());
			credToUpdate.setDefaultNumberOfStudentsPerInstructor(data.getDefaultNumberOfStudentsPerInstructor());
			
			List<CompetenceData1> comps = data.getCompetences();
		    if(comps != null) {
		    	/*
				 * List of competence ids so we can call method that will publish all draft
				 * competences
				 */
				//List<Long> compIds = new ArrayList<>();
		    	boolean recalculateDuration = false;
	    		Iterator<CompetenceData1> compIterator = comps.iterator();
	    		while(compIterator.hasNext()) {
	    			CompetenceData1 cd = compIterator.next();
		    		switch(cd.getObjectStatus()) {
		    			case CREATED:
		    				CredentialCompetence1 cc1 = new CredentialCompetence1();
		    				cc1.setOrder(cd.getOrder());
		    				cc1.setCredential(credToUpdate);
		    				Competence1 comp = (Competence1) persistence.currentManager().load(
		    						Competence1.class, cd.getCompetenceId());
		    				cc1.setCompetence(comp);
		    				saveEntity(cc1);
		    				//compIds.add(cd.getCompetenceId());
		    				//if competence is added to credential
		    				Competence1 competence = new Competence1();
		    				competence.setId(comp.getId());
		    				res.addEvent(eventFactory.generateEventData(
		    						EventType.Attach, userId, competence, credToUpdate, null, null));
		    				recalculateDuration = true;
		    				break;
		    			case CHANGED:
		    				CredentialCompetence1 cc2 = (CredentialCompetence1) persistence.currentManager().load(
				    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
		    				cc2.setOrder(cd.getOrder());
		    				//compIds.add(cd.getCompetenceId());
		    				break;
		    			case REMOVED:
		    				CredentialCompetence1 cc3 = (CredentialCompetence1) persistence.currentManager().load(
				    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
		    				delete(cc3);
		    				Competence1 competence1 = new Competence1();
		    				competence1.setId(cd.getCompetenceId());
		    				res.addEvent(eventFactory.generateEventData(
		    						EventType.Detach, userId, competence1, credToUpdate, null, null));
		    				recalculateDuration = true;
		    				break;
		    			case UP_TO_DATE:
		    				//compIds.add(cd.getCompetenceId());
		    				break;
		    		}
		    	}
		    	
	//	    	if(data.isPublished()) {
	//    			//compManager.publishDraftCompetencesWithoutDraftVersion(compIds);
	//	    		List<EventData> events = compManager.publishCompetences(data.getId(), compIds, creatorId);
	//	    		res.addEvents(events);
	//    		}
	    		//persistence.currentManager().flush();
	    		if(recalculateDuration) {
	    			 credToUpdate.setDuration(getRecalculatedDuration(data.getId()));
	    		}
		    }
    	} else {
    		Date now = new Date();
    		if (data.isDeliveryStartChanged()) {
	    		/*
	    		 * if delivery start is not set or is in future, changes are allowed
	    		 */
	    		if (credToUpdate.getDeliveryStart() == null || credToUpdate.getDeliveryStart().after(now)) {
	    			credToUpdate.setDeliveryStart(data.getDeliveryStart());
	    		} else {
	    			throw new IllegalDataStateException("Update failed. Delivery start time cannot be changed because "
	    					+ "delivery has already started.");
	    		}
    		}
    		
    		if (data.isDeliveryEndChanged()) {
	    		/*
	    		 * if delivery end is not set or is in future, changes are allowed
	    		 */
	    		if (credToUpdate.getDeliveryEnd() == null || credToUpdate.getDeliveryEnd().after(now)) {
	    			credToUpdate.setDeliveryEnd(data.getDeliveryEnd());
	    		} else {
	    			throw new IllegalDataStateException("Update failed. Delivery end time cannot be changed because "
	    					+ "delivery has already ended.");
	    		}
    		}
    	}
	  
	    res.setResult(credToUpdate);
	    return res;
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	public long getCredentialDuration(long credId) throws DbConnectionException {  
		try {
			String query = "SELECT cred.duration " +
					   "FROM Credential1 cred " + 
					   "WHERE cred.id = :credId";
			
			Long duration = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.uniqueResult();
			
			return duration;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential duration");
		}
	}

	@Deprecated
	@Transactional(readOnly = false)
	private void deleteCredentialCompetences(long credId) {
		try {
			String query = "DELETE CredentialCompetence1 comp " +
						   "WHERE comp.credential.id = :credId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.executeUpdate();
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential competences");
		}
	}

	//not transactional
	@Override
	public void enrollInCredential(long credentialId, long userId, LearningContextData context)
			throws DbConnectionException, EventException {
		Result<Void> res = credManager.enrollInCredentialAndGetEvents(credentialId, userId, 0, context);
		for (EventData ev : res.getEvents()) {
			/*
			if student assigned to instructor event should be generated, don't invoke nodechangeobserver
			because instructor will be assigned to student in ES index by enroll course event.
			 */
			//TODO observer refactor - these dependencies between different events and/or observers should be avoided
			if (ev.getEventType() == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR) {
				Class<? extends EventObserver>[] toExclude = new Class[] {NodeChangeObserver.class};
				ev.setObserversToExclude(toExclude);
			}
			eventFactory.generateEvent(ev);
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> enrollInCredentialAndGetEvents(long credentialId, long userId, 
			long instructorThatForcedEnrollId, LearningContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			
			User user = (User) persistence.currentManager().load(User.class, userId);
			
			Credential1 cred = getCredential(credentialId, false, 0);
			TargetCredential1 targetCred = createTargetCredential(cred, user);
			
			long instructorId = 0;
			
			if (!cred.isManuallyAssignStudents()) {
				List<TargetCredential1> targetCredIds = new ArrayList<>();
				targetCredIds.add(targetCred);
				Result<StudentAssignData> res = credInstructorManager.assignStudentsToInstructorAutomatically(
						credentialId, targetCredIds, 0, false, userId, context);
				result.addEvents(res.getEvents());
				List<StudentInstructorPair> assigned = res.getResult().getAssigned();
				if (assigned.size() == 1) {
					StudentInstructorPair pair = assigned.get(0);
					//we need user id, not instructor id
					instructorId = pair.getInstructor().getUser().getId();
				}
	    	}
			
			//TODO cred-redesign-07 implement when assessments are refactored
			//create default assessment for user
			//assessmentManager.createDefaultAssessment(targetCred, instructorId, context);
			
			Map<String, String> params = new HashMap<>();
			params.put("instructorId", instructorId + "");
			String dateString = null;
			Date date = targetCred.getDateCreated();
			if (date != null) {
				dateString = ElasticsearchUtil.getDateStringRepresentation(date);
			}
			params.put("dateEnrolled", dateString);
			if (instructorThatForcedEnrollId > 0) {
				params.put("forcedEnroll", "true");
				params.put("instructorThatEnrolledStudent", instructorThatForcedEnrollId + "");
			}
			params.put("progress", targetCred.getProgress() + "");
			result.addEvent(eventFactory.generateEventData(
					EventType.ENROLL_COURSE, userId, cred, null, context, params));

			//generate completion event if progress is 100
			if (targetCred.getProgress() == 100) {
				result.addEvent(eventFactory.generateEventData(
						EventType.Completion, userId, targetCred, null, context, null));
			}
			
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling in a credential");
		}
	}

	//not transactional
	@Override
	public void enrollStudentsInCredential(long credId, long instructorId, List<Long> userIds, LearningContextData context)
			throws DbConnectionException {
		if (userIds != null) {
			try {
				List<EventData> events = new ArrayList<>();
				for (long userId : userIds) {
					events.addAll(credManager.enrollInCredentialAndGetEvents(
							credId, userId, instructorId, context).getEvents());
				}
				for (EventData ev : events) {
					/*
					if student assigned to instructor event should be generated, don't invoke nodechangeobserver
					because instructor will be assigned to student in ES index by enroll course event.
					 */
					//TODO observer refactor - these dependencies between different events and/or observers should be avoided
					if (ev.getEventType() == EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR) {
						Class<? extends EventObserver>[] toExclude = new Class[] {NodeChangeObserver.class};
						ev.setObserversToExclude(toExclude);
					}
					eventFactory.generateEvent(ev);
				}
			} catch (Exception e) {
				throw new DbConnectionException("Error while enrolling students in a credential");
			}
		}
	}
	
	@Transactional(readOnly = false)
	private TargetCredential1 createTargetCredential(Credential1 cred, User user) {
		TargetCredential1 targetCred = new TargetCredential1();
		targetCred.setCredential(cred);
		targetCred.setUser(user);
		Date now = new Date();
		targetCred.setDateCreated(now);
		targetCred.setDateStarted(now);
		targetCred.setLastAction(now);
		targetCred.setProgress(calculateAndGetCredentialProgress(cred.getId(), user.getId()));
		//if progress is 100, set completion date
		if (targetCred.getProgress() == 100) {
			targetCred.setDateFinished(now);
		}
		
		/*
		 * set first competence and first activity in first competence as next to learn
		 */
		targetCred.setNextCompetenceToLearnId(getIdOfFirstCompetenceInCredential(cred.getId(), user.getId()));

		saveEntity(targetCred);
		
		return targetCred;
	}
	
	private long getIdOfFirstCompetenceInCredential(long credId, long userId) {
		String query = "SELECT credComp.competence.id " +
		       	       "FROM Credential1 cred " + 
		       	       "INNER JOIN cred.competences credComp " +
		       	       "INNER JOIN credComp.competence comp " +
		       	       "LEFT JOIN comp.targetCompetences tComp " +
		       				"WITH tComp.user.id = :userId " +
		       		   "WHERE cred.id = :credId AND (tComp is NULL OR tComp.progress < 100) " +
		       		   "ORDER BY credComp.order";
	
		Long nextId = (Long) persistence.currentManager()
			.createQuery(query)
			.setLong("credId", credId)
			.setLong("userId", userId)
			.setMaxResults(1)
			.uniqueResult();
		
		return nextId != null ? nextId : 0;
	}
	
	private int calculateAndGetCredentialProgress(long credId, long userId) {
		String query = "SELECT floor(AVG(coalesce(tComp.progress, 0)))" +
			       	   "FROM Credential1 cred " + 
			       	   "INNER JOIN cred.competences credComp " +
			       	   "INNER JOIN credComp.competence comp " +
			       	   "LEFT JOIN comp.targetCompetences tComp " +
			       			"WITH tComp.user.id = :userId " +
			       	   "WHERE cred.id = :credId ";

		Integer progress = (Integer) persistence.currentManager()
			.createQuery(query)
			.setLong("credId", credId)
			.setLong("userId", userId)
			.uniqueResult();
		
		return progress != null ? progress : 0;
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> addCompetenceToCredential(long credId, Competence1 comp, long userId) 
			throws DbConnectionException {
		try {
			List<EventData> events = new ArrayList<>();
			Credential1 cred = (Credential1) persistence.currentManager().load(
					Credential1.class, credId);
			
			CredentialCompetence1 cc = new CredentialCompetence1();
			cc.setCompetence(comp);
			cc.setCredential(cred);
			cc.setOrder(cred.getCompetences().size() + 1);
			saveEntity(cc);
			/* 
			 * If duration of added competence is greater than 0 update credential duration
			*/
			//TODO check if this requires select + update and if so, use hql update instead
			if(comp.getDuration() > 0) {
				cred.setDuration(cred.getDuration() + comp.getDuration());
			}
			
			Competence1 competence = new Competence1();
			competence.setId(comp.getId());
			events.add(eventFactory.generateEventData(EventType.Attach, userId, competence, cred, null, null));
			
			return events;
		} catch(Exception e) { 
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding competence to credential");
		}
		
	}
	
	//TODO check if this method can be used for change propagation to target credential
//	private Credential1 createDraftVersionOfCredential(long originalCredentialId) {
//		Credential1 originalCred = getCredential(originalCredentialId, false, 0, 
//				LearningResourceReturnResultType.PUBLISHED_VERSION);
//		
//		Credential1 draftCred = new Credential1();
//		draftCred.setDraft(true);
//		draftCred.setPublished(false);
//		draftCred.setCreatedBy(originalCred.getCreatedBy());
//		draftCred.setTitle(originalCred.getTitle());
//		draftCred.setType(originalCred.getType());
//		draftCred.setDescription(originalCred.getDescription());
//		draftCred.setCompetenceOrderMandatory(originalCred.isCompetenceOrderMandatory());
//		draftCred.setStudentsCanAddCompetences(originalCred.isStudentsCanAddCompetences());
//		draftCred.setManuallyAssignStudents(originalCred.isManuallyAssignStudents());
//		draftCred.setDefaultNumberOfStudentsPerInstructor(originalCred.getDefaultNumberOfStudentsPerInstructor());
//		draftCred.setDuration(originalCred.getDuration());
//	    
//		if(originalCred.getTags() != null) {
//			for(Tag tag : originalCred.getTags()) {
//				draftCred.getTags().add(tag);
//			}
//		}
//		if(originalCred.getHashtags() != null) {
//			for(Tag hashtag : originalCred.getHashtags()) {
//				draftCred.getHashtags().add(hashtag);
//			}
//		}
//	    
//		saveEntity(draftCred);	
//
//		List<CredentialCompetence1> comps = compManager.getCredentialCompetences(originalCredentialId, 
//				false, false, true);
//	    if(comps != null) {
//    		for(CredentialCompetence1 cc : comps) {
//    			CredentialCompetence1 cc1 = new CredentialCompetence1();
//				cc1.setOrder(cc.getOrder());
//				cc1.setCredential(draftCred);
//				cc1.setCompetence(cc.getCompetence());
//				saveEntity(cc1);
//				draftCred.getCompetences().add(cc1);
//    		}	
//	    }
//	    
//		return draftCred;
//	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
//			String query = "SELECT coalesce(originalCred.id, cred.id), coalesce(originalCred.title, cred.title) " +
//					       "FROM CredentialCompetence1 credComp " +
//					       "INNER JOIN credComp.credential cred " +
//					       "LEFT JOIN cred.originalVersion originalCred " +
//					       "WHERE credComp.competence = :comp " +
//					       "AND cred.hasDraft = :boolFalse " +
//					       "AND cred.deleted = :boolFalse";
			String query = "SELECT cred.id, cred.title " +
				       "FROM CredentialCompetence1 credComp " +
				       "INNER JOIN credComp.credential cred " +
				       		"WITH cred.published = :boolTrue " +
				       "WHERE credComp.competence = :comp " +
				       "AND cred.deleted = :boolFalse";
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setEntity("comp", comp)
					.setBoolean("boolTrue", true)
					.setBoolean("boolFalse", false)
					.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			List<CredentialData> resultList = new ArrayList<>();
			for(Object[] row : res) {
				CredentialData cd = new CredentialData(false);
				cd.setId((long) row[0]);
				cd.setTitle((String) row[1]);
				resultList.add(cd);
			}
			return resultList;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialTags(long credentialId) 
			throws DbConnectionException {	
		return getCredentialTags(credentialId, persistence.currentManager());

	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialTags(long credentialId, Session session) 
			throws DbConnectionException {
		try {	
			//if left join is used list with null element would be returned.
			String query = "SELECT tag " +
					       "FROM Credential1 cred " +
					       "INNER JOIN cred.tags tag " +
					       "WHERE cred.id = :credentialId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = session
				.createQuery(query)
				.setLong("credentialId", credentialId)
				.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential tags");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialHashtags(long credentialId) 
			throws DbConnectionException {
		return getCredentialHashtags(credentialId, persistence.currentManager());
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialHashtags(long credentialId, Session session) 
			throws DbConnectionException {
		try {	
			//if left join is used list with null element would be returned.
			String query = "SELECT hashtag " +
					       "FROM Credential1 cred " +
					       "INNER JOIN cred.hashtags hashtag " +
					       "WHERE cred.id = :credentialId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = session
				.createQuery(query)
				.setLong("credentialId", credentialId)
				.list();
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential hashtags");
		}
	}
		
	@Deprecated
	//TODO this method should be removed because all changes will be propagated
	@Transactional(readOnly = false)
	private void updateBasicDataForNotCompletedTargetCredentials(long credentialId) 
			throws DbConnectionException {
		try {	
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			String query = "UPDATE TargetCredential1 targetCred " +
					       "SET targetCred.title = :title, " +
					       "targetCred.description = :description, " +
					       "targetCred.competenceOrderMandatory = :mandatory " +
					       "WHERE targetCred.credential = :cred " +
					       "AND targetCred.progress != :progress";					    

			persistence.currentManager()
				.createQuery(query)
				.setString("title", cred.getTitle())
				.setString("description", cred.getDescription())
				.setEntity("cred", cred)
				.setBoolean("mandatory", cred.isCompetenceOrderMandatory())
				.setInteger("progress", 100)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	@Override
	public List<TargetCredential1> getTargetCredentialsForCredential(long credentialId, 
			boolean justUncompleted) throws DbConnectionException {
		try {		
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT cred " +
				       	   "FROM TargetCredential1 cred " +
				       	   "WHERE cred.credential = :cred ");
			if(justUncompleted) {
				builder.append("AND cred.progress != :progress");
			}
//			String query = "SELECT cred " +
//					       "FROM TargetCredential1 cred " +
//					       "WHERE cred.credential = :cred";					    
			
			Query q = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("cred", cred);
			if(justUncompleted) {
				q.setInteger("progress", 100);
			}
			@SuppressWarnings("unchecked")
			List<TargetCredential1> res = q.list();
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user credentials");
		}
	}
	
	@Deprecated
	@Transactional(readOnly = false)
	private void updateTargetCredentialTagsForUncompletedCredentials(long credentialId) 
			throws DbConnectionException {
		try {
			//TODO cred-redesign-07
//			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(
//					credentialId, true);
//			List<Tag> tags = getCredentialTags(credentialId);
//			for(TargetCredential1 tc : targetCredentials) {
//				tc.getTags().clear();
//				for(Tag tag : tags) {
//					tc.getTags().add(tag);
//				}
//			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	@Deprecated
	@Transactional(readOnly = false)
	private void updateTargetCredentialHashtagsForUncompletedCredentials(long credentialId) 
			throws DbConnectionException {
		try {
			//TODO cred-redesign-07
//			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(
//					credentialId, true);
//			List<Tag> hashtags = getCredentialHashtags(credentialId);
//			for(TargetCredential1 tc : targetCredentials) {
//				tc.getHashtags().clear();
//				for(Tag hashtag : hashtags) {
//					tc.getHashtags().add(hashtag);
//				}
//			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	
	@Deprecated
	//TODO this method should be removed/changed because all changes will be propagated
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCredentialsWithChangedData(long credentialId, CredentialChangeTracker changeTracker) 
			throws DbConnectionException {
		if(changeTracker.isPublished()) {
			if(changeTracker.isTagsChanged()) {
				updateTargetCredentialTagsForUncompletedCredentials(credentialId);
			}
			if(changeTracker.isHashtagsChanged()) {
				updateTargetCredentialHashtagsForUncompletedCredentials(credentialId);
			}
			if(changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged()
					|| changeTracker.isMandatoryFlowChanged()) {
				updateBasicDataForNotCompletedTargetCredentials(credentialId);
			}
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CredentialBookmark> getBookmarkedByIds(long credId) throws DbConnectionException {
		return getBookmarkedByIds(credId, persistence.currentManager());
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CredentialBookmark> getBookmarkedByIds(long credId, Session session) 
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) session.load(Credential1.class, credId);
			String query = "SELECT bookmark " +
						   "FROM CredentialBookmark bookmark " +
						   "WHERE bookmark.credential = :cred";
			
			@SuppressWarnings("unchecked")
			List<CredentialBookmark> bookmarks = session
					.createQuery(query)
					.setEntity("cred", cred)
					.list();
			
			if(bookmarks == null) {
				return new ArrayList<>();
			}
			return bookmarks;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential bookmarks");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void bookmarkCredential(long credId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			CredentialBookmark cb = resourceFactory.bookmarkCredential(credId, userId);
			
			/* 
			 * To avoid SQL query when for example user name is accessed.
			 * This way, only id will be accessible.
			 */
			User actor = new User();
			actor.setId(userId);
			CredentialBookmark bookmark = new CredentialBookmark();
			bookmark.setId(cb.getId());
			Credential1 credential = new Credential1();
			credential.setId(credId);
			
			eventFactory.generateEvent(EventType.Bookmark, actor.getId(), bookmark, credential, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while bookmarking credential");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public CredentialBookmark bookmarkCredential(long credId, long userId) 
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
			User user = (User) persistence.currentManager().load(User.class, userId);
			CredentialBookmark cb = new CredentialBookmark();
			cb.setCredential(cred);
			cb.setUser(user);
			return saveEntity(cb);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while bookmarking credential");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void deleteCredentialBookmark(long credId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			long deletedBookmarkId = resourceFactory.deleteCredentialBookmark(credId, userId);
			/* 
			 * To avoid SQL query when for example user name is accessed.
			 * This way, only id will be accessible.
			 */
			User actor = new User();
			actor.setId(userId);
			CredentialBookmark cb = new CredentialBookmark();
			cb.setId(deletedBookmarkId);
			Credential1 credential = new Credential1();
			credential.setId(credId);
			
			eventFactory.generateEvent(EventType.RemoveBookmark, actor.getId(), cb, credential, 
					context.getPage(), context.getLearningContext(), context.getService(), null);
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential bookmark");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public long deleteCredentialBookmark(long credId, long userId) 
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
			User user = (User) persistence.currentManager().load(User.class, userId);
			String query = "SELECT cb " +
						   "FROM CredentialBookmark cb " +
						   "WHERE cb.credential = :cred " +
						   "AND cb.user = :user";
			
			CredentialBookmark bookmark = (CredentialBookmark) persistence.currentManager()
					.createQuery(query)
					.setEntity("cred", cred)
					.setEntity("user", user)
					.uniqueResult();
			
			long id = bookmark.getId();
			
			delete(bookmark);
			
			return id;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential bookmark");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateDurationForCredentialsWithCompetence(long compId, long duration, Operation op)
			throws DbConnectionException {
		try {
			List<Long> credIds = getIdsOfCredentialsWithCompetence(compId);
			if(!credIds.isEmpty()) {
				String opString = op == Operation.Add ? "+" : "-";
				String query = "UPDATE Credential1 cred SET " +
						   	   "cred.duration = cred.duration " + opString + " :duration " +
						       "WHERE cred.id IN :credIds";
				
				persistence.currentManager()
					.createQuery(query)
					.setLong("duration", duration)
					.setParameterList("credIds", credIds)
					.executeUpdate();
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential duration");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCredentialDuration(long id, long duration) throws DbConnectionException {
		try {
			String query = "UPDATE TargetCredential1 cred SET " +
						   "cred.duration = :duration " +
						   "WHERE cred.id = :credId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("duration", duration)
				.setLong("credId", id)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential duration");
		}
	}

	@Deprecated
	private List<Long> getIdsOfCredentialsWithCompetence(long compId) {
		try {
			String query = "SELECT cred.id " +
					   	   "FROM CredentialCompetence1 credComp " +
					   	   "INNER JOIN credComp.credential cred " +
					       "WHERE credComp.competence.id = :compId";
		
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)	
				.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential ids");
		}
	}
	
	@Deprecated
	public void updateProgressForTargetCredentialWithCompetence(long targetCompId) throws DbConnectionException {
		try {	
			String query1 = "SELECT cred.id " +
					"FROM TargetCompetence1 comp " +
					"INNER JOIN comp.targetCredential cred " +
					"WHERE comp.id = :compId";
	
			Long targetCredId =  (Long) persistence.currentManager()
					.createQuery(query1)
					.setLong("compId", targetCompId)
					.uniqueResult();
			
			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager()
					.load(TargetCredential1.class, targetCredId);
			
			String query = "SELECT comp.progress " +
						   "FROM TargetCompetence1 comp " +
						   "WHERE comp.targetCredential = :cred";
			
			@SuppressWarnings("unchecked")
			List<Integer> res =  persistence.currentManager()
				.createQuery(query)
				.setEntity("cred", targetCred)
				.list();
			
			if(res != null) {
				int cumulativeProgress = 0;
				for(Integer p : res) {
					cumulativeProgress += p.intValue();
				}
				int newProgress = cumulativeProgress / res.size();
				targetCred.setProgress(newProgress); 
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential progress");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> updateCredentialProgress(long targetCompId, long userId, LearningContextData contextData) 
			throws DbConnectionException {
		try {
			List<EventData> events = new ArrayList<>();
			String query = "SELECT tCred.id, cred.id, comp.id, coalesce(tComp.progress, 0) " +
				    	   "FROM TargetCredential1 tCred " +
				    	   "INNER JOIN tCred.credential cred " +
				    	   "INNER JOIN cred.competences credComp1 " +
				    	   "INNER JOIN credComp1.competence comp1 " +
				    	   "INNER JOIN comp1.targetCompetences tComp1 " +
			   		   			"WITH tComp1.id = :targetCompId " +
				    	   "INNER JOIN cred.competences credComp " +
				    	   "INNER JOIN credComp.competence comp " +
				    	   "LEFT JOIN comp.targetCompetences tComp " +
				    	   		"WITH tComp.user.id = :userId " +
				    	   "WHERE tCred.user.id = :userId " +
				   		   "ORDER BY tCred.id, credComp.order";
			
			@SuppressWarnings("unchecked")
			List<Object[]> res =  persistence.currentManager()
				.createQuery(query)
				.setLong("targetCompId", targetCompId)
				.setLong("userId", userId)
				.list();
			
			long currentTCredId = 0;
			long currentCredId = 0;
			long nextCompToLearnId = 0;
			int cumulativeCredProgress = 0;
			int numberOfCompetencesInCredential = 0;
			Date now = new Date();
			if(res != null) {
				for(Object[] obj : res) {
					long tCredId = (long) obj[0];
					long credId = (long) obj[1];
					long compId = (long) obj[2];
					int compProgress = (int) obj[3];
					
					if(tCredId != currentTCredId) {
						if(currentTCredId > 0) {
							int finalCredProgress = cumulativeCredProgress / numberOfCompetencesInCredential;
							events.addAll(updateTargetCredentialProgress(currentTCredId, currentCredId, 
									finalCredProgress, nextCompToLearnId, userId, now, contextData));
						}
						currentTCredId = tCredId;
						currentCredId = credId;
						numberOfCompetencesInCredential = 1;
						cumulativeCredProgress = compProgress;
						nextCompToLearnId = 0;
					} else {
						numberOfCompetencesInCredential ++;
						cumulativeCredProgress += compProgress;
					}
					
					if(nextCompToLearnId == 0 && compProgress < 100) {
						nextCompToLearnId = compId;
					}
				}
				//update last credential
				if(currentTCredId > 0) {
					int finalCredProgress = cumulativeCredProgress / numberOfCompetencesInCredential;
					events.addAll(updateTargetCredentialProgress(currentTCredId, currentCredId, 
							finalCredProgress, nextCompToLearnId, userId, now, contextData));
				}
			}
			return events;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential progress");
		}
	}
	
	private List<EventData> updateTargetCredentialProgress(long tCredId, long credId, int finalCredProgress, 
			long nextCompToLearnId, long userId, Date now, LearningContextData contextData) {
		StringBuilder updateCredQuery = new StringBuilder(
				"UPDATE TargetCredential1 targetCred SET " +
				"targetCred.progress = :progress, " +
				"targetCred.nextCompetenceToLearnId = :nextCompToLearnId ");
		
		if(finalCredProgress == 100) {
			updateCredQuery.append(", targetCred.dateFinished = :dateCompleted ");
		}
		updateCredQuery.append("WHERE targetCred.id = :tCredId");
		
		Query q1 = persistence.currentManager()
			.createQuery(updateCredQuery.toString())
			.setInteger("progress", finalCredProgress)
			.setLong("nextCompToLearnId", nextCompToLearnId)
			.setLong("tCredId", tCredId);
		
		if(finalCredProgress == 100) {
			q1.setDate("dateCompleted", now);
		}
		
		q1.executeUpdate();
		
		List<EventData> events = new ArrayList<>();
		
		TargetCredential1 tCred = new TargetCredential1();
		tCred.setId(tCredId);
		Credential1 cred = new Credential1();
		cred.setId(credId);
		tCred.setCredential(cred);
		
		EventData ev = eventFactory.generateEventData(EventType.ChangeProgress, userId, tCred, null, contextData, null);
		ev.setProgress(finalCredProgress);
		events.add(ev);
//		eventFactory.generateChangeProgressEvent(userId, tCred, finalCredProgress, 
//				lcPage, lcContext, lcService, null);
		if(finalCredProgress == 100) {
			events.add(eventFactory.generateEventData(EventType.Completion, userId, tCred, null, contextData, null));
//			eventFactory.generateEvent(EventType.Completion, user.getId(), tCred, null,
//					lcPage, lcContext, lcService, null);
		}
		return events;
	}

	@Override
	@Transactional(readOnly = true)
	public String getCredentialTitle(long id) throws DbConnectionException {
		return getCredentialTitle(id, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCredentialTitle(long id, CredentialType type) throws DbConnectionException {
			try {
				StringBuilder queryBuilder = new StringBuilder(
					   "SELECT cred.title " +
					   "FROM Credential1 cred " +
					   "WHERE cred.id = :credId ");
				
				if (type != null) {
					queryBuilder.append("AND cred.type = :type");
				}
				
				Query q = persistence.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("credId", id);
				
				if (type != null) {
					q.setString("type", type.name());
				}
				
				return (String) q.uniqueResult();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				throw new DbConnectionException("Error while retrieving credential title");
			}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public String getTargetCredentialTitle(long credId, long userId) throws DbConnectionException {
		try {
			String query = "SELECT cred.title " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.user.id = :userId " +
						   "AND cred.credential.id = :credId";
			
			String title = (String) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("credId", credId)
				.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential title");
		}
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CredentialData getCurrentVersionOfCredentialForManager(long credentialId,
//			boolean loadCreator, boolean loadCompetences) throws DbConnectionException {
//			return getCurrentVersionOfCredentialBasedOnRole(credentialId, 0, loadCreator, 
//					loadCompetences, Role.Manager);
//	}

	@Deprecated
	@SuppressWarnings({ "unchecked" })
	@Override
	@Transactional (readOnly = true)
	public List<TargetCredential1> getAllCredentials(long userid, boolean onlyPubliclyVisible) throws DbConnectionException {
		List<TargetCredential1> result = new ArrayList<>();
		try {
			String query=
				"SELECT targetCredential1 " +
				"FROM TargetCredential1 targetCredential1 " +
				"WHERE targetCredential1.user.id = :userid ";
			
			if (onlyPubliclyVisible) {
				query += "AND targetCredential1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetCredential1.title";
			
			result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userid)
					.list();
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException();
		}
		return result;
	}
	
	@Deprecated
	@Override
	@SuppressWarnings("unchecked")
	@Transactional (readOnly = true)
	public List<TargetCredential1> getAllCompletedCredentials(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		List<TargetCredential1> result = new ArrayList<>();
		try {
			String query =
					"SELECT targetCredential1 " +
					"FROM TargetCredential1 targetCredential1 " +
					"WHERE targetCredential1.user.id = :userid " +
						"AND targetCredential1.progress = 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetCredential1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetCredential1.title";
			  	
			result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userId)
				  	.list();
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException();
		}
		return result;
	}

	@Deprecated
	@SuppressWarnings({ "unchecked" })
	@Override
	@Transactional (readOnly = true)
	public List<TargetCredential1> getAllInProgressCredentials(long userid, boolean onlyPubliclyVisible) throws DbConnectionException {
		List<TargetCredential1> result = new ArrayList<>();
		try {
			String query =
				"SELECT targetCredential1 " +
				"FROM TargetCredential1 targetCredential1 " +
				"WHERE targetCredential1.user.id = :userid " +
					"AND targetCredential1.progress < 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetCredential1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetCredential1.title";
			
			result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userid)
				  	.list();
		} catch (DbConnectionException e) {
			logger.error(e);
			throw new DbConnectionException();
		}
		return result;
	}

	@Deprecated
	@Override
	@Transactional (readOnly = false)
	public void updateHiddenTargetCredentialFromProfile(long credId, boolean hiddenFromProfile)
			throws DbConnectionException {
		try {
			String query = 
				"UPDATE TargetCredential1 targetCredential " +
				"SET targetCredential.hiddenFromProfile = :hiddenFromProfile " +
				"WHERE targetCredential.id = :credId ";
	
			persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setBoolean("hiddenFromProfile", hiddenFromProfile)
				.executeUpdate();
		} catch (Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while updating hiddenFromProfile field of a credential " + credId);
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId) 
			throws DbConnectionException {  
		return getTargetCredentialsProgressAndInstructorInfoForUser(userId, persistence.currentManager());
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId, Session session) 
			throws DbConnectionException {  
		try {
			List<CredentialData> data = new ArrayList<>();
			String query = "SELECT cred.id, targetCred.progress, instructor.user.id, targetCred.dateCreated " +
					   "FROM TargetCredential1 targetCred " + 
					   "INNER JOIN targetCred.credential cred " +
					   "LEFT JOIN targetCred.instructor instructor " +
					   "WHERE targetCred.user.id = :userId";
			
			@SuppressWarnings("unchecked")
			List<Object[]> res = session
					.createQuery(query)
					.setLong("userId", userId)
					.list();
			
			if(res != null) {
				for(Object[] row : res) {
					if(row != null) {
						CredentialData cred = new CredentialData(false);
						cred.setId((long) row[0]);
						cred.setProgress((int) row[1]);
						Long instId = (Long) row[2];
						cred.setInstructorId(instId == null ? 0 : instId);
						cred.setDate((Date) row[3]);
						data.add(cred);
					}
				}
			}
			return data;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user credentials");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public boolean areStudentsManuallyAssignedToInstructor(long credId) throws DbConnectionException {
		try {
			String query = 
					"SELECT cred.manuallyAssignStudents " +
					"FROM Credential1 cred " +
					"WHERE cred.id = :credId";
			
				Boolean res = (Boolean) persistence.currentManager().createQuery(query).
						setLong("credId", credId).
						uniqueResult();
				if(res == null) {
					throw new Exception();
				} 
				return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}

	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<TargetCredential1> getTargetCredentialsForInstructor(long instructorId) 
			throws DbConnectionException {
		try {
			String query = 
					"SELECT cred " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.instructor.id = :instructorId";
			
				@SuppressWarnings("unchecked")
				List<TargetCredential1> creds = persistence.currentManager().createQuery(query).
						setLong("instructorId", instructorId).
						list();
				if(creds == null) {
					return new ArrayList<>();
				}
				return creds;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading target credentials");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public long getUserIdForTargetCredential(long targetCredId) throws DbConnectionException {
		try {
			String query = 
					"SELECT cred.user.id " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.id = :targetCredId";
			
			Long res = (Long) persistence.currentManager().createQuery(query).
					setLong("targetCredId", targetCredId).
					uniqueResult();
			if(res == null) {
				return 0;
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Long> getUserIdsForCredential(long credId) throws DbConnectionException {
		try {
			String query = 
					"SELECT targetCredential.user.id " +
					"FROM TargetCredential1 targetCredential " +
					"WHERE targetCredential.credential.id = :credentialId";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
					.setLong("credentialId", credId)
					.list();
			if(res == null) {
				return new ArrayList<>();
			} 
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
	@Deprecated
	//TODO check with Nikola why 30 is hardcoded
	@Override
	@Transactional(readOnly = true)
	public List<Long> getActiveUserIdsForCredential(long credId) throws DbConnectionException {
		try {
			String query = 
					"SELECT targetCredential.user.id " +
					"FROM TargetCredential1 targetCredential " +
					"WHERE targetCredential.credential.id = :credentialId "+ 
					"AND targetCredential.progress > 30";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
					.setLong("credentialId", credId)
					.list();
			if(res == null) {
				return new ArrayList<>();
			} 
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Long> getUserIdsForTargetCredentials(List<Long> targetCredIds) 
			throws DbConnectionException {
		try {
			if(targetCredIds == null || targetCredIds.isEmpty()) {
				return null;
			}
			String query = 
					"SELECT cred.user.id " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.id IN (:targetCredIds)";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
					.setParameterList("targetCredIds", targetCredIds)
					.list();
			if(res == null) {
				return new ArrayList<>();
			} 
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user ids");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TargetCredential1> getTargetCredentialsForUsers(List<Long> userIds, long credId) 
			throws DbConnectionException {
		try {
			if(userIds == null || userIds.isEmpty()) {
				return null;
			}
			String query = 
					"SELECT cred " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.credential.id = :credId " +
					"AND cred.user.id IN (:userIds)";
			
			@SuppressWarnings("unchecked")
			List<TargetCredential1> res = persistence.currentManager().createQuery(query)
					.setLong("credId", credId)
					.setParameterList("userIds", userIds)
					.list();
		
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user credentials");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void removeFeed(long credId, long feedSourceId) throws DbConnectionException {
		try {
			Credential1 cred = getCredentialWithBlogs(credId);
			FeedSource feedSource = (FeedSource) persistence.currentManager().load(FeedSource.class, feedSourceId);
			cred.getBlogs().remove(feedSource);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while removing blog from the credential");
		}
	}
	
	@Deprecated
	//returns true if new blog is added to the course, false if it already exists
	@Override
	@Transactional(readOnly = false)
	public boolean saveNewCredentialFeed(long credId, String feedLink) 
			throws DbConnectionException, EntityAlreadyExistsException {
		try {
			Credential1 cred = getCredentialWithBlogs(credId);
			
			if(cred != null) {
				FeedSource feedSource = feedSourceManager.getOrCreateFeedSource(null, feedLink);
				List<FeedSource> blogs = cred.getBlogs();
				
				if (!blogs.contains(feedSource)) {
					blogs.add(feedSource);
					return true;
				} else {
					throw new EntityAlreadyExistsException("That Feed Source already exists");
				}
			}
			return false;
		} catch(EntityAlreadyExistsException eae) {
			throw eae;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding new credential feed source");
		}
	}
	
	@Deprecated
	private Credential1 getCredentialWithBlogs(long credId) {
		String query = "SELECT cred " +
				   "FROM Credential1 cred " +
				   "LEFT JOIN fetch cred.blogs " +
				   "WHERE cred.id = :credId";
	
		Credential1 cred = (Credential1) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.uniqueResult();
		
		return cred;
	}
	
	@Override
	@Transactional(readOnly = true)
	public LearningInfo getCredentialLearningInfo(long credId, long userId, boolean loadCompLearningInfo) 
			throws DbConnectionException {
		try {
			String query = "SELECT c.title, cred.nextCompetenceToLearnId, c.competenceOrderMandatory " +
						   "FROM TargetCredential1 cred " +
						   "INNER JOIN cred.credential c " +
						   		"WITH c.id = :credId " +
						   "WHERE cred.user.id = :userId";
			
			Object[] res = (Object[]) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("credId", credId)
				.uniqueResult();
			
			if(res != null) {
				String title = (String) res[0];
				long nextComp = (long) res[1];
				boolean mandatoryOrder = (boolean) res[2];
				
				LearningInfo credLI = LearningInfo.getLearningInfoForCredential(title, mandatoryOrder, nextComp);
				
				if(loadCompLearningInfo && nextComp > 0) {
					return LearningInfo.merge(credLI, compManager.getCompetenceLearningInfo(nextComp, userId));
				}
				
				return credLI;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving learning info");
		}
	}
	
	@Deprecated
	@Override
	@Transactional
	public List<CredentialData> getNRecentlyLearnedInProgressCredentials(Long userid, int limit, boolean loadOneMore) 
			throws DbConnectionException {
		List<CredentialData> result = new ArrayList<>();
		try {
			String query=
					"SELECT tCred, creator, bookmark.id " +
					"FROM TargetCredential1 tCred " +
					"LEFT JOIN tCred.createdBy creator " +
						"WITH tCred.credentialType = :credType " +
					"INNER JOIN tCred.credential cred " +
					"LEFT JOIN cred.bookmarks bookmark " +
					   "WITH bookmark.user.id = :userId " +
					"WHERE tCred.user.id = :userId " +
					"AND tCred.progress < :progress " +
					"ORDER BY tCred.lastAction DESC";
			  
			int limitFinal = loadOneMore ? limit + 1 : limit;
			
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userid)
					.setInteger("progress", 100)
					.setString("credType", LearningResourceType.USER_CREATED.name())
					.setMaxResults(limitFinal)
				  	.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			
			for(Object[] row : res) {
				if(row != null) {
					TargetCredential1 tc = (TargetCredential1) row[0];
					User creator = (User) row[1];
					Long bookmarkId = (Long) row[2];
					CredentialData cd = credentialFactory.getCredentialData(creator, 
							tc, null, null, false);
					if(bookmarkId != null) {
						cd.setBookmarkedByCurrentUser(true);
					}
					result.add(cd);
				}
			}
			return result;
		} catch (DbConnectionException e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while retrieving credential data");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCredentialLastAction(long userId, long credentialId) 
			throws DbConnectionException {
		try {
			String query = "UPDATE TargetCredential1 cred SET " +
					   	   "cred.lastAction = :date " +
					       "WHERE cred.credential.id = :credId " +
					   	   "AND cred.user.id = :userId " +
					       "AND cred.progress < :progress";
			
			persistence.currentManager()
				.createQuery(query)
				.setTimestamp("date", new Date())
				.setLong("credId", credentialId)
				.setLong("userId", userId)
				.setInteger("progress", 100)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating last action for user credential");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public long getTargetCredentialNextCompToLearn(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT cred.nextCompetenceToLearnId " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.user.id = :userId " +
						   "AND cred.credential.id = :credId";
			
			Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("credId", credId)
				.uniqueResult();
			
			return res != null ? res : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving next competency to learn");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long getNumberOfUsersLearningCredential(long credId) 
			throws DbConnectionException {
		try {
			String query = "SELECT COUNT(cred.id) " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.credential.id = :credId";
			
			Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.uniqueResult();
			
			return res != null ? res : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving number of users learning credential");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<StudentData> getCredentialStudentsData(long credId, int limit) 
			throws DbConnectionException {
		try {
			String query = "SELECT cred " +
						   "FROM TargetCredential1 cred " +
						   "INNER JOIN fetch cred.user " +
						   "LEFT JOIN fetch cred.instructor inst " +
						   "LEFT JOIN fetch inst.user " +
						   "WHERE cred.credential.id = :credId " +
						   "ORDER BY cred.dateStarted DESC";
			
			List<TargetCredential1> res = persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setMaxResults(limit)
				.list();
			
			if(res != null) {
				List<StudentData> data = new ArrayList<>();
				for(TargetCredential1 tc : res) {
					StudentData sd = new StudentData(tc.getUser());
					CredentialInstructor ci = tc.getInstructor();
					if(ci != null) {
						sd.setInstructor(credInstructorFactory.getInstructorData(
								tc.getInstructor(), tc.getInstructor().getUser(), 
								0, false));
					}
					sd.setProgress(tc.getProgress());
					Optional<Long> credAssessmentId = assessmentManager
							.getDefaultCredentialAssessmentId(credId, sd.getUser().getId());
					if(credAssessmentId.isPresent()) {
						sd.setAssessmentId(credAssessmentId.get());
					}
					data.add(sd);
				}
				return data;
			}
			
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential members");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CredentialMembersSearchFilter[] getFiltersWithNumberOfStudentsBelongingToEachCategory(long credId) 
			throws DbConnectionException {
		try {
			String query = "SELECT COUNT(cred.id), COUNT(cred.instructor.id), COUNT(case cred.progress when 100 then 1 else null end)  " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.credential.id = :credId";
			
			Object[] res = (Object[]) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.uniqueResult();
			
			if(res != null) {
				long all = (long) res[0];
				CredentialMembersSearchFilter allFilter = new CredentialMembersSearchFilter(
						CredentialMembersSearchFilterValue.All, all);
				long assigned = (long) res[1];
				CredentialMembersSearchFilter unassignedFilter = new CredentialMembersSearchFilter(
						CredentialMembersSearchFilterValue.Unassigned, all - assigned);
				CredentialMembersSearchFilter assignedFilter = new CredentialMembersSearchFilter(
						CredentialMembersSearchFilterValue.Assigned, assigned);
				long completed = (long) res[2];
				CredentialMembersSearchFilter completedFilter = new CredentialMembersSearchFilter(
						CredentialMembersSearchFilterValue.Completed, completed);
				return new CredentialMembersSearchFilter[] {allFilter, unassignedFilter, assignedFilter, completedFilter};
			}
			
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving filters");
		}
	}
	
	@Deprecated
	@Override
	@Transactional (readOnly = true)
	public UserData chooseRandomPeer(long credId, long userId) {
		try {
			String query = 
				"SELECT user " +
				"FROM TargetCredential1 tCred " +
				"INNER JOIN tCred.user user " +
				"WHERE tCred.credential.id = :credId " + 
					"AND user.id != :userId " + 
					"AND user.id NOT IN ( " +
						"SELECT assessment.assessor.id " +
						"FROM CredentialAssessment assessment " +
						"INNER JOIN assessment.targetCredential tCred " +
						"INNER JOIN tCred.credential cred " +
						"WHERE assessment.assessedStudent.id = :userId " +
							"AND cred.id = :credId " +
							"AND assessment.assessor IS NOT NULL " + // can be NULL in default assessments when instructor is not set
					") " + 
				"ORDER BY RAND()";
			
			@SuppressWarnings("unchecked")
			List<User> res = (List<User>) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setLong("userId", userId)
					.setMaxResults(1)
					.list();
			
			if (res != null && !res.isEmpty()) {
				User user = res.get(0);
				return new UserData(user);
			}
			
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving random peer");
		}
	}
	
	@Deprecated
	@Override
	@Transactional (readOnly = true)
	public List<Long> getAssessorIdsForUserAndCredential(long credentialId, long userId) {
		try {
			String query = 
				"SELECT assessment.assessor.id " +
				"FROM CredentialAssessment assessment " +
				"INNER JOIN assessment.targetCredential tCred " +
				"INNER JOIN tCred.credential cred " +
				"WHERE assessment.assessedStudent.id = :userId " +
					"AND cred.id = :credId " +
					"AND assessment.assessor IS NOT NULL "; // can be NULL in default assessments when instructor is not set
			
			@SuppressWarnings("unchecked")
			List<Long> res = (List<Long>) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credentialId)
					.list();
			
			if (res != null) {
				return res;
			}
			
			return new ArrayList<Long>();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving ids of credential assessors for the particular user");
		}
	}
//	public void publishCredential(Credential1 cred, long creatorId, Role role) {
//		try {
//			if(cred.isHasDraft()) {
//				Credential1 draftC = cred.getDraftVersion();
//				/*
//				 * check if credential has at least one competence
//				 */
//				int compNo = draftC.getCompetences().size();
//				if(compNo == 0) {
//					throw new CredentialEmptyException();
//				}
//				long draftCompId = draftC.getId();
//				List<EventData> events = publishDraftVersion(cred, draftC, creatorId, role);
//				fireCredPublishedAgainEditEvent(creatorId, cred, draftCompId, null, null, null);
//			} else {
//				/*
//				 * check if credential has at least one competence
//				 */
//				int compNo = cred.getCompetences().size();
//				if(compNo == 0) {
//					throw new CredentialEmptyException();
//				}
//				cred.setPublished(true);
//				eventFactory.generateEvent(fireFirstTimePublishCredEvent(creatorId, cred));
//			}
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//		}
//	}
//	
//	private List<EventData> publishDraftVersion(Credential1 originalCred, Credential1 draftCred, long creatorId, Role role) {
//		originalCred.setTitle(draftCred.getTitle());
//		originalCred.setDescription(draftCred.getDescription());
//		originalCred.setCompetenceOrderMandatory(draftCred.isCompetenceOrderMandatory());
//		originalCred.setStudentsCanAddCompetences(draftCred.isStudentsCanAddCompetences());
//		originalCred.setDuration(draftCred.getDuration());
//		originalCred.setManuallyAssignStudents(draftCred.isManuallyAssignStudents());
//		originalCred.setDefaultNumberOfStudentsPerInstructor(draftCred.getDefaultNumberOfStudentsPerInstructor());
//	    
//		
//		originalCred.setTags(draftCred.getTags());
//    	originalCred.setHashtags(draftCred.getHashtags());
//
//		List<CredentialCompetence1> comps = compManager
//				.getCredentialCompetences(draftCred.getId(), false, false, true);
//		deleteCredentialCompetences(originalCred.getId());
//		List<Long> compIds = new ArrayList<>();
//	    if(comps != null) {
//    		for(CredentialCompetence1 cc : comps) {
//    			CredentialCompetence1 cc1 = new CredentialCompetence1();
//				cc1.setOrder(cc.getOrder());
//				cc1.setCredential(originalCred);
//				cc1.setCompetence(cc.getCompetence());
//				saveEntity(cc1);
//				originalCred.getCompetences().add(cc1);
//				compIds.add(cc1.getCompetence().getId());
//    		}	
//	    }
//	    List<EventData> events = compManager.publishCompetences(compIds, creatorId, role);
//	    
//	    originalCred.setHasDraft(false);
//	    originalCred.setDraftVersion(null);
//	    originalCred.setPublished(true);
//    	delete(draftCred);
//    	
//    	return events;
//	}
//	
//	private EventData fireFirstTimePublishCredEvent(long userId, Credential1 updatedCred) {
//		EventData ev = new EventData();
//		ev.setEventType(EventType.Create);
//		ev.setActorId(userId);
//		ev.setObject(updatedCred);
//		return ev;
//	}

	@Override
	@Transactional(readOnly = true)
	public List<TagCountData> getTagsForCredentialCompetences(long credentialId) throws DbConnectionException {

		StringBuilder queryBuilder = new StringBuilder(
				"SELECT DISTINCT tag, COUNT(tag) " +
				"FROM CredentialCompetence1 cc " +
				"INNER JOIN cc.competence c " +
				"INNER JOIN c.tags tag " +
				"WHERE cc.credential.id = :credId "+
				"GROUP BY tag");
		
		@SuppressWarnings("unchecked")
		List<Object[]> res = (List<Object[]>) persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", credentialId)
				.list();
		List<TagCountData> tags = new LinkedList<>();
		for (Object[] objects : res) {
			Tag tag = (Tag) objects[0];
			long count = (long) objects[1];
			TagCountData tagCountData = new TagCountData(tag.getTitle(), count);
			tags.add(tagCountData);
		}
		
		return tags;
	}
	
	@Override
	@Transactional(readOnly = true)
	public int getNumberOfTags(long credentialId) throws DbConnectionException {

		return getTagsForCredentialCompetences(credentialId).size();
	}

	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getTargetCompetencesForKeywordSearch(long credentialId) throws DbConnectionException {
		//TODO cred-redesign-07
//		StringBuilder queryBuilder = new StringBuilder(
//				"SELECT DISTINCT targetComp " +
//				"FROM TargetCompetence1 targetComp " + 
//				"LEFT JOIN FETCH targetComp.tags tag "+
//				"WHERE targetComp.targetCredential.id = :credId " +
//				"ORDER BY targetComp.title");
//		
//		@SuppressWarnings("unchecked")
//		List<TargetCompetence1> competences= (List<TargetCompetence1>) persistence.currentManager()
//				.createQuery(queryBuilder.toString())
//				.setLong("credId", credentialId)
//				.list();
//		List<CompetenceData1> data = new ArrayList<>();
//		for(TargetCompetence1 competence : competences){
//			CompetenceData1 cd = new CompetenceData1(false);
//			cd.setTitle(competence.getTitle());
//			cd.setDuration(competence.getDuration());
//			cd.setTagsString(AnnotationUtil.getAnnotationsAsSortedCSV(competence.getTags()));
//			cd.setCredentialId(competence.getTargetCredential().getId());
//			cd.setCompetenceId(competence.getCompetence().getId());
//			cd.setTargetCompId(competence.getId());
//			cd.setActivities(getTargetActivityForKeywordSearch(credentialId));
//			data.add(cd);
//		}
//		return data;
		return null;
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<ActivityData> getTargetActivityForKeywordSearch(long credentialId) throws DbConnectionException {
		//TODO cred-redesign-07
//		StringBuilder queryBuilder = new StringBuilder(
//				"SELECT DISTINCT targetAct " +
//				"FROM TargetCompetence1 targetComp " + 
//				"JOIN targetComp.targetActivities targetAct "+
//				"WHERE targetComp.targetCredential.id = :credId " +
//				"ORDER BY targetAct.title");
//		
//		@SuppressWarnings("unchecked")
//		List<TargetActivity1> activities= (List<TargetActivity1>) persistence.currentManager()
//				.createQuery(queryBuilder.toString())
//				.setLong("credId", credentialId)
//				.list();
//		List<ActivityData> data = new ArrayList<>();
//		for(TargetActivity1 tActivity : activities){
//			ActivityData ad = new ActivityData(false);
//			ad.setTitle(tActivity.getTitle());
//			ad.setTargetActivityId(tActivity.getId());
//			ad.setDurationHours((int) (tActivity.getDuration() / 60));
//			ad.setDurationMinutes((int) (tActivity.getDuration() % 60));
//			ad.calculateDurationString();
//			ad.setCompetenceId(tActivity.getTargetCompetence().getCompetence().getId());
//			ad.setActivityId(tActivity.getActivity().getId());
//			data.add(ad);
//		}
//		return data;
		return null;
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public UserGroupPrivilege getUserPrivilegeForCredential(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT credUserGroup.privilege, cred.createdBy.id, cred.visibleToAll " +
					"FROM CredentialUserGroup credUserGroup " +
					"INNER JOIN credUserGroup.userGroup userGroup " +
					"RIGHT JOIN credUserGroup.credential cred " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE cred.id = :credId " +
					"ORDER BY CASE WHEN credUserGroup.privilege = :editPriv THEN 1 WHEN credUserGroup.privilege = :viewPriv THEN 2 ELSE 3 END";
			
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setParameter("editPriv", UserGroupPrivilege.Edit)
					.setParameter("viewPriv", UserGroupPrivilege.Learn)
					.setMaxResults(1)
					.uniqueResult();
			
			if(res == null) {
				return UserGroupPrivilege.None;
			}
			UserGroupPrivilege priv = (UserGroupPrivilege) res[0];
			if(priv == null) {
				priv = UserGroupPrivilege.None;
			}
			long owner = (long) res[1];
			boolean visibleToAll = (boolean) res[2];
			return owner == userId 
				? UserGroupPrivilege.Edit
				: priv == UserGroupPrivilege.None && visibleToAll ? UserGroupPrivilege.Learn : priv;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privilege for credential");
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public UserAccessSpecification getUserPrivilegesForCredential(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT DISTINCT credUserGroup.privilege, cred.createdBy.id, cred.visibleToAll, cred.type, cred.deliveryStart, cred.deliveryEnd " +
					"FROM CredentialUserGroup credUserGroup " +
					"INNER JOIN credUserGroup.userGroup userGroup " +
					"RIGHT JOIN credUserGroup.credential cred " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE cred.id = :credId";
			
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.list();
			
			long owner = 0;
			boolean visibleToAll = false;
			CredentialType type = null;
			Date deliveryStart = null;
			Date deliveryEnd = null;
			boolean first = true;
			Set<UserGroupPrivilege> privs = new HashSet<>();
			for(Object[] row : res) {
				if(row != null) {
					UserGroupPrivilege priv = (UserGroupPrivilege) row[0];
					if(priv == null) {
						priv = UserGroupPrivilege.None;
					}
					privs.add(priv);
					if(first) {
						owner = (long) row[1];
						visibleToAll = (boolean) row[2];
						type = (CredentialType) row[3];
						deliveryStart = (Date) row[4];
						deliveryEnd = (Date) row[5];
						first = false;
					}
				}
			}
			return CredentialUserAccessSpecification.of(privs, visibleToAll, owner == userId, type, 
					deliveryStart, deliveryEnd);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for credential");
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public ResourceAccessData getResourceAccessData(long credId, long userId, ResourceAccessRequirements req) 
			throws DbConnectionException {
		try {
			UserAccessSpecification spec = getUserPrivilegesForCredential(credId, userId);
			return resourceAccessFactory.determineAccessRights(req, spec);
		} catch (DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for credential");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Credential1> getAllCredentials(Session session) throws DbConnectionException {
		try {
			String query=
					"SELECT cred " +
					"FROM Credential1 cred " +
					"WHERE cred.deleted = :deleted";
			  	
			@SuppressWarnings("unchecked")
			List<Credential1> result = session
					.createQuery(query)
					.setBoolean("deleted", false)
				  	.list();
			
			if(result == null) {
				return new ArrayList<>();
			}
			return result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credentials");
		}
	}
	
	//not transactional
	@Override
	public void updateCredentialVisibility(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged, long actorId,
    		LearningContextData lcd) throws DbConnectionException, EventException {
		try {
			List<EventData> events = 
					credManager.updateCredentialVisibilityAndGetEvents(credId, groups, users, visibleToAll, 
							visibleToAllChanged, actorId, lcd);
			for(EventData ev : events) {
				eventFactory.generateEvent(ev);
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> updateCredentialVisibilityAndGetEvents(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged, long userId,
    		LearningContextData lcd) throws DbConnectionException {
		try {
			List<EventData> events = new ArrayList<>();
			if(visibleToAllChanged) {
				Credential1 cred = (Credential1) persistence.currentManager().load(
						Credential1.class, credId);
				cred.setVisibleToAll(visibleToAll);
				
				Credential1 credential = new Credential1();
				credential.setId(credId);
				credential.setVisibleToAll(visibleToAll);
				events.add(eventFactory.generateEventData(
						EventType.VISIBLE_TO_ALL_CHANGED, 
						userId, 
						credential, null, lcd, null));
			}
			events.addAll(userGroupManager.saveCredentialUsersAndGroups(credId, groups, users, userId, lcd).getEvents());
			return events;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential visibility");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isVisibleToAll(long credId) throws DbConnectionException {
		try {
			String query=
					"SELECT cred.visibleToAll " +
					"FROM Credential1 cred " +
					"WHERE cred.id = :credId";
			  	
			Boolean result = (Boolean) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
				  	.uniqueResult();
			
			return result == null ? false : result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential visibility");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getUnassignedCredentialMembersIds(long credId, List<Long> usersToExclude) 
			throws DbConnectionException {
		try {
			String query=
					"SELECT cred.user.id " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.credential.id = :credId " +
					"AND cred.instructor is NULL";
			
			if(usersToExclude != null && !usersToExclude.isEmpty()) {
				query += " AND cred.user.id NOT IN (:excludeList)";
			}
			  	
			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId);
			
			if(usersToExclude != null && !usersToExclude.isEmpty()) {
				q.setParameterList("excludeList", usersToExclude);
			}
			
			@SuppressWarnings("unchecked")
			List<Long> result = q.list();
			
			return result != null ? result : new ArrayList<>();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential unassigned member ids");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public ResourceAccessData getCredentialAccessRights(long credId, long userId, 
			UserGroupPrivilege neededPrivilege) throws DbConnectionException {
		try {
			//TODO cred-redesign-07
//			UserGroupPrivilege priv = getUserPrivilegeForCredential(credId, userId);
//			return new ResourceAccessData(
//					neededPrivilege.isPrivilegeIncluded(priv), 
//					priv == UserGroupPrivilege.Edit
//			);
			return new ResourceAccessData(true, true, true, true, true);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential access rights for user: " + userId);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getActiveDeliveries(long credId) throws DbConnectionException {
		return getDeliveries(credId, true);
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<List<CredentialData>> getCredentialDeliveriesWithAccessRights(long credId, 
			long userId) throws DbConnectionException {
		List<CredentialData> credentials = getDeliveries(credId, false);
		ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
				.addPrivilege(UserGroupPrivilege.Edit);
		ResourceAccessData access = getResourceAccessData(credId, userId, req);
		return RestrictedAccessResult.of(credentials, access);
	}
	
	@Transactional(readOnly = true)
	private List<CredentialData> getDeliveries(long credId, boolean onlyActive) 
			throws DbConnectionException {
		try {
			StringBuilder query= new StringBuilder(
					"SELECT del " +
					"FROM Credential1 del " +
					"WHERE del.type = :type " +
					"AND del.deliveryOf.id = :credId ");
			
			if(onlyActive) {
				query.append("AND (del.deliveryStart IS NOT NULL AND del.deliveryStart <= :now " +
							 "AND (del.deliveryEnd IS NULL OR del.deliveryEnd > :now))");
			}
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setParameter("type", CredentialType.Delivery);
			
			if(onlyActive) {
				q.setTimestamp("now", new Date());
			}
			
			@SuppressWarnings("unchecked")
			List<Credential1> result = q.list();
			
			List<CredentialData> deliveries = new ArrayList<>();
			for(Credential1 d : result) {
				deliveries.add(credentialFactory.getCredentialData(null, d, null, null, false));
			}
			return deliveries;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential deliveries");
		}
	}
	
	
	
	@Override
	@Transactional(readOnly = false)
	public void archiveCredential(long credId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(credId, true);
			
			Credential1 credential = new Credential1();
			credential.setId(credId);
			eventFactory.generateEvent(EventType.ARCHIVE, userId, context, credential, null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while archiving credential");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void restoreArchivedCredential(long credId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(credId, false);
			
			Credential1 credential = new Credential1();
			credential.setId(credId);
			eventFactory.generateEvent(EventType.RESTORE, userId, context, credential, null, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while restoring credential");
		}
	}
	
	private void updateArchivedProperty(long credId, boolean archived) {
		String query = "UPDATE Credential1 cred " +
					   "SET cred.archived = :archived " +
					   "WHERE cred.id = :credId";
		persistence.currentManager()
				.createQuery(query)
				.setBoolean("archived", archived)
				.setLong("credId", credId)
				.executeUpdate();
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countNumberOfCredentials(CredentialSearchFilterManager searchFilter, long userId, 
			UserGroupPrivilege priv) throws DbConnectionException, NullPointerException {
		try {
			if(searchFilter == null) {
				throw new NullPointerException("Search filter cannot be null");
			}
			
			List<Long> ids = getCredentialsIdsWithSpecifiedPrivilegeForUser(userId, priv);
			
			//if user doesn't have needed privilege for any of the credentials we return 0
			if(ids.isEmpty()) {
				return 0;
			}
			
			StringBuilder query = new StringBuilder(
						"SELECT COUNT(c.id) " +
						"FROM Credential1 c " +
						"WHERE c.id IN (:ids) ");
			
			switch(searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse");
					break;
				case ARCHIVED:
					query.append("AND c.archived = :boolTrue");
					break;
			}
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameterList("ids", ids);
			
			switch(searchFilter) {
				case ACTIVE:
					q.setBoolean("boolFalse", false);
					break;
				case ARCHIVED:
					q.setBoolean("boolTrue", true);
					break;
			}
			
			Long count = (Long) q.uniqueResult();
			
			return count != null ? count : 0;
		} catch(NullPointerException npe) {
			throw npe;
		} catch(IllegalStateException ise) {
			throw ise;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while counting number of credentials");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> searchCredentialsForManager(CredentialSearchFilterManager searchFilter, int limit, 
			int page, LearningResourceSortOption sortOption, long userId) 
					throws DbConnectionException, NullPointerException {
		try {
			if(searchFilter == null || sortOption == null) {
				throw new NullPointerException("Invalid argument values");
			}
			
			List<Long> ids = getCredentialsIdsWithSpecifiedPrivilegeForUser(userId, UserGroupPrivilege.Edit);
			
			//if user doesn't have needed privileges for any of the competences, empty list is returned
			if(ids.isEmpty()) {
				return new ArrayList<>();
			}
			
			StringBuilder query = new StringBuilder(
						"SELECT c " +
						"FROM Credential1 c " +
						"WHERE c.id IN (:ids) ");
			
			switch(searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse ");
					break;
				case ARCHIVED:
					query.append("AND c.archived = :boolTrue ");
					break;
			}
			
			query.append("ORDER BY c." + sortOption.getSortFieldDB() + " " + sortOption.getSortOrder());
			
			Query q = persistence.currentManager()
						.createQuery(query.toString())
						.setParameterList("ids", ids);
					
			switch(searchFilter) {
				case ACTIVE:
					q.setBoolean("boolFalse", false);
					break;
				case ARCHIVED:
					q.setBoolean("boolTrue", true);
					break;
			}
			
			@SuppressWarnings("unchecked")
			List<Credential1> creds = q.list();
			
			List<CredentialData> res = new ArrayList<>();
			for(Credential1 c : creds) {
				res.add(credentialFactory.getCredentialData(null, c, null, null, false));
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credentials");
		}
	}
	
	@Transactional(readOnly = true)
	private List<Long> getCredentialsIdsWithSpecifiedPrivilegeForUser(long userId, UserGroupPrivilege priv) 
			throws DbConnectionException {
		try {
			if(priv == null) {
				throw new NullPointerException("Privilege can not be null");
			}
			if(priv == UserGroupPrivilege.None) {
				throw new IllegalStateException("Privilege is not valid");
			}
			StringBuilder query = new StringBuilder(
					"SELECT distinct cred.id " +
					"FROM CredentialUserGroup credUserGroup " +
					"INNER JOIN credUserGroup.userGroup userGroup " +
					"RIGHT JOIN credUserGroup.credential cred " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE credUserGroup.privilege = :priv ");
			
			switch(priv) {
				case Edit:
					query.append("OR cred.createdBy.id = :userId");
					break;
				case Learn:
					query.append("OR cred.visibleToAll = :boolTrue");
					break;
				default:
					break;
			}
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("userId", userId)
					.setParameter("priv", priv);
			
			if(priv == UserGroupPrivilege.Learn) {
				q.setBoolean("boolTrue", true);
			}
			
			@SuppressWarnings("unchecked")
			List<Long> ids = q.list();
			
			return ids;
		} catch(NullPointerException npe) {
			throw npe;
		} catch(IllegalStateException ise) {
			throw ise;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve credential ids");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfAllCompetencesInACredential(long credId, Session session) throws DbConnectionException {
		try {	
			String query = "SELECT cc.competence.id " +
						   "FROM CredentialCompetence1 cc " +
						   "WHERE cc.credential.id = :credId";
	
			@SuppressWarnings("unchecked")
			List<Long> compIds =  session
					.createQuery(query)
					.setLong("credId", credId)
					.list();
			
			return compIds;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competency ids");
		}
	}
	
	//not transactional
	@Override
	public Credential1 createCredentialDelivery(long credentialId, Date start, Date end, long actorId, 
			LearningContextData context) throws DbConnectionException, CompetenceEmptyException, 
			IllegalDataStateException, EventException {
		Result<Credential1> res = credManager.createCredentialDeliveryAndGetEvents(credentialId, start, end, actorId, 
				context);
		for (EventData ev : res.getEvents()) {
			eventFactory.generateEvent(ev);
		}
		
		return res.getResult();
	}
	
	@Override
	@Transactional (readOnly = false)
	public Result<Credential1> createCredentialDeliveryAndGetEvents(long credentialId, Date start, Date end, 
			long actorId, LearningContextData context) throws DbConnectionException, CompetenceEmptyException,
			IllegalDataStateException {
		try {
			Result<Credential1> res = new Result<>();
			//if end date is before start throw exception
			if (start != null && end != null && start.after(end)) {
				throw new IllegalDataStateException("Delivery cannot be ended before it starts");
			}
			
			Credential1 original = (Credential1) persistence.currentManager().load(Credential1.class, credentialId);
	
			Credential1 cred = new Credential1();
			cred.setTitle(original.getTitle());
			cred.setDescription(original.getDescription());
			cred.setCreatedBy(original.getCreatedBy());
			cred.setDateCreated(new Date());
			cred.setTags(new HashSet<Tag>(original.getTags()));
			cred.setHashtags(new HashSet<Tag>(original.getHashtags()));
			cred.setCompetenceOrderMandatory(original.isCompetenceOrderMandatory());
			cred.setDuration(original.getDuration());
			cred.setManuallyAssignStudents(original.isManuallyAssignStudents());
			cred.setDefaultNumberOfStudentsPerInstructor(original.getDefaultNumberOfStudentsPerInstructor());
			cred.setType(CredentialType.Delivery);
			cred.setDeliveryOf(original);
			cred.setDeliveryStart(start);
			cred.setDeliveryEnd(end);
			
			saveEntity(cred);
			
			res.addEvent(eventFactory.generateEventData(EventType.Create, actorId, cred, null, context, null));
			Set<Tag> hashtags = cred.getHashtags();
			if(!hashtags.isEmpty()) {
				Map<String, String> params = new HashMap<>();
				String csv = StringUtil.convertTagsToCSV(hashtags);
				params.put("newhashtags", csv);
				params.put("oldhashtags", "");
				res.addEvent(eventFactory.generateEventData(EventType.UPDATE_HASHTAGS, actorId, cred, null, context, null));
			}
			
			//lock competencies so they cannot be unpublished after they are published here which would violate our integrity rule
			List<CredentialCompetence1> competences = compManager.getCredentialCompetences(
					credentialId, false, false, true, true);
			
			for (CredentialCompetence1 credComp : competences) {
				//create new credential competence which will be referenced by delivery
				CredentialCompetence1 cc = new CredentialCompetence1();
				cc.setCredential(cred);
				cc.setOrder(credComp.getOrder());
				cc.setCompetence(credComp.getCompetence());
				saveEntity(cc);
				
				//publish competency if not published because creating a delivery means that all competencies must be published
				res.addEvents(compManager.publishCompetenceIfNotPublished(credComp.getCompetence(), actorId).getEvents());
				
				cred.getCompetences().add(cc);
			}
			
			res.setResult(cred);
			
			return res;
		} catch (CompetenceEmptyException|IllegalDataStateException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while creating credential delivery");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfAllCredentialDeliveries(long credId, Session session) throws DbConnectionException {
		try {	
			String query = "SELECT d.id " +
						   "FROM Credential1 d " +
						   "WHERE d.deliveryOf.id = :credId";
	
			@SuppressWarnings("unchecked")
			List<Long> deliveries =  session
					.createQuery(query)
					.setLong("credId", credId)
					.list();
			
			return deliveries;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential delivery ids");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ResourceCreator getCredentialCreator(long credId) throws DbConnectionException {
		try {
			String query = "SELECT c.createdBy " +
					"FROM Credential1 c " +
					"WHERE c.id = :credId";

			User createdBy =  (User) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.uniqueResult();

			return userDataFactory.getResourceCreator(createdBy);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential creator");
		}
	}
}
