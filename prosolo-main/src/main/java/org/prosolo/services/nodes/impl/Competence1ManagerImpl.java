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
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.bigdata.common.exceptions.StaleDataException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.CompetenceBookmark;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.LearningInfo;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.CompetenceUserAccessSpecification;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessFactory;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.nodes.data.resourceAccess.UserAccessSpecification;
import org.prosolo.services.nodes.factory.ActivityDataFactory;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.UserDataFactory;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

	@Override
	//nt
	public Competence1 saveNewCompetence(CompetenceData1 data, long creatorId, long credentialId,
										 LearningContextData context) throws DbConnectionException,
			IllegalDataStateException, EventException {
		//self-invocation
		Result<Competence1> res = self.saveNewCompetenceAndGetEvents(data, creatorId, credentialId, context);
		for (EventData ev : res.getEvents()) {
			//todo observer refactor - generate attach event always when sequential event execution is supported
			if (credentialId == 0 || ev.getEventType() != EventType.Attach) {
				if (context != null) {
					ev.setPage(context.getPage());
					ev.setContext(context.getLearningContext());
					ev.setService(context.getService());
				}
				eventFactory.generateEvent(ev);
			}
		}
		return res.getResult();
	}

	@Override
	@Transactional
	public Result<Competence1> saveNewCompetenceAndGetEvents(CompetenceData1 data, long creatorId, long credentialId,
			LearningContextData context) throws DbConnectionException, IllegalDataStateException {
		try {
			/*
			 * if competence has no activities, it can't be published
			 */
			if (data.isPublished() && (data.getActivities() == null || data.getActivities().isEmpty())) {
				throw new IllegalDataStateException("Can not publish competency without activities.");
			}

			Result<Competence1> result = new Result<>();
			Competence1 comp = new Competence1();
			comp.setTitle(data.getTitle());
			comp.setDateCreated(new Date());
			comp.setDescription(data.getDescription());
			comp.setCreatedBy(loadResource(User.class, creatorId));
			comp.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
			comp.setType(data.getType());
			comp.setPublished(data.isPublished());
			comp.setDuration(data.getDuration());
			comp.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getTagsString())));
			saveEntity(comp);

			if (data.getActivities() != null) {
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

			if (credentialId > 0) {
				result.addEvents(credentialManager.addCompetenceToCredential(credentialId, comp,
						creatorId));
			}

			/*
				todo observer refactor - we do not need credential id as a parameter when attach event is generated
			*/
			Map<String, String> params = null;
			if (credentialId > 0) {
				params = new HashMap<>();
				params.put("credentialId", credentialId + "");
			}
			result.addEvent(eventFactory.generateEventData(EventType.Create, creatorId, comp, null, context,
					params));

			//add Edit privilege to the competence creator
			result.addEvents(userGroupManager.createCompetenceUserGroupAndSaveNewUser(creatorId, comp.getId(),
					UserGroupPrivilege.Edit,true, creatorId, context).getEvents());

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

	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public Competence1 deleteCompetence(CompetenceData1 data, long userId) throws DbConnectionException {
		try {
			if (data.getCompetenceId() > 0) {
				Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class,
						data.getCompetenceId());
				comp.setDeleted(true);
				deleteCompetenceActivities(data.getCompetenceId());
				deleteAllCredentialCompetencesForCompetence(comp.getId());
				eventFactory.generateEvent(EventType.Delete, userId, comp);

				return comp;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence");
		}
	}
	
	@Deprecated
	private void deleteAllCredentialCompetencesForCompetence(long compId) {
		Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
		long duration = comp.getDuration();

		List<CredentialCompetence1> res = getAllCredentialCompetencesForCompetence(compId);
		boolean published = comp.isPublished();
		for (CredentialCompetence1 cc : res) {
			// credMap.put(cc.getCredential(), cc.getOrder());
			Credential1 cred = cc.getCredential();
			// TODO use hql update to avoid select + update
			/*
			 * We should update credentials duration only if competence was
			 * published. If it was not published competence duration was not
			 * event added to credential duration.
			 */
			if (published) {
				cred.setDuration(cred.getDuration() - duration);
			}
			shiftOrderOfCompetencesUp(cred.getId(), cc.getOrder());
			delete(cc);
		}

	}

	@Deprecated
	private List<CredentialCompetence1> getAllCredentialCompetencesForCompetence(long compId) {
		Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
		String query = "SELECT credComp " + "FROM CredentialCompetence1 credComp "
				+ "WHERE credComp.competence = :comp";

		@SuppressWarnings("unchecked")
		List<CredentialCompetence1> res = persistence.currentManager().createQuery(query).setEntity("comp", comp)
				.list();

		return res != null ? res : new ArrayList<>();
	}

	@Deprecated
	private void shiftOrderOfCompetencesUp(long id, int order) {
		List<CredentialCompetence1> compsAfterDeleted = getAllCredentialCompetencesAfterSpecified(id, order);
		for (CredentialCompetence1 cc : compsAfterDeleted) {
			// TODO use hql update to avoid select + update
			cc.setOrder(cc.getOrder() - 1);
		}
	}

	@Deprecated
	private List<CredentialCompetence1> getAllCredentialCompetencesAfterSpecified(long credId, int order) {
		Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
		String query = "SELECT credComp " + "FROM Credential1 cred " + "LEFT JOIN cred.competences credComp "
				+ "WHERE cred = :cred " + "AND credComp.order > :order";

		@SuppressWarnings("unchecked")
		List<CredentialCompetence1> res = persistence.currentManager().createQuery(query).setEntity("cred", cred)
				.setInteger("order", order).list();

		if (res == null) {
			return new ArrayList<>();
		}

		return res;
	}

	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags)
			throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			//TODO cred-redesign-07
