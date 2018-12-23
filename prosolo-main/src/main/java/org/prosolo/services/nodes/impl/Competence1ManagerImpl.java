package org.prosolo.services.nodes.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.learningStage.LearningStage;
import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.assessment.RubricManager;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.EventQueue;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.config.competence.CompetenceLoadConfig;
import org.prosolo.services.nodes.data.*;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.competence.TargetCompetenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceDataFactory;
import org.prosolo.services.nodes.data.resourceAccess.*;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.prosolo.services.util.roles.SystemRoleNames;
import org.prosolo.web.util.ResourceBundleUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.*;

@Service("org.prosolo.services.nodes.Competence1Manager")
public class Competence1ManagerImpl extends AbstractManagerImpl implements Competence1Manager {

	private static final long serialVersionUID = -2783669846949034832L;

	private static Logger logger = Logger.getLogger(Competence1ManagerImpl.class);

	@Inject
	private TagManager tagManager;
	@Inject
	private CompetenceDataFactory competenceFactory;
	@Inject
	private Activity1Manager activityManager;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private ResourceFactory resourceFactory;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UserGroupManager userGroupManager;
	@Inject private ActivityDataFactory activityFactory;
	@Inject private ResourceAccessFactory resourceAccessFactory;
	@Inject private UserDataFactory userDataFactory;
	@Inject private Competence1Manager self;
	@Inject private RoleManager roleManager;
	@Inject private UnitManager unitManager;
	@Inject private LearningEvidenceManager learningEvidenceManager;
	@Inject private LearningEvidenceDataFactory learningEvidenceDataFactory;
	@Inject private RubricManager rubricManager;
	@Inject private AssessmentManager assessmentManager;

	@Override
	//nt
	public Competence1 saveNewCompetence(CompetenceData1 data, long credentialId,
										 UserContextData context) throws DbConnectionException,
			IllegalDataStateException {
		//self-invocation
		Result<Competence1> res = self.saveNewCompetenceAndGetEvents(data, credentialId, context);

		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Competence1> saveNewCompetenceAndGetEvents(CompetenceData1 data, long credentialId,
			UserContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			/*
			 * if competence has no activities, it can't be published
			 */
			if (data.isPublished() && data.getLearningPathType() == LearningPathType.ACTIVITY && (data.getActivities() == null || data.getActivities().isEmpty())) {
				throw new IllegalDataStateException("Can not publish " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " without activities.");
			}

			Competence1 comp = new Competence1();
			comp.setOrganization((Organization) persistence.currentManager().load(Organization.class, context.getOrganizationId()));
			comp.setTitle(data.getTitle());
			comp.setDateCreated(new Date());
			comp.setDescription(data.getDescription());
			comp.setCreatedBy(loadResource(User.class, context.getActorId()));
			comp.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
			comp.setType(data.getType());
			comp.setPublished(data.isPublished());
			if (data.isPublished()) {
				comp.setDatePublished(new Date());
			}
			comp.setDuration(data.getDuration());
			comp.setLearningPathType(data.getLearningPathType());
			comp.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(data.getTagsString())));

