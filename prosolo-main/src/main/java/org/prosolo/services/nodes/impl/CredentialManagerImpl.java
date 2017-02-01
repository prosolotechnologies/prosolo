package org.prosolo.services.nodes.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.CredentialEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.CredentialInstructor;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.CredentialDataFactory;
import org.prosolo.services.nodes.factory.CredentialInstructorDataFactory;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	@Override
	@Transactional(readOnly = false)
	public Credential1 saveNewCredential(CredentialData data, long creatorId, LearningContextData context) 
			throws DbConnectionException {
		Credential1 cred = null;
		try {
			/*
			 * if credential has no competences, it can't be published
			 */
			if(data.isPublished() && (data.getCompetences() == null || data.getCompetences().isEmpty())) {
				throw new CredentialEmptyException();
			}
			cred = resourceFactory.createCredential(data.getTitle(), data.getDescription(),
					data.getTagsString(), data.getHashtagsString(), creatorId,
					data.getType(), data.isMandatoryFlow(), data.isPublished(), data.getDuration(),
					!data.isAutomaticallyAssingStudents(), data.getCompetences(), data.getScheduledPublishDate());
			
			//generate create event only if credential is published
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
			
			if((data.getStatus() == PublishedStatus.SCHEDULED_PUBLISH 
					|| data.getStatus() == PublishedStatus.SCHEDULED_UNPUBLISH) 
					&& data.getScheduledPublishDate() != null) {
				eventFactory.generateEvent(EventType.SCHEDULED_VISIBILITY_UPDATE, creatorId, cred, null, page, lContext, 
						service, null);
			}

			return cred;
		} catch(CredentialEmptyException cee) {
			logger.error(cee);
			throw cee;
		} catch (Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while saving credential");
		} 
	}

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

	@Override
	@Transactional(readOnly = true)
	public CredentialData getCredentialDataWithProgressIfExists(long credentialId, long userId) 
					throws DbConnectionException {
		CredentialData credData = null;
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			String query = "SELECT DISTINCT cred, creator, targetCred.progress, bookmark.id, targetCred.nextCompetenceToLearnId, targetCred.nextActivityToLearnId " +
						   "FROM Credential1 cred " + 
						   "INNER JOIN cred.createdBy creator " +
						   "LEFT JOIN cred.targetCredentials targetCred " + 
						   "WITH targetCred.user.id = :user " +
						   "LEFT JOIN cred.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE cred.id = :credId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("user", user.getId())
					.setLong("credId", credentialId)
					.uniqueResult();

			if (res != null) {
				Credential1 cred = (Credential1) res[0];
				User creator = (User) res[1];
				Integer paramProgress = (Integer) res[2];
				Long paramBookmarkId = (Long) res[3];
				Long nextCompId = (Long) res[4];
				Long nextActId = (Long) res[5];
				if(paramProgress != null) {
					credData = credentialFactory.getCredentialDataWithProgress(creator, cred, null, 
							null, false, paramProgress.intValue(), nextCompId.longValue(),
							nextActId.longValue());
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
		CredentialData credData = null;
		try {
			User user = (User) persistence.currentManager().load(User.class, userId);
			String query = "SELECT cred, creator, bookmark.id " +
						   "FROM Credential1 cred " + 
						   "INNER JOIN cred.createdBy creator " +
						   "LEFT JOIN cred.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE cred.id = :credId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("user", user.getId())
					.setLong("credId", credentialId)
					.uniqueResult();

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
	public CredentialData getFullTargetCredentialOrCredentialData(long credentialId, long userId)
			throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		CredentialData credData = null;
		try {
			credData = getTargetCredentialData(credentialId, userId, true);
			if (credData == null) {
				credData = getCredentialData(credentialId, true, true, userId, UserGroupPrivilege.View);
			}
			return credData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	@Transactional(readOnly = true)
	private CredentialData getTargetCredentialData(long credentialId, long userId, 
			boolean loadCompetences) throws DbConnectionException {
		CredentialData credData = null;
		try {
			TargetCredential1 res = getTargetCredential(credentialId, userId, true, true);

			if (res != null) {
				credData = credentialFactory.getCredentialData(res.getCreatedBy(), 
						res, res.getTags(), res.getHashtags(), true);
				
				//retrieve privilege to be able to tell if user can edit credential
				UserGroupPrivilege priv = getUserPrivilegeForCredential(credentialId, userId);
				credData.setCanEdit(priv == UserGroupPrivilege.Edit);
				//target credential can always be accessed
				credData.setCanAccess(true);
				
				if(credData != null && loadCompetences) {
					List<CompetenceData1> targetCompData = compManager
							.getTargetCompetencesData(res.getId(), false);
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
			boolean loadCreator, boolean loadTags) throws DbConnectionException {
		User user = (User) persistence.currentManager().load(User.class, userId);
//		Credential1 cred = (Credential1) persistence.currentManager().load(
//				Credential1.class, credentialId);
		StringBuilder queryBuilder = new StringBuilder(
				"SELECT DISTINCT targetCred " +
				"FROM TargetCredential1 targetCred ");
		if(loadCreator) {
			queryBuilder.append("INNER JOIN fetch targetCred.createdBy user ");
		}
		if(loadTags) {
			queryBuilder.append("LEFT JOIN fetch targetCred.tags tags " +
					   		    "LEFT JOIN fetch targetCred.hashtags hashtags ");
		}
		queryBuilder.append("WHERE targetCred.credential.id = :credId " +
				   			"AND targetCred.user = :student");
//			String query = "SELECT targetCred " +
//						   "FROM TargetCredential1 targetCred " + 
//						   "INNER JOIN fetch targetCred.createdBy user " + 
//						   "LEFT JOIN fetch targetCred.tags tags " +
//						   "LEFT JOIN fetch targetCred.hashtags hashtags " +
//						   "WHERE targetCred.credential = :cred " +
//						   "AND targetCred.user = :student";

		TargetCredential1 res = (TargetCredential1) persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", credentialId)
				.setEntity("student", user)
				.uniqueResult();

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialData getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences, long userId, UserGroupPrivilege privilege) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			if(privilege == null) {
				throw new IllegalArgumentException();
			}
			Credential1 cred = getCredential(credentialId, loadCreatorData, userId);
			
			if(cred == null) {
				throw new ResourceNotFoundException();
			}
			
			UserGroupPrivilege priv = getUserPrivilegeForCredential(credentialId, userId);
			
			/*
			 * user can access credential:
			 *  - when he has the right privilege and
			 *  - when credential is published if user has View privilege
			 */
			boolean canAccess = privilege.isPrivilegeIncluded(priv);
			
			//user can't access credential with View privilege if credential is not published
			if(canAccess && priv == UserGroupPrivilege.View && !cred.isPublished()) {
				canAccess = false;
			}
			
			User createdBy = loadCreatorData ? cred.getCreatedBy() : null;
			CredentialData credData = credentialFactory.getCredentialData(createdBy, cred,
					cred.getTags(), cred.getHashtags(), true);
			credData.setCanEdit(priv == UserGroupPrivilege.Edit);
			credData.setCanAccess(canAccess);
			
			if(loadCompetences) {
				/*
				 * if edit privilege is needed for use case, we should include information
				 * wheter user can edit competences.
				 */
				boolean includeCanEdit = privilege == UserGroupPrivilege.Edit;
				/*
				 * we should include not published competences if Edit privilege is needed
				 * for this use case and user has Edit privilege, or if None privilege is needed
				 */
				boolean includeNotPublished = privilege == UserGroupPrivilege.Edit 
						&& priv == UserGroupPrivilege.Edit 
						|| privilege == UserGroupPrivilege.None;
				List<CompetenceData1> compsData = compManager.getCredentialCompetencesData(
						credentialId, false, false , false, includeNotPublished, includeCanEdit, 
						userId);
				credData.setCompetences(compsData);
			}
	
			return credData;
		} catch (ResourceNotFoundException rfe) {
			throw rfe;
		} catch(IllegalArgumentException iae) {
			throw iae;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
	/**
	 * Returns credential with specified id. 
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER is passed for {@code returnType}
	 * parameter credential will be returned even if it is first time draft if creator of credential
	 * is user specified by {@code userId}.
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER is passed for {@code returnType}
	 * parameter credential will be returned even if it is first time draft if credential is created by
	 * university.
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
	@Transactional(readOnly = false)
	public Credential1 updateCredential(CredentialData data, long userId,
			LearningContextData context) throws DbConnectionException, CredentialEmptyException, 
			CompetenceEmptyException {
		try {
			/*
			 * if credential has no competences (that are not removed), it can't be published
			 */
			if(data.isPublished()) {
				if(data.getCompetences() == null) {
					throw new CredentialEmptyException();
				}
				long numberOfComps = data.getCompetences().stream().filter(
						comp -> comp.getObjectStatus() != ObjectStatus.REMOVED).count();
				if(numberOfComps == 0) {
					throw new CredentialEmptyException();
				}
			}
			
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
 			
 			if((data.getStatus() == PublishedStatus.SCHEDULED_PUBLISH 
 					|| data.getStatus() == PublishedStatus.SCHEDULED_UNPUBLISH)
 					&& data.getScheduledPublishDate() != null && data.isScheduledPublicDateChanged()) {
				Credential1 cr = new Credential1();
				cr.setId(data.getId());
 				eventFactory.generateEvent(EventType.SCHEDULED_VISIBILITY_UPDATE, userId, cr, null, page, lContext, 
						service, null);
			} 
// 			else if(data.getStatus() != PublishedStatus.SCHEDULED_PUBLISH 
//					&& data.getStatus() != PublishedStatus.SCHEDULED_UNPUBLISH &&
//					data.getScheduledPublishDate() == null && data.isScheduledPublicDateChanged()) {
//				Credential1 cr = new Credential1();
//				cr.setId(data.getId());
// 				eventFactory.generateEvent(EventType.CANCEL_SCHEDULED_VISIBILITY_UPDATE, userId, cr, null, page, lContext, 
//						service, null);
//			}

		    return cred;
		} catch(CredentialEmptyException cee) {
			logger.error(cee);
			//cee.printStackTrace();
			throw cee;
		} catch(CompetenceEmptyException ceex) {
			logger.error(ceex);
			//ceex.printStackTrace();
			throw ceex;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential");
		}
	}
	
	private long getRecalculatedDuration(long credId) {
		String query = "SELECT sum(c.duration) FROM CredentialCompetence1 cc " +
					   "INNER JOIN cc.competence c " +
					   "WHERE cc.credential.id = :credId " +
					   "AND c.published = :published";
		Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setBoolean("published", true)
				.uniqueResult();
		
		return res != null ? res : 0;
	}
	
	private void fireEditEvent(CredentialData data, long userId, 
			Credential1 cred, long originalVersionId, String page, String context,
			String service) throws EventException {   
	    Map<String, String> params = new HashMap<>();
	    CredentialChangeTracker changeTracker = new CredentialChangeTracker(data.isPublished(),
	    		data.isPublishedChanged(), data.isTitleChanged(), data.isDescriptionChanged(), false,
	    		data.isTagsStringChanged(), data.isHashtagsStringChanged(), 
	    		data.isMandatoryFlowChanged());
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    eventFactory.generateEvent(EventType.Edit, userId, cred, null, page, context, service, params);
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Credential1> updateCredentialData(CredentialData data, long creatorId) {
		Result<Credential1> res = new Result<>();
		Credential1 credToUpdate = (Credential1) persistence.currentManager()
				.load(Credential1.class, data.getId());
		
		credToUpdate.setTitle(data.getTitle());
		credToUpdate.setDescription(data.getDescription());
		credToUpdate.setCompetenceOrderMandatory(data.isMandatoryFlow());
		credToUpdate.setPublished(data.isPublished());
		credToUpdate.setStudentsCanAddCompetences(data.isStudentsCanAddCompetences());
		credToUpdate.setManuallyAssignStudents(!data.isAutomaticallyAssingStudents());
		credToUpdate.setDefaultNumberOfStudentsPerInstructor(data.getDefaultNumberOfStudentsPerInstructor());
		//credToUpdate.setVisible(data.isCredVisible());
		credToUpdate.setScheduledPublishDate(data.getScheduledPublishDate());
		
    	if(data.isTagsStringChanged()) {
    		credToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getTagsString())));		     
    	}
    	if(data.isHashtagsStringChanged()) {
    		credToUpdate.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getHashtagsString())));
    	}
	   
		List<CompetenceData1> comps = data.getCompetences();
	    if(comps != null) {
	    	/*
			 * List of competence ids so we can call method that will publish all draft
			 * competences
			 */
			List<Long> compIds = new ArrayList<>();
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
	    				compIds.add(cd.getCompetenceId());
	    				//if competence is added to credential
	    				User user = new User();
	    				user.setId(creatorId);
	    				Competence1 competence = new Competence1();
	    				competence.setId(comp.getId());
	    				res.addEvent(generateEvent(EventType.Attach, user, competence, credToUpdate));
	    				break;
	    			case CHANGED:
	    				CredentialCompetence1 cc2 = (CredentialCompetence1) persistence.currentManager().load(
			    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
	    				cc2.setOrder(cd.getOrder());
	    				compIds.add(cd.getCompetenceId());
	    				break;
	    			case REMOVED:
	    				CredentialCompetence1 cc3 = (CredentialCompetence1) persistence.currentManager().load(
			    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
	    				delete(cc3);
	    				break;
	    			case UP_TO_DATE:
	    				compIds.add(cd.getCompetenceId());
	    				break;
	    		}
	    	}
	    	
	    	if(data.isPublished()) {
    			//compManager.publishDraftCompetencesWithoutDraftVersion(compIds);
	    		List<EventData> events = compManager.publishCompetences(data.getId(), compIds, creatorId);
	    		res.addEvents(events);
    		}
	    }
	    persistence.currentManager().flush();
	    credToUpdate.setDuration(getRecalculatedDuration(data.getId()));
	    res.setResult(credToUpdate);
	    return res;
	}
	
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

	@Override
	@Transactional(readOnly = false)
	public CredentialData enrollInCredential(long credentialId, long userId, LearningContextData context) 
			throws DbConnectionException {
		return enrollInCredential(credentialId, userId, 0, context, null).getResult();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional(readOnly = false)
	private Result<CredentialData> enrollInCredential(long credentialId, long userId, 
			long instructorThatForcedEnrollId, LearningContextData context, Class[] observersToExclude) 
			throws DbConnectionException {
		try {
			Result<CredentialData> result = new Result<>();
			
			User user = (User) persistence.currentManager().load(User.class, userId);
			
			Credential1 cred = getCredential(credentialId, false, 0);
			TargetCredential1 targetCred = createTargetCredential(cred, user);
			long instructorId = 0;
			String page = null;
			String lContext = null;
			String service = null;
			if(context != null) {
				page = context.getPage();
				lContext = context.getLearningContext();
				service = context.getService();
			}
			if(cred.getType() == LearningResourceType.UNIVERSITY_CREATED && 
					!cred.isManuallyAssignStudents()) {
				List<TargetCredential1> targetCredIds = new ArrayList<>();
				targetCredIds.add(targetCred);
				StudentAssignData res = credInstructorManager.assignStudentsToInstructorAutomatically(
						credentialId, targetCredIds, 0, false);
				List<StudentInstructorPair> assigned = res.getAssigned();
				if(assigned.size() == 1) {
					StudentInstructorPair pair = assigned.get(0);
					//we need user id, not instructor id
					instructorId = pair.getInstructor().getUser().getId();
					
					User target = new User();
					target.setId(instructorId);
					User object = new User();
					object.setId(userId);
					Map<String, String> params = new HashMap<>();
					params.put("credId", credentialId + "");
					if(instructorThatForcedEnrollId > 0) {
						params.put("forcedEnroll", "true");
						params.put("instructorThatEnrolledStudent", instructorThatForcedEnrollId + "");
					}
					result.addFiredEvent(eventFactory.generateEvent(
							EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, 
							userId, object, target, 
							page, lContext, service, 
							observersToExclude, params));
//					eventFactory.generateEvent(EventType.STUDENT_ASSIGNED_TO_INSTRUCTOR, 
//							userId, object, target, page, lContext, service, new Class[] {NodeChangeObserver.class}, params);
				}
	    	}
			
			if(cred.getType() == LearningResourceType.UNIVERSITY_CREATED) {
				//create default assessment for user
				assessmentManager.createDefaultAssessment(targetCred, instructorId);
			}
			
			CredentialData cd = credentialFactory.getCredentialData(targetCred.getCreatedBy(), 
					targetCred, targetCred.getTags(), targetCred.getHashtags(), true);
			
			List<TargetCompetence1> comps = targetCred.getTargetCompetences();
			if(comps != null) {
				for(TargetCompetence1 tc : comps) {
					CompetenceData1 compData = competenceFactory.getCompetenceData(null, tc, null, 
							null, true);
					cd.getCompetences().add(compData);
				}
			}
			
			/*
			 * Loaded user instance is not used because that would lead to select query
			 * when trying to get name of a user while capturing ENROLL_COURSE event.
			 */
			User actor = new User();
			actor.setId(userId);
			Map<String, String> params = new HashMap<>();
			params.put("instructorId", instructorId + "");
			String dateString = null;
			Date date = targetCred.getDateCreated();
			if(date != null) {
				DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				dateString = df.format(date);
			}
			params.put("dateEnrolled", dateString);
			if(instructorThatForcedEnrollId > 0) {
				params.put("forcedEnroll", "true");
				params.put("instructorThatEnrolledStudent", instructorThatForcedEnrollId + "");
			}
			result.addFiredEvent(eventFactory.generateEvent(
					EventType.ENROLL_COURSE, 
					actor.getId(), cred, null, 
					page, lContext, service, 
					observersToExclude, params));
//			eventFactory.generateEvent(EventType.ENROLL_COURSE, actor.getId(), cred, null, 
//					page, lContext, service, params);
			
			result.setResult(cd);
			return result;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling in a credential");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void enrollStudentsInCredential(long credId, long instructorId, List<Long> userIds, LearningContextData context) 
			throws DbConnectionException {
		if(userIds != null) {
			try {
				for(long userId : userIds) {
					enrollInCredential(credId, userId, instructorId, context, null);
				}
			} catch(Exception e) {
				throw new DbConnectionException("Error while enrolling students in a credential");
			}
		}
	}
	
	@Transactional(readOnly = false)
	private TargetCredential1 createTargetCredential(Credential1 cred, User user) {
		TargetCredential1 targetCred = new TargetCredential1();
		targetCred.setTitle(cred.getTitle());
		targetCred.setDescription(cred.getDescription());
		targetCred.setCredential(cred);
		targetCred.setUser(user);
		Date now = new Date();
		targetCred.setDateCreated(now);
		targetCred.setDateStarted(now);
		targetCred.setLastAction(now);
		targetCred.setCredentialType(cred.getType());

		if(cred.getTags() != null) {
			Set<Tag> tags = new HashSet<>();
			for(Tag tag : cred.getTags()) {
				tags.add(tag);
			}
			targetCred.setTags(tags);
		}
		
		if(cred.getHashtags() != null) {
			Set<Tag> hashtags = new HashSet<>();
			for(Tag tag : cred.getHashtags()) {
				hashtags.add(tag);
			}
			targetCred.setHashtags(hashtags);
		}
		
		targetCred.setDuration(cred.getDuration());
		targetCred.setStudentsCanAddCompetences(cred.isStudentsCanAddCompetences());
		targetCred.setCompetenceOrderMandatory(cred.isCompetenceOrderMandatory());
		targetCred.setCreatedBy(cred.getCreatedBy());
		saveEntity(targetCred);
		
		List<TargetCompetence1> targetComps = compManager.createTargetCompetences(cred.getId(), 
				targetCred);
		targetCred.setTargetCompetences(targetComps);
		
		/*
		 * set first competence and first activity in first competence as next to learn
		 */
		targetCred.setNextCompetenceToLearnId(targetComps.get(0).getCompetence().getId());
		
		if (!targetComps.isEmpty() && !targetComps.get(0).getTargetActivities().isEmpty()) {
			targetCred.setNextActivityToLearnId(targetComps.get(0).getTargetActivities()
					.get(0).getActivity().getId());
		}
		return targetCred;
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
			 * If duration of added competence is greater than 0 and competence is published
			 * update credential duration
			*/
			//TODO check if this requires select + update and if so, use hql update instead
			if(comp.getDuration() > 0 && comp.isPublished()) {
				cred.setDuration(cred.getDuration() + comp.getDuration());
			}
			
			User user = new User();
			user.setId(userId);
			Competence1 competence = new Competence1();
			competence.setId(comp.getId());
			events.add(generateEvent(EventType.Attach, user, competence, cred));
			
			return events;
		} catch(Exception e) { 
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding competence to credential");
		}
		
	}
	
	private EventData generateEvent(EventType attach, User user, Competence1 comp, Credential1 cred) {
		EventData event = new EventData();
		event.setEventType(EventType.Attach);
		event.setActorId(user.getId());
		event.setObject(comp);
		event.setTarget(cred);
		return event;
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

	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialTags(long credentialId) 
			throws DbConnectionException {	
		return getCredentialTags(credentialId, persistence.currentManager());

	}
	
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
	
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCredentialHashtags(long credentialId) 
			throws DbConnectionException {
		return getCredentialHashtags(credentialId, persistence.currentManager());
	}
	
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
	
	@Transactional(readOnly = false)
	private void updateTargetCredentialTagsForUncompletedCredentials(long credentialId) 
			throws DbConnectionException {
		try {
			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(
					credentialId, true);
			List<Tag> tags = getCredentialTags(credentialId);
			for(TargetCredential1 tc : targetCredentials) {
				tc.getTags().clear();
				for(Tag tag : tags) {
					tc.getTags().add(tag);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	@Transactional(readOnly = false)
	private void updateTargetCredentialHashtagsForUncompletedCredentials(long credentialId) 
			throws DbConnectionException {
		try {
			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(
					credentialId, true);
			List<Tag> hashtags = getCredentialHashtags(credentialId);
			for(TargetCredential1 tc : targetCredentials) {
				tc.getHashtags().clear();
				for(Tag hashtag : hashtags) {
					tc.getHashtags().add(hashtag);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	
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
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialBookmark> getBookmarkedByIds(long credId) throws DbConnectionException {
		return getBookmarkedByIds(credId, persistence.currentManager());
	}
	
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
	public void updateCredentialAndCompetenceProgressAndNextActivityToLearn(long credId, 
			long targetCompId, long targetActIda, long userId, LearningContextData contextData) 
					throws DbConnectionException {
		try {
			String query = "SELECT tCred.id, comp.id, tComp.id, act.id, tComp.progress, tAct.completed " +
						   "FROM TargetCredential1 tCred " +
						   "LEFT JOIN tCred.targetCompetences tComp " +
						   "INNER JOIN tComp.competence comp " +
						   "LEFT JOIN tComp.targetActivities tAct " +
						   "INNER JOIN tAct.activity act " +
						   "WHERE tCred.credential.id = :credId " +
						   "AND tCred.user.id = :userId " +
						   "ORDER BY tComp.order, tAct.order";
		
			@SuppressWarnings("unchecked")
			List<Object[]> res =  persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credId)
				.setLong("userId", userId)
				.list();
			
			long nextCompToLearnId = 0;
			long nextActToLearnId = 0;
			long currentCompId = 0;
			int cumulativeCredProgress = 0;
			int numberOfCompetencesInCredential = 0;
			int cumulativeCompProgress = 0;
			int numberOfActivitiesInACompetence = 0;
			long nextActToLearnInACompetenceId = 0;
			long changedCompId = 0;
			if(res != null) {
				for(Object[] obj : res) {
					long compId = (long) obj[1];
					long tCompId = (long) obj[2];
					long actId = (long) obj[3];
					long compProgress = (int) obj[4];
					boolean actCompleted = (boolean) obj[5];
					
					int progress = actCompleted ? 100 : 0;
					if(tCompId != currentCompId) {
						currentCompId = tCompId;
						numberOfCompetencesInCredential ++;
						if(tCompId != targetCompId) {
							cumulativeCredProgress += compProgress;
						}
					}
					if(tCompId == targetCompId) {
						if(changedCompId == 0) {
							changedCompId = compId;
						}
						cumulativeCompProgress += progress;
						numberOfActivitiesInACompetence ++;
						if(nextActToLearnInACompetenceId == 0 && !actCompleted) {
							nextActToLearnInACompetenceId = actId;
						}
					}
					if(nextActToLearnId == 0 && !actCompleted) {
						nextActToLearnId = actId;
						nextCompToLearnId = compId;
					}
				}
				long targetCredId = (long) res.get(0)[0];
				int finalCompProgress = cumulativeCompProgress / numberOfActivitiesInACompetence;
				int finalCredProgress = (cumulativeCredProgress + finalCompProgress) 
						/ numberOfCompetencesInCredential;
				

				String updateCredQuery = "UPDATE TargetCredential1 targetCred SET " +
										 "targetCred.progress = :progress, " +
										 "targetCred.nextCompetenceToLearnId = :nextCompToLearnId, " +
										 "targetCred.nextActivityToLearnId = :nextActToLearnId " +
										 "WHERE targetCred.id = :targetCredId";
				persistence.currentManager()
					.createQuery(updateCredQuery)
					.setInteger("progress", finalCredProgress)
					.setLong("nextCompToLearnId", nextCompToLearnId)
					.setLong("nextActToLearnId", nextActToLearnId)
					.setLong("targetCredId", targetCredId)
					.executeUpdate();
				
				/*
				 * Update competence progress and id of next activity to learn.
				 * Next activity to learn should be updated if competence progress is not 100.
				 * If competence is completed there is no need to update next activity to learn.
				 */
				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE TargetCompetence1 targetComp SET " +
		 				 	   "targetComp.progress = :progress ");
				if(finalCompProgress != 100) {
					builder.append(", targetComp.nextActivityToLearnId = :nextActToLearnId ");
				}
				builder.append("WHERE targetComp.id = :targetCompId");
				
				Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setInteger("progress", finalCompProgress)
					.setLong("targetCompId", targetCompId);
				if(finalCompProgress != 100) {
					q.setLong("nextActToLearnId", nextActToLearnInACompetenceId);
				}
				q.executeUpdate();
				
				User user = new User();
				user.setId(userId);
				TargetCredential1 tCred = new TargetCredential1();
				tCred.setId(targetCredId);
				Credential1 cred = new Credential1();
				cred.setId(credId);
				tCred.setCredential(cred);
				String lcPage = contextData != null ? contextData.getPage() : null; 
				String lcContext = contextData != null ? contextData.getLearningContext() : null; 
				String lcService = contextData != null ? contextData.getService() : null; 
				eventFactory.generateChangeProgressEvent(user.getId(), tCred, finalCredProgress, 
						lcPage, lcContext, lcService, null);
				if(finalCredProgress == 100) {
					eventFactory.generateEvent(EventType.Completion, user.getId(), tCred, null,
							lcPage, lcContext, lcService, null);
				}
				if(finalCompProgress == 100) {
					TargetCompetence1 tComp = new TargetCompetence1();
					tComp.setId(targetCompId);
					Competence1 competence = new Competence1();
					competence.setId(changedCompId);
					tComp.setCompetence(competence);
					
					eventFactory.generateEvent(EventType.Completion, user.getId(), tComp, null,
							lcPage, lcContext, lcService, null);
				}
				
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential progress");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCredentialTitle(long id) throws DbConnectionException {
		return getCredentialTitleForCredentialWithType(id, null);
	}
	
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
	
	@Override
	@Transactional(readOnly = true)
	public String getCredentialTitleForCredentialWithType(long id, LearningResourceType type) 
			throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder(
				   "SELECT cred.title " +
				   "FROM Credential1 cred " +
				   "WHERE cred.id = :credId ");
			
			if(type != null) {
				queryBuilder.append("AND cred.type = :type");
			}
			
			Query q = persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", id);
			
			if(type != null) {
				q.setParameter("type", type);
			}
			
			String title = (String) q.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential title");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getTargetCredentialsProgressAndInstructorInfoForUser(long userId) 
			throws DbConnectionException {  
		return getTargetCredentialsProgressAndInstructorInfoForUser(userId, persistence.currentManager());
	}
	
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
	public List<Long> getTargetCredentialIdsForUsers(List<Long> userIds, long credId) 
			throws DbConnectionException {
		try {
			if(userIds == null || userIds.isEmpty()) {
				return null;
			}
			String query = 
					"SELECT cred.id " +
					"FROM TargetCredential1 cred " +
					"WHERE cred.credential.id = :credId " +
					"AND cred.user.id IN (:userIds)";
			
			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
					.setLong("credId", credId)
					.setParameterList("userIds", userIds)
					.list();
			if(res == null) {
				return new ArrayList<>();
			} 
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user credential ids");
		}
	}
	
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
	public CredentialData getTargetCredentialTitleAndLearningOrderInfo(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT cred.title, cred.nextCompetenceToLearnId, cred.nextActivityToLearnId, cred.competenceOrderMandatory " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.user.id = :userId " +
						   "AND cred.credential.id = :credId";
			
			Object[] res = (Object[]) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("credId", credId)
				.uniqueResult();
			
			if(res != null) {
				String title = (String) res[0];
				long nextComp = (long) res[1];
				long nextAct = (long) res[2];
				boolean mandatoryOrder = (boolean) res[3];
				
				CredentialData cd = new CredentialData(false);
				cd.setTitle(title);
				cd.setNextCompetenceToLearnId(nextComp);
				cd.setNextActivityToLearnId(nextAct);
				cd.setMandatoryFlow(mandatoryOrder);
				return cd;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential data");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public Object[] getCredentialAndCompetenceTitle(long credId, long compId) 
			throws DbConnectionException {
		try {
			String query = 
					"SELECT DISTINCT cred.title, comp.title " +
					"FROM Credential1 cred, Competence1 comp " +
					"WHERE cred.id = :credId " +
						"AND comp.id = :compId";
			
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setLong("compId", compId)
					.uniqueResult();
			
			if (res != null) {
				return res;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential and competence title");
		}
	}
	
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
	
	@Override
	@Transactional(readOnly = true)
	public CredentialData getTargetCredentialNextCompAndActivityToLearn(long credId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT cred.nextCompetenceToLearnId, cred.nextActivityToLearnId " +
						   "FROM TargetCredential1 cred " +
						   "WHERE cred.user.id = :userId " +
						   "AND cred.credential.id = :credId";
			
			Object[] res = (Object[]) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("credId", credId)
				.uniqueResult();
			
			if(res != null) {
				long nextComp = (long) res[0];
				long nextAct = (long) res[1];
				
				CredentialData cd = new CredentialData(false);
				cd.setNextCompetenceToLearnId(nextComp);
				cd.setNextActivityToLearnId(nextAct);
				return cd;
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential data");
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
					sd.setCredProgress(tc.getProgress());
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
				long completed = (long) res[2];
				CredentialMembersSearchFilter completedFilter = new CredentialMembersSearchFilter(
						CredentialMembersSearchFilterValue.Completed, completed);
				return new CredentialMembersSearchFilter[] {allFilter, unassignedFilter, completedFilter};
			}
			
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving filters");
		}
	}
	
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
					.setParameter("viewPriv", UserGroupPrivilege.View)
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
				: priv == UserGroupPrivilege.None && visibleToAll ? UserGroupPrivilege.View : priv;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privilege for credential");
		}
	}
	
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
	
	@Override
	@Transactional(readOnly = false)
	public void updateCredentialVisibility(long credId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged) 
    				throws DbConnectionException {
		try {
			if(visibleToAllChanged) {
				Credential1 cred = (Credential1) persistence.currentManager().load(
						Credential1.class, credId);
				cred.setVisibleToAll(visibleToAll);
			}
			userGroupManager.saveCredentialUsersAndGroups(credId, groups, users);
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
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential unassigned member ids");
		}
	}
}
