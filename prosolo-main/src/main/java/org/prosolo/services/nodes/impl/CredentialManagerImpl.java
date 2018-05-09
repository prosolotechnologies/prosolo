package org.prosolo.services.nodes.impl;

import com.amazonaws.services.identitymanagement.model.EntityAlreadyExistsException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.feeds.FeedSourceManager;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.config.credential.CredentialLoadConfig;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.credential.CategorizedCredentialsData;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.credential.TargetCredentialData;
import org.prosolo.services.nodes.data.instructor.StudentAssignData;
import org.prosolo.services.nodes.data.instructor.StudentInstructorPair;
import org.prosolo.services.nodes.data.resourceAccess.*;
import org.prosolo.services.nodes.factory.*;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.prosolo.services.util.roles.SystemRoleNames;
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
	@Inject
	private CredentialManager self;
	@Inject
	private UserDataFactory userDataFactory;
	@Inject
	private ActivityDataFactory activityDataFactory;
	@Inject
	private RoleManager roleManager;
	@Inject
	private UnitManager unitManager;
	@Inject
	private LearningResourceLearningStageDataFactory learningResourceLearningStageDataFactory;
	@Inject
	private OrganizationManager orgManager;
	@Inject private RubricManager rubricManager;

	@Override
	//nt
	public Credential1 saveNewCredential(CredentialData data, UserContextData context)
			throws DbConnectionException {
		//self-invocation
		Result<Credential1> res = self.saveNewCredentialAndGetEvents(data, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<Credential1> saveNewCredentialAndGetEvents(CredentialData data, UserContextData context)
			throws DbConnectionException {
		try {
			Credential1 cred = new Credential1();
			cred.setOrganization((Organization) persistence.currentManager().load(Organization.class,
					context.getOrganizationId()));
			cred.setCreatedBy(loadResource(User.class, context.getActorId()));
			cred.setType(CredentialType.Original);
			cred.setTitle(data.getTitle());
			cred.setDescription(data.getDescription());
			cred.setDateCreated(new Date());
			cred.setCompetenceOrderMandatory(data.isMandatoryFlow());
			cred.setDuration(data.getDuration());
			cred.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(data.getTagsString())));
			cred.setHashtags(new HashSet<>(tagManager.parseCSVTagsAndSave(data.getHashtagsString())));
			cred.setManuallyAssignStudents(!data.isAutomaticallyAssingStudents());
			cred.setCategory(data.getCategory() != null
					? (CredentialCategory) persistence.currentManager().load(CredentialCategory.class, data.getCategory().getId())
					: null);

			if (data.isLearningStageEnabled()) {
				cred.setLearningStage((LearningStage) persistence.currentManager().load(LearningStage.class, data.getLearningStage().getId()));
			}

			setAssessmentRelatedData(cred, data, true);

			saveEntity(cred);

			if (data.getAssessmentTypes() != null) {
				for (AssessmentTypeConfig atc : data.getAssessmentTypes()) {
					CredentialAssessmentConfig cac = new CredentialAssessmentConfig();
					cac.setCredential(cred);
					cac.setAssessmentType(atc.getType());
					cac.setEnabled(atc.isEnabled());
					saveEntity(cac);
				}
			}

			if (data.getCompetences() != null) {
				for (CompetenceData1 cd : data.getCompetences()) {
					/*
					TODO learning in stages - stage is not set here because adding existing competences
					through search will be disabled
					 */
					CredentialCompetence1 cc = new CredentialCompetence1();
					cc.setOrder(cd.getOrder());
					cc.setCredential(cred);
					Competence1 comp = (Competence1) persistence.currentManager().load(
							Competence1.class, cd.getCompetenceId());
					cc.setCompetence(comp);
					saveEntity(cc);
				}
			}

			Result<Credential1> res = new Result<>();

			res.appendEvent(eventFactory.generateEventData(
					EventType.Create, context, cred, null, null, null));

			//add Edit privilege to the credential creator
			res.appendEvents(userGroupManager.createCredentialUserGroupAndSaveNewUser(
					context.getActorId(), cred.getId(),
					UserGroupPrivilege.Edit, true, context).getEventQueue());

			//add credential to all units where credential creator is manager
			res.appendEvents(addCredentialToDefaultUnits(cred.getId(), context));

			res.setResult(cred);

			logger.info("New credential is created with id " + cred.getId());

			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving credential");
		}
	}

	private void setAssessmentRelatedData(Credential1 credential, CredentialData data, boolean updateRubric) throws IllegalDataStateException {
		credential.setGradingMode(data.getAssessmentSettings().getGradingMode());
		switch (data.getAssessmentSettings().getGradingMode()) {
			case AUTOMATIC:
				credential.setRubric(null);
				break;
			case MANUAL:
				if (updateRubric) {
					credential.setRubric(rubricManager.getRubricForLearningResource(data.getAssessmentSettings()));
				}
				break;
			case NONGRADED:
				credential.setRubric(null);
				break;
		}
		credential.setMaxPoints(
				isPointBasedCredential(credential.getGradingMode(), credential.getRubric())
						? (data.getAssessmentSettings().getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getAssessmentSettings().getMaxPointsString()))
						: 0);
	}

	private boolean isPointBasedCredential(GradingMode gradingMode, Rubric rubric) {
		return gradingMode == GradingMode.MANUAL && (rubric == null || rubric.getRubricType() == RubricType.POINT || rubric.getRubricType() == RubricType.POINT_RANGE);
	}

	/**
	 * Connects credential to all units credential creator (context actor) is manager in.
	 *
	 * @param credId
	 * @param context
	 * @return
	 */
	private EventQueue addCredentialToDefaultUnits(long credId, UserContextData context) {
		long managerRoleId = roleManager.getRoleIdByName(SystemRoleNames.MANAGER);
		List<Long> unitsWithManagerRole = unitManager.getUserUnitIdsInRole(context.getActorId(), managerRoleId);
		EventQueue events = EventQueue.newEventQueue();
		for (long unitId : unitsWithManagerRole) {
			events.appendEvents(unitManager.addCredentialToUnitAndGetEvents(credId, unitId, context).getEventQueue());
		}
		return events;
	}

	//non transactional
	@Override
	public void deleteDelivery(long deliveryId, UserContextData context)
			throws DbConnectionException, StaleDataException, DataIntegrityViolationException {
		//self invocation so spring can intercept the call and start transaction
		Result<Void> res = self.deleteDeliveryAndGetEvents(deliveryId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> deleteDeliveryAndGetEvents(long deliveryId, UserContextData context)
			throws DbConnectionException, DataIntegrityViolationException, StaleDataException {
		try {
			Result<Void> res = new Result<>();
			if (deliveryId > 0) {
				Credential1 del = new Credential1();
				del.setId(deliveryId);
				res.appendEvent(eventFactory.generateEventData(EventType.Delete,
						context, del, null, null, null));
			
				//delete delivery from database
				deleteById(Credential1.class, deliveryId, persistence.currentManager());
			}
			//to force eventual exceptions on commit so they can be caught
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
			String query = "SELECT DISTINCT cred, creator, targetCred.progress, bookmark.id, targetCred.nextCompetenceToLearnId, cat " +
					"FROM Credential1 cred " +
					"LEFT JOIN cred.category cat " +
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
				CredentialCategory category = (CredentialCategory) res[5];
				if (paramProgress != null) {
					credData = credentialFactory.getCredentialDataWithProgress(creator, cred, category, null,
							null, false, paramProgress.intValue(), nextCompId.longValue());
				} else {
					credData = credentialFactory.getCredentialData(creator, cred, category, null, null, null, false);
				}
				if (paramBookmarkId != null) {
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
			StringBuilder query = new StringBuilder(
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

				credData = credentialFactory.getCredentialData(creator, cred, null, null, null, null, false);

				if (paramBookmarkId != null) {
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

	@Override
	@Transactional(readOnly = true)
	public CredentialData getFullTargetCredentialOrCredentialData(long credentialId, long userId)
			throws ResourceNotFoundException, DbConnectionException {
		CredentialData credData;
		try {
			credData = getTargetCredentialData(credentialId, userId,
					CredentialLoadConfig.of(true, true, true, false,false, true, false, true,
							CompetenceLoadConfig.of(true, true, false, false, false)));
			if (credData == null) {
				return getCredentialData(credentialId, true, false, true, true, userId, AccessMode.USER);
			}

			return credData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialData getTargetCredentialData(long credentialId, long userId,
												 CredentialLoadConfig credentialLoadConfig) throws DbConnectionException {
		CredentialData credData;
		try {
			TargetCredential1 res = getTargetCredential(credentialId, userId, credentialLoadConfig);

			if (res != null) {
				Set<CredentialAssessmentConfig> aConfig = credentialLoadConfig.isLoadAssessmentConfig() ? res.getCredential().getAssessmentConfig() : null;
				User creator = credentialLoadConfig.isLoadCreator() ? res.getCredential().getCreatedBy() : null;
				User student = credentialLoadConfig.isLoadStudent() ? res.getUser() : null;
				Set<Tag> tags = credentialLoadConfig.isLoadTags() ? res.getCredential().getTags() : null;
				Set<Tag> hashtags = credentialLoadConfig.isLoadTags() ? res.getCredential().getHashtags() : null;
				credData = credentialFactory.getCredentialData(
						res, creator, student, aConfig, tags, hashtags, false);
				if (credentialLoadConfig.isLoadAssessmentCount()) {
					credData.setNumberOfAssessments(assessmentManager.getNumberOfApprovedAssessmentsForUserCredential(res.getId()));
				}
				if (credData != null && credentialLoadConfig.isLoadCompetences()) {
					List<CompetenceData1> targetCompData = compManager
							.getCompetencesForCredential(credentialId, userId, credentialLoadConfig.getCompetenceLoadConfig());
					credData.setCompetences(targetCompData);
					if (credentialLoadConfig.isLoadAssessmentConfig()) {
						for (AssessmentTypeConfig conf : credData.getAssessmentTypes()) {
							if (conf.isEnabled()) {
								conf.setGradeSummary(assessmentManager.getCredentialAssessmentsGradeSummary(credentialId, userId, conf.getType()));
							}
						}
					}
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
	public TargetCredential1 getTargetCredential(long credentialId, long userId, CredentialLoadConfig credentialLoadConfig) throws DbConnectionException {
//		boolean loadCreator, boolean loadStudent,
//		boolean loadTags, boolean loadInstructor
		User user = (User) persistence.currentManager().load(User.class, userId);

		StringBuilder queryBuilder = new StringBuilder(
				"SELECT targetCred " +
						"FROM TargetCredential1 targetCred " +
						"INNER JOIN fetch targetCred.credential cred ");
		if (credentialLoadConfig.isLoadCreator()) {
			queryBuilder.append("INNER JOIN fetch cred.createdBy user ");
		}
		if (credentialLoadConfig.isLoadStudent()) {
			queryBuilder.append("INNER JOIN fetch targetCred.user ");
		}
		if (credentialLoadConfig.isLoadTags()) {
			queryBuilder.append("LEFT JOIN fetch cred.tags tags " +
					"LEFT JOIN fetch cred.hashtags hashtags ");
		}
		if (credentialLoadConfig.isLoadInstructor()) {
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
	public CredentialData getCredentialDataForEdit(long credentialId) throws DbConnectionException {
		try {
			CredentialData cd = getCredentialData(credentialId, true, true, true, true, 0, AccessMode.MANAGER);
			/*
			if learning in stages is enabled for credential, learning stages with credential info for each stage are loaded
			but if learning in stages is not enabled, only learning stages are retrieved.
			  */
			if (cd.isLearningStageEnabled()) {
				cd.addLearningStages(getCredentialLearningStagesData(cd.getOrganizationId(), cd.getFirstLearningStageCredentialId()));
			} else {
				cd.addLearningStages(orgManager.getOrganizationLearningStagesForLearningResource(cd.getOrganizationId()));
			}
			return cd;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<LearningResourceLearningStage> getCredentialLearningStagesData(long orgId, long firstStageCredId) throws DbConnectionException {
		try {
			String query =
					"SELECT ls, cred.id FROM LearningStage ls " +
							"LEFT JOIN ls.credentials cred " +
							"WITH cred.id = :firstStageCredId OR cred.firstLearningStageCredential.id = :firstStageCredId " +
							"WHERE ls.organization.id = :orgId " +
							"ORDER BY ls.order";

			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setLong("orgId", orgId)
					.setLong("firstStageCredId", firstStageCredId)
					.list();

			return learningResourceLearningStageDataFactory.getLearningResourceLearningStages(res);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the learning stages");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialData getCredentialData(long credentialId, boolean loadCreatorData,
															   boolean loadCategoryData,
															   boolean loadAssessmentConfig,
															   boolean loadCompetences, long userId,
															   AccessMode accessMode)
			throws ResourceNotFoundException, DbConnectionException {
		try {
			Credential1 cred = getCredential(credentialId, loadCreatorData, loadCategoryData);

			if (cred == null) {
				throw new ResourceNotFoundException();
			}

			User createdBy = loadCreatorData ? cred.getCreatedBy() : null;
			Set<CredentialAssessmentConfig> assessmentConfig = loadAssessmentConfig ? cred.getAssessmentConfig() : null;
			CredentialCategory cc = loadCategoryData ? cred.getCategory() : null;
			CredentialData credData = credentialFactory.getCredentialData(createdBy, cred, cc, assessmentConfig, cred.getTags(),
					cred.getHashtags(), true);

			if (loadCompetences) {
				//if user sent a request, we should always return enrolled competencies if he is enrolled
				if (accessMode == AccessMode.USER) {
					credData.setCompetences(compManager.getCompetencesForCredential(credentialId, userId, CompetenceLoadConfig.of(true, false, false, false, false)));
				} else {
					/*
					 * always include not published competences
					 */
					credData.setCompetences(compManager.getCredentialCompetencesData(
							credentialId, false, false, false, true));
				}
			}

			return credData;
		} catch (ResourceNotFoundException e) {
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	/**
	 * Returns credential with specified id.
	 *
	 * @param credentialId
	 * @param loadCreatorData
	 * @return
	 * @throws DbConnectionException
	 */
	private Credential1 getCredential(long credentialId, boolean loadCreatorData, boolean loadCategoryData)
			throws DbConnectionException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT cred FROM Credential1 cred ");

			if (loadCreatorData) {
				builder.append("INNER JOIN fetch cred.createdBy user ");
			}
			if (loadCategoryData) {
				builder.append("LEFT JOIN fetch cred.category ");
			}
			builder.append("LEFT JOIN fetch cred.tags tags ");
			builder.append("LEFT JOIN fetch cred.hashtags hashtags ");
			builder.append("WHERE cred.id = :credentialId AND cred.deleted = :deleted ");

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

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Credential1 updateCredential(CredentialData data, UserContextData context)
			throws DbConnectionException, StaleDataException, IllegalDataStateException {
		try {
			Result<Credential1> res = resourceFactory.updateCredential(data, context);
			Credential1 cred = res.getResult();

			eventFactory.generateEvents(res.getEventQueue());

			fireEditEvent(data, cred, context);
			//we should generate update hashtags only for deliveries
			if (data.getType() == CredentialType.Delivery && data.isHashtagsStringChanged()) {
				Map<String, String> params = new HashMap<>();
				params.put("newhashtags", data.getHashtagsString());
				params.put("oldhashtags", data.getOldHashtags());
				eventFactory.generateEvent(EventType.UPDATE_HASHTAGS, context, cred, null, null, params);
			}
			/* 
			 * flushing to force lock timeout exception so it can be caught here.
			 * It is rethrown as StaleDataException.
			 */
			persistence.currentManager().flush();
			return cred;
		} catch (StaleDataException e) {
			logger.error(e);
			throw e;
		} catch (HibernateOptimisticLockingFailureException e) {
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

	private void fireEditEvent(CredentialData data, Credential1 cred,
							   UserContextData context) {
	    Map<String, String> params = new HashMap<>();
	    CredentialChangeTracker changeTracker = new CredentialChangeTracker(
	    		data.isTitleChanged(), data.isDescriptionChanged(), false,
	    		data.isTagsStringChanged(), data.isHashtagsStringChanged(), 
	    		data.isMandatoryFlowChanged());
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    eventFactory.generateEvent(EventType.Edit, context, cred, null,null, params);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Result<Credential1> updateCredentialData(CredentialData data, UserContextData context)
			throws StaleDataException, IllegalDataStateException {
		Result<Credential1> res = new Result<>();
		Credential1 credToUpdate = (Credential1) persistence.currentManager()
				.load(Credential1.class, data.getId());
		
		/* this check is needed to find out if credential is changed from the moment credential data
		 * is loaded for edit to the moment update request is sent
		 */
		if (credToUpdate.getVersion() != data.getVersion()) {
			throw new StaleDataException("Credential edited in the meantime");
		}

		//create delivery start and end dates from timestamps
		Date deliveryStart = DateUtil.getDateFromMillis(data.getDeliveryStartTime());
		Date deliveryEnd = DateUtil.getDateFromMillis(data.getDeliveryEndTime());
		
		/*
		 * if it is a delivery and end date is before start throw exception
		 */
		if (data.getType() == CredentialType.Delivery
				&& deliveryStart != null && deliveryEnd != null
				&& deliveryStart.after(deliveryEnd)) {
			throw new IllegalDataStateException("Delivery cannot be ended before it starts");
		}

		//group of attributes that can be changed on delivery and original credential
		credToUpdate.setTitle(data.getTitle());
		credToUpdate.setDescription(data.getDescription());
		credToUpdate.setCompetenceOrderMandatory(data.isMandatoryFlow());
		credToUpdate.setManuallyAssignStudents(!data.isAutomaticallyAssingStudents());

		if (data.isTagsStringChanged()) {
			credToUpdate.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(
					data.getTagsString())));
		}
		if (data.isHashtagsStringChanged()) {
			credToUpdate.setHashtags(new HashSet<>(tagManager.parseCSVTagsAndSave(
					data.getHashtagsString())));
		}

		//this group of attributes can be changed only for original credential and not for delivery
		if (data.getType() == CredentialType.Original) {
			CredentialCategory category = data.getCategory() != null
					? (CredentialCategory) persistence.currentManager().load(CredentialCategory.class, data.getCategory().getId())
					: null;
			credToUpdate.setCategory(category);
			//propagate category change to deliveries
			res.appendEvents(setCategoryForCredentialDeliveries(data.getId(), category, context));

			credToUpdate.setDefaultNumberOfStudentsPerInstructor(data.getDefaultNumberOfStudentsPerInstructor());

			LearningStage learningStage = null;
			long firstStageCredId = 0;
			if (data.isLearningStageEnabledChanged()) {
				if (data.isLearningStageEnabled()) {
					learningStage = (LearningStage) persistence.currentManager().load(LearningStage.class, data.getLearningStage().getId());
					credToUpdate.setLearningStage(learningStage);
					//if learning in stages is enabled, this credential is first stage credential
					firstStageCredId = data.getId();
				} else {
					if (credToUpdate.getFirstLearningStageCredential() != null) {
						//if first learning stage is not null we get first stage cred id that way
						firstStageCredId = credToUpdate.getFirstLearningStageCredential().getId();
						credToUpdate.setFirstLearningStageCredential(null);
					} else {
						//if first stage cred is null, it means this credential is first stage cred
						firstStageCredId = credToUpdate.getId();
					}
					credToUpdate.setLearningStage(null);
				}
			}

			if (data.getAssessmentTypes() != null) {
				for (AssessmentTypeConfig atc : data.getAssessmentTypes()) {
					if (atc.hasObjectChanged()) {
						CredentialAssessmentConfig cac = (CredentialAssessmentConfig) persistence.currentManager().load(CredentialAssessmentConfig.class, atc.getId());
						cac.setEnabled(atc.isEnabled());
					}
				}
			}
			setAssessmentRelatedData(credToUpdate, data, data.getAssessmentSettings().isRubricChanged());

			List<CompetenceData1> comps = data.getCompetences();
			if (comps != null) {
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
		    				res.appendEvent(eventFactory.generateEventData(
		    						EventType.Attach, context, competence, credToUpdate,null, null));
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
		    				res.appendEvent(eventFactory.generateEventData(
		    						EventType.Detach, context, competence1, credToUpdate, null, null));
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

		    if (data.isLearningStageEnabledChanged()) {
				persistence.currentManager().flush();
				/*
				TODO learning in stages - if we enable adding competences through search
				this would not work for those competencies because they should have learning stage
				updated even if credential learning in stages flag have not changed
				 */
				res.appendEvents(setLearningStageForCredentialCompetences(data.getId(), learningStage, context));
				if (learningStage == null) {
					res.appendEvents(disableStagesForCredentialsInOtherStages(firstStageCredId, data.getId(), context));
				}
			}
    	} else {
    		updateDeliveryTimes(credToUpdate, data, deliveryStart, deliveryEnd);
		}

		res.setResult(credToUpdate);
		return res;
	}

	private EventQueue setCategoryForCredentialDeliveries(long credentialId, CredentialCategory category, UserContextData context) {
		String q =
				"UPDATE Credential1 del " +
				"SET del.category = :category " +
				"WHERE del.deliveryOf.id = :credId";

		persistence.currentManager().createQuery(q)
				.setLong("credId", credentialId)
				.setParameter("category", category)
				.executeUpdate();

		List<Long> deliveryIds = getIdsOfAllCredentialDeliveries(credentialId, persistence.currentManager());
		EventQueue queue = EventQueue.newEventQueue();
		for (long id : deliveryIds) {
			Credential1 delivery = new Credential1();
			delivery.setId(id);
			queue.appendEvent(eventFactory.generateEventData(EventType.CREDENTIAL_CATEGORY_UPDATE, context, delivery, null, null, null));
		}

		return queue;
	}

	private EventQueue setLearningStageForCredentialCompetences(long credentialId, LearningStage stage, UserContextData context) {
//		String query =
//				"UPDATE credential_competence1 cc " +
//				"INNER JOIN competence1 c ON cc.competence = c.id ";
//		if (stage == null) {
//			query +=
//					"SET c.learning_stage = NULL, " +
//					"c.first_learning_stage_competence = NULL ";
//		} else {
//			query +=
//					"SET c.learning_stage = :learningStageId ";
//		}
//
//		query +=
//				"WHERE cc.credential = :credId";
//
//		Query q = persistence.currentManager()
//				.createSQLQuery(query)
//				.setLong("credId", credentialId);
//
//		if (stage != null) {
//			q.setLong("learningStageId", stage.getId());
//		}
//
//		int affected = q.executeUpdate();
//
//		logger.info("Number of credential competences with updated learning stage: " + affected);

		List<CredentialCompetence1> credComps = compManager.getCredentialCompetences(
				credentialId, false, false, true, false);
		EventQueue queue = EventQueue.newEventQueue();
		for (CredentialCompetence1 cc : credComps) {
			queue.appendEvents(compManager.updateCompetenceLearningStage(cc.getCompetence(), stage, context));
		}
		return queue;
	}

	private EventQueue disableStagesForCredentialsInOtherStages(long firstStageCredId, long credentialToExcludeId, UserContextData context) {
		List<Credential1> credentials = getOtherCredentialsFromLearningStageGroup(firstStageCredId, credentialToExcludeId);
		EventQueue queue = EventQueue.newEventQueue();
		for (Credential1 cred : credentials) {
			queue.appendEvents(disableLearningInStagesForCredential(cred, context));
			queue.appendEvents(setLearningStageForCredentialCompetences(cred.getId(), null, context));
		}
		return queue;
	}

	private List<Credential1> getOtherCredentialsFromLearningStageGroup(long firstStageCredId, long credentialToExcludeId) {
		String query =
				"SELECT cred " +
				"FROM Credential1 cred " +
				"WHERE cred.id != :credToExclude " +
				"AND (cred.firstLearningStageCredential = :firstStageCredId " +
						"OR cred.id = :firstStageCredId)";

		@SuppressWarnings("unchecked")
		List<Credential1> creds = persistence.currentManager()
				.createQuery(query)
				.setLong("firstStageCredId", firstStageCredId)
				.setLong("credToExclude", credentialToExcludeId)
				.list();
		return creds;
	}

	//not transactional
	@Override
	public void enrollInCredential(long credentialId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.enrollInCredentialAndGetEvents(credentialId, context.getActorId(),
				0, context);

		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> enrollInCredentialAndGetEvents(long credentialId, long userId,
													   long instructorThatForcedEnrollId, UserContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();

			User user = (User) persistence.currentManager().load(User.class, userId);

			Credential1 cred = getCredential(credentialId, false, false);
			TargetCredential1 targetCred = createTargetCredential(cred, user);

			/*
			TODO actor doesn't have to be enrolled student, it could be an instructor that triggered
			student enroll, that should be taken into account in all places.
			*/
			User student = new User();
			student.setId(userId);
			result.appendEvent(eventFactory.generateEventData(
					EventType.ENROLL_COURSE, context, cred, student, null, null));

			long instructorId = 0;

			if (!cred.isManuallyAssignStudents()) {
				List<TargetCredential1> targetCredIds = new ArrayList<>();
				targetCredIds.add(targetCred);
				Result<StudentAssignData> res = credInstructorManager.assignStudentsToInstructorAutomatically(
						credentialId, targetCredIds, 0, false, context);
				result.appendEvents(res.getEventQueue());
				List<StudentInstructorPair> assigned = res.getResult().getAssigned();
				if (assigned.size() == 1) {
					StudentInstructorPair pair = assigned.get(0);
					//we need user id, not instructor id
					instructorId = pair.getInstructor().getUser().getId();
				}
			}

			//create default assessment for user
			result.appendEvents(assessmentManager.createInstructorAssessmentAndGetEvents(targetCred, instructorId, context).getEventQueue());
			//create self assessment if enabled
			if (cred.getAssessmentConfig()
					.stream()
					.filter(config -> config.getAssessmentType() == AssessmentType.SELF_ASSESSMENT)
					.findFirst().get()
					.isEnabled()) {
				result.appendEvents(assessmentManager.createSelfAssessmentAndGetEvents(targetCred, context).getEventQueue());
			}

			//generate completion event if progress is 100
			if (targetCred.getProgress() == 100) {
				result.appendEvent(eventFactory.generateEventData(
						EventType.Completion, context, targetCred, null, null, null));
			}

			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling in a credential");
		}
	}

	//not transactional
	@Override
	public void enrollStudentsInCredential(long credId, long instructorId, List<Long> userIds, UserContextData context)
			throws DbConnectionException {
		if (userIds != null) {
			try {
				EventQueue events = EventQueue.newEventQueue();
				for (long userId : userIds) {
					events.appendEvents(self.enrollInCredentialAndGetEvents(
							credId, userId, instructorId, context).getEventQueue());
				}

				eventFactory.generateEvents(events);
			} catch (Exception e) {
				throw new DbConnectionException("Error while enrolling students in a credential");
			}
		}
	}

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
	@Transactional
	public EventQueue addCompetenceToCredential(long credId, Competence1 comp, UserContextData context)
			throws DbConnectionException {
		try {
			EventQueue eventQueue = EventQueue.newEventQueue();
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
			if (comp.getDuration() > 0) {
				cred.setDuration(cred.getDuration() + comp.getDuration());
			}

			Competence1 competence = new Competence1();
			competence.setId(comp.getId());
			eventQueue.appendEvent(eventFactory.generateEventData(EventType.Attach, context, competence, cred,null, null));
			
			return eventQueue;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding competence to credential");
		}

	}

	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId,
																			  CredentialType type)
			throws DbConnectionException {
		try {
			String query = "SELECT cred.id, cred.title " +
					"FROM CredentialCompetence1 credComp " +
					"INNER JOIN credComp.credential cred " +
					"WHERE credComp.competence.id = :compId " +
					"AND cred.deleted = :boolFalse ";

			if (type != null) {
				query += "AND cred.type = :type";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setBoolean("boolFalse", false);

			if (type != null) {
				q.setString("type", type.name());
			}

			@SuppressWarnings("unchecked")
			List<Object[]> res = q.list();

			if (res == null) {
				return new ArrayList<>();
			}

			List<CredentialData> resultList = new ArrayList<>();
			for (Object[] row : res) {
				CredentialData cd = new CredentialData(false);
				cd.setId((long) row[0]);
				cd.setTitle((String) row[1]);
				resultList.add(cd);
			}
			return resultList;
		} catch (Exception e) {
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
			String query = "SELECT tag " +
					"FROM Credential1 cred " +
					"INNER JOIN cred.tags tag " +
					"WHERE cred.id = :credentialId";
			@SuppressWarnings("unchecked")
			List<Tag> res = session
					.createQuery(query)
					.setLong("credentialId", credentialId)
					.list();
			if (res == null) {
				return new ArrayList<>();
			}

			return res;
		} catch (Exception e) {
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
			String query = "SELECT hashtag " +
					"FROM Credential1 cred " +
					"INNER JOIN cred.hashtags hashtag " +
					"WHERE cred.id = :credentialId";
			@SuppressWarnings("unchecked")
			List<Tag> res = session
					.createQuery(query)
					.setLong("credentialId", credentialId)
					.list();
			if (res == null) {
				return new ArrayList<>();
			}

			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential hashtags");
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
			if (justUncompleted) {
				builder.append("AND cred.progress != :progress");
			}
//			String query = "SELECT cred " +
//					       "FROM TargetCredential1 cred " +
//					       "WHERE cred.credential = :cred";					    

			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setEntity("cred", cred);
			if (justUncompleted) {
				q.setInteger("progress", 100);
			}
			@SuppressWarnings("unchecked")
			List<TargetCredential1> res = q.list();
			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user credentials");
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

			if (bookmarks == null) {
				return new ArrayList<>();
			}
			return bookmarks;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential bookmarks");
		}
	}

	@Override
	public void bookmarkCredential(long credId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.bookmarkCredentialAndGetEvents(credId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> bookmarkCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
			User user = (User) persistence.currentManager().load(User.class, context.getActorId());
			CredentialBookmark cb = new CredentialBookmark();
			cb.setCredential(cred);
			cb.setUser(user);
			saveEntity(cb);

			CredentialBookmark bookmark = new CredentialBookmark();
			bookmark.setId(cb.getId());
			Credential1 credential = new Credential1();
			credential.setId(credId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.Bookmark, context, bookmark, credential, null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while bookmarking credential");
		}
	}

	@Override
	public void deleteCredentialBookmark(long credId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.deleteCredentialBookmarkAndGetEvents(credId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> deleteCredentialBookmarkAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
			User user = (User) persistence.currentManager().load(User.class, context.getActorId());
			String query = "SELECT cb " +
					"FROM CredentialBookmark cb " +
					"WHERE cb.credential = :cred " +
					"AND cb.user = :user";

			CredentialBookmark bookmark = (CredentialBookmark) persistence.currentManager()
					.createQuery(query)
					.setEntity("cred", cred)
					.setEntity("user", user)
					.uniqueResult();

			long deletedBookmarkId = bookmark.getId();

			delete(bookmark);

			CredentialBookmark cb = new CredentialBookmark();
			cb.setId(deletedBookmarkId);
			Credential1 credential = new Credential1();
			credential.setId(credId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.RemoveBookmark, context, cb, credential,null, null));
			return res;
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
			if (!credIds.isEmpty()) {
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
		} catch (Exception e) {
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

			if (res == null) {
				return new ArrayList<>();
			}

			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential ids");
		}
	}

	@Override
	@Transactional
	public EventQueue updateCredentialProgress(long targetCompId, UserContextData context)
			throws DbConnectionException {
		try {
			EventQueue events = EventQueue.newEventQueue();
			TargetCompetence1 tc = (TargetCompetence1) persistence.currentManager().load(
					TargetCompetence1.class, targetCompId);
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
					"WITH tComp.user.id = :studentId " +
					"WHERE tCred.user.id = :studentId " +
					"ORDER BY tCred.id, credComp.order";

			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setLong("targetCompId", targetCompId)
					.setLong("studentId", tc.getUser().getId())
					.list();

			long currentTCredId = 0;
			long currentCredId = 0;
			long nextCompToLearnId = 0;
			int cumulativeCredProgress = 0;
			int numberOfCompetencesInCredential = 0;
			Date now = new Date();
			if (res != null) {
				for (Object[] obj : res) {
					long tCredId = (long) obj[0];
					long credId = (long) obj[1];
					long compId = (long) obj[2];
					int compProgress = (int) obj[3];

					if (tCredId != currentTCredId) {
						if (currentTCredId > 0) {
							int finalCredProgress = cumulativeCredProgress / numberOfCompetencesInCredential;
							events.appendEvents(updateTargetCredentialProgress(currentTCredId, currentCredId,
									finalCredProgress, nextCompToLearnId, now, context));
						}
						currentTCredId = tCredId;
						currentCredId = credId;
						numberOfCompetencesInCredential = 1;
						cumulativeCredProgress = compProgress;
						nextCompToLearnId = 0;
					} else {
						numberOfCompetencesInCredential++;
						cumulativeCredProgress += compProgress;
					}

					if (nextCompToLearnId == 0 && compProgress < 100) {
						nextCompToLearnId = compId;
					}
				}
				//update last credential
				if (currentTCredId > 0) {
					int finalCredProgress = cumulativeCredProgress / numberOfCompetencesInCredential;
					events.appendEvents(updateTargetCredentialProgress(currentTCredId, currentCredId,
							finalCredProgress, nextCompToLearnId, now, context));
				}
			}
			return events;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential progress");
		}
	}

	private EventQueue updateTargetCredentialProgress(long tCredId, long credId, int finalCredProgress,
														   long nextCompToLearnId, Date now, UserContextData context) {
		StringBuilder updateCredQuery = new StringBuilder(
				"UPDATE TargetCredential1 targetCred SET " +
						"targetCred.progress = :progress, " +
						"targetCred.nextCompetenceToLearnId = :nextCompToLearnId ");

		if (finalCredProgress == 100) {
			updateCredQuery.append(", targetCred.dateFinished = :dateCompleted ");
		}
		updateCredQuery.append("WHERE targetCred.id = :tCredId");

		Query q1 = persistence.currentManager()
				.createQuery(updateCredQuery.toString())
				.setInteger("progress", finalCredProgress)
				.setLong("nextCompToLearnId", nextCompToLearnId)
				.setLong("tCredId", tCredId);

		if (finalCredProgress == 100) {
			q1.setDate("dateCompleted", now);
		}

		q1.executeUpdate();

		EventQueue events = EventQueue.newEventQueue();

		TargetCredential1 tCred = new TargetCredential1();
		tCred.setId(tCredId);
		Credential1 cred = new Credential1();
		cred.setId(credId);
		tCred.setCredential(cred);

		EventData ev = eventFactory.generateEventData(EventType.ChangeProgress,
				context, tCred, null, null, null);
		ev.setProgress(finalCredProgress);
		events.appendEvent(ev);
//		eventFactory.generateChangeProgressEvent(userId, tCred, finalCredProgress,
//				lcPage, lcContext, lcService, null);
		if (finalCredProgress == 100) {
			events.appendEvent(eventFactory.generateEventData(EventType.Completion, context, tCred, null, null, null));
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
	public CredentialData getTargetCredentialDataAndTargetCompetencesData(long credentialId, long userId) throws DbConnectionException {
		CredentialData credentialData = getTargetCredentialData(credentialId, userId, CredentialLoadConfig.of(false, false, true, false, false, true, false, true, null));
		if (credentialData != null && credentialData.isEnrolled()) {
			credentialData.setCompetences(compManager.getCompetencesForCredential(credentialId, userId, CompetenceLoadConfig.of(false, false, true, true, false)));
			return credentialData;
		}
		return null;
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

//	@Override
//	@Transactional(readOnly = true)
//	public CredentialData getCurrentVersionOfCredentialForManager(long credentialId,
//			boolean loadCreator, boolean loadCompetences) throws DbConnectionException {
//			return getCurrentVersionOfCredentialBasedOnRole(credentialId, 0, loadCreator, 
//					loadCompetences, Role.Manager);
//	}

	@SuppressWarnings({"unchecked"})
	@Override
	@Transactional(readOnly = true)
	public List<TargetCredentialData> getAllCredentials(long userid, boolean onlyPubliclyVisible) throws DbConnectionException {
		return getTargetCredentials(userid, onlyPubliclyVisible, false, false, UserLearningProgress.ANY);
	}

	@SuppressWarnings("unchecked")
	private List<CategorizedCredentialsData> getCategorizedTargetCredentials(long userId, boolean onlyPubliclyVisible,
                                                                             UserLearningProgress progress)
			throws DbConnectionException {
		List<TargetCredentialData> targetCredentials = getTargetCredentials(userId, onlyPubliclyVisible, true, true, progress);
		return credentialFactory.groupCredentialsByCategory(targetCredentials);
	}

	private List<TargetCredentialData> getTargetCredentials(long userId, boolean onlyPubliclyVisible,
														    boolean sortByCategory, boolean loadNumberOfAssessments, UserLearningProgress progress)
			throws DbConnectionException {
		try {
			String query =
					"SELECT targetCredential1 " +
							"FROM TargetCredential1 targetCredential1 " +
							"INNER JOIN fetch targetCredential1.credential cred ";

			if (sortByCategory) {
				query += "LEFT JOIN fetch cred.category cat ";
			}

			query += "WHERE targetCredential1.user.id = :userid ";

			switch (progress) {
				case COMPLETED:
					query += "AND targetCredential1.progress = 100 ";
					break;
				case IN_PROGRESS:
					query += "AND targetCredential1.progress < 100 ";
					break;
				default:
					break;
			}

			if (onlyPubliclyVisible) {
				query += " AND targetCredential1.hiddenFromProfile = false ";
			}

			query += "ORDER BY " + (sortByCategory ? "cat.title, " : "") + " cred.title";

			List<TargetCredentialData> resultList = new ArrayList<>();

			List<TargetCredential1> result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userId)
					.list();

			for(TargetCredential1 targetCredential1 : result) {
				int numberOfAssessments = 0;
				if (loadNumberOfAssessments) {
					numberOfAssessments = assessmentManager.getNumberOfApprovedAssessmentsForUserCredential(targetCredential1.getId());
				}
				TargetCredentialData targetCredentialData = new TargetCredentialData(targetCredential1, sortByCategory ? targetCredential1.getCredential().getCategory() : null, numberOfAssessments);
				resultList.add(targetCredentialData);
			}

			return resultList;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading target credentials");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<CategorizedCredentialsData> getAllCompletedCredentials(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		return getCategorizedTargetCredentials(userId, onlyPubliclyVisible, UserLearningProgress.COMPLETED);
	}

	@SuppressWarnings({"unchecked"})
	@Override
	@Transactional(readOnly = true)
	public List<CategorizedCredentialsData> getAllInProgressCredentials(long userid, boolean onlyPubliclyVisible) throws DbConnectionException {
		return getCategorizedTargetCredentials(userid, onlyPubliclyVisible, UserLearningProgress.IN_PROGRESS);
	}

	@Override
	@Transactional(readOnly = false)
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

			if (res != null) {
				for (Object[] row : res) {
					if (row != null) {
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
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user credentials");
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
			if (creds == null) {
				return new ArrayList<>();
			}
			return creds;
		} catch (Exception e) {
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
			if (res == null) {
				return 0;
			}
			return res;
		} catch (Exception e) {
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
			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getActiveUserIdsForCredential(long credId) throws DbConnectionException {
		try {
			String query =
					"SELECT targetCredential.user.id " +
					"FROM TargetCredential1 targetCredential " +
					"WHERE targetCredential.credential.id = :credentialId " +
						"AND targetCredential.progress < 100";

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query)
					.setLong("credentialId", credId)
					.list();
			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
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
			if (targetCredIds == null || targetCredIds.isEmpty()) {
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
			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
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
			if (userIds == null || userIds.isEmpty()) {
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
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user credentials");
		}
	}

	@Override
	@Transactional
	public void removeFeed(long credId, long feedSourceId) throws DbConnectionException {
		try {
			Credential1 cred = getCredentialWithBlogs(credId);
			FeedSource feedSource = (FeedSource) persistence.currentManager().load(FeedSource.class, feedSourceId);
			cred.getBlogs().remove(feedSource);
		} catch (Exception e) {
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

			if (cred != null) {
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
		} catch (EntityAlreadyExistsException eae) {
			throw eae;
		} catch (Exception e) {
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

			if (res != null) {
				String title = (String) res[0];
				long nextComp = (long) res[1];
				boolean mandatoryOrder = (boolean) res[2];

				LearningInfo credLI = LearningInfo.getLearningInfoForCredential(title, mandatoryOrder, nextComp);

				if (loadCompLearningInfo && nextComp > 0) {
					return LearningInfo.merge(credLI, compManager.getCompetenceLearningInfo(nextComp, userId));
				}

				return credLI;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving learning info");
		}
	}

	@Override
	@Transactional
	public List<CredentialData> getNRecentlyLearnedInProgressCredentials(Long userid, int limit, boolean loadOneMore)
			throws DbConnectionException {
		List<CredentialData> result = new ArrayList<>();
		try {
			String query =
					"SELECT tCred, creator, bookmark.id " +
							"FROM TargetCredential1 tCred " +
							"INNER JOIN tCred.credential cred " +
							"LEFT JOIN cred.createdBy creator " +
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
					.setMaxResults(limitFinal)
					.list();

			if (res == null) {
				return new ArrayList<>();
			}

			for (Object[] row : res) {
				if (row != null) {
					TargetCredential1 tc = (TargetCredential1) row[0];
					User creator = (User) row[1];
					Long bookmarkId = (Long) row[2];
					CredentialData cd = credentialFactory.getCredentialData(
							tc, creator, null, null, null, null, false);
					if (bookmarkId != null) {
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
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating last action for user credential");
		}
	}

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
		} catch (Exception e) {
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
		} catch (Exception e) {
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

			if (res != null) {
				List<StudentData> data = new ArrayList<>();
				for (TargetCredential1 tc : res) {
					StudentData sd = new StudentData(tc.getUser());
					CredentialInstructor ci = tc.getInstructor();
					if (ci != null) {
						sd.setInstructor(credInstructorFactory.getInstructorData(
								tc.getInstructor(), tc.getInstructor().getUser(),
								0, false));
					}
					sd.setProgress(tc.getProgress());
					Optional<Long> credAssessmentId = assessmentManager
							.getInstructorCredentialAssessmentId(credId, sd.getUser().getId());
					if (credAssessmentId.isPresent()) {
						sd.setAssessmentId(credAssessmentId.get());
					}
					data.add(sd);
				}
				return data;
			}

			return null;
		} catch (Exception e) {
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

			if (res != null) {
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
				return new CredentialMembersSearchFilter[]{allFilter, unassignedFilter, assignedFilter, completedFilter};
			}

			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving filters");
		}
	}

	@Override
	@Transactional(readOnly = true)
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
						"WHERE assessment.student.id = :userId " +
							"AND cred.id = :credId " +
							"AND assessment.assessor IS NOT NULL " + // can be NULL in default assessments when instructor is not set
						"AND assessment.type = :aType " +
						") " +
				"ORDER BY RAND()";

			@SuppressWarnings("unchecked")
			List<User> res = (List<User>) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setLong("userId", userId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
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
	@Transactional(readOnly = true)
	public List<Tag> getTagsForCredential(long credentialId) throws DbConnectionException {

		StringBuilder queryBuilder = new StringBuilder(
				"SELECT tags " +
						"FROM Credential1 cred " +
						"LEFT JOIN cred.tags tags " +
						"WHERE cred.id = :credId ");

		@SuppressWarnings("unchecked")
		List<Tag> res = persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", credentialId)
				.list();

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Tag> getHashtagsForCredential(long credentialId) throws DbConnectionException {

		StringBuilder queryBuilder = new StringBuilder(
				"SELECT hashtags " +
						"FROM Credential1 cred " +
						"LEFT JOIN cred.hashtags hashtags  " +
						"WHERE cred.id = :credId ");

		@SuppressWarnings("unchecked")
		List<Tag> res = persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("credId", credentialId)
				.list();

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> getTagsFromCredentialCompetencesAndActivities(long credentialId) throws DbConnectionException {
		List<Tag> tags = getTagsFromCredentialCompetences(credentialId);
		//we neeed unique tags, so we pass competence tags to be excluded from result
		tags.addAll(getTagsFromCredentialActivities(credentialId, tags));
		List<String> tagNames = new ArrayList<>();
		for (Tag tag : tags) {
			tagNames.add(tag.getTitle());
		}

		return tagNames;
	}

	private List<Tag> getTagsFromCredentialCompetences(long credentialId) {
		String query = "SELECT DISTINCT tag " +
				"FROM CredentialCompetence1 cc " +
				"INNER JOIN cc.competence c " +
				"INNER JOIN c.tags tag " +
				"WHERE cc.credential.id = :credId";

		@SuppressWarnings("unchecked")
		List<Tag> res = (List<Tag>) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credentialId)
				.list();
		return res;
	}

	private List<Tag> getTagsFromCredentialActivities(long credentialId, List<Tag> tagsToExclude) throws DbConnectionException {

		String query = "SELECT DISTINCT tag " +
				"FROM CompetenceActivity1 ca " +
				"INNER JOIN ca.activity a " +
				"INNER JOIN a.tags tag ";

		if (tagsToExclude != null && !tagsToExclude.isEmpty()) {
			query += "WITH tag NOT IN (:tags) ";
		}

		query += "INNER JOIN ca.competence comp " +
				"INNER JOIN comp.credentialCompetences cc " +
				"WITH cc.credential.id = :credId";

		Query q = persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credentialId);

		if (tagsToExclude != null && !tagsToExclude.isEmpty()) {
			q.setParameterList("tags", tagsToExclude);
		}

		@SuppressWarnings("unchecked")
		List<Tag> res = q.list();

		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public int getNumberOfTags(long credentialId) throws DbConnectionException {
		List<Tag> tags = getTagsFromCredentialCompetences(credentialId);
		tags.addAll(getTagsFromCredentialActivities(credentialId, tags));
		return tags.size();
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCompetencesForKeywordSearch(long credentialId) throws DbConnectionException {
		String query =
				"SELECT DISTINCT comp " +
				"FROM Competence1 comp " +
				"LEFT JOIN FETCH comp.tags tag " +
				"INNER JOIN comp.credentialCompetences cComp " +
				"WITH cComp.credential.id = :credId " +
				"ORDER BY comp.title";

		@SuppressWarnings("unchecked")
		List<Competence1> competences = (List<Competence1>) persistence.currentManager()
				.createQuery(query)
				.setLong("credId", credentialId)
				.list();

		List<CompetenceData1> data = new ArrayList<>();
		for (Competence1 competence : competences) {
			data.add(competenceFactory.getCompetenceData(null, competence, null,
					competence.getTags(), false));
		}
		return data;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ActivityData> getActivitiesForKeywordSearch(long credentialId) throws DbConnectionException {
		try {
			String query =
					"SELECT DISTINCT cAct " +
					"FROM CompetenceActivity1 cAct " +
					"INNER JOIN fetch cAct.activity act " +
					"LEFT JOIN FETCH act.tags tag " +
					"INNER JOIN cAct.competence comp " +
					"INNER JOIN comp.credentialCompetences cComp " +
					"WITH cComp.credential.id = :credId " +
					"ORDER BY act.title";

			@SuppressWarnings("unchecked")
			List<CompetenceActivity1> activities = (List<CompetenceActivity1>) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credentialId)
					.list();

			List<ActivityData> data = new ArrayList<>();
			for (CompetenceActivity1 cAct : activities) {
				data.add(activityDataFactory.getActivityData(cAct, null, null, cAct.getActivity().getTags(), false));
			}
			return data;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving activities");
		}
	}

	@Transactional(readOnly = true)
	@Override
	public UserAccessSpecification getUserPrivilegesForCredential(long credId, long userId)
			throws DbConnectionException {
		try {
			String query = "SELECT DISTINCT credUserGroup.privilege, cred.visibleToAll, cred.type, cred.deliveryStart, cred.deliveryEnd " +
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

			boolean visibleToAll = false;
			CredentialType type = null;
			Date deliveryStart = null;
			Date deliveryEnd = null;
			boolean first = true;
			Set<UserGroupPrivilege> privs = new HashSet<>();
			for (Object[] row : res) {
				if (row != null) {
					UserGroupPrivilege priv = (UserGroupPrivilege) row[0];
					if (priv == null) {
						priv = UserGroupPrivilege.None;
					}
					privs.add(priv);
					if (first) {
						visibleToAll = (boolean) row[1];
						type = (CredentialType) row[2];
						deliveryStart = (Date) row[3];
						deliveryEnd = (Date) row[4];
						first = false;
					}
				}
			}
			return CredentialUserAccessSpecification.of(privs, visibleToAll, type,
					deliveryStart, deliveryEnd);
		} catch (Exception e) {
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
			return resourceAccessFactory.determineAccessRights(userId, credId, req, spec);
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Credential1> getAllCredentials(long orgId, Session session) throws DbConnectionException {
		try {
			String query =
					"SELECT cred " +
							"FROM Credential1 cred " +
							"WHERE cred.deleted = :deleted ";

			if (orgId > 0) {
				query += "AND cred.organization.id = :orgId";
			}

			Query q = session
					.createQuery(query)
					.setBoolean("deleted", false);

			if (orgId > 0) {
				q.setLong("orgId", orgId);
			}

			@SuppressWarnings("unchecked")
			List<Credential1> result = q.list();

			if (result == null) {
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
										   List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
										   UserContextData context) throws DbConnectionException {
		try {
			EventQueue events =
					self.updateCredentialVisibilityAndGetEvents(credId, groups, users, visibleToAll,
							visibleToAllChanged, context);
			eventFactory.generateEvents(events);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	@Transactional(readOnly = false)
	public EventQueue updateCredentialVisibilityAndGetEvents(long credId, List<ResourceVisibilityMember> groups,
																  List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged,
																  UserContextData context) throws DbConnectionException {
		try {
			EventQueue events = EventQueue.newEventQueue();
			if (visibleToAllChanged) {
				Credential1 cred = (Credential1) persistence.currentManager().load(
						Credential1.class, credId);
				cred.setVisibleToAll(visibleToAll);

				Credential1 credential = new Credential1();
				credential.setId(credId);
				credential.setVisibleToAll(visibleToAll);
				events.appendEvent(eventFactory.generateEventData(
						EventType.VISIBLE_TO_ALL_CHANGED, context, credential, null, null,null));
			}
			events.appendEvents(userGroupManager.saveCredentialUsersAndGroups(credId, groups, users, context).getEventQueue());
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
			String query =
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
			String query =
					"SELECT cred.user.id " +
							"FROM TargetCredential1 cred " +
							"WHERE cred.credential.id = :credId " +
							"AND cred.instructor is NULL";

			if (usersToExclude != null && !usersToExclude.isEmpty()) {
				query += " AND cred.user.id NOT IN (:excludeList)";
			}

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId);

			if (usersToExclude != null && !usersToExclude.isEmpty()) {
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

	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getOngoingDeliveries(long credId) throws DbConnectionException {
		return getDeliveries(credId, true, CredentialSearchFilterManager.ACTIVE);
	}

	@Override
	@Transactional (readOnly = true)
	public List<CredentialData> getOngoingDeliveriesFromAllStages(long firstStageCredentialId) throws DbConnectionException {
		try {
			String query =
					"SELECT del " +
					"FROM Credential1 del " +
					"INNER JOIN del.deliveryOf origCred " +
							"WITH origCred.id = :credId or origCred.firstLearningStageCredential.id = :credId " +
					"WHERE del.type = :type " +
					"AND (del.deliveryStart IS NOT NULL AND del.deliveryStart <= :now " +
					"AND (del.deliveryEnd IS NULL OR del.deliveryEnd > :now))";

			Query q = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", firstStageCredentialId)
					.setParameter("type", CredentialType.Delivery)
					.setTimestamp("now", new Date());

			@SuppressWarnings("unchecked")
			List<Credential1> result = q.list();

			List<CredentialData> deliveries = new ArrayList<>();
			for (Credential1 d : result) {
				deliveries.add(credentialFactory.getCredentialData(null, d, null, null, null, null, true));
			}
			return deliveries;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential deliveries");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<List<CredentialData>> getCredentialDeliveriesWithAccessRights(long credId,
																								long userId, CredentialSearchFilterManager filter ) throws DbConnectionException {
		List<CredentialData> credentials = getDeliveries(credId, false, filter);
		ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.MANAGER)
				.addPrivilege(UserGroupPrivilege.Edit);
		ResourceAccessData access = getResourceAccessData(credId, userId, req);
		return RestrictedAccessResult.of(credentials, access);
	}

	private List<CredentialData> getDeliveries(long credId, boolean onlyOngoing, CredentialSearchFilterManager filter)
			throws DbConnectionException {
		try {
			StringBuilder query = new StringBuilder(
					"SELECT del " +
							"FROM Credential1 del " +
							"WHERE del.type = :type " +
							"AND del.deliveryOf.id = :credId ");

			if (onlyOngoing) {
				query.append("AND (del.deliveryStart IS NOT NULL AND del.deliveryStart <= :now " +
						"AND (del.deliveryEnd IS NULL OR del.deliveryEnd > :now))");
			}

			if (filter.equals(CredentialSearchFilterManager.ACTIVE)) {
				query.append(" AND del.archived IS FALSE");
			} else if (filter.equals(CredentialSearchFilterManager.ARCHIVED)) {
				query.append(" AND del.archived IS TRUE");
			}

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("credId", credId)
					.setParameter("type", CredentialType.Delivery);

			if (onlyOngoing) {
				q.setTimestamp("now", new Date());
			}

			@SuppressWarnings("unchecked")
			List<Credential1> result = q.list();

			List<CredentialData> deliveries = new ArrayList<>();
			for (Credential1 d : result) {
				deliveries.add(credentialFactory.getCredentialData(null, d, null,null, null, null, true));
			}
			return deliveries;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential deliveries");
		}
	}

	@Override
	public void archiveCredential(long credId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.archiveCredentialAndGetEvents(credId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> archiveCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(credId, true);

			Credential1 credential = new Credential1();
			credential.setId(credId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.ARCHIVE, context, credential,null, null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while archiving credential");
		}
	}

	@Override
	public void restoreArchivedCredential(long credId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.restoreArchivedCredentialAndGetEvents(credId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> restoreArchivedCredentialAndGetEvents(long credId, UserContextData context)
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(credId, false);

			Credential1 credential = new Credential1();
			credential.setId(credId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.RESTORE, context, credential,null, null, null));
			return res;
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

	private long countNumberOfCredentials(CredentialSearchFilterManager searchFilter, List<Long> credIds) {
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(c.id) " +
						"FROM Credential1 c " +
						"WHERE c.id IN (:ids) ");

		switch (searchFilter) {
			case ACTIVE:
				query.append("AND c.archived IS FALSE");
				break;
			case ARCHIVED:
				query.append("AND c.archived IS TRUE");
				break;
		}

		Query q = persistence.currentManager()
				.createQuery(query.toString())
				.setParameterList("ids", credIds);

		Long count = (Long) q.uniqueResult();

		return count != null ? count : 0;
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<CredentialData> searchCredentialsForManager(CredentialSearchFilterManager searchFilter, int limit,
																	   int page, LearningResourceSortOption sortOption, long userId)
			throws DbConnectionException, NullPointerException {
		PaginatedResult<CredentialData> res = new PaginatedResult<>();
		try {
			if (searchFilter == null || sortOption == null) {
				throw new NullPointerException("Invalid argument values");
			}

			List<Long> ids = getCredentialsIdsWithEditPrivilege(userId, CredentialType.Original);

			//if user doesn't have needed privileges for any of the credentials, empty list is returned
			if (ids.isEmpty()) {
				return res;
			}

			long count = countNumberOfCredentials(searchFilter, ids);

			if (count > 0) {
				res.setHitsNumber(count);
				res.setFoundNodes(getCredentialsForManager(searchFilter, limit, page, sortOption, ids));
			}
			return res;
		} catch (NullPointerException npe) {
			logger.error("Error", npe);
			throw npe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credentials");
		}
	}

	private List<CredentialData> getCredentialsForManager(CredentialSearchFilterManager searchFilter, int limit,
														  int page, LearningResourceSortOption sortOption,
														  List<Long> credIds) {
		StringBuilder query = new StringBuilder(
				"SELECT c " +
				"FROM Credential1 c " +
				"WHERE c.id IN (:ids) ");

		switch (searchFilter) {
			case ACTIVE:
				query.append("AND c.archived IS FALSE ");
				break;
			case ARCHIVED:
				query.append("AND c.archived IS TRUE ");
				break;
		}

		query.append("ORDER BY c." + sortOption.getSortFieldDB() + " " + sortOption.getSortOrder());

		Query q = persistence.currentManager()
				.createQuery(query.toString())
				.setParameterList("ids", credIds)
				.setFirstResult(page * limit)
				.setMaxResults(limit);

		@SuppressWarnings("unchecked")
		List<Credential1> creds = q.list();

		List<CredentialData> res = new ArrayList<>();
		for (Credential1 c : creds) {
			CredentialData cd = credentialFactory.getCredentialData(null, c, null, null, null, null, false);
			cd.setDeliveries(getOngoingDeliveries(c.getId()));
			res.add(cd);
		}
		return res;
	}

	private List<Long> getCredentialsIdsWithEditPrivilege(long userId, CredentialType type)
			throws DbConnectionException {
		try {
			StringBuilder query = new StringBuilder(
					"SELECT distinct cred.id " +
							"FROM CredentialUserGroup credUserGroup " +
							"INNER JOIN credUserGroup.userGroup userGroup " +
							"INNER JOIN credUserGroup.credential cred " +
							"INNER JOIN userGroup.users user " +
							"WITH user.user.id = :userId " +
							"WHERE credUserGroup.privilege = :priv ");

			if (type != null) {
				query.append("AND cred.type = :type");
			}

			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setLong("userId", userId)
					.setString("priv", UserGroupPrivilege.Edit.name());
			if (type != null) {
					q.setString("type", type.name());
			}

			@SuppressWarnings("unchecked")
			List<Long> ids = q.list();

			return ids;
		} catch (Exception e) {
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
			List<Long> compIds = session
					.createQuery(query)
					.setLong("credId", credId)
					.list();

			return compIds;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competency ids");
		}
	}

	//not transactional
	@Override
	public Credential1 createCredentialDelivery(long credentialId, long start, long end,
												UserContextData context) throws DbConnectionException, IllegalDataStateException {
		Result<Credential1> res = self.createCredentialDeliveryAndGetEvents(
				credentialId, DateUtil.getDateFromMillis(start), DateUtil.getDateFromMillis(end), context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Result<Credential1> createCredentialDeliveryAndGetEvents(long credentialId, Date start, Date end,
																	UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			//if end date is before start throw exception
			if (start != null && end != null && start.after(end)) {
				throw new IllegalDataStateException("Delivery cannot be ended before it starts");
			}

			Credential1 original = (Credential1) persistence.currentManager().load(Credential1.class, credentialId);

			Credential1 cred = duplicateCredential(original, original, original.getCreatedBy().getId(), CredentialType.Delivery,
					start, end);
			cred.setDuration(original.getDuration());
			cred.setLearningStage(original.getLearningStage());

			saveEntity(cred);
			Result<Credential1> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.Create, context, cred, null,
					null, null));

			Set<Tag> hashtags = cred.getHashtags();
			if (!hashtags.isEmpty()) {
				Map<String, String> params = new HashMap<>();
				String csv = StringUtil.convertTagsToCSV(hashtags);
				params.put("newhashtags", csv);
				params.put("oldhashtags", "");
				res.appendEvent(eventFactory.generateEventData(EventType.UPDATE_HASHTAGS,
						context, cred, null, null, params));
			}

			//lock competencies so they cannot be unpublished after they are published here which would violate our integrity rule
			List<CredentialCompetence1> competences = compManager.getCredentialCompetences(
					credentialId, false, false, true, true);

			//if credential does not have at least one competency, delivery should not be created
			if (competences.isEmpty()) {
				throw new IllegalDataStateException("Can not create delivery without competencies");
			}

			for (CredentialCompetence1 credComp : competences) {
				//create new credential competence which will be referenced by delivery
				CredentialCompetence1 cc = new CredentialCompetence1();
				cc.setCredential(cred);
				cc.setOrder(credComp.getOrder());
				cc.setCompetence(credComp.getCompetence());
				saveEntity(cc);

				//publish competency if not published because creating a delivery means that all competencies must be published
				res.appendEvents(compManager.publishCompetenceIfNotPublished(credComp.getCompetence(), context)
						.getEventQueue());

				cred.getCompetences().add(cc);
			}

			userGroupManager.propagateUserGroupEditPrivilegesFromCredentialToDeliveryAndGetEvents(
					credentialId, cred.getId(), context, persistence.currentManager());

			res.setResult(cred);

			return res;
		} catch (IllegalDataStateException e) {
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
			List<Long> deliveries = session
					.createQuery(query)
					.setLong("credId", credId)
					.list();

			return deliveries;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential delivery ids");
		}
	}

	private List<CredentialData> getCredentialIdsAndTypeForOwner(long ownerId) {
		String query = "SELECT cred.id, cred.type " +
				"FROM Credential1 cred " +
				"WHERE cred.createdBy.id = :ownerId";

		List<Object[]> res = persistence.currentManager()
				.createQuery(query)
				.setLong("ownerId", ownerId)
				.list();
		List<CredentialData> data = new ArrayList<>();
		for (Object[] row : res) {
			CredentialData cd = new CredentialData(false);
			cd.setId((Long) row[0]);
			cd.setType((CredentialType) row[1]);
			data.add(cd);
		}
		return data;
	}

	@Override
	@Transactional
	public Result<Void> updateCredentialCreator(long newCreatorId, long oldCreatorId,
												UserContextData context) throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			List<CredentialData> credentialsWithOldOwner = getCredentialIdsAndTypeForOwner(oldCreatorId);

			String query = "UPDATE Credential1 cred SET " +
					"cred.createdBy = :newCreatorId " +
					"WHERE cred.createdBy = :oldCreatorId";

			persistence.currentManager()
					.createQuery(query)
					.setLong("newCreatorId", newCreatorId)
					.setLong("oldCreatorId", oldCreatorId)
					.executeUpdate();

			for (CredentialData cd : credentialsWithOldOwner) {
				/*
					privilege should be removed from old owner and added to new owner only for original credentials,
					deliveries only inherit those privileges from original.
				 */
				if (cd.getType() == CredentialType.Original) {
					//remove Edit privilege from old owner
					result.appendEvents(userGroupManager.removeUserFromDefaultCredentialGroupAndGetEvents(
							oldCreatorId, cd.getId(), UserGroupPrivilege.Edit, context).getEventQueue());
					//add edit privilege to new owner
					result.appendEvents(userGroupManager.saveUserToDefaultCredentialGroupAndGetEvents(
							newCreatorId, cd.getId(), UserGroupPrivilege.Edit, context).getEventQueue());
				}

				//for all credentials and deliveries change_owner event should be generated
				result.appendEvent(getOwnerChangeEvent(cd.getId(), oldCreatorId, newCreatorId, context));
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential creator");
		}
	}

	private EventData getOwnerChangeEvent(long credId, long oldOwnerId, long newOwnerId, UserContextData context) {
		Credential1 cred = new Credential1();
		cred.setId(credId);
		Map<String, String> params = new HashMap<>();
		params.put("oldOwnerId", oldOwnerId + "");
		params.put("newOwnerId", newOwnerId + "");
		return eventFactory.generateEventData(EventType.OWNER_CHANGE, context, cred, null,
				null, params);
	}

	@Override
	@Transactional(readOnly = true)
	public ResourceCreator getCredentialCreator(long credId) throws DbConnectionException {
		try {
			String query = "SELECT c.createdBy " +
					"FROM Credential1 c " +
					"WHERE c.id = :credId";

			User createdBy = (User) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.uniqueResult();

			return userDataFactory.getResourceCreator(createdBy);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential creator");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getCredentialDeliveriesForUserWithInstructPrivilege(long userId)
			throws DbConnectionException {
		try {
			/*
			deliveries with instruct privilege are retrieved by using instructors added to credential
			because of the better performance than the approach with checking for Instruct privilege.
			That does not change end result because only users that are added to delivery as instructors
			have Instruct privilege for that delivery. If that assumption changes in the future, this
			method would not return correct results.
			 */
			String query =
					"SELECT del " +
							"FROM Credential1 del " +
							"INNER JOIN del.credInstructors instructor " +
							"WITH instructor.user.id = :userId " +
							"WHERE del.type = :type";

			@SuppressWarnings("unchecked")
			List<Credential1> result = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setString("type", CredentialType.Delivery.name())
					.list();

			List<CredentialData> deliveries = new ArrayList<>();
			for (Credential1 d : result) {
				deliveries.add(credentialFactory.getCredentialData(null, d, null, null, null, null, false));
			}

			return deliveries;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential deliveries");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long getCredentialIdForDelivery(long deliveryId) throws DbConnectionException {
		try {
			String query =
					"SELECT c.deliveryOf.id " +
							"FROM Credential1 c " +
							"WHERE c.id = :credId";

			return (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("credId", deliveryId)
					.uniqueResult();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getDeliveryIdsForCredential(long credId) throws DbConnectionException {
		try {
			String query =
					"SELECT del.id " +
							"FROM Credential1 del " +
							"WHERE del.type = :type " +
							"AND del.deliveryOf.id = :credId";

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setString("type", CredentialType.Delivery.name())
					.list();

			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving credential delivery ids");
		}
	}

	@Override
	//nt
	public void changeOwner(long credId, long newOwnerId, UserContextData context) throws DbConnectionException {
		eventFactory.generateEvents(self.changeOwnerAndGetEvents(credId, newOwnerId, context).getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> changeOwnerAndGetEvents(long credId, long newOwnerId, UserContextData context) throws DbConnectionException {
		try {
			List<Credential1> credWithDeliveries = getCredentialWithDeliveries(credId);

			if (!credWithDeliveries.isEmpty()) {
				long oldOwnerId = credWithDeliveries.get(0).getCreatedBy().getId();

				updateCredentialAndDeliveriesOwner(credId, newOwnerId);

				Result<Void> res = new Result<>();
				for (Credential1 c : credWithDeliveries) {
					res.appendEvent(getOwnerChangeEvent(c.getId(), oldOwnerId, newOwnerId, context));
				}
				return res;
			}
			return Result.empty();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while changing the credential owner");
		}
	}

	private List<Credential1> getCredentialWithDeliveries(long credId) {
		String q =
				"SELECT c FROM Credential1 c " +
				"WHERE c.id = :credId " +
				"OR c.deliveryOf.id = :credId";

		@SuppressWarnings("unchecked")
		List<Credential1> credWithDeliveries = persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credId)
				.list();

		return credWithDeliveries;
	}

	private void updateCredentialAndDeliveriesOwner(long credId, long newOwnerId) {
		String q =
				"UPDATE Credential1 c " +
				"SET c.createdBy.id = :newOwnerId " +
				"WHERE c.id = :credId " +
				"OR c.deliveryOf.id = :credId";

		int affected = persistence.currentManager()
				.createQuery(q)
				.setLong("credId", credId)
				.setLong("newOwnerId", newOwnerId)
				.executeUpdate();

		logger.info("Owner updated for " + affected + " credentials");
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfUncompletedDeliveries(long userId) throws DbConnectionException {
		return getIdsOfDeliveriesUserIsLearning(userId, UserLearningProgress.IN_PROGRESS);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfDeliveriesUserIsLearning(long userId, UserLearningProgress progress)
			throws DbConnectionException {
		try {
			String query =
					"SELECT targetCredential1.credential.id " +
					"FROM TargetCredential1 targetCredential1 " +
					"WHERE targetCredential1.user.id = :userid ";

			switch (progress) {
				case COMPLETED:
					query += "AND targetCredential1.progress = 100";
					break;
				case IN_PROGRESS:
					query += "AND targetCredential1.progress < 100";
					break;
				default:
					break;
			}

			List<Long> result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userId)
					.list();

			return result;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving deliveries");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getIdsOfDeliveriesUserIsLearningContainingCompetence(long userId, long compId)
			throws DbConnectionException {
		try {
			String query =
					"SELECT cred.id " +
					"FROM TargetCredential1 targetCredential1 " +
					"INNER JOIN targetCredential1.credential cred " +
					"INNER JOIN cred.competences comp " +
					"WITH comp.competence.id = :compId " +
					"WHERE targetCredential1.user.id = :userid";


			List<Long> result = persistence.currentManager()
					.createQuery(query)
					.setLong("userid", userId)
					.setLong("compId", compId)
					.list();

			return result;
		} catch (DbConnectionException e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while retrieving deliveries");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isUserEnrolled(long credId, long userId) {
		long tcId = getTargetCredentialId(credId, userId);
		return tcId > 0;
	}

	private List<CredentialData> getCredentialsForAdmin(long unitId, CredentialSearchFilterManager searchFilter, int limit,
															int page, LearningResourceSortOption sortOption) {
		//return only first stage credentials and credentials with disabled learning in stages
		StringBuilder query = new StringBuilder(
				"SELECT c " +
				"FROM Credential1 c " +
				"LEFT JOIN fetch c.category " +
				"INNER JOIN c.credentialUnits u " +
						"WITH u.unit.id = :unitId " +
				"WHERE c.type = :credType " +
				"AND c.firstLearningStageCredential IS NULL ");

		switch (searchFilter) {
			case ACTIVE:
				query.append("AND c.archived IS FALSE ");
				break;
			case ARCHIVED:
				query.append("AND c.archived IS TRUE ");
				break;
		}

		query.append("ORDER BY lower(c." + sortOption.getSortFieldDB() + ") " + sortOption.getSortOrder());

		@SuppressWarnings("unchecked")
		List<Credential1> creds = persistence.currentManager()
				.createQuery(query.toString())
				.setString("credType", CredentialType.Original.name())
				.setLong("unitId", unitId)
				.setFirstResult(page * limit)
				.setMaxResults(limit)
				.list();

		List<CredentialData> res = new ArrayList<>();
		for (Credential1 c : creds) {
			CredentialData cd = credentialFactory.getCredentialData(null, c, c.getCategory(), null, null, null, true);
			//if learning in stages is enabled, load active deliveries from all stages, otherwise load active deliveries from this credential only
			if (cd.isLearningStageEnabled()) {
				cd.setDeliveries(getOngoingDeliveriesFromAllStages(c.getId()));
			} else {
				cd.setDeliveries(getOngoingDeliveries(c.getId()));
			}
			res.add(cd);
		}
		return res;
	}

	private long countNumberOfCredentialsForAdmin(long unitId, CredentialSearchFilterManager searchFilter) {
		StringBuilder query = new StringBuilder(
				"SELECT COUNT(c.id) " +
						"FROM Credential1 c " +
						"INNER JOIN c.credentialUnits u " +
						"WITH u.unit.id = :unitId " +
						"WHERE c.type = :credType ");

		switch (searchFilter) {
			case ACTIVE:
				query.append("AND c.archived IS FALSE");
				break;
			case ARCHIVED:
				query.append("AND c.archived IS TRUE");
				break;
		}

		Query q = persistence.currentManager()
				.createQuery(query.toString())
				.setLong("unitId", unitId)
				.setString("credType", CredentialType.Original.name());

		Long count = (Long) q.uniqueResult();

		return count != null ? count : 0;
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedResult<CredentialData> searchCredentialsForAdmin(long unitId, CredentialSearchFilterManager searchFilter, int limit,
																	 int page, LearningResourceSortOption sortOption)
			throws DbConnectionException, NullPointerException {
		PaginatedResult<CredentialData> res = new PaginatedResult<>();
		try {
			if (searchFilter == null || sortOption == null) {
				throw new NullPointerException("Invalid argument values");
			}

			long count = countNumberOfCredentialsForAdmin(unitId, searchFilter);

			if (count > 0) {
				res.setHitsNumber(count);
				res.setFoundNodes(getCredentialsForAdmin(unitId, searchFilter, limit, page, sortOption));
			}
			return res;
		} catch (NullPointerException npe) {
			logger.error("Error", npe);
			throw npe;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credentials");
		}
	}


	//nt
	@Override
	public void updateDeliveryStartAndEnd(CredentialData deliveryData, UserContextData context)
			throws StaleDataException, IllegalDataStateException, DbConnectionException {
		Result<Void> res = self.updateDeliveryStartAndEndAndGetEvents(deliveryData, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> updateDeliveryStartAndEndAndGetEvents(CredentialData deliveryData, UserContextData context)
			throws StaleDataException, IllegalDataStateException, DbConnectionException {
		try {
			Result<Void> res = new Result<>();
			Credential1 delivery = (Credential1) persistence.currentManager()
					.load(Credential1.class, deliveryData.getId());

			//create delivery start and end dates from timestamps
			Date deliveryStart = DateUtil.getDateFromMillis(deliveryData.getDeliveryStartTime());
			Date deliveryEnd = DateUtil.getDateFromMillis(deliveryData.getDeliveryEndTime());

			/*
			 * if it is a delivery and end date is before start throw exception
			 */
			if (deliveryStart != null && deliveryEnd != null
					&& deliveryStart.after(deliveryEnd)) {
				throw new IllegalDataStateException("Delivery cannot be ended before it starts");
			}

			updateDeliveryTimes(delivery, deliveryData, deliveryStart, deliveryEnd);
			persistence.currentManager().flush();

			Credential1 del = new Credential1();
			del.setId(delivery.getId());

			Map<String, String> params = new HashMap<>();
			params.put("deliveryStart", deliveryData.getDeliveryStartTime() + "");
			params.put("deliveryEnd", deliveryData.getDeliveryEndTime() + "");
			res.appendEvent(eventFactory.generateEventData(EventType.UPDATE_DELIVERY_TIMES, context, del, null, null, params));
			return res;
		} catch (HibernateOptimisticLockingFailureException e) {
			logger.error("Error", e);
			throw new StaleDataException("Delivery edited in the meantime");
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating delivery");
		}
	}

	private void updateDeliveryTimes(Credential1 delivery, CredentialData deliveryData, Date deliveryStart, Date deliveryEnd) throws IllegalDataStateException {
		Date now = new Date();
		if (deliveryData.isDeliveryStartChanged()) {
			/*
			 * if delivery start is not set or is in future, changes are allowed
			 */
			if (delivery.getDeliveryStart() == null || delivery.getDeliveryStart().after(now)) {
				delivery.setDeliveryStart(deliveryStart);
			} else {
				throw new IllegalDataStateException("Update failed. Delivery start time cannot be changed because "
						+ "delivery has already started.");
			}
		}

		if (deliveryData.isDeliveryEndChanged()) {
			/*
			 * if delivery end is not set or is in future, changes are allowed
			 */
			if (delivery.getDeliveryEnd() == null || delivery.getDeliveryEnd().after(now)) {
				delivery.setDeliveryEnd(deliveryEnd);
			} else {
				throw new IllegalDataStateException("Update failed. Delivery end time cannot be changed because "
						+ "delivery has already ended.");
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Long getInstructorUserId(long userId, long credId, Session session) throws DbConnectionException {
		try {
			String query = "SELECT instr.user.id FROM TargetCredential1 tc " +
					"INNER JOIN tc.instructor instr " +
					"WHERE tc.user.id = :userId " +
					"AND tc.credential.id = :credId";

			return (Long) session.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.uniqueResult();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving instructor info");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Credential1 getCredentialWithCompetences(long credentialId, CredentialType type) throws DbConnectionException {
		try {
			String q =
					"SELECT cred FROM Credential1 cred " +
							"LEFT JOIN fetch cred.competences credComp " +
							"LEFT JOIN fetch credComp.competence comp " +
							"WHERE cred.id = :credId " +
							"AND cred.type = :deliveryType";

			return (Credential1) persistence.currentManager()
					.createQuery(q)
					.setLong("credId", credentialId)
					.setString("deliveryType", type.name())
					.uniqueResult();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the credential data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Long> getUsersLearningDelivery(long deliveryId) throws DbConnectionException {
		try {
			String usersLearningQ =
					"SELECT targetCred.user.id FROM TargetCredential1 targetCred " +
					"WHERE targetCred.credential.id = :credId";
			@SuppressWarnings("unchecked")
			List<Long> usersLearningCredential = persistence.currentManager()
					.createQuery(usersLearningQ)
					.setLong("credId", deliveryId)
					.list();
			return usersLearningCredential;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the students data");
		}
	}

	@Override
	//nt
	public long createCredentialInLearningStage(long basedOnCredentialId, long learningStageId, boolean copyCompetences, UserContextData context) throws DbConnectionException {
		Result<Credential1> res = self.createCredentialInLearningStageAndGetEvents(basedOnCredentialId, learningStageId, copyCompetences, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult().getId();
	}

	@Override
	@Transactional
	public Result<Credential1> createCredentialInLearningStageAndGetEvents(long basedOnCredentialId, long learningStageId, boolean copyCompetences, UserContextData context) throws DbConnectionException {
		try {
			Result<Credential1> res = new Result<>();

			Credential1 original = (Credential1) persistence.currentManager().load(Credential1.class, basedOnCredentialId);

			Credential1 newCredential = duplicateCredential(original, original.getDeliveryOf(), context.getActorId(), original.getType(),
					original.getDeliveryStart(), original.getDeliveryEnd());
			newCredential.setLearningStage((LearningStage) persistence.currentManager().load(LearningStage.class, learningStageId));

			Credential1 firstStageCred = original.getFirstLearningStageCredential() != null
					? original.getFirstLearningStageCredential()
					: original;
			newCredential.setFirstLearningStageCredential(firstStageCred);
			if (copyCompetences) {
				//if competences are copied, duration will be the same as in original credential
				newCredential.setDuration(original.getDuration());
			}
			saveEntity(newCredential);

			res.appendEvent(eventFactory.generateEventData(
					EventType.Create, context, newCredential, null, null, null));

			//add Edit privilege to the credential creator
			res.appendEvents(userGroupManager.createCredentialUserGroupAndSaveNewUser(
					context.getActorId(), newCredential.getId(),
					UserGroupPrivilege.Edit, true, context).getEventQueue());

			//add credential to all units where credential creator is manager
			res.appendEvents(addCredentialToDefaultUnits(newCredential.getId(), context));

			if (copyCompetences) {
				List<CredentialCompetence1> competences = original.getCompetences();
				for (CredentialCompetence1 credComp : competences) {
					Result<Competence1> compRes = compManager.getOrCreateCompetenceInLearningStageAndGetEvents(
							credComp.getCompetence().getId(), learningStageId, context);
					res.appendEvents(compRes.getEventQueue());
					CredentialCompetence1 cc = new CredentialCompetence1();
					cc.setCompetence(compRes.getResult());
					cc.setCredential(newCredential);
					cc.setOrder(credComp.getOrder());
					saveEntity(cc);
					newCredential.getCompetences().add(cc);

					Competence1 competence = new Competence1();
					competence.setId(compRes.getResult().getId());
					Credential1 cred = new Credential1();
					cred.setId(newCredential.getId());
					res.appendEvent(eventFactory.generateEventData(
							EventType.Attach, context, competence, cred,null, null));
				}
			}
			res.setResult(newCredential);
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error saving the credential");
		}
	}

	private Credential1 duplicateCredential(Credential1 original, Credential1 deliveryOf, long creatorId, CredentialType type,
											Date deliveryStart, Date deliveryEnd) {
		try {
			Credential1 cred = new Credential1();
			cred.setOrganization(original.getOrganization());
			cred.setTitle(original.getTitle());
			cred.setCategory(original.getCategory());
			cred.setDescription(original.getDescription());
			cred.setCreatedBy((User) persistence.currentManager().load(User.class, creatorId));
			cred.setDateCreated(new Date());
			cred.setTags(new HashSet<>(original.getTags()));
			cred.setHashtags(new HashSet<>(original.getHashtags()));
			cred.setCompetenceOrderMandatory(original.isCompetenceOrderMandatory());
			//cred.setDuration(original.getDuration());
			cred.setManuallyAssignStudents(original.isManuallyAssignStudents());
			cred.setDefaultNumberOfStudentsPerInstructor(original.getDefaultNumberOfStudentsPerInstructor());
			cred.setType(type);
			cred.setDeliveryOf(deliveryOf);
			cred.setDeliveryStart(deliveryStart);
			cred.setDeliveryEnd(deliveryEnd);
			cred.setVisibleToAll(original.isVisibleToAll());
			//set assessment related data
			cred.setGradingMode(original.getGradingMode());
			cred.setMaxPoints(original.getMaxPoints());
			cred.setRubric(original.getRubric());
			saveEntity(cred);
			for (CredentialAssessmentConfig cac : original.getAssessmentConfig()) {
				CredentialAssessmentConfig credAssessmentConf = new CredentialAssessmentConfig();
				credAssessmentConf.setCredential(cred);
				credAssessmentConf.setAssessmentType(cac.getAssessmentType());
				credAssessmentConf.setEnabled(cac.isEnabled());
				saveEntity(credAssessmentConf);
			}

			return cred;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error copying the credential");
		}
	}

	@Override
	@Transactional
	public EventQueue disableLearningStagesForOrganizationCredentials(long orgId, UserContextData context) throws DbConnectionException {
		try {
			List<Credential1> creds = getAllCredentialsWithLearningStagesEnabled(orgId);
			EventQueue queue = EventQueue.newEventQueue();
			for (Credential1 cred : creds) {
				queue.appendEvents(disableLearningInStagesForCredential(cred, context));
			}
			return queue;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error disabling learning in stages for credentials in organization: " + orgId);
		}
	}

	private EventQueue disableLearningInStagesForCredential(Credential1 cred, UserContextData context) {
		 cred.setLearningStage(null);
		 cred.setFirstLearningStageCredential(null);

		 Credential1 credential = new Credential1();
		 credential.setId(cred.getId());
		 EventQueue queue = EventQueue.newEventQueue();
		 queue.appendEvent(eventFactory.generateEventData(EventType.LEARNING_STAGE_UPDATE, context, credential, null, null, null));
	 	 return queue;
	 }

	/**
	 * Does not return deliveries
	 *
	 * @param orgId
	 * @return
	 * @throws DbConnectionException
	 */
	private List<Credential1> getAllCredentialsWithLearningStagesEnabled(long orgId) throws DbConnectionException {
		String query =
				"SELECT cred " +
				"FROM Credential1 cred " +
				"WHERE cred.deleted IS FALSE " +
				"AND cred.organization.id = :orgId " +
				"AND cred.learningStage IS NOT NULL " +
				"AND cred.type = :originalType";

		@SuppressWarnings("unchecked")
		List<Credential1> result = persistence.currentManager()
				.createQuery(query)
				.setLong("orgId", orgId)
				.setString("originalType", CredentialType.Original.name())
				.list();

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentTypeConfig> getCredentialAssessmentTypesConfig(long credId) throws DbConnectionException {
		try {
			String q =
					"SELECT conf FROM CredentialAssessmentConfig conf " +
					"WHERE conf.credential.id = :credId";
			@SuppressWarnings("unchecked")
			List<CredentialAssessmentConfig> assessmentTypesConfig = persistence.currentManager()
					.createQuery(q)
					.setLong("credId", credId)
					.list();
			return credentialFactory.getAssessmentConfig(assessmentTypesConfig);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the assessment types config for credential");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long getTargetCredentialId(long credId, long studentId) throws DbConnectionException {
		try {
			String query =
					"SELECT targetCredential.id " +
							"FROM TargetCredential1 targetCredential " +
							"WHERE targetCredential.user.id = :userId " +
							"AND targetCredential.credential.id = :credId";

			Long result = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", studentId)
					.setLong("credId", credId)
					.uniqueResult();

			return result != null ? result.longValue() : 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading target credential id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CredentialCategory getCredentialCategory(long categoryId) throws DbConnectionException {
		try {
			return (CredentialCategory) persistence.currentManager().get(CredentialCategory.class, categoryId);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential category");
		}
	}

	@Override
	@Transactional
	public CredentialData getTargetCredentialDataWithEvidencesAndAssessmentCount(long credentialId, long studentId) {
		TargetCredential1 tc = getTargetCredentialForStudentAndCredential(credentialId, studentId);
		return getTargetCredentialData(credentialId, studentId,
				CredentialLoadConfig.of(false, true, true, true,false, true, tc.isCredentialAssessmentsDisplayed(), false,
						CompetenceLoadConfig.of(false, false, false, tc.isEvidenceDisplayed(), tc.isCompetenceAssessmentsDisplayed())));
	}

	private TargetCredential1 getTargetCredentialForStudentAndCredential(long credentialId, long studentId) {
		String q =
				"SELECT tc FROM TargetCredential1 tc " +
				"WHERE tc.credential.id = :credId " +
				"AND tc.user.id = :studentId";
		return (TargetCredential1) persistence.currentManager().createQuery(q)
				.setLong("credId", credentialId)
				.setLong("studentId", studentId)
				.uniqueResult();
	}

	@Override
	@Transactional
	public void updateCredentialAssessmentsVisibility(long targetCredentialId, boolean displayAssessments) {
		try {
			TargetCredential1 tc = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId);
			tc.setCredentialAssessmentsDisplayed(displayAssessments);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating credentialAssessmentsDisplayed field of a target credential " + targetCredentialId);
		}
	}

	@Override
	@Transactional
	public void updateCompetenceAssessmentsVisibility(long targetCredentialId, boolean displayAssessments) {
		try {
			TargetCredential1 tc = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId);
			tc.setCompetenceAssessmentsDisplayed(displayAssessments);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating competenceAssessmentsDisplayed field of a target credential " + targetCredentialId);
		}
	}

	@Override
	@Transactional
	public void updateEvidenceVisibility(long targetCredentialId, boolean displayEvidence) {
		try {
			TargetCredential1 tc = (TargetCredential1) persistence.currentManager().load(TargetCredential1.class, targetCredentialId);
			tc.setEvidenceDisplayed(displayEvidence);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating evidenceDisplayed field of a target credential " + targetCredentialId);
		}
	}
}
