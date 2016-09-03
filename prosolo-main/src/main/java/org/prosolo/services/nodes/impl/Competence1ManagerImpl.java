package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
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
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.common.exception.CompetenceEmptyException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventData;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.LearningResourceReturnResultType;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.impl.util.EntityPublishTransition;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
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

	@Override
	@Transactional(readOnly = false)
	public Competence1 saveNewCompetence(CompetenceData1 data, long creatorId, long credentialId,
			LearningContextData context) throws DbConnectionException {
		Competence1 comp = null;
		try {
			/*
			 * if competence has no activities, it can't be published
			 */
			if(data.isPublished() && (data.getActivities() == null || data.getActivities().isEmpty())) {
				throw new CompetenceEmptyException();
			}
			Result<Competence1> res = resourceFactory.createCompetence(data.getTitle(), 
					data.getDescription(), data.getTagsString(), creatorId, 
					data.isStudentAllowedToAddActivities(), data.getType(), data.isPublished(), 
					data.getDuration(), data.getActivities(), credentialId);
			
			comp = res.getResult();
			
			/*
			 * generate events for event data returned
			 */
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			for(EventData ev : res.getEvents()) {
				ev.setPage(page);
				ev.setContext(lContext);
				ev.setService(service);
				eventFactory.generateEvent(ev);
			}

			if(data.isPublished()) {
				eventFactory.generateEvent(EventType.Create, creatorId, comp, null, page, lContext,
						service, null);
			} else {
				eventFactory.generateEvent(EventType.Create_Draft, creatorId, comp, null, page, lContext,
						service, null);
			}

			return comp;
		} catch(CompetenceEmptyException cee) {
			logger.error(cee);
			//cee.printStackTrace();
			throw cee;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competence");
		} 
	}

	@Override
	@Transactional(readOnly = false)
	public Competence1 deleteCompetence(long originalCompId, CompetenceData1 data, long userId) 
			throws DbConnectionException {
		try {
			if(originalCompId > 0) {
				Competence1 comp = (Competence1) persistence.currentManager()
						.load(Competence1.class, originalCompId);
				comp.setDeleted(true);
				
				if(data.isDraft()) {
					Competence1 draftVersion = (Competence1) persistence.currentManager()
							.load(Competence1.class, data.getCompetenceId());
					comp.setDraftVersion(null);
					delete(draftVersion);
				}
				
				deleteCompetenceActivities(originalCompId);
				
				deleteAllCredentialCompetencesForCompetence(comp.getId());
	
				if(data.isPublished() || data.isDraft()) {
					eventFactory.generateEvent(EventType.Delete, userId, comp);
				}
				
				return comp;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence");
		}
	}
	
	private void deleteAllCredentialCompetencesForCompetence(long compId) {
		Competence1 comp = (Competence1) persistence.currentManager()
				.load(Competence1.class, compId);
		//TODO check if comp is loaded or not because it exists in session
		long duration = comp.getDuration();
		
		String query = "SELECT credComp " +
			       	   "FROM CredentialCompetence1 credComp " + 
			       	   "WHERE credComp.competence = :comp";

		@SuppressWarnings("unchecked")
		List<CredentialCompetence1> res = persistence.currentManager()
			.createQuery(query)
			.setEntity("comp", comp)
			.list();
		
		if(res != null) {
			for(CredentialCompetence1 cc : res) {
				//credMap.put(cc.getCredential(), cc.getOrder());
				Credential1 cred = cc.getCredential();
				//TODO use hql update to avoid select + update
				cred.setDuration(cred.getDuration() - duration);
				shiftOrderOfCompetencesUp(cred.getId(), cc.getOrder());
				delete(cc);
			}
		}
		
	}

	private void shiftOrderOfCompetencesUp(long id, int order) {
		List<CredentialCompetence1> compsAfterDeleted = getAllCredentialCompetencesAfterSpecified(
				id, order);
		for(CredentialCompetence1 cc : compsAfterDeleted) {
			//TODO use hql update to avoid select + update
			cc.setOrder(cc.getOrder() - 1);
		}
	}

	private List<CredentialCompetence1> getAllCredentialCompetencesAfterSpecified(long credId, int order) {
		Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
		String query = "SELECT credComp " +
		       	   "FROM Credential1 cred " +
		       	   "LEFT JOIN cred.competences credComp " +
		       	   "WHERE cred = :cred " +
		       	   "AND credComp.order > :order";
		
		@SuppressWarnings("unchecked")
		List<CredentialCompetence1> res = persistence.currentManager()
			.createQuery(query)
			.setEntity("cred", cred)
			.setInteger("order", order)
			.list();
		
		if(res == null) {
			return new ArrayList<>();
		}
		
		return res;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getTargetCompetencesData(long targetCredentialId, boolean loadTags) 
			throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			//TODO add activity loading, maybe boolean flags loadCreator, loadActivities
			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
					TargetCredential1.class, targetCredentialId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT targetComp " +
				       	   "FROM TargetCompetence1 targetComp " + 
				       	   "INNER JOIN fetch targetComp.createdBy user ");
			if(loadTags) {
				builder.append("LEFT JOIN fetch comp.tags tags ");
			}
			builder.append("WHERE targetComp.targetCredential = :targetCred " +
				       	   "ORDER BY targetComp.order");
//			String query = "SELECT targetComp " +
//					       "FROM TargetCompetence1 targetComp " + 
//					       "INNER JOIN fetch targetComp.createdBy user " +
//					       "LEFT JOIN fetch comp.tags tags " +
//					       "WHERE targetComp.targetCredential = :targetCred " +
//					       "ORDER BY targetComp.order";

			@SuppressWarnings("unchecked")
			List<TargetCompetence1> res = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("targetCred", targetCred)
				.list();

			if (res != null) {
				for (TargetCompetence1 targetComp : res) {
					Set<Tag> tags = loadTags ? targetComp.getTags() : null;
					CompetenceData1 compData = competenceFactory.getCompetenceData(
							targetComp.getCreatedBy(), targetComp, tags, null, true);
					result.add(compData);
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
	@Transactional(readOnly = false)
	public List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred, User user) 
			throws DbConnectionException {
		try {
			List<CredentialCompetence1> credComps = getCredentialCompetences(credId, 
					false, true, false);
			List<TargetCompetence1> targetComps =  new ArrayList<>();
			for(CredentialCompetence1 cc : credComps) {
				TargetCompetence1 targetComp = createTargetCompetence(targetCred, cc, user);
				targetComps.add(targetComp);
			}
			return targetComps;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling competences");
		}
	}
	
	@Transactional(readOnly = false)
	private TargetCompetence1 createTargetCompetence(TargetCredential1 targetCred, 
			CredentialCompetence1 cc, User user) {
		TargetCompetence1 targetComp = new TargetCompetence1();
		Competence1 comp = cc.getCompetence();
		targetComp.setTitle(comp.getTitle());
		targetComp.setDescription(comp.getDescription());
		targetComp.setTargetCredential(targetCred);
		targetComp.setCompetence(comp);
		targetComp.setDuration(comp.getDuration());
		targetComp.setStudentAllowedToAddActivities(comp.isStudentAllowedToAddActivities());
		targetComp.setOrder(cc.getOrder());
		targetComp.setCreatedBy(user);
		targetComp.setType(comp.getType());
		
		if(comp.getTags() != null) {
			Set<Tag> tags = new HashSet<>();
			for(Tag tag : comp.getTags()) {
				tags.add(tag);
			}
			targetComp.setTags(tags);
		}
		saveEntity(targetComp);
		
		List<TargetActivity1> targetActivities = activityManager.createTargetActivities(
				comp.getId(), targetComp, user);
		targetComp.setTargetActivities(targetActivities);
		
		/*
		 * set first activity as next to learn
		 */
		if (!targetActivities.isEmpty())
			targetComp.setNextActivityToLearnId(targetActivities.get(0).getActivity().getId());
		return targetComp;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, long userId, LearningResourceReturnResultType returnType,
			boolean shouldTrackChanges) throws DbConnectionException {
		try {
			Competence1 comp = getCompetence(credId, compId, loadCreator, loadTags, userId, 
					returnType, false);
		    if(comp != null) {
				User creator = loadCreator ? comp.getCreatedBy() : null;
				Set<Tag> tags = loadTags ? comp.getTags() : null;
				
				CompetenceData1 compData = competenceFactory.getCompetenceData(
						creator, comp, tags, shouldTrackChanges);
				
				if(loadActivities) {
					List<ActivityData> activities = activityManager.getCompetenceActivitiesData(compId);
					compData.setActivities(activities);
				}
				
				return compData;
		    } 
		    return null;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceDataForUser(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, long userId, boolean shouldTrackChanges) 
					throws DbConnectionException {
			return getCompetenceData(credId, compId, loadCreator, loadTags, loadActivities, userId, 
					LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER, shouldTrackChanges);
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceDataForManager(long credId, long compId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean shouldTrackChanges) 
					throws DbConnectionException {
			return getCompetenceData(credId, compId, loadCreator, loadTags, loadActivities, 0, 
					LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER, shouldTrackChanges);
	}
	
	/**
	 * Returns competence with specified id. 
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER is passed for {@code returnType}
	 * parameter competence will be returned even if it is first time draft if creator of competence
	 * is user specified by {@code userId}.
	 * If LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER is passed for {@code returnType}
	 * parameter competence will be returned even if it is first time draft if competence is created by
	 * university.
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param userId
	 * @param returnType
	 * @return
	 */
	@Transactional(readOnly = true)
	private Competence1 getCompetence(long credId, long compId, boolean loadCreator, boolean loadTags,
			long userId, LearningResourceReturnResultType returnType, boolean returnIfDraftVersion) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT comp ");
		/* 
		 * if credential id is passed need to make sure that competence really is in credential with 
		 * specified id
		 */
		if(credId > 0) {
			builder.append("FROM CredentialCompetence1 credComp " +
						   "INNER JOIN credComp.competence comp " +
						   		"WITH comp.id = :compId " +
						   		"AND comp.deleted = :deleted ");
		} else {
			builder.append("FROM Competence1 comp ");
		}
//		builder.append("SELECT comp " + 
//					   "FROM Competence1 comp ");
		
		String condition = "AND (comp.published = :published OR  "
				+ "comp.hasDraft = :hasDraft ";
		switch(returnType) {
			case ANY:
				condition = "";
				break;
			case PUBLISHED_VERSION:
				condition += ") ";
				break;
			case FIRST_TIME_DRAFT_FOR_USER:
				condition += "OR comp.createdBy.id = :userId) ";
				break;
			case FIRST_TIME_DRAFT_FOR_MANAGER:
				condition += "OR comp.type = :type) ";
				break;
		}
		
		if(credId > 0) {
			if(!returnIfDraftVersion) {
				builder.append("AND comp.draft = :draft ");
			}
			builder.append(condition);
		}
		
		if(loadCreator) {
			builder.append("INNER JOIN fetch comp.createdBy user ");
		}
		if(loadTags) {
			builder.append("LEFT JOIN fetch comp.tags tags ");
		}
//		if(loadActivities) {
//			builder.append("LEFT JOIN fetch comp.activities compAct " +
//					       "INNER JOIN fetch compAct.activity act ");
//		}
		if(credId > 0) {
			builder.append("WHERE credComp.credential.id = :credId");
		} else {
			builder.append("WHERE comp.id = :compId " +
					   "AND comp.deleted = :deleted ");
			if(!returnIfDraftVersion) {
				builder.append("AND comp.draft = :draft ");
			}
			builder.append(condition);
		}
		
		logger.info("QUERY: " + builder.toString());
		Query q = persistence.currentManager()
			.createQuery(builder.toString())
			.setLong("compId", compId)
			.setBoolean("deleted", false);
		
		if(credId > 0) {
			q.setLong("credId", credId);
		}
		
		if(!returnIfDraftVersion) {
			q.setBoolean("draft", false);
		}
		if(returnType != LearningResourceReturnResultType.ANY) {
			q.setBoolean("published", true)
			 .setBoolean("hasDraft", true);
		}
		if(returnType == LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER) {
			q.setLong("userId", userId);
		} else if(returnType == LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_MANAGER) {
			q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
		}

		Competence1 res = (Competence1) q.uniqueResult();
		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceDataForEdit(long credId, long competenceId, long creatorId, 
			boolean loadActivities) throws DbConnectionException {
		return getCurrentVersionOfCompetenceBasedOnRole(credId, competenceId, creatorId, false, 
				loadActivities, Role.User);
	}
	
	@Transactional(readOnly = true)
	private CompetenceData1 getCurrentVersionOfCompetenceBasedOnRole(long credId, long competenceId, 
			long creatorId, boolean loadCreator, boolean loadActivities, Role role) 
					throws DbConnectionException {
		try {
			StringBuilder queryBuilder = new StringBuilder("SELECT comp ");
			if(credId > 0) {
				queryBuilder.append("FROM CredentialCompetence1 credComp " +
							   		"INNER JOIN credComp.competence comp " +
							   			"WITH comp.id = :compId " +
							   			"AND comp.deleted = :deleted " +
							   			"AND comp.draft = :draft ");
			} else {
				queryBuilder.append("FROM Competence1 comp ");
			}
			queryBuilder.append("LEFT JOIN fetch comp.tags tags ");

			if(loadCreator) {
				queryBuilder.append("INNER JOIN fetch comp.createdBy ");
			}
			
			//StringBuilder queryBuilder1 = new StringBuilder(queryBuilder.toString());
			if(credId > 0) {
				queryBuilder.append("WHERE credComp.credential.id IN (:credIds) ");
			} else {
				queryBuilder.append("WHERE comp.id = :compId " +
						"AND comp.deleted = :deleted " +
						"AND comp.draft = :draft ");
			}
			if(role == Role.User) {
				queryBuilder.append("AND comp.type = :type " +
									"AND comp.createdBy.id = :user");
			} else {
				queryBuilder.append("AND comp.type = :type ");
			}
						   
			Query q = persistence.currentManager()
					.createQuery(queryBuilder.toString())
					.setLong("compId", competenceId)
					.setBoolean("deleted", false)
					.setBoolean("draft", false);
			
			if(role == Role.User) {
				q.setParameter("type", LearningResourceType.USER_CREATED);
				q.setLong("user", creatorId);
			} else {
				q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
			}
			if(credId > 0) {
				List<Long> ids = new ArrayList<>();
				ids.add(credId);
				Optional<Long> draftVersionCredId = credentialManager
						.getDraftVersionIdIfExists(credId);
				if(draftVersionCredId.isPresent()) {
					ids.add(draftVersionCredId.get());
				}
				q.setParameterList("credIds", ids);
			}
			
			Competence1 res = (Competence1) q.uniqueResult();
			
			if(res != null) {
				CompetenceData1 cd = null;
				if(res.isHasDraft()) {
					StringBuilder queryBuilder1 = new StringBuilder();
					queryBuilder1.append("SELECT comp " +
							   "FROM Competence1 comp " + 
							   "LEFT JOIN fetch comp.tags tags ");

					if(loadCreator) {
						queryBuilder1.append("INNER JOIN fetch comp.createdBy ");
					}
					
					queryBuilder1.append("WHERE comp = :draftVersion "); 

					Competence1 draftComp = (Competence1) persistence.currentManager()
							.createQuery(queryBuilder1.toString())
							.setEntity("draftVersion", res.getDraftVersion())
							.uniqueResult();
					if(draftComp != null) {
						User creator = loadCreator ? draftComp.getCreatedBy() : null;
						cd = competenceFactory.getCompetenceData(creator, draftComp, 
								draftComp.getTags(), true);
					}	
				} else {
					User creator = loadCreator ? res.getCreatedBy() : null;
					cd = competenceFactory.getCompetenceData(creator, res, res.getTags(), true);
				}
				if(cd != null && loadActivities) {
					List<ActivityData> activities = activityManager
							.getCompetenceActivitiesData(cd.getCompetenceId());
					cd.setActivities(activities);
				}
				
				List<CredentialData> credentials = credentialManager
						.getCredentialsWithIncludedCompetenceBasicData(res.getId());
				cd.setCredentialsWithIncludedCompetence(credentials);
				return cd;
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
	public Competence1 updateCompetence(long originalCompId, CompetenceData1 data, long userId, 
			LearningContextData context) 
			throws DbConnectionException, CompetenceEmptyException {
		try {
			/*
			 * if competence has no activities (that are not removed), it can't be published
			 */
			if(data.isPublished()) {
				if(data.getActivities() == null) {
					throw new CompetenceEmptyException();
				}
				long numberOfActivities = data.getActivities().stream().filter(
						act -> act.getObjectStatus() != ObjectStatus.REMOVED).count();
				if(numberOfActivities == 0) {
					throw new CompetenceEmptyException();
				}
			}
			
			Competence1 updatedComp = resourceFactory.updateCompetence(data);		  
		    
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			if(data.isPublished()) {
				//competence remains published
				if(!data.isPublishedChanged()) {
					fireSameVersionCompEditEvent(data, userId, updatedComp, 0, page, lContext,
							service);
				} 
				/*
				 * this means that competence is published for the first time
				 */
				else if(!data.isDraft()) {
					EventData ev = fireFirstTimePublishCompEvent(userId, updatedComp);
					ev.setPage(page);
					ev.setContext(lContext);
					ev.setService(service);
					eventFactory.generateEvent(ev);
				}
				/*
				 * Competence becomes published again. Because data can show what has changed
				 * based on draft version, we can't use that. We need to know what has changed based on
				 * original competence, so all fields are treated as changed.
				 */
				else {
					EventData ev = fireCompPublishedAgainEditEvent(userId, updatedComp, 
							data.getCompetenceId());
					ev.setPage(page);
					ev.setContext(lContext);
					ev.setService(service);
					eventFactory.generateEvent(ev);
				}
			
			} else {
				/*
				 * if competence remains draft
				 */
				if(!data.isPublishedChanged()) {
					long originalVersionId = 0;
					if(data.isDraft()) {
						originalVersionId = originalCompId;
					}
					fireSameVersionCompEditEvent(data, userId, updatedComp, originalVersionId, page, 
							lContext, service);
				} 
				/*
				 * This means that competence was published before so draft version is created.
				 */
				else {
					EventData ev = fireDraftVersionCompCreatedEvent(updatedComp, userId, 
							data.getCompetenceId());
					ev.setPage(page);
					ev.setContext(lContext);
					ev.setService(service);
					eventFactory.generateEvent(ev);
				}
			}

		    return updatedComp;
		} catch(CompetenceEmptyException cee) {
			logger.error(cee);
			//cee.printStackTrace();
			throw cee;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence");
		}
	}
	
	private EventData fireFirstTimePublishCompEvent(long userId, Competence1 updatedComp) {
		EventData ev = new EventData();
		ev.setEventType(EventType.Create);
		ev.setActorId(userId);
		ev.setObject(updatedComp);
		return ev;
	}

	private void fireSameVersionCompEditEvent(CompetenceData1 data, long userId, 
			Competence1 updatedComp, long originalVersionId, String page, String context, String service) 
					throws EventException {
		Map<String, String> params = new HashMap<>();
	    CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
	    		false, data.isTitleChanged(), data.isDescriptionChanged(), false, 
	    		data.isTagsStringChanged(), data.isStudentAllowedToAddActivitiesChanged());
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    if(originalVersionId > 0) {
	    	params.put("originalVersionId", originalVersionId + "");
	    }
	    EventType event = data.isPublished() ? EventType.Edit : EventType.Edit_Draft;
	    eventFactory.generateEvent(event, userId, updatedComp, null, page, context, service, params);
	}
	
	private EventData fireCompPublishedAgainEditEvent(long userId, 
			Competence1 updatedComp, long draftVersionId) throws EventException {
		Map<String, String> params = new HashMap<>();
	    CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(true,
	    		true, true, true, true, true, true);
	    Gson gson = new GsonBuilder().create();
	    String jsonChangeTracker = gson.toJson(changeTracker);
	    params.put("changes", jsonChangeTracker);
	    params.put("draftVersionId", draftVersionId + "");
	    EventData ev = new EventData();
	    ev.setEventType(EventType.Edit);
	    ev.setActorId(userId);
	    ev.setObject(updatedComp);
	    ev.setParameters(params);
	    return ev;
	}
	
	private EventData fireDraftVersionCompCreatedEvent(Competence1 updatedComp, long userId, 
			long originalVersionId) throws EventException {
		Map<String, String> params = new HashMap<>();
		params.put("originalVersionId", originalVersionId + "");
		EventData event = new EventData();
		event.setEventType(EventType.Create_Draft);
		event.setActorId(userId);
		event.setObject(updatedComp);
		event.setParameters(params);
		return event;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Competence1 updateCompetence(CompetenceData1 data) throws DbConnectionException {
		Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
				data.getCompetenceId());
		/*
		 * draft should be created if something changed, draft option is chosen 
		 * and competence was published before this update
		*/
		EntityPublishTransition publishTransition = (!data.isPublished() && data.isPublishedChanged()) ? 
				EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION :
				EntityPublishTransition.NO_TRANSITION;
		/*
		 * check if published option was chosen, it was draft before update and 
		 * draft version (and not original competence) of competence is updated.
		 * Last check added because it is possible for original competence to be
		 * draft and that it doesn't have draft version because it was never published.
		*/
		if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
			publishTransition = (data.isPublished() && data.isPublishedChanged() && data.isDraft()) ? 
					publishTransition = EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED :
					publishTransition;
		}
		
		return updateCompetenceData(comp, publishTransition, data);		   
	}
	
	/**
	 * Updates all competence updatable fields and takes into
	 * account transitions between competence publish status
	 * so if status is changed from published to draft, draft
	 * version is inserted and original competence is referencing
	 * this version. If status is changed from draft to published,
	 * it is checked if competence has been published once and if 
	 * it has, that means that draft version exists and this version
	 * is deleted.
	 * @param comp should be persistent object
	 * @param publishTransition
	 * @param data
	 */
	private Competence1 updateCompetenceData(Competence1 comp, EntityPublishTransition publishTransition,
			CompetenceData1 data) {
		Competence1 compToUpdate = null;
		switch(publishTransition) {
			case FROM_PUBLISHED_TO_DRAFT_VERSION:
				compToUpdate = new Competence1();
				compToUpdate.setDraft(true);
				compToUpdate.setCreatedBy(comp.getCreatedBy());
				compToUpdate.setType(comp.getType());
				comp.setHasDraft(true);
				comp.setPublished(false);
				//TODO duration should probably be fetched from database to be sure that most recent duration is used
				compToUpdate.setDuration(getCompetenceDuration(comp.getId()));
				break;
			case FROM_DRAFT_VERSION_TO_PUBLISHED:
				compToUpdate = getOriginalCompetenceForDraft(comp.getId());
				compToUpdate.setHasDraft(false);
				compToUpdate.setDraftVersion(null);
		    	/*
		    	 * old duration is taken from original competence and new duration is
		    	 * fetched from db and not from data object because competence
		    	 * duration can be updated from other places (when activity is updated)
		    	 * and data object may not capture last changes
		    	 */
				//logger.info("DURATION " + comp.getDuration());
				long mostRecentDuration = comp.getDuration();
		    	updateCredentialsDuration(compToUpdate.getId(), mostRecentDuration, 
		    			compToUpdate.getDuration());
				compToUpdate.setDuration(mostRecentDuration);
		    	delete(comp);
		    	break;
			case NO_TRANSITION:
				compToUpdate = comp;
				break;
		}
		
		compToUpdate.setTitle(data.getTitle());
		compToUpdate.setDescription(data.getDescription());
		compToUpdate.setPublished(data.isPublished());
		compToUpdate.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
	    if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
	    	if(data.isTagsStringChanged()) {
	    		compToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    				data.getTagsString())));		     
	    	}
	    } else {
	    	compToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    			data.getTagsString())));
	    }
	    
		if(publishTransition == EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION) {
			saveEntity(compToUpdate);
			comp.setDraftVersion(compToUpdate);
		}
		
		if(publishTransition == EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED) {
			//delete original activities because we will add all activities from draft version
			deleteCompetenceActivities(compToUpdate.getId());
		}

		List<ActivityData> activities = data.getActivities();
	    if(activities != null) {
	    	/*
    		 * List of activity ids so we can call method that will publish all draft
    		 * activities
    		 */
    		List<Long> actIds = new ArrayList<>();
	    	if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
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
		    				actIds.add(bad.getActivityId());
		    				break;
		    			case CHANGED:
		    				CompetenceActivity1 ca2 = (CompetenceActivity1) persistence
		    					.currentManager().load(CompetenceActivity1.class, 
		    							bad.getCompetenceActivityId());
		    				ca2.setOrder(bad.getOrder());
		    				actIds.add(bad.getActivityId());
		    				break;
		    			case REMOVED:
		    				CompetenceActivity1 ca3 = (CompetenceActivity1) persistence.currentManager().load(
		    						CompetenceActivity1.class, bad.getCompetenceActivityId());
		    				delete(ca3);
		    				break;
		    			case UP_TO_DATE:
		    				actIds.add(bad.getActivityId());
		    				break;
		    		}
		    	}
	    	} else {
	    		Iterator<ActivityData> actIterator = activities.iterator();
	    		
	    		while(actIterator.hasNext()) {
	    			ActivityData bad = actIterator.next();
		    		if(bad.getObjectStatus() != ObjectStatus.REMOVED) {
		    			CompetenceActivity1 ca = new CompetenceActivity1();
	    				ca.setOrder(bad.getOrder());
	    				ca.setCompetence(compToUpdate);
	    				Activity1 act = (Activity1) persistence.currentManager().load(
	    						Activity1.class, bad.getActivityId());
	    				ca.setActivity(act);
	    				saveEntity(ca);
	    				actIds.add(bad.getActivityId());
		    		}
	    		}
	    	}
	    	if(data.isPublished()) {
	    		activityManager.publishDraftActivities(actIds);
	    	}
	    }
	    
	    return compToUpdate;
	}
	
	@Transactional(readOnly = true)
	public long getCompetenceDuration(long compId) throws DbConnectionException {  
		try {
			String query = "SELECT comp.duration " +
					   "FROM Competence1 comp " + 
					   "WHERE comp.id = :compId";
			
			Long duration = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
					.uniqueResult();
			
			return duration;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence duration");
		}
	}
	
	private void updateCredentialsDuration(long compId, long newDuration, long oldDuration) {
		long durationChange = newDuration - oldDuration;
    	Operation op = null;
    	if(durationChange > 0) {
    		op = Operation.Add;
    	} else {
    		durationChange = -durationChange;
    		op = Operation.Subtract;
    	}
		credentialManager.updateDurationForCredentialsWithCompetence(compId, 
    			durationChange, op);
		
	}

	private void deleteCompetenceActivities(long compId) throws DbConnectionException {
		try {
			String query = "DELETE CompetenceActivity1 compAct " +
			       	       "WHERE compAct.competence.id = :compId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.executeUpdate();
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting competence activities");
		}
	}

	@Transactional(readOnly = true)
	private Competence1 getOriginalCompetenceForDraft(long draftCompId) {
		try {
			Competence1 draftComp = (Competence1) persistence.currentManager().load(
					Competence1.class, draftCompId);
			String query = "SELECT comp " +
					   "FROM Competence1 comp " + 
					   "WHERE comp.draftVersion = :draft";
			
			Competence1 original = (Competence1) persistence.currentManager()
					.createQuery(query)
					.setEntity("draft", draftComp)
					.uniqueResult();
			return original;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence data");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished) 
					throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			List<CredentialCompetence1> res = getCredentialCompetences(credentialId, 
					loadCreator, loadTags, includeNotPublished);

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
			boolean loadTags, boolean includeNotPublished) 
					throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT distinct credComp " + 
						   "FROM CredentialCompetence1 credComp " +
						   "INNER JOIN fetch credComp.competence comp ");
			if(loadCreator) {
				builder.append("INNER JOIN fetch comp.createdBy user ");
			}
			if(loadTags) {
				builder.append("LEFT JOIN fetch comp.tags tags ");
			}
	
			builder.append("WHERE credComp.credential = :credential " + 
					   "AND comp.deleted = :deleted " + 
					   "AND comp.draft = :draft " +
					   "AND credComp.deleted = :deleted ");
			
			if(!includeNotPublished) {
				builder.append("AND (comp.published = :published OR (comp.published = :notPublished " +
						   	   "AND comp.hasDraft = :hasDraft)) ");
			}
			
			builder.append("ORDER BY credComp.order");

			Query query = persistence.currentManager()
					.createQuery(builder.toString())
					.setEntity("credential", cred)
					.setBoolean("deleted", false)
					.setBoolean("draft", false);
			if(!includeNotPublished) {
				query
					.setBoolean("published", true)
					.setBoolean("notPublished", false)
					.setBoolean("hasDraft", true);
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
	
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId) 
			throws DbConnectionException {
		return getCompetenceTags(compId, persistence.currentManager());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Tag> getCompetenceTags(long compId, Session session) 
			throws DbConnectionException {
		try {		
			//if left join is used list with null element would be returned.
			String query = "SELECT tag " +
					       "FROM Competence1 comp " +
					       "INNER JOIN comp.tags tag " +
					       "WHERE comp.id = :compId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = session
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
			throw new DbConnectionException("Error while loading competence tags");
		}
	}
			
	@Transactional(readOnly = false)
	private void updateTargetCompetenceBasicDataForUncompletedCredentials(long compId) 
			throws DbConnectionException {
		try {	
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
					compId);
			
			List<Long> ids = getTargetCompetenceIdsForUncompletedCredentials(compId);
			
			if(!ids.isEmpty()) {
				String query = "UPDATE TargetCompetence1 targetComp " +
						       "SET targetComp.title = :title, " +
						       "targetComp.description = :description, " +
						       "targetComp.studentAllowedToAddActivities = :studentAllowedToAddActivities " +
						       "WHERE targetComp.id IN (:compIds)";					    
	
				persistence.currentManager()
					.createQuery(query)
					.setString("title", comp.getTitle())
					.setString("description", comp.getDescription())
					.setBoolean("studentAllowedToAddActivities", comp.isStudentAllowedToAddActivities())
					.setParameterList("compIds", ids)
					.executeUpdate();
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user competences");
		}
	}
	
	@Transactional(readOnly = true)
	private List<Long> getTargetCompetenceIdsForUncompletedCredentials(long compId) {
		try {	
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
					compId);
			
			String query = "SELECT comp.id FROM TargetCompetence1 comp " +
				       	   "INNER JOIN comp.targetCredential cred " +
				       			"WITH cred.progress != :progress " +
				       	   "WHERE comp.competence = :comp";					    

			@SuppressWarnings("unchecked")
			List<Long> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("comp", comp)
				.setInteger("progress", 100)
				.list();
			
			if(res == null) {
				return new ArrayList<>();
			}
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence ids");
		}
	}

	@Transactional(readOnly = true)
	private List<TargetCompetence1> getTargetCompetencesForCompetence(long compId, 
			boolean justUncompleted) 
			throws DbConnectionException {
		try {		
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, 
					compId);
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT comp " +
				           "FROM TargetCompetence1 comp ");
			if(justUncompleted) {
				builder.append("INNER JOIN comp.targetCredential cred " +
			       		          "WITH cred.progress != :progress ");
			}
			builder.append("WHERE comp.competence = :comp");