			if (credentialId > 0) {
				//TODO learning in stages - for now we propagate learning stage from credential
				Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credentialId);
				if (cred.getLearningStage() != null) {
					comp.setLearningStage(cred.getLearningStage());
				}
			}

			setAssessmentRelatedData(comp, data, true);

			saveEntity(comp);

			if (data.getAssessmentTypes() != null) {
				for (AssessmentTypeConfig atc : data.getAssessmentTypes()) {
					CompetenceAssessmentConfig cac = new CompetenceAssessmentConfig();
					cac.setCompetence(comp);
					cac.setAssessmentType(atc.getType());
					cac.setEnabled(atc.isEnabled());
					saveEntity(cac);
				}
			}

			if (data.getLearningPathType() == LearningPathType.ACTIVITY && data.getActivities() != null) {
				for (ActivityData bad : data.getActivities()) {
					CompetenceActivity1 ca = new CompetenceActivity1();
					ca.setOrder(bad.getOrder());
					ca.setCompetence(comp);
					Activity1 act = (Activity1) persistence.currentManager().load(
							Activity1.class, bad.getActivityId());
					ca.setActivity(act);
					saveEntity(ca);
				}
			}
			Result<Competence1> result = new Result<>();
			result.appendEvent(eventFactory.generateEventData(EventType.Create,
					context, comp, null, null, null));

			if (credentialId > 0) {
				result.appendEvents(credentialManager.addCompetenceToCredential(credentialId, comp,
						context));
			}

			//add Edit privilege to the competence creator
			result.appendEvents(userGroupManager.createCompetenceUserGroupAndSaveNewUser(
					context.getActorId(), comp.getId(),
					UserGroupPrivilege.Edit,true, context).getEventQueue());

			//add competence to all units where competence creator is manager
			result.appendEvents(addCompetenceToDefaultUnits(comp.getId(), context));

			logger.info("New competence is created with id " + comp.getId());
			result.setResult(comp);
			return result;
		} catch (IllegalDataStateException e) {
			logger.error(e);
			//cee.printStackTrace();
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competence");
		}
	}

	private void setAssessmentRelatedData(Competence1 competence, CompetenceData1 data, boolean updateRubric) throws IllegalDataStateException {
		competence.setGradingMode(data.getAssessmentSettings().getGradingMode());
		switch (data.getAssessmentSettings().getGradingMode()) {
			case AUTOMATIC:
				competence.setRubric(null);
				break;
			case MANUAL:
				if (updateRubric) {
					competence.setRubric(rubricManager.getRubricForLearningResource(data.getAssessmentSettings()));
				}
				break;
			case NONGRADED:
				competence.setRubric(null);
				break;
		}
		competence.setMaxPoints(
				isPointBasedCompetence(competence.getGradingMode(), competence.getRubric())
						? (data.getAssessmentSettings().getMaxPointsString().isEmpty() ? 0 : Integer.parseInt(data.getAssessmentSettings().getMaxPointsString()))
						: 0);
	}

	private boolean isPointBasedCompetence(GradingMode gradingMode, Rubric rubric) {
		return gradingMode == GradingMode.MANUAL && (rubric == null || rubric.getRubricType() == RubricType.POINT || rubric.getRubricType() == RubricType.POINT_RANGE);
	}

	/**
	 * Connects competence to all units competence creator (context actor) is manager in.
	 *
	 * @param compId
	 * @param context
	 * @return
	 */
	private EventQueue addCompetenceToDefaultUnits(long compId, UserContextData context) {
		long managerRoleId = roleManager.getRoleIdByName(SystemRoleNames.MANAGER);
		List<Long> unitsWithManagerRole = unitManager.getUserUnitIdsInRole(context.getActorId(), managerRoleId);
		EventQueue events = EventQueue.newEventQueue();
		for (long unitId : unitsWithManagerRole) {
			events.appendEvents(unitManager.addCompetenceToUnitAndGetEvents(compId, unitId, context).getEventQueue());
		}
		return events;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCompetencesForCredential(long credId, long userId, CompetenceLoadConfig compLoadConfig)
			throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT credComp, tComp " +
				       	   "FROM Credential1 cred " + 
				       	   "INNER JOIN cred.competences credComp " +
				       	   "INNER JOIN fetch credComp.competence comp " +
				       	   "LEFT JOIN comp.targetCompetences tComp " +
				       			"WITH tComp.user.id = :userId ");

			if (compLoadConfig.isLoadCreator()) {
				builder.append("INNER JOIN fetch comp.createdBy user ");
			}

			if (compLoadConfig.isLoadTags()) {
				builder.append("LEFT JOIN fetch comp.tags tags ");
			}
			builder.append("WHERE cred.id = :credId " +
				       	   "ORDER BY credComp.order");

			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
				.createQuery(builder.toString())
				.setLong("credId", credId)
				.setLong("userId", userId)
				.list();

			if (res != null) {
				//duplicates need to be filtered because same if there is more than one tag for competence there will be duplicates
				Set<Long> uniqueComps = new HashSet<>();
				for (Object[] row : res) {
					CredentialCompetence1 cc = (CredentialCompetence1) row[0];
					if (!uniqueComps.contains(cc.getId())) {
						uniqueComps.add(cc.getId());
						
						Competence1 comp = cc.getCompetence();
						Set<Tag> tags = compLoadConfig.isLoadTags() ? comp.getTags() : null;
						User createdBy = compLoadConfig.isLoadCreator() ? comp.getCreatedBy() : null;
						TargetCompetence1 tComp = (TargetCompetence1) row[1];
						CompetenceData1 compData;
						if (tComp != null) {
							compData = competenceFactory.getCompetenceData(createdBy, tComp, cc.getOrder(), null, tags, null,
									false);
							if (compData != null) {
								if (compData.getLearningPathType() == LearningPathType.ACTIVITY && compLoadConfig.isLoadActivities()) {
									List<ActivityData> activities = activityManager
											.getTargetActivitiesData(compData.getTargetCompId());
									compData.setActivities(activities);
								} else if (compData.getLearningPathType() == LearningPathType.EVIDENCE && compLoadConfig.isLoadEvidence()) {
									//load user evidences
									List<LearningEvidenceData> compEvidences = learningEvidenceManager.getUserEvidencesForACompetence(compData.getTargetCompId(), false);
									compData.setEvidences(compEvidences);
								}
								//load number of competence assessments if needed
								if (compLoadConfig.isLoadAssessmentCount()) {
									compData.setNumberOfAssessments(assessmentManager.getNumberOfApprovedAssessmentsForUserCompetence(compData.getCompetenceId(), userId));
								}
							}
						} else {
							compData = competenceFactory.getCompetenceData(createdBy, cc, null, tags, false);
							if (compData.getLearningPathType() == LearningPathType.ACTIVITY && compLoadConfig.isLoadActivities()) {
								List<ActivityData> activities = activityManager.getCompetenceActivitiesData(
										compData.getCompetenceId());
								compData.setActivities(activities);
							}
						}
						result.add(compData);
					}
				}
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	public TargetCompetence1 enrollInCompetence(long compId, long userId, UserContextData context)
			throws DbConnectionException {
		Result<TargetCompetence1> res = self.enrollInCompetenceAndGetEvents(compId, userId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<TargetCompetence1> enrollInCompetenceAndGetEvents(long compId, long userId, UserContextData context)
			throws DbConnectionException {
		try {
			Date now = new Date();
			TargetCompetence1 targetComp = new TargetCompetence1();
			targetComp.setDateCreated(now);
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			targetComp.setCompetence(comp);
			User user = (User) persistence.currentManager().load(User.class, userId);
			targetComp.setUser(user);
			
			saveEntity(targetComp);

			if (comp.getLearningPathType() == LearningPathType.ACTIVITY) {
				List<TargetActivity1> targetActivities = activityManager.createTargetActivities(targetComp);
				targetComp.setTargetActivities(targetActivities);
			
				/*
				 * set first activity as next to learn
				 */
				if (!targetActivities.isEmpty()) {
					targetComp.setNextActivityToLearnId(targetActivities.get(0).getActivity().getId());
				}
			}
			
			Competence1 competence = new Competence1();
			competence.setId(compId);
			Map<String, String> params = new HashMap<>();
			params.put("dateEnrolled", DateUtil.getMillisFromDate(now) + "");

			Result<TargetCompetence1> res = new Result<>();
			res.setResult(targetComp);
			res.appendEvent(eventFactory.generateEventData(EventType.ENROLL_COMPETENCE, context, competence, null, null, params));

			//create self assessment if enabled
			if (comp.getAssessmentConfig()
					.stream()
					.filter(config -> config.getAssessmentType() == AssessmentType.SELF_ASSESSMENT)
					.findFirst().get()
					.isEnabled()) {
				res.appendEvents(assessmentManager.createSelfCompetenceAssessmentAndGetEvents(compId, userId, context).getEventQueue());
			}

			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling a competence");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CompetenceData1> getCompetenceDataWithAccessRightsInfo(long credId, long compId, 
			boolean loadCreator, boolean loadAssessmentConfig, boolean loadTags, boolean loadActivities, long userId,
			ResourceAccessRequirements req, boolean shouldTrackChanges) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			if(req == null) {
				throw new IllegalArgumentException();
			}
			CompetenceData1 compData = getCompetenceData(credId, compId, loadCreator, loadAssessmentConfig, loadTags, loadActivities,
					shouldTrackChanges);
			
			ResourceAccessData access = getResourceAccessData(compId, userId, req);
			
			return RestrictedAccessResult.of(compData, access);
		} catch (ResourceNotFoundException rfe) {
			throw rfe;
		} catch(IllegalArgumentException iae) { 
			throw iae;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, 
			boolean loadAssessmentConfig, boolean loadTags, boolean loadActivities, boolean shouldTrackChanges)
					throws ResourceNotFoundException, DbConnectionException {
		try {
			Competence1 comp = getCompetence(credId, compId, loadCreator, loadTags, true);
			
			if (comp == null) {
				throw new ResourceNotFoundException();
			}
			
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<CompetenceAssessmentConfig> assessmentConfig = loadAssessmentConfig ? comp.getAssessmentConfig() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, assessmentConfig, tags, shouldTrackChanges);

			//activities should be loaded only if learning path is activity based
			if (compData.getLearningPathType() == LearningPathType.ACTIVITY && loadActivities) {
				List<ActivityData> activities = activityManager.getCompetenceActivitiesData(compId);
				compData.setActivities(activities);
			}

			return compData;
		} catch (ResourceNotFoundException rfe) {
			throw rfe;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	/**
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param returnIfArchived
	 * @return
	 */
	private Competence1 getCompetence(long credId, long compId, boolean loadCreator, boolean loadTags,
			boolean returnIfArchived) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT comp ");
		/*
		 * if credential id is passed need to make sure that competence really
		 * is in credential with specified id
		 */
		if(credId > 0) {
			builder.append("FROM CredentialCompetence1 credComp " +
						   "INNER JOIN credComp.competence comp " +
						   		"WITH comp.id = :compId " +
						   	    "AND comp.deleted = :deleted ");
			if(!returnIfArchived) {
				builder.append("AND comp.archived = :archived ");
			}
		} else {
			builder.append("FROM Competence1 comp ");
		}
		// builder.append("SELECT comp " +
		// "FROM Competence1 comp ");

		if (loadCreator) {
			builder.append("INNER JOIN fetch comp.createdBy user ");
		}
		if (loadTags) {
			builder.append("LEFT JOIN fetch comp.tags tags ");
		}
		// if(loadActivities) {
		// builder.append("LEFT JOIN fetch comp.activities compAct " +
		// "INNER JOIN fetch compAct.activity act ");
		// }
		if (credId > 0) {
			builder.append("WHERE credComp.credential.id = :credId");
		} else {
			builder.append("WHERE comp.id = :compId " +
					   "AND comp.deleted = :deleted ");
			if(!returnIfArchived) {
				builder.append("AND comp.archived = :archived ");
			}
		}

		logger.info("QUERY: " + builder.toString());
		Query q = persistence.currentManager().createQuery(builder.toString()).setLong("compId", compId)
				.setBoolean("deleted", false);

		if (credId > 0) {
			q.setLong("credId", credId);
		}
		if(!returnIfArchived) {
			q.setBoolean("archived", false);
		}

		Competence1 res = (Competence1) q.uniqueResult();
		return res;
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Competence1 updateCompetence(CompetenceData1 data, UserContextData context)
			throws DbConnectionException, IllegalDataStateException, StaleDataException {
		try {
			/*
			 * if competence has Activity learning path and has no activities (that are not removed), it can't
			 * be published
			 */
			if (data.isPublished() && data.getLearningPathType() == LearningPathType.ACTIVITY) {
				if (data.getActivities() == null) {
					throw new IllegalDataStateException("Competency should have at least one activity");
				}
				long numberOfActivities = data.getActivities().stream().filter(
						act -> act.getObjectStatus() != ObjectStatus.REMOVED).count();
				if (numberOfActivities == 0) {
					throw new IllegalDataStateException("Competency should have at least one activity");
				}
			}

			Competence1 updatedComp = resourceFactory.updateCompetence(data, context.getActorId());

			fireCompEditEvent(data, updatedComp, context);
			
			/* 
			 * flushing to force lock timeout exception so it can be caught here.
			 * It is rethrown as StaleDataException.
			 */
			persistence.currentManager().flush();
		    
			return updatedComp;
		} catch(StaleDataException|IllegalDataStateException e) {
			logger.error(e);
			//cee.printStackTrace();
			throw e;
		} catch(HibernateOptimisticLockingFailureException e) {
			e.printStackTrace();
			logger.error(e);
			throw new StaleDataException("Competence edited in the meantime");
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence");
		}
	}

	private void fireCompEditEvent(CompetenceData1 data, Competence1 updatedComp,
								   UserContextData context) {
		Map<String, String> params = new HashMap<>();
		CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
				data.isPublishedChanged(), data.isTitleChanged(), data.isDescriptionChanged(), false,
				data.isTagsStringChanged(), data.isStudentAllowedToAddActivitiesChanged());
		Gson gson = new GsonBuilder().create();
		String jsonChangeTracker = gson.toJson(changeTracker);
		params.put("changes", jsonChangeTracker);

		eventFactory.generateEvent(EventType.Edit, context, updatedComp,null, null, params);
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public Competence1 updateCompetenceData(CompetenceData1 data, long userId) throws StaleDataException,
		IllegalDataStateException {
		Competence1 compToUpdate = (Competence1) persistence.currentManager()
				.load(Competence1.class, data.getCompetenceId(), LockOptions.UPGRADE);
		
		/* this check is needed to find out if competence is changed from the moment competence data
		 * is loaded for edit to the moment update request is sent
		 */
		if(compToUpdate.getVersion() != data.getVersion()) {
			throw new StaleDataException("Competence edited in the meantime");
		}
		
		/* if competence should be unpublished we need to check if there are ongoing deliveries with this competence
		 * and if so, unpublish should not be allowed
		 */
		if(!data.isPublished() && data.isPublishedChanged()) {
			boolean canUnpublish = !isThereOngoingDeliveryWithCompetence(data.getCompetenceId());
			if(!canUnpublish) {
				throw new IllegalDataStateException("Competency can not be unpublished because there is an ongoing credential delivery with this competency");
			}
		}
		
		compToUpdate.setTitle(data.getTitle());
		compToUpdate.setDescription(data.getDescription());
		compToUpdate.setPublished(data.isPublished());
		//if it is first publish set publish date
		if(data.isPublished() && data.getDatePublished() == null) {
			compToUpdate.setDatePublished(new Date());
		}
    	if(data.isTagsStringChanged()) {
    		compToUpdate.setTags(new HashSet<>(tagManager.parseCSVTagsAndSave(
    				data.getTagsString())));		     
    	}

		if (data.getAssessmentTypes() != null) {
			for (AssessmentTypeConfig atc : data.getAssessmentTypes()) {
				if (atc.hasObjectChanged()) {
					CompetenceAssessmentConfig cac = (CompetenceAssessmentConfig) persistence.currentManager().load(CompetenceAssessmentConfig.class, atc.getId());
					cac.setEnabled(atc.isEnabled());
				}
			}
		}
    	
    	//these changes are not allowed if competence was once published
    	if(data.getDatePublished() == null) {
			compToUpdate.setLearningPathType(data.getLearningPathType());
    		compToUpdate.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());

    		if (data.getLearningPathType() == LearningPathType.ACTIVITY) {
				List<ActivityData> activities = data.getActivities();
				if (activities != null) {
					boolean recalculateDuration = false;
					Iterator<ActivityData> actIterator = activities.iterator();
					while (actIterator.hasNext()) {
						ActivityData bad = actIterator.next();
						switch (bad.getObjectStatus()) {
							case CREATED:
								CompetenceActivity1 ca1 = new CompetenceActivity1();
								ca1.setOrder(bad.getOrder());
								ca1.setCompetence(compToUpdate);
								Activity1 act = (Activity1) persistence.currentManager().load(
										Activity1.class, bad.getActivityId());
								ca1.setActivity(act);
								saveEntity(ca1);
								recalculateDuration = true;
								break;
							case CHANGED:
								CompetenceActivity1 ca2 = (CompetenceActivity1) persistence
										.currentManager().load(CompetenceActivity1.class,
												bad.getCompetenceActivityId());
								ca2.setOrder(bad.getOrder());
								break;
							case REMOVED:
								CompetenceActivity1 ca3 = (CompetenceActivity1) persistence.currentManager().load(
										CompetenceActivity1.class, bad.getCompetenceActivityId());
								delete(ca3);
								recalculateDuration = true;
								break;
							case UP_TO_DATE:
								break;
						}
					}

					//activityManager.publishDraftActivities(0, userId, actIds);
//	    		updateDurationForAllCredentialsWithCompetence(data.getCompetenceId(), 
//	    				Operation.Add, compToUpdate.getDuration());
					if (recalculateDuration) {
						persistence.currentManager().flush();
						long oldDuration = compToUpdate.getDuration();
						long newDuration = getRecalculatedDuration(compToUpdate.getId());
						compToUpdate.setDuration(newDuration);
						updateCredDuration(compToUpdate.getId(), newDuration, oldDuration);
					}
				}
			} else if (data.isLearningPathChanged()) {
				//if new learning path is evidence and it was activity delete all competence activities and set duration to 0
				compToUpdate.getActivities().clear();
				compToUpdate.setDuration(0);
			}

			setAssessmentRelatedData(compToUpdate, data, data.getAssessmentSettings().isRubricChanged());
    	}
	    
	    return compToUpdate;
	}

	private void updateCredDuration(long compId, long newDuration, long oldDuration) {
		long durationChange = newDuration - oldDuration;
		Operation op = null;
		if (durationChange == 0) {
			return;
		}
		if (durationChange > 0) {
			op = Operation.Add;
		} else {
			durationChange = -durationChange;
			op = Operation.Subtract;
		}
		credentialManager.updateDurationForCredentialsWithCompetence(compId, durationChange, op);
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator,
															  boolean loadTags, boolean loadLearningPathData, boolean includeNotPublished)
					throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			List<CredentialCompetence1> res = getCredentialCompetences(credentialId, loadCreator, loadTags,
					includeNotPublished);

			for (CredentialCompetence1 credComp : res) {
				User creator = loadCreator ? credComp.getCompetence().getCreatedBy() : null;
				Set<Tag> tags = loadTags ? credComp.getCompetence().getTags() : null;
				
				CompetenceData1 compData = competenceFactory.getCompetenceData(
						creator, credComp, null, tags, true);
				
				if (compData.getLearningPathType() == LearningPathType.ACTIVITY && loadLearningPathData) {
					List<ActivityData> activities = activityManager.getCompetenceActivitiesData(
							credComp.getCompetence().getId());
					compData.setActivities(activities);
				}

				result.add(compData);
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator,
			boolean loadTags, boolean includeNotPublished) throws DbConnectionException {
		return getCredentialCompetences(credentialId, loadCreator, loadTags, includeNotPublished, false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean includeNotPublished, boolean usePessimisticLock) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credentialId);

			StringBuilder builder = new StringBuilder();

			builder.append("SELECT credComp " + 
						   "FROM CredentialCompetence1 credComp " +
						   "INNER JOIN fetch credComp.competence comp ");
			if(loadCreator) {
				builder.append("INNER JOIN fetch comp.createdBy user ");
			}
			if (loadTags) {
				builder.append("LEFT JOIN fetch comp.tags tags ");
			}

			builder.append("WHERE credComp.credential = :credential " + "AND comp.deleted = :deleted "
					+ "AND credComp.deleted = :deleted ");

			if (!includeNotPublished) {
				builder.append("AND comp.published = :published ");
			}

			builder.append("ORDER BY credComp.order");

			Query query = persistence.currentManager().createQuery(builder.toString()).setEntity("credential", cred)
					.setBoolean("deleted", false);
			if (!includeNotPublished) {
				query.setBoolean("published", true);
			}
			
			if (usePessimisticLock) {
				query.setLockOptions(LockOptions.UPGRADE);
			}
		
			@SuppressWarnings("unchecked")
			List<CredentialCompetence1> res = query.list();

			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential competences data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId) throws DbConnectionException {
		return getCompetenceTags(compId, persistence.currentManager());
	}

	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId, Session session) throws DbConnectionException {
		try {
			String query = "SELECT tag " + "FROM Competence1 comp " + "INNER JOIN comp.tags tag "
					+ "WHERE comp.id = :compId";
			@SuppressWarnings("unchecked")
			List<Tag> res = session.createQuery(query).setLong("compId", compId).list();
			if (res == null) {
				return new ArrayList<>();
			}

			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence tags");
		}
	}

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public EventData addActivityToCompetence(long compId, Activity1 act, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			/*
			 * Lock the competence row in db so it can't be updated while we have the lock.
			 * That way, we can avoid the situation where competence is published concurrently and
			 * we add new activity to it after it is published which violates data integrity rule
			 *
			 * or when competence learning path type changed in the meantime and new learning path type does not
			 * support adding activities
			 */
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId, 
					LockOptions.UPGRADE);
			
			//if publish date is not null then new activities are not allowed to be added. Only limited edits are allowed.
			if (comp.getDatePublished() != null) {
				throw new IllegalDataStateException("After competence is first published, new activities can not be added. Only limited edits allowed.");
			}

			if (comp.getLearningPathType() != LearningPathType.ACTIVITY) {
				throw new IllegalDataStateException("Competency doesn't support adding activities");
			}

			CompetenceActivity1 ca = new CompetenceActivity1();
			ca.setActivity(act);
			ca.setCompetence(comp);
			ca.setOrder(comp.getActivities().size() + 1);
			saveEntity(ca);

			/* 
			 * If duration of added activity is greater than 0 update competence duration
			*/
			if(act.getDuration() > 0) {
				comp.setDuration(comp.getDuration() + act.getDuration());
				credentialManager.updateDurationForCredentialsWithCompetence(compId, act.getDuration(), Operation.Add);
			}

			return null;

		} catch(IllegalDataStateException idse) {
			logger.error(idse);
			throw idse;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding activity to competence");
		}

	}

	// private Competence1 createDraftVersionOfCompetence(long originalCompId) {
	// Competence1 originalComp = getCompetence(0, originalCompId, false, true,
	// 0,
	// LearningResourceReturnResultType.PUBLISHED_VERSION, false);
	//
	// Competence1 draftComp = new Competence1();
	// draftComp.setDraft(true);
	// draftComp.setPublished(false);
	// draftComp.setCreatedBy(originalComp.getCreatedBy());
	// draftComp.setTitle(originalComp.getTitle());
	// draftComp.setDescription(originalComp.getDescription());
	// draftComp.setStudentAllowedToAddActivities(originalComp.isStudentAllowedToAddActivities());
	// draftComp.setDuration(originalComp.getDuration());
	// draftComp.setType(originalComp.getType());
	//
	// if(originalComp.getTags() != null) {
	// for(Tag tag : originalComp.getTags()) {
	// draftComp.getTags().add(tag);
	// }
	// }
	//
	// saveEntity(draftComp);
	//
	// List<CompetenceActivity1> activities = activityManager
	// .getCompetenceActivities(originalCompId, true);
	// if(activities != null) {
	// for(CompetenceActivity1 ca : activities) {
	// CompetenceActivity1 ca1 = new CompetenceActivity1();
	// ca1.setOrder(ca.getOrder());
	// ca1.setCompetence(draftComp);
	// ca1.setActivity(ca.getActivity());
	// saveEntity(ca1);
	// draftComp.getActivities().add(ca1);
	// }
	// }
	//
	// return draftComp;
	// }

	@Override
	@Transactional(readOnly = false)
	public void updateDurationForCompetenceWithActivity(long actId, long duration, Operation op)
			throws DbConnectionException {
		try {
			Optional<Long> compId = getCompetenceIdForActivity(actId);

			if (compId.isPresent()) {
				long competenceId = compId.get();
				String opString = op == Operation.Add ? "+" : "-";
				String query = "UPDATE Competence1 comp SET " + "comp.duration = comp.duration " + opString
						+ " :duration " + "WHERE comp.id = :compId";

				persistence.currentManager().createQuery(query).setLong("duration", duration)
						.setLong("compId", competenceId).executeUpdate();

				credentialManager.updateDurationForCredentialsWithCompetence(competenceId, duration, op);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
	}

	private Optional<Long> getCompetenceIdForActivity(long actId) {
		String query = "SELECT comp.id FROM CompetenceActivity1 cAct " + "INNER JOIN cAct.competence comp "
				+ "WHERE cAct.activity.id = :actId";

		Long res = (Long) persistence.currentManager().createQuery(query).setLong("actId", actId).uniqueResult();

		return res == null ? Optional.empty() : Optional.of(res);
	}

	@Override
	@Transactional(readOnly = true)
	public String getCompetenceTitle(long id) throws DbConnectionException {
		try {
			String query = "SELECT comp.title " + "FROM Competence1 comp " + "WHERE comp.id = :compId";

			String title = (String) persistence.currentManager().createQuery(query).setLong("compId", id)
					.uniqueResult();

			return title;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence title");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public LearningInfo getCompetenceLearningInfo(long compId, long userId) throws DbConnectionException {
		try {
			String query = "SELECT comp.title, tComp.nextActivityToLearnId " +
						   "FROM TargetCompetence1 tComp " +
						   "INNER JOIN tComp.competence comp " +
						   		"WITH comp.id = :compId " +
						   "WHERE tComp.user.id = :userId";
			
			Object[] res = (Object[]) persistence.currentManager()
				.createQuery(query)
				.setLong("userId", userId)
				.setLong("compId", compId)
				.uniqueResult();
			
			if(res != null) {
				String title = (String) res[0];
				long nextAct = (long) res[1];
				
				return LearningInfo.getLearningInfoForCompetence(title, nextAct);
			}
			return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving learning info");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CompetenceData1> getFullTargetCompetenceOrCompetenceData(long credId, long compId,
			long userId) throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException {
		CompetenceData1 compData = null;
		try {
			compData = getTargetCompetenceData(credId, compId, userId, true, true);
			if (compData == null) {
//				compData = getCompetenceData(compId, true, true, true, userId,
//						LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER, true);
				//compData = getCompetenceDataForUser(credId, compId, true, true, true, userId, true);
				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.USER)
						.addPrivilege(UserGroupPrivilege.Learn)
						.addPrivilege(UserGroupPrivilege.Edit);
				return getCompetenceDataWithAccessRightsInfo(credId, compId, true, true, true, true, userId,
						req, false);
			}
				
			/* if user is aleardy learning competence, he doesn't need any of the privileges;
			 * we just need to determine which privileges he has (can he edit or instruct a competence)
			 */
			ResourceAccessRequirements req = ResourceAccessRequirements.of(AccessMode.USER);
			ResourceAccessData access = getResourceAccessData(compId, userId, req);
			return RestrictedAccessResult.of(compData, access);
		} catch(ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch (IllegalArgumentException iae) {
			throw iae;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	/**
	 * Returns full target competence data when id of a target competence is not
	 * known.
	 * 
	 * @param credId
	 * @param compId
	 * @param userId
	 * @param loadLearningPathContent
	 * @return
	 * @throws DbConnectionException
	 */
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getTargetCompetenceData(long credId, long compId, long userId,
			boolean loadAssessmentConfig, boolean loadLearningPathContent) throws DbConnectionException {
		CompetenceData1 compData;
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT targetComp " +
					"FROM TargetCompetence1 targetComp " +
					"INNER JOIN fetch targetComp.competence comp ");

			if (credId > 0) {
				builder.append("INNER JOIN comp.credentialCompetences credComp " +
						"WITH credComp.credential.id = :credId ");
			}

			builder.append("INNER JOIN fetch comp.createdBy user " +
					"LEFT JOIN fetch comp.tags tags " +
					"WHERE targetComp.user.id = :userId " +
					"AND comp.id = :compId ");


			logger.info("QUERY: " + builder.toString());
			Query q = persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("compId", compId)
					.setLong("userId", userId);

			if (credId > 0) {
				q.setLong("credId", credId);
			}

			TargetCompetence1 res = (TargetCompetence1) q.uniqueResult();

			if (res != null) {
				Set<CompetenceAssessmentConfig> assessmentSettings = loadAssessmentConfig ? res.getCompetence().getAssessmentConfig() : null;
				compData = competenceFactory.getCompetenceData(res.getCompetence().getCreatedBy(), res, 0,
						assessmentSettings, res.getCompetence().getTags(), null, true);

				if (compData != null && loadLearningPathContent) {
					if (compData.getLearningPathType() == LearningPathType.ACTIVITY) {
						List<ActivityData> activities = activityManager
								.getTargetActivitiesData(compData.getTargetCompId());
						compData.setActivities(activities);
					} else {
						//load user evidences
						List<LearningEvidenceData> compEvidences = learningEvidenceManager.getUserEvidencesForACompetence(compData.getTargetCompId(), true);
						compData.setEvidences(compEvidences);
					}
					if (loadAssessmentConfig) {
						for (AssessmentTypeConfig conf : compData.getAssessmentTypes()) {
							if (conf.isEnabled()) {
								conf.setGradeSummary(assessmentManager.getCompetenceAssessmentsGradeSummary(compId, userId, conf.getType()));
							}
							//TODO hack - load the most restrictive credential blind assessment mode for Peer Assessment from all credentials with this competency
							if (conf.getType() == AssessmentType.PEER_ASSESSMENT) {
								conf.setBlindAssessmentMode(getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(compId, conf.getType()));
							}
						}
					}
				}
				return compData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<Competence1> getAllCompetences(long orgId, Session session) throws DbConnectionException {
		try {
			String query = "SELECT comp " + "FROM Competence1 comp " + "WHERE comp.deleted = :deleted ";

			if (orgId > 0) {
				query += "AND comp.organization.id = :orgId";
			}

			Query q = session.createQuery(query).setBoolean("deleted", false);

			if (orgId > 0) {
				q.setLong("orgId", orgId);
			}

			@SuppressWarnings("unchecked")
			List<Competence1> result = q.list();

			if (result == null) {
				return new ArrayList<>();
			}
			return result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competences");
		}
	}

	// @Override
	// @Transactional(readOnly = true)
	// public CompetenceData1 getCompetenceForManager(long competenceId, boolean
	// loadCreator,
	// boolean loadActivities, Mode mode) throws DbConnectionException {
	// try {
	// StringBuilder queryBuilder = new StringBuilder();
	// queryBuilder.append("SELECT comp " +
	// "FROM Competence1 comp " +
	// "LEFT JOIN fetch comp.tags tags ");
	//
	// if(loadCreator) {
	// queryBuilder.append("INNER JOIN fetch comp.createdBy ");
	// }
	//
	// StringBuilder queryBuilder1 = new StringBuilder(queryBuilder.toString());
	// queryBuilder1.append("WHERE comp.id = :compId " +
	// "AND comp.deleted = :deleted " +
	// "AND comp.draft = :draft ");
	// if(mode == Mode.Edit) {
	// queryBuilder1.append("AND comp.type = :type ");
	// } else {
	// queryBuilder1.append("AND (comp.type = :type OR (comp.published =
	// :published " +
	// "OR comp.hasDraft = :hasDraft))");
	// }
	//
	// Query q = persistence.currentManager()
	// .createQuery(queryBuilder1.toString())
	// .setLong("compId", competenceId)
	// .setBoolean("deleted", false)
	// .setParameter("type", LearningResourceType.UNIVERSITY_CREATED)
	// .setBoolean("draft", false);
	//
	// if(mode == Mode.View) {
	// q.setBoolean("published", true)
	// .setBoolean("hasDraft", true);
	// }
	//
	// Competence1 res = (Competence1) q.uniqueResult();
	//
	// if(res != null) {
	// CompetenceData1 cd = null;
	// if(res.isHasDraft() && (mode == Mode.Edit || (mode == Mode.View
	// && res.getType() == LearningResourceType.UNIVERSITY_CREATED))) {
	// String query2 = queryBuilder.toString() +
	// " WHERE comp = :draftVersion";
	// Competence1 draftComp = (Competence1) persistence.currentManager()
	// .createQuery(query2)
	// .setEntity("draftVersion", res.getDraftVersion())
	// .uniqueResult();
	// if(draftComp != null) {
	// User creator = loadCreator ? draftComp.getCreatedBy() : null;
	// cd = competenceFactory.getCompetenceData(creator, draftComp,
	// draftComp.getTags(), true);
	// }
	// } else {
	// User creator = loadCreator ? res.getCreatedBy() : null;
	// cd = competenceFactory.getCompetenceData(creator, res, res.getTags(),
	// true);
	// }
	// if(cd != null && loadActivities) {
	// List<ActivityData> activities = activityManager
	// .getCompetenceActivitiesData(cd.getCompetenceId());
	// cd.setActivities(activities);
	// }
	//
	// List<CredentialData> credentials = credentialManager
	// .getCredentialsWithIncludedCompetenceBasicData(res.getId());
	// cd.setCredentialsWithIncludedCompetence(credentials);
	// return cd;
	// }
	//
	// return null;
	// } catch (Exception e) {
	// logger.error(e);
	// e.printStackTrace();
	// throw new DbConnectionException("Error while loading competence data");
	// }
	// }

	@Transactional(readOnly = true)
	@Override
	public UserAccessSpecification getUserPrivilegesForCompetence(long compId, long userId) 
			throws DbConnectionException {
		try {
			String query = "SELECT DISTINCT compUserGroup.privilege, comp.visibleToAll, comp.type, comp.published, comp.datePublished " +
					"FROM CompetenceUserGroup compUserGroup " +
					"INNER JOIN compUserGroup.userGroup userGroup " +
					"RIGHT JOIN compUserGroup.competence comp " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE comp.id = :compId";
			
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("compId", compId)
					.list();
			
			boolean visibleToAll = false;
			LearningResourceType type = null;
			boolean published = false;
			Date datePublished = null;
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
						type = (LearningResourceType) row[2];
						published = (boolean) row[3];
						datePublished = (Date) row[4];
						first = false;
					}
				}
			}
			return CompetenceUserAccessSpecification.of(
					privs, visibleToAll, published, datePublished, type);
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for competency");
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public ResourceAccessData getResourceAccessData(long compId, long userId, ResourceAccessRequirements req) 
			throws DbConnectionException {
		try {
			UserAccessSpecification spec = getUserPrivilegesForCompetence(compId, userId);
			return resourceAccessFactory.determineAccessRights(userId, compId, req, spec);
		} catch (DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for competency");
		}
	}
	
	private List<Long> getCompetencesIdsWithSpecifiedPrivilegeForUser(long userId, UserGroupPrivilege priv)
			throws DbConnectionException {
		try {
			if(priv == null) {
				throw new NullPointerException("Privilege can not be null");
			}
			if(priv == UserGroupPrivilege.None) {
				throw new IllegalStateException("Privilege is not valid");
			}
			StringBuilder query = new StringBuilder(
					"SELECT distinct comp.id " +
					"FROM CompetenceUserGroup compUserGroup " +
					"INNER JOIN compUserGroup.userGroup userGroup " +
					"RIGHT JOIN compUserGroup.competence comp " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE compUserGroup.privilege = :priv ");
			
			switch(priv) {
				case Edit:
					query.append("OR comp.createdBy.id = :userId");
					break;
				case Learn:
					query.append("OR comp.visibleToAll = :boolTrue");
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
			throw new DbConnectionException("Error while trying to retrieve competences ids");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isVisibleToAll(long compId) throws DbConnectionException {
		try {
			String query = "SELECT comp.visibleToAll " + "FROM Competence1 comp " + "WHERE comp.id = :compId";

			Boolean result = (Boolean) persistence.currentManager().createQuery(query).setLong("compId", compId)
					.uniqueResult();

			return result == null ? false : result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence visibility");
		}
	}

	//nt
	@Override
	public void updateCompetenceVisibility(long compId, List<ResourceVisibilityMember> groups,
										   List<ResourceVisibilityMember> users, boolean visibleToAll,
										   boolean visibleToAllChanged, UserContextData context)
			throws DbConnectionException {
		try {
			EventQueue events =
					self.updateCompetenceVisibilityAndGetEvents(compId, groups, users, visibleToAll,
							visibleToAllChanged, context);
			eventFactory.generateEvents(events);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	@Transactional
	public EventQueue updateCompetenceVisibilityAndGetEvents(long compId, List<ResourceVisibilityMember> groups,
																  List<ResourceVisibilityMember> users, boolean visibleToAll,
																  boolean visibleToAllChanged, UserContextData context)
			throws DbConnectionException {
		try {
			EventQueue events = EventQueue.newEventQueue();
			if(visibleToAllChanged) {
				Competence1 comp = (Competence1) persistence.currentManager().load(
						Competence1.class, compId);

				comp.setVisibleToAll(visibleToAll);

				Competence1 competence = new Competence1();
				competence.setId(compId);
				competence.setVisibleToAll(visibleToAll);

				events.appendEvent(eventFactory.generateEventData(
						EventType.VISIBLE_TO_ALL_CHANGED, context, competence, null, null, null));
			}
			events.appendEvents(userGroupManager.saveCompetenceUsersAndGroups(compId, groups, users, context)
					.getEventQueue());
			return events;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential visibility");
		}
	}

	private long getRecalculatedDuration(long compId) {
		String query = "SELECT sum(a.duration) FROM CompetenceActivity1 ca " +
					   "INNER JOIN ca.activity a " +
					   "WHERE ca.competence.id = :compId";
		
		Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.uniqueResult();

		return res != null ? res : 0;
	}

	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceDataWithProgressIfExists(long compId, long userId) 
					throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			String query = "SELECT DISTINCT comp, creator, targetComp.progress, bookmark.id, targetComp.nextActivityToLearnId " +
						   "FROM Competence1 comp " + 
						   "INNER JOIN comp.createdBy creator " +
						   "LEFT JOIN comp.targetCompetences targetComp " + 
						   "WITH targetComp.user.id = :user " +
						   "LEFT JOIN comp.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE comp.id = :compId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("user", userId)
					.setLong("compId", compId)
					.uniqueResult();

			if (res != null) {
				Competence1 comp = (Competence1) res[0];
				User creator = (User) res[1];
				Integer paramProgress = (Integer) res[2];
				Long paramBookmarkId = (Long) res[3];
				Long nextActId = (Long) res[4];
				if(paramProgress != null) {
					compData = competenceFactory.getCompetenceDataWithProgress(creator, comp, null, 
							paramProgress.intValue(), nextActId.longValue(), false);
				} else {
					compData = competenceFactory.getCompetenceData(creator, comp, null, null, false);
				}
				if(paramBookmarkId != null) {
					compData.setBookmarkedByCurrentUser(true);
				}
				
				return compData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getBasicCompetenceData(long compId, long userId) 
					throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			String query = "SELECT comp, creator, bookmark.id " +
						   "FROM Competence1 comp " + 
						   "INNER JOIN comp.createdBy creator " +
						   "LEFT JOIN comp.bookmarks bookmark " +
						   "WITH bookmark.user.id = :user " +
						   "WHERE comp.id = :compId";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("user", userId)
					.setLong("compId", compId)
					.uniqueResult();

			if (res != null) {
				Competence1 comp = (Competence1) res[0];
				User creator = (User) res[1];
				Long paramBookmarkId = (Long) res[2];

				compData = competenceFactory.getCompetenceData(creator, comp, null, null, false);

				if(paramBookmarkId != null) {
					compData.setBookmarkedByCurrentUser(true);
				}
				
				return compData;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	public void bookmarkCompetence(long compId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.bookmarkCompetenceAndGetEvents(compId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> bookmarkCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			User user = (User) persistence.currentManager().load(User.class, context.getActorId());
			CompetenceBookmark cb = new CompetenceBookmark();
			cb.setCompetence(comp);
			cb.setUser(user);
			saveEntity(cb);

			CompetenceBookmark bookmark = new CompetenceBookmark();
			bookmark.setId(cb.getId());
			Competence1 competence = new Competence1();
			competence.setId(compId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.Bookmark, context, bookmark, competence, null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while bookmarking competence");
		}
	}

	@Override
	public void deleteCompetenceBookmark(long compId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.deleteCompetenceBookmarkAndGetEvents(compId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional
	public Result<Void> deleteCompetenceBookmarkAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(
					Competence1.class, compId);
			User user = (User) persistence.currentManager().load(User.class,
					context.getActorId());
			String query = "SELECT cb " +
						   "FROM CompetenceBookmark cb " +
						   "WHERE cb.competence = :comp " +
						   "AND cb.user = :user";
			
			CompetenceBookmark bookmark = (CompetenceBookmark) persistence.currentManager()
					.createQuery(query)
					.setEntity("comp", comp)
					.setEntity("user", user)
					.uniqueResult();
			
			long id = bookmark.getId();
			
			delete(bookmark);
			
			CompetenceBookmark cb = new CompetenceBookmark();
			cb.setId(id);
			Competence1 competence = new Competence1();
			competence.setId(compId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.RemoveBookmark, context, cb, competence, null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence bookmark");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceBookmark> getBookmarkedByIds(long compId) throws DbConnectionException {
		return getBookmarkedByIds(compId, persistence.currentManager());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceBookmark> getBookmarkedByIds(long compId, Session session) 
			throws DbConnectionException {
		try {
			String query = "SELECT bookmark " +
						   "FROM CompetenceBookmark bookmark " +
						   "WHERE bookmark.competence.id = :compId";
			
			@SuppressWarnings("unchecked")
			List<CompetenceBookmark> bookmarks = session
					.createQuery(query)
					.setLong("compId", compId)
					.list();
			
			if(bookmarks == null) {
				return new ArrayList<>();
			}
			return bookmarks;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence bookmarks");
		}
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<TargetCompetence1> getTargetCompetencesForCompetence(long compId, 
			boolean justUncompleted) throws DbConnectionException {
		try {		
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT comp " +
				       	   "FROM TargetCompetence1 comp " +
				       	   "WHERE comp.competence.id = :compId ");
			if(justUncompleted) {
				builder.append("AND comp.progress != :progress");
			}			    
			
			Query q = persistence.currentManager()
				.createQuery(builder.toString())
				.setLong("compId", compId);
			if(justUncompleted) {
				q.setInteger("progress", 100);
			}
			@SuppressWarnings("unchecked")
			List<TargetCompetence1> res = q.list();
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user competences");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countNumberOfStudentsLearningCompetence(long compId) throws DbConnectionException {
		try {
			String query = "SELECT COUNT(tc.id) " +
						   "FROM TargetCompetence1 tc " +
						   "WHERE tc.competence.id = :compId";
			
			Long count = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.uniqueResult();
			
			return count != null ? count : 0;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while counting number of users learning competence");
		}
	}

	@Override
	public void archiveCompetence(long compId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.archiveCompetenceAndGetEvents(compId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> archiveCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(compId, true);
			
			Competence1 competence = new Competence1();
			competence.setId(compId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.ARCHIVE, context,
					competence, null,null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while archiving competence");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public long countNumberOfCompetences(CompetenceSearchFilter searchFilter, long userId, UserGroupPrivilege priv) 
				throws DbConnectionException, NullPointerException {
		try {
			if(searchFilter == null) {
				throw new NullPointerException("Search filter cannot be null");
			}
			
			List<Long> ids = getCompetencesIdsWithSpecifiedPrivilegeForUser(userId, priv);
			
			//if user doesn't have needed privilege for any of the competences we return 0
			if(ids.isEmpty()) {
				return 0;
			}
			
			StringBuilder query = new StringBuilder(
						"SELECT COUNT(c.id) " +
						"FROM Competence1 c " +
						"WHERE c.id IN (:ids) ");
			
			switch (searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse");
					break;
				case DRAFT:
					query.append("AND c.archived = :boolFalse " +
							"AND c.published = :boolFalse " +
							"AND c.datePublished IS NULL ");
					break;
				case UNPUBLISHED:
					query.append("AND c.archived = :boolFalse " +
							"AND c.published = :boolFalse " +
							"AND c.datePublished IS NOT NULL ");
					break;
				case PUBLISHED:
					query.append("AND c.archived = :boolFalse " +
							 	 "AND c.published = :boolTrue");
					break;
				case ARCHIVED:
					query.append("AND c.archived = :boolTrue");
					break;
			}
			
			Query q = persistence.currentManager()
					.createQuery(query.toString())
					.setParameterList("ids", ids);
			
			switch (searchFilter) {
				case ACTIVE:
				case DRAFT:
				case UNPUBLISHED:
					q.setBoolean("boolFalse", false);
					break;
				case PUBLISHED:
					q.setBoolean("boolFalse", false);
					q.setBoolean("boolTrue", true);
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
			throw new DbConnectionException("Error while counting number of competences");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> searchCompetencesForManager(CompetenceSearchFilter searchFilter, int limit, int page, long userId)
				throws DbConnectionException, NullPointerException {
		try {
			if(searchFilter == null) {
				throw new NullPointerException("Invalid argument values");
			}
			
			List<Long> ids = getCompetencesIdsWithSpecifiedPrivilegeForUser(userId, UserGroupPrivilege.Edit);
			
			//if user doesn't have needed privileges for any of the competences, empty list is returned
			if(ids.isEmpty()) {
				return new ArrayList<>();
			}
			
			StringBuilder query = new StringBuilder(
						"SELECT c " +
						"FROM Competence1 c " +
						"WHERE c.id IN (:ids) ");
			
			switch (searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse ");
					break;
				case DRAFT:
					query.append("AND c.archived = :boolFalse " +
								 "AND c.published = :boolFalse " +
								 "AND c.datePublished IS NULL ");
					break;
				case UNPUBLISHED:
					query.append("AND c.archived = :boolFalse " +
								 "AND c.published = :boolFalse " +
					 			 "AND c.datePublished IS NOT NULL ");
					break;
				case PUBLISHED:
					query.append("AND c.archived = :boolFalse " +
							 	 "AND c.published = :boolTrue ");
					break;
				case ARCHIVED:
					query.append("AND c.archived = :boolTrue ");
					break;
			}

			query.append("ORDER BY c.title ASC");
			
			Query q = persistence.currentManager()
						.createQuery(query.toString())
						.setParameterList("ids", ids);
					
			switch (searchFilter) {
				case ACTIVE:
				case DRAFT:
				case UNPUBLISHED:
					q.setBoolean("boolFalse", false);
					break;
				case PUBLISHED:
					q.setBoolean("boolFalse", false);
					q.setBoolean("boolTrue", true);
					break;
				case ARCHIVED:
					q.setBoolean("boolTrue", true);
					break;
			}
			
			@SuppressWarnings("unchecked")
			List<Competence1> comps = q.list();
			
			List<CompetenceData1> res = new ArrayList<>();
			for(Competence1 c : comps) {
				CompetenceData1 cd = competenceFactory.getCompetenceData(null, c, null, null, false);
				cd.setNumberOfStudents(countNumberOfStudentsLearningCompetence(cd.getCompetenceId()));
				res.add(cd);
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competences");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public long duplicateCompetence(long compId, UserContextData context)
			throws DbConnectionException {
		Result<Competence1> res = self.duplicateCompetenceAndGetEvents(compId, context);
		eventFactory.generateEvents(res.getEventQueue());
		return res.getResult().getId();
	}

	@Override
	@Transactional
	public Result<Competence1> duplicateCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().get(Competence1.class, compId);
			return duplicateCompetence(comp, "Copy of " + comp.getTitle(), comp, null, null, context);
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error creating the competence bcc");
		}
	}

	@Override
	@Transactional
	public Result<Competence1> getOrCreateCompetenceInLearningStageAndGetEvents(long basedOnCompId, long learningStageId, UserContextData context)
			throws DbConnectionException {
		long firstStageCompId = 0;
		try {
			Competence1 comp = (Competence1) persistence.currentManager().get(Competence1.class, basedOnCompId);
			LearningStage ls = (LearningStage) persistence.currentManager().load(LearningStage.class, learningStageId);
			/*
			if order of learning stage is 2, it means previous learning stage is first, otherwise we get the
			first stage comp from previous stage comp. Unlike with credentials, it can happen that there is no
			first stage comp referenced, so these competences would not be connected.
			 */
			Competence1 firstStageComp = ls.getOrder() == 2 ? comp : comp.getFirstLearningStageCompetence();
			firstStageCompId = firstStageComp.getId();
			return duplicateCompetence(
					comp,
					comp.getTitle(),
					null,
					ls,
					firstStageComp,
					context);
		} catch (DataIntegrityViolationException e) {
			logger.debug("Error", e);
			//if competence in that stage already exists, return it
			if (firstStageCompId > 0) {
				Competence1 existingComp = getCompetenceInLearningStage(firstStageCompId, learningStageId);
				if (existingComp != null) {
					Result<Competence1> res = new Result<>();
					res.setResult(existingComp);
					logger.debug("Competence in learning stage already exists so it is returned");
					return res;
				}
			}
			throw e;
		} catch(Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error creating the competence for the learning stage");
		}
	}

	private Competence1 getCompetenceInLearningStage(long firstStageCompId, long learningStageId) {
		String query =
				"SELECT comp FROM Competence1 comp " +
				"WHERE comp.learningStage.id = :lsId " +
				"AND comp.firstLearningStageCompetence.id = :firstStageCompId";
		return (Competence1) persistence.currentManager()
				.createQuery(query)
				.setLong("lsId", learningStageId)
				.setLong("firstStageCompId", firstStageCompId)
				.uniqueResult();
	}

	private Result<Competence1> duplicateCompetence(Competence1 original, String title, Competence1 versionOf,
											LearningStage lStage, Competence1 firstStageComp, UserContextData context) {
		Competence1 competence = new Competence1();

		User user = (User) persistence.currentManager().load(User.class, context.getActorId());
		competence.setOrganization(original.getOrganization());
		competence.setTitle(title);
		competence.setDescription(original.getDescription());
		competence.setDateCreated(new Date());
		competence.setCreatedBy(user);
		competence.setDuration(original.getDuration());
		competence.setStudentAllowedToAddActivities(original.isStudentAllowedToAddActivities());
		competence.setType(original.getType());
		competence.setArchived(false);
		competence.setPublished(false);
		competence.setOriginalVersion(versionOf);
		competence.setLearningStage(lStage);
		competence.setFirstLearningStageCompetence(firstStageComp);
		//set assessment related data
		competence.setGradingMode(original.getGradingMode());
		competence.setMaxPoints(original.getMaxPoints());
		competence.setRubric(original.getRubric());
		competence.setLearningPathType(original.getLearningPathType());
		saveEntity(competence);
		for (CompetenceAssessmentConfig cac : original.getAssessmentConfig()) {
			CompetenceAssessmentConfig compAssessmentConfig = new CompetenceAssessmentConfig();
			compAssessmentConfig.setCompetence(competence);
			compAssessmentConfig.setAssessmentType(cac.getAssessmentType());
			compAssessmentConfig.setEnabled(cac.isEnabled());
			saveEntity(compAssessmentConfig);
		}
		/*
		if this line is put before saveEntity and there is an exception thrown so competence can't be saved, hibernate would still issue
		insert statements for saving competence tags which would lead to another exception because competence is not saved.
		 */
		competence.setTags(new HashSet<>(original.getTags()));

		Result<Competence1> res = new Result<>();
		res.setResult(competence);

		Competence1 c = new Competence1();
		c.setId(competence.getId());
		res.appendEvent(eventFactory.generateEventData(EventType.Create, context, c, null, null, null));

		if (competence.getLearningPathType() == LearningPathType.ACTIVITY) {
			Result<List<CompetenceActivity1>> activityCopies = copyCompetenceActivities(competence, original.getActivities(), context);
			competence.getActivities().addAll(activityCopies.getResult());
			res.appendEvents(activityCopies.getEventQueue());
		}

		//add Edit privilege to the competence creator
		res.appendEvents(userGroupManager.createCompetenceUserGroupAndSaveNewUser(
				context.getActorId(), competence.getId(),
				UserGroupPrivilege.Edit,true, context).getEventQueue());

		//add competence to all units where competence creator is manager
		res.appendEvents(addCompetenceToDefaultUnits(competence.getId(), context));

		return res;
	}

	private Result<List<CompetenceActivity1>> copyCompetenceActivities(
			Competence1 competenceToAddActivitiesTo, List<CompetenceActivity1> activities, UserContextData context) {
		Result<List<CompetenceActivity1>> res = new Result<>();
		List<CompetenceActivity1> activityCopies = new LinkedList<>();
		for (CompetenceActivity1 compActivity : activities) {
			Result<CompetenceActivity1> actRes = activityManager.cloneActivity(
					compActivity, competenceToAddActivitiesTo.getId(), context);
			res.appendEvents(actRes.getEventQueue());
			activityCopies.add(actRes.getResult());
		}
		res.setResult(activityCopies);
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCompetenceTitleForCompetenceWithType(long id, LearningResourceType type)
			throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder(
				   "SELECT c.title " +
				   "FROM Competence1 c " +
				   "WHERE c.id = :compId ");
			
			if(type != null) {
				queryBuilder.append("AND c.type = :type");
			}
			
			Query q = persistence.currentManager()
				.createQuery(queryBuilder.toString())
				.setLong("compId", id);
			
			if(type != null) {
				q.setParameter("type", type);
			}
			
			String title = (String) q.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence title");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<TargetCompetence1> getTargetCompetencesForUser(long userId, Session session) 
			throws DbConnectionException {  
		try {
			String query = "SELECT tc " +
					   "FROM TargetCompetence1 tc " + 
					   "WHERE tc.user.id = :userId";
			
			@SuppressWarnings("unchecked")
			List<TargetCompetence1> res = session
					.createQuery(query)
					.setLong("userId", userId)
					.list();
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving user competences");
		}
	}

	@Override
	public void restoreArchivedCompetence(long compId, UserContextData context)
			throws DbConnectionException {
		Result<Void> res = self.restoreArchivedCompetenceAndGetEvents(compId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}
	
	@Override
	@Transactional
	public Result<Void> restoreArchivedCompetenceAndGetEvents(long compId, UserContextData context)
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(compId, false);
			
			Competence1 competence = new Competence1();
			competence.setId(compId);

			Result<Void> res = new Result<>();
			res.appendEvent(eventFactory.generateEventData(EventType.RESTORE, context, competence, null,null, null));
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while restoring competence");
		}
	}
	
	private void updateArchivedProperty(long compId, boolean archived) {
		String query = "UPDATE Competence1 comp " +
					   "SET comp.archived = :archived " +
					   "WHERE comp.id = :compId";
		persistence.currentManager()
				.createQuery(query)
				.setBoolean("archived", archived)
				.setLong("compId", compId)
				.executeUpdate();
	}
	
	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CompetenceData1> getCompetenceForEdit(long credId, long compId, long userId, 
			AccessMode accessMode) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			ResourceAccessRequirements req = ResourceAccessRequirements.of(accessMode)
					.addPrivilege(UserGroupPrivilege.Edit);
			RestrictedAccessResult<CompetenceData1> res = getCompetenceDataWithAccessRightsInfo(credId, compId, true, true, true, true, userId,
					req, true);
			
			boolean canUnpublish = !isThereOngoingDeliveryWithCompetence(compId);
			res.getResource().setCanUnpublish(canUnpublish);
			
			return res;
		} catch (ResourceNotFoundException|IllegalArgumentException|DbConnectionException e) {
			throw e;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	private boolean isThereOngoingDeliveryWithCompetence(long compId) throws DbConnectionException {
		String query = "SELECT COUNT(cred.id) " +
					   "FROM CredentialCompetence1 credComp " +
					   "INNER JOIN credComp.credential cred " +
					   		"WITH cred.type = :type " +
					   		"AND (cred.deliveryEnd is NULL " +
					   			 "OR cred.deliveryEnd > :now) " +
					   "WHERE credComp.competence.id = :compId";
		
		Long count = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setParameter("type", CredentialType.Delivery)
				.setDate("now", new Date())
				.uniqueResult();
		
		return count != null ? count > 0 : false;
    }
	
	@Override
	@Transactional(readOnly = false)
	public EventQueue updateCompetenceProgress(long targetCompId, UserContextData context)
			throws DbConnectionException {
		try {
			EventQueue eventQueue = EventQueue.newEventQueue();
			
			String query = "SELECT comp.id, act.id, tAct.completed " +
				   "FROM TargetActivity1 tAct " +
				   "INNER JOIN tAct.targetCompetence tComp " +
				   "INNER JOIN tComp.competence comp " +
				   "INNER JOIN tAct.activity act " +
				   "WHERE tComp.id = :tCompId " +
				   "ORDER BY tAct.order";
	
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
				.createQuery(query)
				.setLong("tCompId", targetCompId)
				.list();
			
			if(res != null) {
				int cumulativeCompProgress = 0;
				int numberOfActivitiesInACompetence = 0;
				long nextActToLearnInACompetenceId = 0;
				
				for(Object[] obj : res) {
					long actId = (long) obj[1];
					boolean actCompleted = (boolean) obj[2];
					
					int progress = actCompleted ? 100 : 0;
					cumulativeCompProgress += progress;
					numberOfActivitiesInACompetence ++;
					if(nextActToLearnInACompetenceId == 0 && !actCompleted) {
						nextActToLearnInACompetenceId = actId;
					}
				}
				int finalCompProgress = cumulativeCompProgress / numberOfActivitiesInACompetence;
				
				eventQueue.appendEvents(updateCompetenceProgress(targetCompId, (long) res.get(0)[0], finalCompProgress,
						nextActToLearnInACompetenceId, context));
				
				eventQueue.appendEvents(credentialManager.updateCredentialProgress(targetCompId, context));
			}
			return eventQueue;
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competency progress");
		}
	}

	private EventQueue updateCompetenceProgress(long targetCompId, long compId, int finalCompProgress,
			long nextActivityToLearnId, UserContextData context) {
		Date now = new Date();
		
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE TargetCompetence1 targetComp SET " +
 				 	   "targetComp.progress = :progress, " +
					   "targetComp.nextActivityToLearnId = :nextActToLearnId ");
		if (finalCompProgress == 100) {
			builder.append(", targetComp.dateCompleted = :dateCompleted ");
		}
		builder.append("WHERE targetComp.id = :targetCompId");
		
		Query q = persistence.currentManager()
			.createQuery(builder.toString())
			.setInteger("progress", finalCompProgress)
			.setLong("targetCompId", targetCompId)
			.setLong("nextActToLearnId", nextActivityToLearnId);
		
		if (finalCompProgress == 100) {
			q.setTimestamp("dateCompleted", now);
		}
		q.executeUpdate();
		
		EventQueue events = EventQueue.newEventQueue();
		
		TargetCompetence1 tComp = new TargetCompetence1();
		tComp.setId(targetCompId);
		Competence1 competence = new Competence1();
		competence.setId(compId);
		tComp.setCompetence(competence);

		Map<String, String> params = new HashMap<>();

		if (finalCompProgress == 100) {
			params.put("dateCompleted", DateUtil.getMillisFromDate(now) + "");

			events.appendEvent(eventFactory.generateEventData(
					EventType.Completion, context, tComp, null,null, params));
		}

		EventData ev = eventFactory.generateEventData(EventType.ChangeProgress,
				context, tComp, null, null, params);
		ev.setProgress(finalCompProgress);
		events.appendEvent(ev);
//		eventFactory.generateChangeProgressEvent(userId, tComp, finalCompProgress, 
//				lcPage, lcContext, lcService, params);

		return events;
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Result<Void> publishCompetenceIfNotPublished(Competence1 comp, UserContextData context)
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<Void> res = new Result<>();
			
			if (!comp.isPublished()) {
				/*
				 * if competence has activity learning path, check if competence has at least one activity - if not, it can't be published
				 */
				if (comp.getLearningPathType() == LearningPathType.ACTIVITY) {
					int numberOfActivities = comp.getActivities().size();
					if (numberOfActivities == 0) {
						throw new IllegalDataStateException("Can not publish " + ResourceBundleUtil.getMessage("label.competence").toLowerCase() + " without activities.");
					}
				}
			
				comp.setPublished(true);
				Date newDatePublished = null;
				if (comp.getDatePublished() == null) {
					newDatePublished = new Date();
					comp.setDatePublished(newDatePublished);
				}

				Competence1 c = new Competence1();
				c.setId(comp.getId());
				c.setPublished(true);
				c.setDatePublished(newDatePublished);
				res.appendEvent(eventFactory.generateEventData(EventType.STATUS_CHANGED,
						context, c, null, null,null));
			}
			
			return res;
		} catch (IllegalDataStateException e) {
			logger.error("Error", e);
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while publishing competency");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ResourceCreator getCompetenceCreator(long compId) throws DbConnectionException {
		try {
			String query = "SELECT c.createdBy " +
					"FROM Competence1 c " +
					"WHERE c.id = :compId";

			User createdBy =  (User) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.uniqueResult();

			return userDataFactory.getResourceCreator(createdBy);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competency creator");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<TargetCompetence1> getTargetCompetencesForCredentialAndUser(long credId, long userId)
			throws DbConnectionException {
		try {
			String query = "SELECT tComp " +
					"FROM Credential1 cred " +
					"INNER JOIN cred.competences credComp " +
					"INNER JOIN credComp.competence comp " +
					"INNER JOIN comp.targetCompetences tComp " +
						"WITH tComp.user.id = :userId " +
					"WHERE cred.id = :credId " +
					"ORDER BY credComp.order";

			@SuppressWarnings("unchecked")
			List<TargetCompetence1> res = persistence.currentManager()
					.createQuery(query)
					.setLong("credId", credId)
					.setLong("userId", userId)
					.list();

			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading user competencies");
		}
	}

	private List<Long> getCompetenceIdsForOwner(long ownerId) {
		String query = "SELECT comp.id " +
				"FROM Competence1 comp " +
				"WHERE comp.createdBy.id = :ownerId";

		return persistence.currentManager()
				.createQuery(query)
				.setLong("ownerId", ownerId)
				.list();
	}

	@Override
	@Transactional
	public Result<Void> updateCompetenceCreator(long newCreatorId, long oldCreatorId,
												UserContextData context)
			throws DbConnectionException {
		try {
			Result<Void> result = new Result<>();
			List<Long> competencesWithOldOwner = getCompetenceIdsForOwner(oldCreatorId);

			String query = "UPDATE Competence1 comp SET " +
					"comp.createdBy = :newCreatorId " +
					"WHERE comp.createdBy = :oldCreatorId";

			persistence.currentManager()
				.createQuery(query)
				.setLong("newCreatorId", newCreatorId)
				.setLong("oldCreatorId", oldCreatorId)
				.executeUpdate();

			for (long id : competencesWithOldOwner) {
				//remove Edit privilege from old owner
				result.appendEvents(userGroupManager.removeUserFromDefaultCompetenceGroupAndGetEvents(
						oldCreatorId, id, UserGroupPrivilege.Edit, context).getEventQueue());
				//add edit privilege to new owner
				result.appendEvents(userGroupManager.saveUserToDefaultCompetenceGroupAndGetEvents(
						newCreatorId, id, UserGroupPrivilege.Edit, context).getEventQueue());

				//for all competencies change_owner event should be generated
				result.appendEvent(getOwnerChangeEvent(id, oldCreatorId, newCreatorId, context));
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating creator of competences");
		}
	}

	private EventData getOwnerChangeEvent(long compId, long oldOwnerId, long newOwnerId, UserContextData context) {
		Competence1 comp = new Competence1();
		comp.setId(compId);
		Map<String, String> params = new HashMap<>();
		params.put("oldOwnerId", oldOwnerId + "");
		params.put("newOwnerId", newOwnerId + "");
		return eventFactory.generateEventData(EventType.OWNER_CHANGE,
				context, comp, null, null, params);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Tag> getTagsForCompetence(long competenceId) throws DbConnectionException {
		
		StringBuilder queryBuilder = new StringBuilder(
				"SELECT tags " +
				"FROM Competence1 comp " +
				"LEFT JOIN comp.tags tags  " +
				"WHERE comp.id = :compId ");
		
		@SuppressWarnings("unchecked")
		List<Tag> res = persistence.currentManager()
			.createQuery(queryBuilder.toString())
			.setLong("compId", competenceId)
			.list();
		
		return res;
	}
	
	@Override
	@Transactional (readOnly = false)
	public void updateHiddenTargetCompetenceFromProfile(long compId, boolean hiddenFromProfile)
			throws DbConnectionException {
		try {
			String query = 
				"UPDATE TargetCompetence1 targetComptence1 " +
				"SET targetComptence1.hiddenFromProfile = :hiddenFromProfile " +
				"WHERE targetComptence1.id = :compId ";
	
			persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setBoolean("hiddenFromProfile", hiddenFromProfile)
				.executeUpdate();
		} catch (Exception e) {
			logger.error(e);
			throw new DbConnectionException("Error while updating hiddenFromProfile field of a competence " + compId);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional (readOnly = true)
	public List<TargetCompetenceData> getAllCompletedCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		return getTargetCompetences(userId, onlyPubliclyVisible, true, UserLearningProgress.COMPLETED);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	@Transactional (readOnly = true)
	public List<TargetCompetenceData> getAllInProgressCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		return getTargetCompetences(userId, onlyPubliclyVisible, true, UserLearningProgress.IN_PROGRESS);
	}

	@SuppressWarnings("unchecked")
	private List<TargetCompetenceData> getTargetCompetences(long userId, boolean onlyPubliclyVisible, boolean loadNumberOfAssessments,
															UserLearningProgress progress)
			throws DbConnectionException {
		try {
			String query =
					"SELECT targetComptence1 " +
							"FROM TargetCompetence1 targetComptence1 " +
							"INNER JOIN targetComptence1.competence comp " +
							"WHERE targetComptence1.user.id = :userId ";

			switch (progress) {
				case COMPLETED:
					query += "AND targetComptence1.progress = 100 ";
					break;
				case IN_PROGRESS:
					query += "AND targetComptence1.progress < 100 ";
					break;
				default:
					break;
			}

			if (onlyPubliclyVisible) {
				query += " AND targetComptence1.hiddenFromProfile = false ";
			}

			query += "ORDER BY comp.title";

			List<TargetCompetenceData> resultList = new ArrayList<>();

			List<TargetCompetence1> res = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.list();

			for(TargetCompetence1 targetCompetence1 : res) {
				int numberOfAssessments = 0;
				boolean assessmentDisplayEnabled = false;
				if (loadNumberOfAssessments) {
					numberOfAssessments = assessmentManager.getNumberOfApprovedAssessmentsForUserCompetence(targetCompetence1.getCompetence().getId(), targetCompetence1.getUser().getId());
					assessmentDisplayEnabled = isCompetenceAssessmentDisplayEnabled(targetCompetence1.getCompetence().getId(), targetCompetence1.getUser().getId());
				}
				resultList.add(new TargetCompetenceData(targetCompetence1, numberOfAssessments, assessmentDisplayEnabled));
			}
			return resultList;

		} catch (Exception e) {
			logger.error(e);
			throw new DbConnectionException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isCompetenceAssessmentDisplayEnabled(long competenceId, long studentId) {
		try {
			String q =
					"SELECT comp.id FROM Competence1 comp " +
							"INNER JOIN comp.credentialCompetences cc " +
							"INNER JOIN cc.credential cred " +
							"INNER JOIN cred.targetCredentials tCred " +
							"WITH tCred.user.id = :studentId " +
							"AND tCred.competenceAssessmentsDisplayed IS TRUE " +
							"WHERE comp.id = :compId";

			Long res = (Long) persistence.currentManager().createQuery(q)
					.setLong("studentId", studentId)
					.setLong("compId", competenceId)
					.setMaxResults(1)
					.uniqueResult();

			return res != null;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error checking if competence assessment display is enabled");
		}
	}

	@Override
	//nt
	public void changeOwner(long compId, long newOwnerId, UserContextData context) throws DbConnectionException {
		eventFactory.generateEvents(self.changeOwnerAndGetEvents(compId, newOwnerId, context).getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> changeOwnerAndGetEvents(long compId, long newOwnerId, UserContextData context) throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);

			long oldOwnerId = comp.getCreatedBy().getId();

			comp.setCreatedBy((User) persistence.currentManager().load(User.class, newOwnerId));

			return Result.of(EventQueue.of(getOwnerChangeEvent(compId, oldOwnerId, newOwnerId, context)));
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error while changing the competency owner");
		}
	}

	@Override
	@Transactional
	public EventQueue disableLearningStagesForOrganizationCompetences(long orgId, UserContextData context) throws DbConnectionException {
		try {
			List<Competence1> comps = getAllCompetencesWithLearningStagesEnabled(orgId);
			EventQueue queue = EventQueue.newEventQueue();
			for (Competence1 comp : comps) {
				queue.appendEvents(updateCompetenceLearningStage(comp, null, context));
			}
			return queue;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error disabling learning in stages for competences in organization: " + orgId);
		}
	}

	@Override
	@Transactional
	public EventQueue updateCompetenceLearningStage(Competence1 competence, LearningStage stage, UserContextData context) throws DbConnectionException {
		try {
			EventQueue queue = EventQueue.newEventQueue();
			competence.setLearningStage(stage);
			//if stages are enabled it means that this is the first stage so reference to the first stage competence should remain null
			if (stage == null) {
				competence.setFirstLearningStageCompetence(null);
			}
			Competence1 comp = new Competence1();
			comp.setId(competence.getId());
			queue.appendEvent(eventFactory.generateEventData(EventType.LEARNING_STAGE_UPDATE, context, comp, null, null, null));
			return queue;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating the competence learning stage");
		}
	}

	private List<Competence1> getAllCompetencesWithLearningStagesEnabled(long orgId) {
		String query =
				"SELECT comp " +
				"FROM Competence1 comp " +
				"WHERE comp.deleted IS FALSE " +
				"AND comp.organization.id = :orgId " +
				"AND comp.learningStage IS NOT NULL";

		@SuppressWarnings("unchecked")
		List<Competence1> result = persistence.currentManager()
				.createQuery(query)
				.setLong("orgId", orgId)
				.list();

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public LearningPathType getCompetenceLearningPathType(long compId) throws DbConnectionException {
		try {
			String query =
					"SELECT comp.learningPathType " +
							"FROM Competence1 comp " +
							"WHERE comp.deleted IS FALSE " +
							"AND comp.id = :compId";

			return (LearningPathType) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.uniqueResult();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading competence learning path type");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public UserData chooseRandomPeer(long compId, long userId) throws DbConnectionException {
		try {
			String query =
					"SELECT user " +
					"FROM TargetCompetence1 tComp " +
					"INNER JOIN tComp.user user " +
					"WHERE tComp.competence.id = :compId " +
					"AND user.id != :userId " +
					"AND user.id NOT IN ( " +
						"SELECT assessment.assessor.id " +
						"FROM CompetenceAssessment assessment " +
						"WHERE assessment.student.id = :userId " +
						"AND assessment.competence.id = :compId " +
						"AND assessment.assessor IS NOT NULL " + // can be NULL in default assessments when instructor is not set
						"AND assessment.type = :aType " +
					") " +
					"ORDER BY RAND()";

			@SuppressWarnings("unchecked")
			User res = (User) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setLong("userId", userId)
					.setString("aType", AssessmentType.PEER_ASSESSMENT.name())
					.setMaxResults(1)
					.uniqueResult();

			return res != null ? new UserData(res) : null;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving random peer");
		}
	}

	@Override
	public void completeCompetence(long targetCompetenceId, UserContextData context) throws DbConnectionException {
		Result<Void> res = self.completeCompetenceAndGetEvents(targetCompetenceId, context);
		eventFactory.generateEvents(res.getEventQueue());
	}

	@Override
	@Transactional
	public Result<Void> completeCompetenceAndGetEvents(long targetCompetenceId, UserContextData context)
			throws DbConnectionException {
		try {
			TargetCompetence1 tc = (TargetCompetence1) persistence.currentManager()
					.load(TargetCompetence1.class, targetCompetenceId);
			tc.setProgress(100);
			tc.setDateCompleted(new Date());

			Result<Void> res = new Result<>();
			TargetCompetence1 tComp = new TargetCompetence1();
			tComp.setId(targetCompetenceId);
			res.appendEvent(eventFactory.generateEventData(
					EventType.Completion, context, tComp, null, null, null));

			EventData ev = eventFactory.generateEventData(EventType.ChangeProgress,
					context, tComp, null, null, null);
			ev.setProgress(100);
			res.appendEvent(ev);

			//flush in order to calculate correct progress for credential
			persistence.currentManager().flush();
			res.appendEvents(credentialManager.updateCredentialProgress(targetCompetenceId, context));

			return res;
		} catch (DbConnectionException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error marking the competence as completed");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public TargetCompetence1 getTargetCompetence(long compId, long userId) throws DbConnectionException {
		try {
			String query =
					"SELECT tComp " +
					"FROM TargetCompetence1 tComp " +
					"WHERE tComp.competence.id = :compId " +
					"AND tComp.user.id = :userId ";

			return (TargetCompetence1) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.setLong("userId", userId)
					.uniqueResult();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error retrieving target competence id");
		}
	}

	@Override
	@Transactional
	public void checkIfCompetenceIsPartOfACredential(long credId, long compId)
			throws ResourceNotFoundException {
		/*
		 * check if passed credential has specified competence
		 */
		if(credId > 0) {
			String query1 = "SELECT credComp.id " +
					"FROM CredentialCompetence1 credComp " +
					"WHERE credComp.credential.id = :credId " +
					"AND credComp.competence.id = :compId";

			@SuppressWarnings("unchecked")
			List<Long> res1 = persistence.currentManager()
					.createQuery(query1)
					.setLong("credId", credId)
					.setLong("compId", compId)
					.list();

			if(res1 == null || res1.isEmpty()) {
				throw new ResourceNotFoundException();
			}
		}
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isUserEnrolled(long compId, long userId) throws DbConnectionException {
		try {
			return getTargetCompetenceId(compId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error checking if user is enrolled in a competence");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getTargetCompetenceOrCompetenceData(
			long compId, long studentId, boolean loadAssessmentConfig, boolean loadLearningPathContent,
			boolean loadCreator, boolean loadTags) throws DbConnectionException {
		try {
			CompetenceData1 compData = getTargetCompetenceData(0, compId, studentId, loadAssessmentConfig, loadLearningPathContent);
			if (compData == null) {
				compData = getCompetenceData(0, compId, loadCreator, loadAssessmentConfig, loadTags, loadLearningPathContent, false);
			}

			return compData;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the competence data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<AssessmentTypeConfig> getCompetenceAssessmentTypesConfig(long compId, boolean loadBlindAssessmentMode) throws DbConnectionException {
		try {
			String q =
					"SELECT conf FROM CompetenceAssessmentConfig conf " +
					"WHERE conf.competence.id = :compId";
			@SuppressWarnings("unchecked")
			List<CompetenceAssessmentConfig> assessmentTypesConfig = persistence.currentManager()
					.createQuery(q)
					.setLong("compId", compId)
					.list();
			List<AssessmentTypeConfig> res = competenceFactory.getAssessmentConfig(assessmentTypesConfig);
			if (loadBlindAssessmentMode) {
				res
						.stream()
						.filter(conf -> conf.getType() == AssessmentType.PEER_ASSESSMENT)
						.findFirst().get()
						.setBlindAssessmentMode(getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(compId, AssessmentType.PEER_ASSESSMENT));
			}
			return res;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the assessment types config for competence");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long getTargetCompetenceId(long compId, long studentId) throws DbConnectionException {
		try {
			String query =
					"SELECT tc.id " +
					"FROM TargetCompetence1 tc " +
					"WHERE tc.user.id = :userId " +
					"AND tc.competence.id = :compId";

			Long result = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", studentId)
					.setLong("compId", compId)
					.uniqueResult();

			return result != null ? result.longValue() : 0;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading target competence id");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public BlindAssessmentMode getTheMostRestrictiveCredentialBlindAssessmentModeForAssessmentTypeAndCompetence(long compId, AssessmentType assessmentType) {
		try {
			String q =
					"SELECT conf.blindAssessmentMode FROM CredentialCompetence1 cc " +
					"INNER JOIN cc.credential cred " +
					"INNER JOIN cred.assessmentConfig conf " +
					"WITH conf.assessmentType = :assessmentType " +
					"WHERE cc.competence.id = :compId " +
					"AND cred.type = :deliveryType " +
					"ORDER BY CASE WHEN conf.blindAssessmentMode = :doubleBlindMode THEN 1 WHEN conf.blindAssessmentMode = :blindMode THEN 2 ELSE 3 END";

			return (BlindAssessmentMode) persistence.currentManager()
					.createQuery(q)
					.setLong("compId", compId)
					.setString("assessmentType", assessmentType.name())
					.setString("doubleBlindMode", BlindAssessmentMode.DOUBLE_BLIND.name())
					.setString("blindMode", BlindAssessmentMode.BLIND.name())
					.setString("deliveryType", CredentialType.Delivery.name())
					.setMaxResults(1)
					.uniqueResult();
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading the blind assessment mode for competency");
		}
	}

	@Override
	@Transactional
	public void saveEvidenceSummary(long targetCompetenceId, String evidenceSummary) {
		try {
			TargetCompetence1 tc = (TargetCompetence1) persistence.currentManager().load(TargetCompetence1.class, targetCompetenceId);
			tc.setEvidenceSummary(evidenceSummary);
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error updating the evidence summary for target competence with id: " + targetCompetenceId);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long getIdOfFirstCredentialCompetenceIsAddedToAndStudentHasLearnPrivilegeFor(long compId, long studentId) {
		try {
			String query = "SELECT cred.id, tc.id " +
					"FROM CredentialCompetence1 cc " +
					"INNER JOIN cc.credential cred " +
					"WITH cred.type = :type " +
					"LEFT JOIN cred.targetCredentials tc " +
					"WITH tc.user.id = :userId " +
					"WHERE cc.competence.id = :compId";

			List<Object[]> res = (List<Object[]>) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", studentId)
					.setLong("compId", compId)
					.setString("type", CredentialType.Delivery.name())
					.list();

			for (Object[] row : res) {
				long credId = (long) row[0];
				if (row[1] != null) {
					/*
					if student is enrolled in credential it means he has a privilege to learn it so
					return id of that credential
					 */
					return credId;
				}
				ResourceAccessData resourceAccess = credentialManager.getResourceAccessData(
						credId,
						studentId,
						ResourceAccessRequirements.of(AccessMode.USER).addPrivilege(UserGroupPrivilege.Learn));
				if (resourceAccess.isCanLearn()) {
					/*
					if student is allowed to learn credential return id of that credential
					 */
					return credId;
				}
			}
			return 0L;
		} catch (Exception e) {
			logger.error("Error", e);
			throw new DbConnectionException("Error loading credential id");
		}
	}

}