//			//TODO add activity loading, maybe boolean flags loadCreator, loadActivities
//			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
//					TargetCredential1.class, targetCredentialId);
//			StringBuilder builder = new StringBuilder();
//			builder.append("SELECT targetComp " +
//				       	   "FROM TargetCompetence1 targetComp " + 
//				       	   "INNER JOIN fetch targetComp.createdBy user ");
//			if(loadTags) {
//				builder.append("LEFT JOIN fetch targetComp.tags tags ");
//			}
//			builder.append("WHERE targetComp.targetCredential = :targetCred " +
//				       	   "ORDER BY targetComp.order");
////			String query = "SELECT targetComp " +
////					       "FROM TargetCompetence1 targetComp " + 
////					       "INNER JOIN fetch targetComp.createdBy user " +
////					       "LEFT JOIN fetch comp.tags tags " +
////					       "WHERE targetComp.targetCredential = :targetCred " +
////					       "ORDER BY targetComp.order";
//
//			@SuppressWarnings("unchecked")
//			List<TargetCompetence1> res = persistence.currentManager()
//				.createQuery(builder.toString())
//				.setEntity("targetCred", targetCred)
//				.list();
//
//			if (res != null) {
//				for (TargetCompetence1 targetComp : res) {
//					Set<Tag> tags = loadTags ? targetComp.getTags() : null;
//					CompetenceData1 compData = competenceFactory.getCompetenceData(
//							targetComp.getCreatedBy(), targetComp, tags, null, true);
//					result.add(compData);
//				}
//			}
//			return result;
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getUserCompetencesForCredential(long credId, long userId, boolean loadCreator,
		 boolean loadTags, boolean loadActivities)
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

			if (loadCreator) {
				builder.append("INNER JOIN fetch comp.createdBy user ");
			}

			if (loadTags) {
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
						Set<Tag> tags = loadTags ? comp.getTags() : null;
						User createdBy = loadCreator ? comp.getCreatedBy() : null;
						TargetCompetence1 tComp = (TargetCompetence1) row[1];
						CompetenceData1 compData = null; 
						if (tComp != null) {
							compData = competenceFactory.getCompetenceData(createdBy, tComp, cc.getOrder(), tags, null, 
									false);
							if (loadActivities) {
								List<ActivityData> activities = activityManager
										.getTargetActivitiesData(compData.getTargetCompId());
								compData.setActivities(activities);
							}
						} else {
							compData = competenceFactory.getCompetenceData(createdBy, cc, tags, false);
							if (loadActivities) {
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

	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred)
			throws DbConnectionException {
		try {
			List<CredentialCompetence1> credComps = getCredentialCompetences(credId,
					false, true, false);
			List<TargetCompetence1> targetComps =  new ArrayList<>();
			for(CredentialCompetence1 cc : credComps) {
				//TargetCompetence1 targetComp = createTargetCompetence(targetCred, cc);
				//targetComps.add(targetComp);
			}
			return targetComps;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling competences");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public CompetenceData1 enrollInCompetenceAndGetCompetenceData(long compId, long userId, 
			LearningContextData context) throws DbConnectionException {
		try {
			TargetCompetence1 targetComp = enrollInCompetence(compId, userId, context);
			
			CompetenceData1 cd = competenceFactory.getCompetenceData(targetComp.getCompetence().getCreatedBy(), 
					targetComp, 0, targetComp.getCompetence().getTags(), null, false);
			
			if(targetComp.getTargetActivities() != null) {
				for(TargetActivity1 ta : targetComp.getTargetActivities()) {
					ActivityData act = activityFactory.getActivityData(ta, null, null, false, 0, false);
					cd.addActivity(act);
				}
			}
			
			return cd;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling a competence");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public TargetCompetence1 enrollInCompetence(long compId, long userId, LearningContextData context) 
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
			
			List<TargetActivity1> targetActivities = activityManager.createTargetActivities(targetComp);
			targetComp.setTargetActivities(targetActivities);
			
			/*
			 * set first activity as next to learn
			 */
			if (!targetActivities.isEmpty()) {
				targetComp.setNextActivityToLearnId(targetActivities.get(0).getActivity().getId());
			}
			
			Competence1 competence = new Competence1();
			competence.setId(compId);
			Map<String, String> params = new HashMap<>();
			String dateEnrolledString = null;
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			dateEnrolledString = df.format(now);
			params.put("dateEnrolled", dateEnrolledString);
			eventFactory.generateEvent(EventType.ENROLL_COMPETENCE, userId, context, competence, null, params);
			
			return targetComp;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling a competence");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CompetenceData1> getCompetenceDataWithAccessRightsInfo(long credId, long compId, 
			boolean loadCreator, boolean loadTags, boolean loadActivities, long userId, 
			ResourceAccessRequirements req, boolean shouldTrackChanges) 
					throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			if(req == null) {
				throw new IllegalArgumentException();
			}
			CompetenceData1 compData = getCompetenceData(credId, compId, loadCreator, loadTags, loadActivities, 
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
			boolean loadTags, boolean loadActivities, boolean shouldTrackChanges) 
					throws ResourceNotFoundException, DbConnectionException {
		try {
			Competence1 comp = getCompetence(credId, compId, loadCreator, loadTags, true);
			
			if(comp == null) {
				throw new ResourceNotFoundException();
			}
			
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, tags, shouldTrackChanges);
			
			if(loadActivities) {
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
	@Transactional(readOnly = true)
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
	public Competence1 updateCompetence(CompetenceData1 data, long userId, LearningContextData context) 
			throws DbConnectionException, IllegalDataStateException, StaleDataException {
		try {
			/*
			 * if competence has no activities (that are not removed), it can't
			 * be published
			 */
			if(data.isPublished()) {
				if(data.getActivities() == null) {
					throw new IllegalDataStateException("Competency should have at least one activity");
				}
				long numberOfActivities = data.getActivities().stream().filter(
						act -> act.getObjectStatus() != ObjectStatus.REMOVED).count();
				if(numberOfActivities == 0) {
					throw new IllegalDataStateException("Competency should have at least one activity");
				}
			}

			Competence1 updatedComp = resourceFactory.updateCompetence(data, userId);

			String page = context != null ? context.getPage() : null;
			String lContext = context != null ? context.getLearningContext() : null;
			String service = context != null ? context.getService() : null;

			fireCompEditEvent(data, userId, updatedComp, page, lContext, service);
			
			/* 
			 * flushing to force lock timeout exception so it can be catched here. 
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

	private void fireCompEditEvent(CompetenceData1 data, long userId, Competence1 updatedComp, String page,
			String context, String service) throws EventException {
		Map<String, String> params = new HashMap<>();
		CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
				data.isPublishedChanged(), data.isTitleChanged(), data.isDescriptionChanged(), false,
				data.isTagsStringChanged(), data.isStudentAllowedToAddActivitiesChanged());
		Gson gson = new GsonBuilder().create();
		String jsonChangeTracker = gson.toJson(changeTracker);
		params.put("changes", jsonChangeTracker);

		eventFactory.generateEvent(EventType.Edit, userId, updatedComp, null, page, context, service, params);
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
		
		/* if competence should be unpublished we need to check if there are active deliveries with this competence 
		 * and if so, unpublish should not be allowed
		 */
		if(!data.isPublished() && data.isPublishedChanged()) {
			boolean canUnpublish = !isThereAnActiveDeliveryWithACompetence(data.getCompetenceId());
			if(!canUnpublish) {
				throw new IllegalDataStateException("Competency can not be unpublished because there is an active credential delivery with this competency");
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
    		compToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getTagsString())));		     
    	}
    	
    	//these changes are not allowed if competence was once published
    	if(data.getDatePublished() == null) {
    		compToUpdate.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
			
    		List<ActivityData> activities = data.getActivities();
		    if(activities != null) {
		    	boolean recalculateDuration = false;
	    		Iterator<ActivityData> actIterator = activities.iterator();
	    		while(actIterator.hasNext()) {
	    			ActivityData bad = actIterator.next();
		    		switch(bad.getObjectStatus()) {
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
	    		if(recalculateDuration) {
		    		persistence.currentManager().flush();
		    		long oldDuration = compToUpdate.getDuration();
		    		long newDuration = getRecalculatedDuration(compToUpdate.getId());
		    		compToUpdate.setDuration(newDuration);
		    		updateCredDuration(compToUpdate.getId(), newDuration, oldDuration);
	    		}
		    }
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

	// @Transactional(readOnly = true)
	// public long getCompetenceDuration(long compId) throws
	// DbConnectionException {
	// try {
	// String query = "SELECT comp.duration " +
	// "FROM Competence1 comp " +
	// "WHERE comp.id = :compId";
	//
	// Long duration = (Long) persistence.currentManager()
	// .createQuery(query)
	// .setLong("compId", compId)
	// .uniqueResult();
	//
	// return duration;
	// } catch(Exception e) {
	// logger.error(e);
	// e.printStackTrace();
	// throw new DbConnectionException("Error while retrieving competence
	// duration");
	// }
	// }

	// private void updateCredentialsDuration(long compId, long newDuration,
	// long oldDuration) {
	// long durationChange = newDuration - oldDuration;
	// Operation op = null;
	// if(durationChange > 0) {
	// op = Operation.Add;
	// } else {
	// durationChange = -durationChange;
	// op = Operation.Subtract;
	// }
	// credentialManager.updateDurationForCredentialsWithCompetence(compId,
	// durationChange, op);
	//
	// }

	@Deprecated
	private void deleteCompetenceActivities(long compId) throws DbConnectionException {
		try {
			String query = "DELETE CompetenceActivity1 compAct " + "WHERE compAct.competence.id = :compId";

			persistence.currentManager().createQuery(query).setLong("compId", compId).executeUpdate();

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence activities");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished, long userId) 
					throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			List<CredentialCompetence1> res = getCredentialCompetences(credentialId, loadCreator, loadTags,
					includeNotPublished);

			for (CredentialCompetence1 credComp : res) {
				User creator = loadCreator ? credComp.getCompetence().getCreatedBy() : null;
				Set<Tag> tags = loadTags ? credComp.getCompetence().getTags() : null;
				
				CompetenceData1 compData = competenceFactory.getCompetenceData(
						creator, credComp, tags, true);
				
				if(loadActivities) {
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
	
//	@Override
//	@Transactional(readOnly = true)
//	public CompetenceData1 getTargetCompetenceData(long targetCompId, boolean loadActivities, 
//			boolean loadCredentialTitle) throws DbConnectionException {
//		CompetenceData1 compData = null;
//		try {			
//			String query = "SELECT targetComp " +
//						   "FROM TargetCompetence1 targetComp " + 
//						   "INNER JOIN fetch targetComp.createdBy user " + 
//						   "LEFT JOIN fetch targetComp.tags tags " +
//						   "WHERE targetComp.id = :id";
//
//			TargetCompetence1 res = (TargetCompetence1) persistence.currentManager()
//					.createQuery(query)
//					.setLong("id", targetCompId)
//					.uniqueResult();
//
//			if (res != null) {
//				Credential1 cred = null;
//				if(loadCredentialTitle) {
//					String query1 = "SELECT cred.id, cred.title " +
//									"FROM TargetCompetence1 comp " +
//									"INNER JOIN comp.targetCredential targetCred " +
//									"INNER JOIN targetCred.credential cred " +
//									"WHERE comp = :comp";
//					Object[] credentialData = (Object[]) persistence.currentManager()
//							.createQuery(query1)
//							.setEntity("comp", res)
//							.uniqueResult();
//					
//					cred = new Credential1();
//					cred.setId((long) credentialData[0]);
//					cred.setTitle((String) credentialData[1]);
//					
//				}
//				compData = competenceFactory.getCompetenceData(res.getCreatedBy(), res, 
//						res.getTags(), cred, true);
//				
//				if(compData != null && loadActivities) {
//					List<ActivityData> activities = activityManager
//							.getTargetActivitiesData(targetCompId);
//					compData.setActivities(activities);
//				}
//				return compData;
//			}
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading competence data");
//		}
//	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId) throws DbConnectionException {
		return getCompetenceTags(compId, persistence.currentManager());
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId, Session session) throws DbConnectionException {
		try {
			// if left join is used list with null element would be returned.
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
		
	@Deprecated
	@Transactional(readOnly = false)
	private void updateTargetCompetenceBasicDataForUncompletedCredentials(long compId) throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);

			List<Long> ids = getTargetCompetenceIdsForUncompletedCredentials(compId);

			if (!ids.isEmpty()) {
				String query = "UPDATE TargetCompetence1 targetComp " + "SET targetComp.title = :title, "
						+ "targetComp.description = :description, "
						+ "targetComp.studentAllowedToAddActivities = :studentAllowedToAddActivities "
						+ "WHERE targetComp.id IN (:compIds)";

				persistence.currentManager().createQuery(query).setString("title", comp.getTitle())
						.setString("description", comp.getDescription())
						.setBoolean("studentAllowedToAddActivities", comp.isStudentAllowedToAddActivities())
						.setParameterList("compIds", ids).executeUpdate();
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating creator of competences");
		}
	}
	
	@Deprecated
	@Transactional(readOnly = true)
	private List<Long> getTargetCompetenceIdsForUncompletedCredentials(long compId) {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);

			String query = "SELECT comp.id FROM TargetCompetence1 comp " + "INNER JOIN comp.targetCredential cred "
					+ "WITH cred.progress != :progress " + "WHERE comp.competence = :comp";

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager().createQuery(query).setEntity("comp", comp)
					.setInteger("progress", 100).list();

			if (res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence ids");
		}
	}

	//should be removed probably, new method is created
//	@Deprecated
//	@Transactional(readOnly = true)
//	private List<TargetCompetence1> getTargetCompetencesForCompetence(long compId, 
//			boolean justUncompleted) 
//			throws DbConnectionException {
//		try {		
//			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
//					compId);
//			StringBuilder builder = new StringBuilder();
//			builder.append("SELECT comp " +
//				           "FROM TargetCompetence1 comp ");
//			if(justUncompleted) {
//				builder.append("INNER JOIN comp.targetCredential cred " +
//			       		          "WITH cred.progress != :progress ");
//			}
//			builder.append("WHERE comp.competence = :comp");
////			String query = "SELECT comp " +
////					       "FROM TargetCompetence1 comp " +
////					       "INNER JOIN comp.targetCredential cred " +
////					       		"WITH cred.progress != :progress " +
////					       "WHERE comp.competence = :comp";					    
//			Query q = persistence.currentManager()
//				.createQuery(builder.toString())
//				.setEntity("comp", comp);
//			if(justUncompleted) {
//				q.setInteger("progress", 100);
//			}
//			@SuppressWarnings("unchecked")
//			List<TargetCompetence1> res = q.list();
//			if(res == null) {
//				return new ArrayList<>();
//			}
//			
//			return res;
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading user competences");
//		}
//	}
	
	@Deprecated
	@Transactional(readOnly = false)
	private void updateTargetCompetencesTags(long compId) throws DbConnectionException {
		try {
			//TODO cred-redesign-07
//			List<TargetCompetence1> targetComps = getTargetCompetencesForCompetence(compId, true);
//			List<Tag> tags = getCompetenceTags(compId);
//			for(TargetCompetence1 tc : targetComps) {
//				tc.getTags().clear();
//				for(Tag tag : tags) {
//					tc.getTags().add(tag);
//				}
//			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user competences");
		}
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCompetencesWithChangedData(long compId, CompetenceChangeTracker changeTracker)
			throws DbConnectionException {
		if (changeTracker.isPublished()) {
			if (changeTracker.isTagsChanged()) {
				updateTargetCompetencesTags(compId);
			}
			if (changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged()
					|| changeTracker.isStudentAllowedToAddActivitiesChanged()) {
				updateTargetCompetenceBasicDataForUncompletedCredentials(compId);

				/**
				 * not needed because if duration is changed here (when draft
				 * version of competence is published that means that new
				 * activities were added to competence and that should not be
				 * propagated
				 */
				// if(changeTracker.isDurationChanged()) {
				// recalculateTargetCompetencesAndCredentialsDuration(compId);
				// }
			}
		}
	}

	// @Transactional(readOnly = true)
	// private void recalculateTargetCompetencesAndCredentialsDuration(long
	// compId) {
	// try {
	// String query1 = "SELECT cred.id " +
	// "FROM TargetCompetence1 comp " +
	// "INNER JOIN comp.targetCredential cred " +
	// "WITH cred.progress != :progress " +
	// "WHERE comp.competence.id = :compId";
	//
	// @SuppressWarnings("unchecked")
	// List<Long> credIds = persistence.currentManager()
	// .createQuery(query1)
	// .setInteger("progress", 100)
	// .setLong("compId", compId)
	// .list();
	//
	// if(!credIds.isEmpty()) {
	// String query2 = "SELECT cred.id, comp.duration " +
	// "FROM TargetCredential1 cred " +
	// "LEFT JOIN cred.targetCompetences comp " +
	// "WHERE cred.id IN (:credIds) " +
	// "ORDER BY cred.id";
	//
	// @SuppressWarnings("unchecked")
	// List<Object[]> res = persistence.currentManager()
	// .createQuery(query2)
	// .setParameterList("credIds", credIds)
	// .list();
	//
	// if(res != null) {
	// long currentCredId = 0;
	// long cumulativeCredDuration = 0;
	// for(Object[] row : res) {
	// long credId = (long) row[0];
	// long duration = (long) row[1];
	//
	// if(credId == currentCredId) {
	// cumulativeCredDuration += duration;
	// } else {
	// if(currentCredId != 0) {
	// credentialManager.updateTargetCredentialDuration(currentCredId,
	// cumulativeCredDuration);
	// }
	// currentCredId = credId;
	// cumulativeCredDuration = duration;
	// }
	// }
	// if(currentCredId != 0) {
	// credentialManager.updateTargetCredentialDuration(currentCredId,
	// cumulativeCredDuration);
	// }
	// }
	// }
	// } catch(Exception e) {
	// logger.error(e);
	// e.printStackTrace();
	// throw new DbConnectionException("Error while recalculating duration");
	// }
	// }

	// @Deprecated
	// @Override
	// @Transactional(readOnly = false)
	// public void publishDraftCompetencesWithoutDraftVersion(List<Long>
	// compIds)
	// throws DbConnectionException {
	// try {
	// if(compIds == null || compIds.isEmpty()) {
	// return;
	// }
	//
	// String query = "UPDATE Competence1 comp " +
	// "SET comp.published = :published " +
	// "WHERE comp.hasDraft = :hasDraft " +
	// "AND comp.id IN :compIds";
	// persistence.currentManager()
	// .createQuery(query)
	// .setBoolean("published", true)
	// .setBoolean("hasDraft", false)
	// .setParameterList("compIds", compIds)
	// .executeUpdate();
	//
	// for(Long compId : compIds) {
	// activityManager.publishActivitiesFromCompetence(compId);
	// }
	// } catch(Exception e) {
	// logger.error(e);
	// e.printStackTrace();
	// throw new DbConnectionException("Error while updating competences");
	// }
	// }

	@Override
	@Transactional(readOnly = false, rollbackFor = Exception.class)
	public EventData addActivityToCompetence(long compId, Activity1 act, long userId) 
			throws DbConnectionException, IllegalDataStateException {
		try {
			/*
			 * Lock the competence row in db so it can't be updated while we have the lock.
			 * That way, we can avoid the situation where competence is published concurrently and
			 * we add new activity to it after it is published which violates data integrity rule
			 */
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId, 
					LockOptions.UPGRADE);
			
			//if publish date is not null then new activities are not allowed to be added. Only limited edits are allowed.
			if(comp.getDatePublished() != null) {
				throw new IllegalDataStateException("After competence is first published, new activities can not be added. Only limited edits allowed.");
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

	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCompetenceDuration(long id, long duration) throws DbConnectionException {
		try {
			String query = "UPDATE TargetCompetence1 comp SET " + "comp.duration = :duration "
					+ "WHERE comp.id = :compId";

			persistence.currentManager().createQuery(query).setLong("duration", duration).setLong("compId", id)
					.executeUpdate();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
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
	
	@Deprecated
	@Override
	@Transactional(readOnly = false)
	public void updateProgressForTargetCompetenceWithActivity(long targetActId) throws DbConnectionException {
		try {
			String query1 = "SELECT comp.id " + "FROM TargetActivity1 act " + "INNER JOIN act.targetCompetence comp "
					+ "WHERE act.id = :actId";

			Long targetCompId = (Long) persistence.currentManager().createQuery(query1).setLong("actId", targetActId)
					.uniqueResult();

			TargetCompetence1 targetComp = (TargetCompetence1) persistence.currentManager()
					.load(TargetCompetence1.class, targetCompId);

			String query = "SELECT CASE act.completed " + "WHEN true then 100 " + "else 0 " + "END "
					+ "FROM TargetActivity1 act " + "WHERE act.targetCompetence = :comp";

			@SuppressWarnings("unchecked")
			List<Integer> res = persistence.currentManager().createQuery(query).setEntity("comp", targetComp).list();

			if (res != null) {
				int cumulativeProgress = 0;
				for (Integer p : res) {
					cumulativeProgress += p.intValue();
				}
				int newProgress = cumulativeProgress / res.size();
				targetComp.setProgress(newProgress);
				persistence.currentManager().flush();

				credentialManager.updateProgressForTargetCredentialWithCompetence(targetCompId);
			}

			// String query = "UPDATE TargetCompetence1 comp SET " +
			// "comp.progress = comp.progress + :progress / " +
			// "(SELECT count(act.id) FROM TargetActivity1 act " +
			// "WHERE act.targetCompetence = comp) " +
			// "WHERE comp.id = :compId";
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence progress");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public RestrictedAccessResult<CompetenceData1> getFullTargetCompetenceOrCompetenceData(long credId, long compId, 
			long userId) throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException {
		CompetenceData1 compData = null;
		try {
			compData = getTargetCompetenceData(credId, compId, userId, true);
			if (compData == null) {
//				compData = getCompetenceData(compId, true, true, true, userId,
//						LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER, true);
				//compData = getCompetenceDataForUser(credId, compId, true, true, true, userId, true);
				ResourceAccessRequirements req = ResourceAccessRequirements
						.of(AccessMode.USER)
						.addPrivilege(UserGroupPrivilege.Learn)
						.addPrivilege(UserGroupPrivilege.Edit);
				return getCompetenceDataWithAccessRightsInfo(credId, compId, true, true, true, userId, 
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
	 * @param loadActivities
	 * @param loadCredentialTitle
	 * @return
	 * @throws DbConnectionException
	 */
	@Transactional(readOnly = true)
	private CompetenceData1 getTargetCompetenceData(long credId, long compId, long userId, 
			boolean loadActivities) throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT targetComp " +
					       "FROM TargetCompetence1 targetComp " +
						   "INNER JOIN fetch targetComp.competence comp ");
			
			if(credId > 0) {
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
			
			if(credId > 0) {
				q.setLong("credId", credId);
			}

			TargetCompetence1 res = (TargetCompetence1) q.uniqueResult();
			
			if (res != null) {
				compData = competenceFactory.getCompetenceData(res.getCompetence().getCreatedBy(), res, 0,
						res.getCompetence().getTags(), null, true);
				
				if(compData != null && loadActivities) {
					List<ActivityData> activities = activityManager
							.getTargetActivitiesData(compData.getTargetCompId());
					compData.setActivities(activities);
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

	/**
	 * Return all draft competences that satisfy condition: for user role if
	 * competence creator id equals {@code userId}, for manager role if
	 * competence is created by university
	 * 
	 * @param compIds
	 * @param userId
	 * @param role
	 * @return
	 */
	@Deprecated
	@Transactional(readOnly = true)
	private List<Competence1> getDraftCompetencesFromList(List<Long> compIds, long userId) {
		StringBuilder queryB = new StringBuilder(
				"SELECT comp FROM Competence1 comp " + "WHERE comp.id IN (:ids) " + "AND comp.published = :published ");

		Query q = persistence.currentManager().createQuery(queryB.toString()).setParameterList("ids", compIds)
				.setBoolean("published", false);

		@SuppressWarnings("unchecked")
		List<Competence1> comps = q.list();
		if (comps == null) {
			return new ArrayList<>();
		}
		return comps;
	}

//	private Competence1 publishDraftVersion(Competence1 originalComp, Competence1 draftComp) {
//		originalComp.setTitle(draftComp.getTitle());
//		originalComp.setDescription(draftComp.getDescription());
//		originalComp.setStudentAllowedToAddActivities(draftComp.isStudentAllowedToAddActivities());
//		originalComp.setDuration(draftComp.getDuration());
//	    
//		originalComp.getTags().clear();
//		if(draftComp.getTags() != null) {
//			for(Tag tag : draftComp.getTags()) {
//				originalComp.getTags().add(tag);
//			}
//		}	
//
//		List<CompetenceActivity1> activities = activityManager
//				.getCompetenceActivities(draftComp.getId(), true);
//		deleteCompetenceActivities(originalComp.getId());
//	    if(activities != null) {
//    		for(CompetenceActivity1 ca : activities) {
//    			CompetenceActivity1 ca1 = new CompetenceActivity1();
//				ca1.setOrder(ca.getOrder());
//				ca1.setCompetence(originalComp);
//				ca1.setActivity(ca.getActivity());
//				saveEntity(ca1);
//				originalComp.getActivities().add(ca1);
//    		}	
//	    }
//	    
//	    originalComp.setHasDraft(false);
//		originalComp.setDraftVersion(null);
//		originalComp.setPublished(true);
//    	delete(draftComp);
//    	/*
//    	 * old duration is taken from original competence and new duration is
//    	 * taken from draft version
//    	 */
//    	updateCredentialsDuration(originalComp.getId(), draftComp.getDuration(), 
//    			originalComp.getDuration());
//	    
//		return originalComp;
//	}
	
	@Deprecated
	@SuppressWarnings({ "unchecked"})
	@Transactional
	@Override
	public List<TargetCompetence1> getAllCompletedCompetences(Long userId, boolean hiddenFromProfile)
			throws DbConnectionException {
		List<TargetCompetence1> result = new ArrayList<>();
		List<Long> listOfCredentialIds = new ArrayList<>();
		try {
			String query;
			query = "SELECT targetCredential1.id " + "FROM TargetCredential1  targetCredential1 "
					+ "WHERE targetCredential1.user.id = :userId ";

			listOfCredentialIds = persistence.currentManager().createQuery(query).setLong("userId", userId).list();

			query = "SELECT targetComptence1 " + "FROM TargetCompetence1 targetComptence1 "
					+ "WHERE targetComptence1.targetCredential.id in (:listOfCredentialIds) "
					+ "AND targetComptence1.progress = :progress "
					+ "AND targetComptence1.hiddenFromProfile = :hiddenFromProfile";

			if (!listOfCredentialIds.isEmpty()) {
				result = persistence.currentManager().createQuery(query)
						.setParameterList("listOfCredentialIds", listOfCredentialIds).setInteger("progress", 100)
						.setBoolean("hiddenFromProfile", hiddenFromProfile).list();
			}

		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
		return result;
	}

	@Deprecated
	@SuppressWarnings({ "unchecked"})
	@Transactional
	@Override
	public List<TargetCompetence1> getAllUnfinishedCompetences(Long userId, boolean hiddenFromProfile)
			throws DbConnectionException {
		List<TargetCompetence1> result = new ArrayList<>();
		List<Long> listOfCredentialIds = new ArrayList<>();
		try {
			String query;
			query = "SELECT targetCredential1.id " + "FROM TargetCredential1  targetCredential1 "
					+ "WHERE targetCredential1.user.id = :userId ";

			listOfCredentialIds = persistence.currentManager().createQuery(query).setLong("userId", userId).list();

			query = "SELECT targetComptence1 " + "FROM TargetCompetence1 targetComptence1 "
					+ "WHERE targetComptence1.targetCredential.id in (:listOfCredentialIds) "
					+ "AND targetComptence1.progress != 100 "
					+ "AND targetComptence1.hiddenFromProfile = :hiddenFromProfile";

			if (!listOfCredentialIds.isEmpty()) {
				result = persistence.currentManager().createQuery(query)
						.setParameterList("listOfCredentialIds", listOfCredentialIds)
						.setBoolean("hiddenFromProfile", hiddenFromProfile).list();
			}

		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
		return result;
	}
	
	@Deprecated
	@Override
	@Transactional(readOnly = true)
	public List<Competence1> getAllCompetences(Session session) throws DbConnectionException {
		try {
			String query = "SELECT comp " + "FROM Competence1 comp " + "WHERE comp.deleted = :deleted";

			@SuppressWarnings("unchecked")
			List<Competence1> result = session.createQuery(query).setBoolean("deleted", false).list();

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
			return resourceAccessFactory.determineAccessRights(req, spec);
		} catch (DbConnectionException dce) {
			throw dce;
		} catch(Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Error while trying to retrieve user privileges for competency");
		}
	}
	
	@Transactional(readOnly = true)
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
										   boolean visibleToAllChanged, long actorId, LearningContextData lcd)
			throws DbConnectionException, EventException {
		try {
			List<EventData> events =
					self.updateCompetenceVisibilityAndGetEvents(compId, groups, users, visibleToAll,
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
	public List<EventData> updateCompetenceVisibilityAndGetEvents(long compId, List<ResourceVisibilityMember> groups,
																  List<ResourceVisibilityMember> users, boolean visibleToAll,
																  boolean visibleToAllChanged, long actorId,
																  LearningContextData lcd) throws DbConnectionException {
		try {
			List<EventData> events = new ArrayList<>();
			if(visibleToAllChanged) {
				Competence1 comp = (Competence1) persistence.currentManager().load(
						Competence1.class, compId);

				comp.setVisibleToAll(visibleToAll);

				Competence1 competence = new Competence1();
				competence.setId(compId);
				competence.setVisibleToAll(visibleToAll);

				events.add(eventFactory.generateEventData(
						EventType.VISIBLE_TO_ALL_CHANGED,
						actorId, competence, null, lcd, null));
			}
			events.addAll(userGroupManager.saveCompetenceUsersAndGroups(compId, groups, users, actorId, lcd)
					.getEvents());
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
					compData = competenceFactory.getCompetenceData(creator, comp, null, false);
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

				compData = competenceFactory.getCompetenceData(creator, comp, null, false);

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
	@Transactional(readOnly = false)
	public void bookmarkCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			User user = (User) persistence.currentManager().load(User.class, userId);
			CompetenceBookmark cb = new CompetenceBookmark();
			cb.setCompetence(comp);
			cb.setUser(user);
			saveEntity(cb);
			
			/* 
			 * To avoid SQL query when for example user name is accessed.
			 * This way, only id will be accessible.
			 */
			User actor = new User();
			actor.setId(userId);
			CompetenceBookmark bookmark = new CompetenceBookmark();
			bookmark.setId(cb.getId());
			Competence1 competence = new Competence1();
			competence.setId(compId);
			
			eventFactory.generateEvent(EventType.Bookmark, actor.getId(), context, bookmark, competence, null);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while bookmarking competence");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void deleteCompetenceBookmark(long compId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			User user = (User) persistence.currentManager().load(User.class, userId);
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
			
			eventFactory.generateEvent(EventType.RemoveBookmark, userId, context, cb, competence, null);
			
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
	@Transactional(readOnly = false)
	public void archiveCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(compId, true);
			
			Competence1 competence = new Competence1();
			competence.setId(compId);
			eventFactory.generateEvent(EventType.ARCHIVE, userId, context, competence, null, null);
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
			
			switch(searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse");
					break;
				case DRAFT:
					query.append("AND c.archived = :boolFalse " +
								 "AND c.published = :boolFalse");
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
			
			switch(searchFilter) {
				case ACTIVE:
				case DRAFT:
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
	public List<CompetenceData1> searchCompetencesForManager(CompetenceSearchFilter searchFilter, int limit, int page, 
			LearningResourceSortOption sortOption, long userId) 
				throws DbConnectionException, NullPointerException {
		try {
			if(searchFilter == null || sortOption == null) {
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
			
			switch(searchFilter) {
				case ACTIVE:
					query.append("AND c.archived = :boolFalse ");
					break;
				case DRAFT:
					query.append("AND c.archived = :boolFalse " +
								 "AND c.published = :boolFalse ");
					break;
				case PUBLISHED:
					query.append("AND c.archived = :boolFalse " +
							 	 "AND c.published = :boolTrue ");
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
				case DRAFT:
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
				CompetenceData1 cd = competenceFactory.getCompetenceData(null, c, null, false);
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
	public long duplicateCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException, EventException {
		Result<Competence1> res = resourceFactory.duplicateCompetence(compId, userId);
		for(EventData ev : res.getEvents()) {
			if(context != null) {
				ev.setPage(context.getPage());
				ev.setContext(context.getLearningContext());
				ev.setService(context.getService());
			}
			eventFactory.generateEvent(ev);
		}
		return res.getResult().getId();
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
	@Transactional(readOnly = false)
	public void restoreArchivedCompetence(long compId, long userId, LearningContextData context) 
			throws DbConnectionException {
		try {
			//use hql instead of loading object and setting property to avoid version check
			updateArchivedProperty(compId, false);
			
			Competence1 competence = new Competence1();
			competence.setId(compId);
			eventFactory.generateEvent(EventType.RESTORE, userId, context, competence, null, null);
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
			RestrictedAccessResult<CompetenceData1> res = getCompetenceDataWithAccessRightsInfo(credId, compId, true, true, true, userId, 
					req, true);
			
			boolean canUnpublish = !isThereAnActiveDeliveryWithACompetence(compId);
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
	
	@Transactional(readOnly = true)
	private boolean isThereAnActiveDeliveryWithACompetence(long compId) throws DbConnectionException {
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
	public List<EventData> updateCompetenceProgress(long targetCompId, long userId, LearningContextData contextData) 
			throws DbConnectionException {
		try {
			List<EventData> events = new ArrayList<>();
			
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
				
				events.addAll(updateCompetenceProgress(targetCompId, (long) res.get(0)[0], finalCompProgress, 
						nextActToLearnInACompetenceId, userId, contextData));
				
				events.addAll(credentialManager.updateCredentialProgress(targetCompId, userId, contextData));
			}
			return events;
		} catch (DbConnectionException dce) {
			throw dce;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competency progress");
		}
	}

	private List<EventData> updateCompetenceProgress(long targetCompId, long compId, int finalCompProgress, 
			long nextActivityToLearnId, long userId, LearningContextData contextData) {
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
			q.setDate("dateCompleted", now);
		}
		q.executeUpdate();
		
		List<EventData> events = new ArrayList<>();
		
		TargetCompetence1 tComp = new TargetCompetence1();
		tComp.setId(targetCompId);
		Competence1 competence = new Competence1();
		competence.setId(compId);
		tComp.setCompetence(competence);
		
		EventData ev = eventFactory.generateEventData(EventType.ChangeProgress, userId, tComp, null, 
				contextData, null);
		ev.setProgress(finalCompProgress);
		events.add(ev);
//		eventFactory.generateChangeProgressEvent(userId, tComp, finalCompProgress, 
//				lcPage, lcContext, lcService, params);
		if(finalCompProgress == 100) {
			Map<String, String> params = new HashMap<>();
			String dateCompletedStr = null;
			if(now != null) {
				DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				dateCompletedStr = df.format(now);
			}
			params.put("dateCompleted", dateCompletedStr);
			events.add(eventFactory.generateEventData(EventType.Completion, userId, tComp, null, 
					contextData, params));
//			eventFactory.generateEvent(EventType.Completion, userId, tComp, null,
//					lcPage, lcContext, lcService, null);
		}
		return events;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Result<Void> publishCompetenceIfNotPublished(Competence1 comp, long actorId) 
			throws DbConnectionException, IllegalDataStateException {
		try {
			Result<Void> res = new Result<>();
			
			if (!comp.isPublished()) {
				/*
				 * check if competence has at least one activity - if not, it can't be published
				 */
				int numberOfActivities = comp.getActivities().size();
				if (numberOfActivities == 0) {
					throw new IllegalDataStateException("Can not publish competency without activities.");
				}
			
				comp.setPublished(true);
				if (comp.getDatePublished() == null) {
					comp.setDatePublished(new Date());
				}

				Competence1 c = new Competence1();
				c.setId(comp.getId());
				res.addEvent(eventFactory.generateEventData(EventType.STATUS_CHANGED, actorId, c, null, null, null));
			}
			
			return res;
		} catch (IllegalDataStateException e) {
			logger.error(e);
			throw e;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
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
	public Result<Void> updateCompetenceCreator(long newCreatorId, long oldCreatorId)
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
				result.addEvents(userGroupManager.removeUserFromDefaultCompetenceGroupAndGetEvents(
						oldCreatorId, id, UserGroupPrivilege.Edit, 0, null).getEvents());
				//add edit privilege to new owner
				result.addEvents(userGroupManager.saveUserToDefaultCompetenceGroupAndGetEvents(
						newCreatorId, id, UserGroupPrivilege.Edit, 0, null).getEvents());
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating creator of competences");
		}
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
	public List<TargetCompetence1> getAllCompletedCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		try {
			String query =
				"SELECT targetComptence1 " +
				"FROM TargetCompetence1 targetComptence1 " +
				"WHERE targetComptence1.targetCredential.id IN (" +
					"SELECT targetCredential1.id " +
					"FROM TargetCredential1 targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId " +
				") " + 
			    "AND targetComptence1.progress = 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetComptence1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetComptence1.title";
			
			return persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.list();
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	@Transactional (readOnly = true)
	public List<TargetCompetence1> getAllInProgressCompetences(long userId, boolean onlyPubliclyVisible) throws DbConnectionException {
		try {
			String query =
				"SELECT targetComptence1 " +
				"FROM TargetCompetence1 targetComptence1 " +
				"WHERE targetComptence1.targetCredential.id IN (" +
					"SELECT targetCredential1.id " +
					"FROM TargetCredential1 targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId " +
				") " + 
			    "AND targetComptence1.progress < 100 ";
			
			if (onlyPubliclyVisible) {
				query += " AND targetComptence1.hiddenFromProfile = false ";
			}
			
			query += "ORDER BY targetComptence1.title";
			
			return persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.list();
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
	}

}