//			String query = "SELECT comp " +
//					       "FROM TargetCompetence1 comp " +
//					       "INNER JOIN comp.targetCredential cred " +
//					       		"WITH cred.progress != :progress " +
//					       "WHERE comp.competence = :comp";					    
			Query q = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("comp", comp);
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
	
	@Transactional(readOnly = false)
	private void updateTargetCompetencesTags(long compId) 
			throws DbConnectionException {
		try {
			List<TargetCompetence1> targetComps = getTargetCompetencesForCompetence(compId, true);
			List<Tag> tags = getCompetenceTags(compId);
			for(TargetCompetence1 tc : targetComps) {
				tc.getTags().clear();
				for(Tag tag : tags) {
					tc.getTags().add(tag);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user competences");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCompetencesWithChangedData(long compId, CompetenceChangeTracker changeTracker) 
			throws DbConnectionException {
		if(changeTracker.isPublished()) {
			if(changeTracker.isTagsChanged()) {
				updateTargetCompetencesTags(compId);
			}
			if(changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged() 
					|| changeTracker.isStudentAllowedToAddActivitiesChanged()) {
				updateTargetCompetenceBasicDataForUncompletedCredentials(compId);
				
				/**
				 * not needed because if duration is changed here (when draft version
				 * of competence is published that means that new activities were added
				 * to competence and that should not be propagated
				 */
//				if(changeTracker.isDurationChanged()) {
//					recalculateTargetCompetencesAndCredentialsDuration(compId);
//				}
			}
		}
	}
	
//	@Transactional(readOnly = true)
//	private void recalculateTargetCompetencesAndCredentialsDuration(long compId) {
//		try {
//			String query1 = "SELECT cred.id " +
//					"FROM TargetCompetence1 comp " +
//					"INNER JOIN comp.targetCredential cred " +
//						"WITH cred.progress != :progress " +
//					"WHERE comp.competence.id = :compId";
//	
//			@SuppressWarnings("unchecked")
//			List<Long> credIds = persistence.currentManager()
//					.createQuery(query1)
//					.setInteger("progress", 100)
//					.setLong("compId", compId)
//					.list();
//			
//			if(!credIds.isEmpty()) {
//				String query2 = "SELECT cred.id, comp.duration " +
//							    "FROM TargetCredential1 cred " +
//							    "LEFT JOIN cred.targetCompetences comp " +
//							    "WHERE cred.id IN (:credIds) " +									    
//							    "ORDER BY cred.id";
//			
//				@SuppressWarnings("unchecked")
//				List<Object[]> res = persistence.currentManager()
//						.createQuery(query2)
//						.setParameterList("credIds", credIds)
//						.list();
//				
//				if(res != null) {
//					long currentCredId = 0;					
//					long cumulativeCredDuration = 0;
//					for(Object[] row : res) {
//						long credId = (long) row[0];
//						long duration = (long) row[1];
//						
//						if(credId == currentCredId) {
//							cumulativeCredDuration += duration;
//						} else {
//							if(currentCredId != 0) {
//								credentialManager.updateTargetCredentialDuration(currentCredId, 
//										cumulativeCredDuration);
//							}
//							currentCredId = credId;
//							cumulativeCredDuration = duration;
//						}				
//					}
//					if(currentCredId != 0) {
//						credentialManager.updateTargetCredentialDuration(currentCredId, 
//								cumulativeCredDuration);
//					}
//				}
//			}
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while recalculating duration");
//		}
//	}

//	@Deprecated
//	@Override
//	@Transactional(readOnly = false)
//	public void publishDraftCompetencesWithoutDraftVersion(List<Long> compIds) 
//			throws DbConnectionException {
//		try {
//			if(compIds == null || compIds.isEmpty()) {
//				return;
//			}
//			
//			String query = "UPDATE Competence1 comp " +
//						   "SET comp.published = :published " + 
//						   "WHERE comp.hasDraft = :hasDraft " +
//						   "AND comp.id IN :compIds";
//			persistence.currentManager()
//				.createQuery(query)
//				.setBoolean("published", true)
//				.setBoolean("hasDraft", false)
//				.setParameterList("compIds", compIds)
//				.executeUpdate();
//			
//			for(Long compId : compIds) {
//				activityManager.publishActivitiesFromCompetence(compId);
//			}
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while updating competences");
//		}
//	}
	
	@Override
	@Transactional(readOnly = false)
	public EventData addActivityToCompetence(long compId, Activity1 act, long userId) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(
					Competence1.class, compId);
			
			/*
			 * if competence has draft version, that version is loaded and if competence 
			 * is published draft version will be created and attached to original competence
			 */
			boolean draftVersionCreated = false;
			Competence1 draftComp = null;
			if(comp.isHasDraft()) {
				draftComp = comp.getDraftVersion();
			} else if(comp.isPublished()) {
				draftComp = createDraftVersionOfCompetence(compId);
				comp.setHasDraft(true);
				comp.setPublished(false);
				comp.setDraftVersion(draftComp);
				draftVersionCreated = true;
			}
			
			Competence1 compToUpdate = draftComp != null ? draftComp : comp;
			CompetenceActivity1 ca = new CompetenceActivity1();
			ca.setActivity(act);
			ca.setCompetence(compToUpdate);
			ca.setOrder(compToUpdate.getActivities().size() + 1);
			saveEntity(ca);
			/* 
			 * If duration of added activity is greater than 0,
			 * update competence duration
			*/
			if(act.getDuration() > 0) {
				/*
				 * if it is draft competence we know that draft version can't be
				 * attached to credential so there is no need to issue update to credential table.
				 */
				if(draftComp != null) {
					compToUpdate.setDuration(compToUpdate.getDuration() + act.getDuration());
				} else {
					updateDurationForCompetencesWithActivity(act.getId(), act.getDuration(), 
							Operation.Add);
				}
			}
			
			if(draftVersionCreated) {
				User user = new User();
				user.setId(userId);
				return fireDraftVersionCompCreatedEvent(compToUpdate, userId, 
						compId);
			}
			return null;
		} catch(Exception e) { 
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding activity to competence");
		}
		
	}
	
	private Competence1 createDraftVersionOfCompetence(long originalCompId) {
		Competence1 originalComp = getCompetence(0, originalCompId, false, true, 0, 
				LearningResourceReturnResultType.PUBLISHED_VERSION, false);
		
		Competence1 draftComp = new Competence1();
		draftComp.setDraft(true);
		draftComp.setPublished(false);
		draftComp.setCreatedBy(originalComp.getCreatedBy());
		draftComp.setTitle(originalComp.getTitle());
		draftComp.setDescription(originalComp.getDescription());
		draftComp.setStudentAllowedToAddActivities(originalComp.isStudentAllowedToAddActivities());
		draftComp.setDuration(originalComp.getDuration());
		draftComp.setType(originalComp.getType());
	    
		if(originalComp.getTags() != null) {
			for(Tag tag : originalComp.getTags()) {
				draftComp.getTags().add(tag);
			}
		}
	    
		saveEntity(draftComp);	

		List<CompetenceActivity1> activities = activityManager
				.getCompetenceActivities(originalCompId, true);
	    if(activities != null) {
    		for(CompetenceActivity1 ca : activities) {
    			CompetenceActivity1 ca1 = new CompetenceActivity1();
				ca1.setOrder(ca.getOrder());
				ca1.setCompetence(draftComp);
				ca1.setActivity(ca.getActivity());
				saveEntity(ca1);
				draftComp.getActivities().add(ca1);
    		}	
	    }
	    
		return draftComp;
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateDurationForCompetencesWithActivity(long actId, long duration, Operation op) 
			throws DbConnectionException {
		try {
			List<CompetenceData1> compIdsWithActivity = getCompetenceIdsAndVersionInfoWithActivity(actId);
			long originalVersionId = 0;
			List<Long> compIds = new ArrayList<>();
			for(CompetenceData1 cd : compIdsWithActivity) {
				if(!cd.isDraft()) {
					originalVersionId = cd.getCompetenceId();
				}
				compIds.add(cd.getCompetenceId());
			}
			if(!compIds.isEmpty()) {
				String opString = op == Operation.Add ? "+" : "-";
				String query = "UPDATE Competence1 comp SET " +
							   "comp.duration = comp.duration " + opString + " :duration " +
							   "WHERE comp.id IN (:compIds)";
				
				persistence.currentManager()
					.createQuery(query)
					.setLong("duration", duration)
					.setParameterList("compIds", compIds)
					.executeUpdate();
				
				if(originalVersionId > 0) {
					credentialManager.updateDurationForCredentialsWithCompetence(originalVersionId, 
							duration, op);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
	}
	
	private List<CompetenceData1> getCompetenceIdsAndVersionInfoWithActivity(long actId) {
		String query = "SELECT comp.id, comp.draft FROM CompetenceActivity1 cAct " +
				"INNER JOIN cAct.competence comp " +
				"WHERE cAct.activity.id = :actId";
		
		@SuppressWarnings("unchecked")
		List<Object[]> res = persistence.currentManager()
			.createQuery(query)
			.setLong("actId", actId)	
			.list();
		
		List<CompetenceData1> comps = new ArrayList<>();
		if(res != null) {
			for(Object[] row : res) {
				if(row != null) {
					long id = (long) row[0];
					boolean draft = (boolean) row[1];
					CompetenceData1 cd = new CompetenceData1(false);
					cd.setCompetenceId(id);
					cd.setDraft(draft);
					comps.add(cd);
				}
			}
		}
		
		return comps;
				
	}

	@Override
	@Transactional(readOnly = false)
	public void updateTargetCompetenceDuration(long id, long duration) throws DbConnectionException {
		try {
			String query = "UPDATE TargetCompetence1 comp SET " +
						   "comp.duration = :duration " +
						   "WHERE comp.id = :compId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("duration", duration)
				.setLong("compId", id)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCompetenceTitle(long id) throws DbConnectionException {
		try {
			String query = "SELECT comp.title " +
						   "FROM Competence1 comp " +
						   "WHERE comp.id = :compId";
			
			String title = (String) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", id)
				.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence title");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getTargetCompetenceTitle(long targetCompId) throws DbConnectionException {
		try {
			String query = "SELECT comp.title " +
						   "FROM TargetCompetence1 comp " +
						   "WHERE comp.id = :compId";
			
			String title = (String) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", targetCompId)
				.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence title");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getCompetenceDraftOrOriginalTitle(long id) throws DbConnectionException {
		try {
			String query = "SELECT coalesce(draftComp.title, comp.title) " +
						   "FROM Competence1 comp " +
						   "LEFT JOIN comp.draftVersion draftComp " +
						   "WHERE comp.id = :compId";
			
			String title = (String) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", id)
				.uniqueResult();
			
			return title;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence title");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateProgressForTargetCompetenceWithActivity(long targetActId) 
			throws DbConnectionException {
		try {
			String query1 = "SELECT comp.id " +
							"FROM TargetActivity1 act " +
							"INNER JOIN act.targetCompetence comp " +
							"WHERE act.id = :actId";
			
			Long targetCompId =  (Long) persistence.currentManager()
					.createQuery(query1)
					.setLong("actId", targetActId)
					.uniqueResult();
			
			TargetCompetence1 targetComp = (TargetCompetence1) persistence.currentManager()
					.load(TargetCompetence1.class, targetCompId);
			
			String query = "SELECT CASE act.completed " +
							   "WHEN true then 100 " +
							   "else 0 " + 
							   "END " +
						   "FROM TargetActivity1 act " +
						   "WHERE act.targetCompetence = :comp";
			
			@SuppressWarnings("unchecked")
			List<Integer> res =  persistence.currentManager()
				.createQuery(query)
				.setEntity("comp", targetComp)
				.list();
			
			if(res != null) {
				int cumulativeProgress = 0;
				for(Integer p : res) {
					cumulativeProgress += p.intValue();
				}
				int newProgress = cumulativeProgress / res.size();
				targetComp.setProgress(newProgress); 
				persistence.currentManager().flush();
				
				credentialManager.updateProgressForTargetCredentialWithCompetence(targetCompId);
			}
			
//			String query = "UPDATE TargetCompetence1 comp SET " +
//			   "comp.progress = comp.progress + :progress / " +
//			   		"(SELECT count(act.id) FROM TargetActivity1 act " +
//			   		"WHERE act.targetCompetence = comp) " +
//			   "WHERE comp.id = :compId";
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence progress");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getFullTargetCompetenceOrCompetenceData(long credId, long compId, 
			long userId) throws DbConnectionException {
		CompetenceData1 compData = null;
		try {
			compData = getTargetCompetenceData(credId, compId, userId, true, true);
			if (compData == null) {
//				compData = getCompetenceData(compId, true, true, true, userId,
//						LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER, true);
				compData = getCompetenceDataForUser(credId, compId, true, true, true, userId, true);
			}
				
			return compData;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	/**
	 * Returns full target competence data when id of a target competence is not known.
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
			boolean loadActivities, boolean loadCredentialTitle) throws DbConnectionException {
		CompetenceData1 compData = null;
		try {		
			String query = "SELECT targetComp " +
					   "FROM TargetCompetence1 targetComp " +
					   "INNER JOIN targetComp.targetCredential targetCred " +
					   		"WITH targetCred.credential.id = :credId " +
					   		"AND targetCred.user.id = :userId " +
				   	   "INNER JOIN fetch targetComp.createdBy user " + 
					   "LEFT JOIN fetch targetComp.tags tags " +
				   	   "WHERE targetComp.competence.id = :compId";


			TargetCompetence1 res = (TargetCompetence1) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("credId", credId)
					.setLong("compId", compId)
					.uniqueResult();

			if (res != null) {
				compData = competenceFactory.getCompetenceData(res.getCreatedBy(), res, 
						res.getTags(), null, true);
				
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
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCurrentVersionOfCompetenceForManager(long credId, long competenceId,
			boolean loadCreator, boolean loadActivities) throws DbConnectionException {
			return getCurrentVersionOfCompetenceBasedOnRole(credId, competenceId, 0, loadCreator, 
					loadActivities, Role.Manager);
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> publishCompetences(List<Long> compIds, long creatorId, Role role) 
			throws DbConnectionException, CompetenceEmptyException {
		try {
			//get all draft competences
			List<EventData> events = new ArrayList<>();
			List<Competence1> comps = getDraftCompetencesFromList(compIds, creatorId, role);
			//iterate through list and if competence does not have draft version set to published, 
			//if it has, copy data from draft version to original and set published to true
			User user = new User();
			user.setId(creatorId);
			for(Competence1 c : comps) {
				if(c.isHasDraft()) {
					Competence1 draftC = getCompetence(0, c.getDraftVersion().getId(), false, true, 
							0, LearningResourceReturnResultType.ANY, true);
					long draftCompId = draftC.getId();
					publishDraftVersion(c, draftC);
					events.add(fireCompPublishedAgainEditEvent(creatorId, c, draftCompId));
				} else {
					c.setPublished(true);
					events.add(fireFirstTimePublishCompEvent(creatorId, c));
				}
				/*
				 * check if competence has at least one activity - if not, it can't be published
				 */
				int numberOfActivities = c.getActivities().size();
				if(numberOfActivities == 0) {
					throw new CompetenceEmptyException();
				}
			}
			
			List<EventData> actEvents = activityManager.publishActivitiesFromCompetences(compIds);
			events.addAll(actEvents);
			return events;
		} catch(CompetenceEmptyException cee) {
			logger.error(cee);
			//cee.printStackTrace();
			throw cee;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while publishing competences");
		}
	}
	
	/**
	 * Return all draft competences that satisfy condition:
	 * for user role if competence creator id equals {@code userId},
	 * for manager role if competence is created by university
	 * @param compIds
	 * @param userId
	 * @param role
	 * @return
	 */
	@Transactional(readOnly = true)
	private List<Competence1> getDraftCompetencesFromList(List<Long> compIds, long userId, Role role) {
		StringBuilder queryB = new StringBuilder("SELECT comp FROM Competence1 comp " +
					   "WHERE comp.id IN (:ids) " +
					   "AND comp.published = :published ");
		
		String cond = "";
		if(role == Role.Manager) {
			cond = "AND comp.type = :type ";
		} else if(role == Role.User) {
			cond = "AND comp.createdBy.id = :userId ";
		}
		
		queryB.append(cond);
		
		Query q = persistence.currentManager()
				.createQuery(queryB.toString())
				.setParameterList("ids", compIds)
				.setBoolean("published", false);
		
		if(role == Role.Manager) {
			q.setParameter("type", LearningResourceType.UNIVERSITY_CREATED);
		} else if(role == Role.User) {
			q.setLong("userId", userId);
		}
		
		@SuppressWarnings("unchecked")
		List<Competence1> comps = q.list();
		if(comps == null) {
			return new ArrayList<>();
		}
		return comps;
	}
	
	private Competence1 publishDraftVersion(Competence1 originalComp, Competence1 draftComp) {
		originalComp.setTitle(draftComp.getTitle());
		originalComp.setDescription(draftComp.getDescription());
		originalComp.setStudentAllowedToAddActivities(draftComp.isStudentAllowedToAddActivities());
		originalComp.setDuration(draftComp.getDuration());
	    
		originalComp.getTags().clear();
		if(draftComp.getTags() != null) {
			for(Tag tag : draftComp.getTags()) {
				originalComp.getTags().add(tag);
			}
		}	

		List<CompetenceActivity1> activities = activityManager
				.getCompetenceActivities(draftComp.getId(), true);
		deleteCompetenceActivities(originalComp.getId());
	    if(activities != null) {
    		for(CompetenceActivity1 ca : activities) {
    			CompetenceActivity1 ca1 = new CompetenceActivity1();
				ca1.setOrder(ca.getOrder());
				ca1.setCompetence(originalComp);
				ca1.setActivity(ca.getActivity());
				saveEntity(ca1);
				originalComp.getActivities().add(ca1);
    		}	
	    }
	    
	    originalComp.setHasDraft(false);
		originalComp.setDraftVersion(null);
		originalComp.setPublished(true);
    	delete(draftComp);
    	/*
    	 * old duration is taken from original competence and new duration is
    	 * taken from draft version
    	 */
    	updateCredentialsDuration(originalComp.getId(), draftComp.getDuration(), 
    			originalComp.getDuration());
	    
		return originalComp;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Optional<Long> getCompetenceDraftVersionIdForOriginal(long competenceId) throws DbConnectionException {
		try {
			String query = "SELECT comp.draftVersion.id " +
					"FROM Competence1 comp " +
					"WHERE comp.id = :compId";
			
			Long draftVersionId = (Long) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", competenceId)
					.uniqueResult();
			if(draftVersionId == null) {
				return Optional.empty();
			}
			return Optional.of(draftVersionId);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence id");
		}
	}
	
	@SuppressWarnings({ "unchecked"})
	@Transactional
	@Override
	public List<TargetCompetence1> getAllCompletedCompetences(Long userId, boolean hiddenFromProfile) throws DbConnectionException {
		List<TargetCompetence1> result = new ArrayList<>();
		List<Long> listOfCredentialIds = new ArrayList<>();
		try {
			String query;
			query = "SELECT targetCredential1.id " +
					"FROM TargetCredential1  targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId ";
			
			listOfCredentialIds = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
				  	.list();
			
			query =
					"SELECT targetComptence1 " +
					"FROM TargetCompetence1 targetComptence1 " +
					"WHERE targetComptence1.targetCredential.id in (:listOfCredentialIds) " + 
				    "AND targetComptence1.progress = :progress "+ 
					"AND targetComptence1.hiddenFromProfile = :hiddenFromProfile";
			
			if(!listOfCredentialIds.isEmpty()) {
				result = persistence.currentManager()
						.createQuery(query)
						.setParameterList("listOfCredentialIds", listOfCredentialIds)
						.setInteger("progress", 100)
						.setBoolean("hiddenFromProfile", hiddenFromProfile)
					  	.list();
			}
			  	
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked"})
	@Transactional
	@Override
	public List<TargetCompetence1> getAllUnfinishedCompetences(Long userId, boolean hiddenFromProfile)
			throws DbConnectionException {
		List<TargetCompetence1> result = new ArrayList<>();
		List<Long> listOfCredentialIds = new ArrayList<>();
		try {
			String query;
			query = "SELECT targetCredential1.id " +
					"FROM TargetCredential1  targetCredential1 " + 
					"WHERE targetCredential1.user.id = :userId ";
			
			listOfCredentialIds = persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
				  	.list();
			
			query =
					"SELECT targetComptence1 " +
					"FROM TargetCompetence1 targetComptence1 " +
					"WHERE targetComptence1.targetCredential.id in (:listOfCredentialIds) " + 
				    "AND targetComptence1.progress != 100 "+ 
					"AND targetComptence1.hiddenFromProfile = :hiddenFromProfile";
			
			if(!listOfCredentialIds.isEmpty()) {
				result = persistence.currentManager()
						.createQuery(query)
						.setParameterList("listOfCredentialIds", listOfCredentialIds)
						.setBoolean("hiddenFromProfile", hiddenFromProfile)
					  	.list();
			}
			  	
		} catch (DbConnectionException e) {
			e.printStackTrace();
			throw new DbConnectionException();
		}
		return result;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Competence1> getAllCompetencesWithTheirDraftVersions(Session session) 
			throws DbConnectionException {
		try {
			String query=
					"SELECT comp " +
					"FROM Competence1 comp " +
					"LEFT JOIN fetch comp.draftVersion " +
					"WHERE comp.deleted = :deleted " +
					"AND comp.draft = :draft";
			  	
			@SuppressWarnings("unchecked")
			List<Competence1> result = session
					.createQuery(query)
					.setBoolean("deleted", false)
					.setBoolean("draft", false)
				  	.list();
			
			if(result == null) {
				return new ArrayList<>();
			}
			return result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competences");
		}
	}
	
//	@Override
//	@Transactional(readOnly = true)
//	public CompetenceData1 getCompetenceForManager(long competenceId, boolean loadCreator, 
//			boolean loadActivities, Mode mode) throws DbConnectionException {
//		try {
//			StringBuilder queryBuilder = new StringBuilder();
//			queryBuilder.append("SELECT comp " +
//					   "FROM Competence1 comp " + 
//					   "LEFT JOIN fetch comp.tags tags ");
//
//			if(loadCreator) {
//				queryBuilder.append("INNER JOIN fetch comp.createdBy ");
//			}
//			
//			StringBuilder queryBuilder1 = new StringBuilder(queryBuilder.toString());
//			queryBuilder1.append("WHERE comp.id = :compId " +
//					"AND comp.deleted = :deleted " +
//					"AND comp.draft = :draft ");
//			if(mode == Mode.Edit) {
//				queryBuilder1.append("AND comp.type = :type ");
//			} else {
//				queryBuilder1.append("AND (comp.type = :type  OR (comp.published = :published " +
//					"OR comp.hasDraft = :hasDraft))");
//			}
//						   
//			Query q = persistence.currentManager()
//					.createQuery(queryBuilder1.toString())
//					.setLong("compId", competenceId)
//					.setBoolean("deleted", false)
//					.setParameter("type", LearningResourceType.UNIVERSITY_CREATED)
//					.setBoolean("draft", false);
//			
//			if(mode == Mode.View) {
//				q.setBoolean("published", true)
//				 .setBoolean("hasDraft", true);
//			}
//			
//			Competence1 res = (Competence1) q.uniqueResult();
//			
//			if(res != null) {
//				CompetenceData1 cd = null;
//				if(res.isHasDraft() && (mode == Mode.Edit  || (mode == Mode.View 
//						&& res.getType() == LearningResourceType.UNIVERSITY_CREATED))) {
//					String query2 = queryBuilder.toString() + 
//							" WHERE comp = :draftVersion";
//					Competence1 draftComp = (Competence1) persistence.currentManager()
//							.createQuery(query2)
//							.setEntity("draftVersion", res.getDraftVersion())
//							.uniqueResult();
//					if(draftComp != null) {
//						User creator = loadCreator ? draftComp.getCreatedBy() : null;
//						cd = competenceFactory.getCompetenceData(creator, draftComp, 
//								draftComp.getTags(), true);
//					}	
//				} else {
//					User creator = loadCreator ? res.getCreatedBy() : null;
//					cd = competenceFactory.getCompetenceData(creator, res, res.getTags(), true);
//				}
//				if(cd != null && loadActivities) {
//					List<ActivityData> activities = activityManager
//							.getCompetenceActivitiesData(cd.getCompetenceId());
//					cd.setActivities(activities);
//				}
//				
//				List<CredentialData> credentials = credentialManager
//						.getCredentialsWithIncludedCompetenceBasicData(res.getId());
//				cd.setCredentialsWithIncludedCompetence(credentials);
//				return cd;
//			}
//			
//			return null;
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading competence data");
//		}
//	}
}
