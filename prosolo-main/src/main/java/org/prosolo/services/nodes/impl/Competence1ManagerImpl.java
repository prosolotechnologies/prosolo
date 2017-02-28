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
import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
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
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
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
	@Inject
	private UserGroupManager userGroupManager;

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

			
			eventFactory.generateEvent(EventType.Create, creatorId, comp, null, page, lContext,
					service, null);
			
//			if((data.getStatus() == PublishedStatus.SCHEDULED_PUBLISH 
//					|| data.getStatus() == PublishedStatus.SCHEDULED_UNPUBLISH) 
//					&& data.getScheduledPublicDate() != null) {
//				eventFactory.generateEvent(EventType.SCHEDULED_VISIBILITY_UPDATE, creatorId, comp, null, page, lContext, 
//						service, null);
//			}

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
	public Competence1 deleteCompetence(CompetenceData1 data, long userId) 
			throws DbConnectionException {
		try {
			if(data.getCompetenceId() > 0) {
				Competence1 comp = (Competence1) persistence.currentManager()
						.load(Competence1.class, data.getCompetenceId());
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
	
	private void deleteAllCredentialCompetencesForCompetence(long compId) {
		Competence1 comp = (Competence1) persistence.currentManager()
				.load(Competence1.class, compId);
		long duration = comp.getDuration();

		List<CredentialCompetence1> res = getAllCredentialCompetencesForCompetence(compId);
		boolean published = comp.isPublished();
		for(CredentialCompetence1 cc : res) {
			//credMap.put(cc.getCredential(), cc.getOrder());
			Credential1 cred = cc.getCredential();
			//TODO use hql update to avoid select + update
			/*
			 * We should update credentials duration only if competence was published. 
			 * If it was not published competence duration was not event added to credential
			 * duration.
			 */
			if(published) {
				cred.setDuration(cred.getDuration() - duration);
			}
			shiftOrderOfCompetencesUp(cred.getId(), cc.getOrder());
			delete(cc);
		}
		
	}
	
	private List<CredentialCompetence1> getAllCredentialCompetencesForCompetence(long compId) {
		Competence1 comp = (Competence1) persistence.currentManager()
				.load(Competence1.class, compId);
		String query = "SELECT credComp " +
		       	   "FROM CredentialCompetence1 credComp " + 
		       	   "WHERE credComp.competence = :comp";

		@SuppressWarnings("unchecked")
		List<CredentialCompetence1> res = persistence.currentManager()
			.createQuery(query)
			.setEntity("comp", comp)
			.list();
		
		return res != null ? res : new ArrayList<>();
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
			//TODO cred-redesign-07
//			//TODO add activity loading, maybe boolean flags loadCreator, loadActivities
//			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
//					TargetCredential1.class, targetCredentialId);
//			StringBuilder builder = new StringBuilder();
//			builder.append("SELECT targetComp " +
//				       	   "FROM TargetCompetence1 targetComp " + 
//				       	   "INNER JOIN fetch targetComp.createdBy user ");
//			if(loadTags) {
//				builder.append("LEFT JOIN fetch comp.tags tags ");
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
	@Transactional(readOnly = false)
	public List<TargetCompetence1> createTargetCompetences(long credId, TargetCredential1 targetCred) 
			throws DbConnectionException {
		try {
			List<CredentialCompetence1> credComps = getCredentialCompetences(credId, 
					false, true, false);
			List<TargetCompetence1> targetComps =  new ArrayList<>();
			for(CredentialCompetence1 cc : credComps) {
				TargetCompetence1 targetComp = createTargetCompetence(targetCred, cc);
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
			CredentialCompetence1 cc) {
		//TODO cred-redesign-07
//		TargetCompetence1 targetComp = new TargetCompetence1();
//		Competence1 comp = cc.getCompetence();
//		targetComp.setTitle(comp.getTitle());
//		targetComp.setDescription(comp.getDescription());
//		targetComp.setTargetCredential(targetCred);
//		targetComp.setCompetence(comp);
//		targetComp.setDuration(comp.getDuration());
//		targetComp.setStudentAllowedToAddActivities(comp.isStudentAllowedToAddActivities());
//		targetComp.setOrder(cc.getOrder());
//		targetComp.setCreatedBy(comp.getCreatedBy());
//		targetComp.setType(comp.getType());
//		
//		if(comp.getTags() != null) {
//			Set<Tag> tags = new HashSet<>();
//			for(Tag tag : comp.getTags()) {
//				tags.add(tag);
//			}
//			targetComp.setTags(tags);
//		}
//		saveEntity(targetComp);
//		
//		List<TargetActivity1> targetActivities = activityManager.createTargetActivities(
//				comp.getId(), targetComp);
//		targetComp.setTargetActivities(targetActivities);
//		
//		/*
//		 * set first activity as next to learn
//		 */
//		if (!targetActivities.isEmpty()) {
//			targetComp.setNextActivityToLearnId(targetActivities.get(0).getActivity().getId());
//		}
//		return targetComp;
		return null;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long credId, long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, long userId, UserGroupPrivilege privilege,
			boolean shouldTrackChanges) throws ResourceNotFoundException, IllegalArgumentException, DbConnectionException {
		try {
			if(privilege == null) {
				throw new IllegalArgumentException();
			}
			Competence1 comp = getCompetence(credId, compId, loadCreator, loadTags, userId);
			
			if(comp == null) {
				throw new ResourceNotFoundException();
			}
			
			UserGroupPrivilege priv = getUserPrivilegeForCompetence(credId, compId, userId);
			/*
			 * user can access competence:
			 *  - when he has the right privilege and
			 *  - when competence is published if user has View privilege
			 */
			boolean canAccess = privilege.isPrivilegeIncluded(priv);
			if(canAccess && priv == UserGroupPrivilege.Learn && !comp.isPublished()) {
				canAccess = false;
			}
			
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, tags, shouldTrackChanges);
			compData.setCanEdit(priv == UserGroupPrivilege.Edit);
			compData.setCanAccess(canAccess);
			
			if(loadActivities) {
				/*
				 * we should include not published activities if Edit privilege is needed
				 * for this use case and user has Edit privilege, or if None privilege is needed
				 */
				boolean includeNotPublished = privilege == UserGroupPrivilege.Edit 
						&& priv == UserGroupPrivilege.Edit 
						|| privilege == UserGroupPrivilege.None;
				List<ActivityData> activities = activityManager.getCompetenceActivitiesData(compId,
						includeNotPublished);
				compData.setActivities(activities);
			}
			
			return compData;
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
	
	/**
	 * @param credId
	 * @param compId
	 * @param loadCreator
	 * @param loadTags
	 * @param userId
	 * @return
	 */
	@Transactional(readOnly = true)
	private Competence1 getCompetence(long credId, long compId, boolean loadCreator, boolean loadTags,
			long userId) {
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
		}
		
		logger.info("QUERY: " + builder.toString());
		Query q = persistence.currentManager()
			.createQuery(builder.toString())
			.setLong("compId", compId)
			.setBoolean("deleted", false);
		
		if(credId > 0) {
			q.setLong("credId", credId);
		}

		Competence1 res = (Competence1) q.uniqueResult();
		return res;
	}
	
	@Override
	@Transactional(readOnly = false)
	public Competence1 updateCompetence(CompetenceData1 data, long userId, 
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
			
			Competence1 updatedComp = resourceFactory.updateCompetence(data, userId);		  
		    
			String page = context != null ? context.getPage() : null; 
			String lContext = context != null ? context.getLearningContext() : null; 
			String service = context != null ? context.getService() : null; 
			
			fireCompEditEvent(data, userId, updatedComp, page, lContext, service);

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

	private void fireCompEditEvent(CompetenceData1 data, long userId, 
			Competence1 updatedComp, String page, String context, String service) 
					throws EventException {
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
	@Transactional(readOnly = false)
	public Competence1 updateCompetenceData(CompetenceData1 data, long userId) {
		Competence1 compToUpdate = (Competence1) persistence.currentManager()
				.load(Competence1.class, data.getCompetenceId());
		
		compToUpdate.setTitle(data.getTitle());
		compToUpdate.setDescription(data.getDescription());
		compToUpdate.setPublished(data.isPublished());
		compToUpdate.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
    	if(data.isTagsStringChanged()) {
    		compToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
    				data.getTagsString())));		     
    	}

		List<ActivityData> activities = data.getActivities();
	    if(activities != null) {
	    	/*
    		 * List of activity ids so we can call method that will publish all draft
    		 * activities
    		 */
    		List<Long> actIds = new ArrayList<>();
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
	    
	    	if(data.isPublished()) {
	    		activityManager.publishDraftActivities(0, userId, actIds);
//	    		updateDurationForAllCredentialsWithCompetence(data.getCompetenceId(), 
//	    				Operation.Add, compToUpdate.getDuration());
	    		persistence.currentManager().flush();
	    		long oldDuration = compToUpdate.getDuration();
	    		long newDuration = getRecalculatedDuration(compToUpdate.getId());
	    		compToUpdate.setDuration(newDuration);
	    		if(data.isPublishedChanged()) {
	    			oldDuration = 0;
	    		}
	    		updateCredDuration(compToUpdate.getId(), newDuration, oldDuration);
//	    		credentialManager.updateDurationForCredentialsWithCompetence(data.getCompetenceId(), 
//	    				compToUpdate.getDuration(), Operation.Add);
	    	}
	    	if(!data.isPublished() && data.isPublishedChanged()) {
//	    		updateDurationForAllCredentialsWithCompetence(data.getCompetenceId(), 
//	    				Operation.Subtract, compToUpdate.getDuration());
	    		credentialManager.updateDurationForCredentialsWithCompetence(data.getCompetenceId(), 
	    				compToUpdate.getDuration(), Operation.Subtract);
	    	}
	    }
	    
	    return compToUpdate;
	}
	
	private void updateCredDuration(long compId, long newDuration, long oldDuration) {
		long durationChange = newDuration - oldDuration;
    	Operation op = null;
    	if(durationChange == 0) {
    		return;
    	}
    	if(durationChange > 0) {
    		op = Operation.Add;
    	} else {
    		durationChange = -durationChange;
    		op = Operation.Subtract;
    	}
    	credentialManager.updateDurationForCredentialsWithCompetence(compId, 
				durationChange, op);
	}
	
//	@Transactional(readOnly = true)
//	public long getCompetenceDuration(long compId) throws DbConnectionException {  
//		try {
//			String query = "SELECT comp.duration " +
//					   "FROM Competence1 comp " + 
//					   "WHERE comp.id = :compId";
//			
//			Long duration = (Long) persistence.currentManager()
//					.createQuery(query)
//					.setLong("compId", compId)
//					.uniqueResult();
//			
//			return duration;
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while retrieving competence duration");
//		}
//	}
	
//	private void updateCredentialsDuration(long compId, long newDuration, long oldDuration) {
//		long durationChange = newDuration - oldDuration;
//    	Operation op = null;
//    	if(durationChange > 0) {
//    		op = Operation.Add;
//    	} else {
//    		durationChange = -durationChange;
//    		op = Operation.Subtract;
//    	}
//		credentialManager.updateDurationForCredentialsWithCompetence(compId, 
//    			durationChange, op);
//		
//	}

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
	
	@Override
	@Transactional(readOnly = true)
	public List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities, boolean includeNotPublished, 
			boolean includeCanEdit, long userId) 
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
				
				if(includeCanEdit) {
					UserGroupPrivilege priv = getUserPrivilegeForCompetence(credentialId,
							credComp.getCompetence().getId(), userId);
					if(priv == UserGroupPrivilege.Edit) {
						compData.setCanEdit(true);
					}
				}
				
				if(loadActivities) {
					List<ActivityData> activities = activityManager.getCompetenceActivitiesData(
							credComp.getCompetence().getId(), includeNotPublished);
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
					   "AND credComp.deleted = :deleted ");
			
			if(!includeNotPublished) {
				builder.append("AND comp.published = :published ");
			}
			
			builder.append("ORDER BY credComp.order");

			Query query = persistence.currentManager()
					.createQuery(builder.toString())
					.setEntity("credential", cred)
					.setBoolean("deleted", false);
			if(!includeNotPublished) {
				query.setBoolean("published", true);
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
			
			CompetenceActivity1 ca = new CompetenceActivity1();
			ca.setActivity(act);
			ca.setCompetence(comp);
			ca.setOrder(comp.getActivities().size() + 1);
			saveEntity(ca);
			/* 
			 * If duration of added activity is greater than 0 and activity is published
			 * update competence duration
			*/
			if(act.getDuration() > 0 && act.isPublished()) {
				comp.setDuration(comp.getDuration() + act.getDuration());
				credentialManager.updateDurationForCredentialsWithCompetence(compId, 
						act.getDuration(), Operation.Add);
			}
			
			return null;
		} catch(Exception e) { 
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding activity to competence");
		}
		
	}
	
//	private Competence1 createDraftVersionOfCompetence(long originalCompId) {
//		Competence1 originalComp = getCompetence(0, originalCompId, false, true, 0, 
//				LearningResourceReturnResultType.PUBLISHED_VERSION, false);
//		
//		Competence1 draftComp = new Competence1();
//		draftComp.setDraft(true);
//		draftComp.setPublished(false);
//		draftComp.setCreatedBy(originalComp.getCreatedBy());
//		draftComp.setTitle(originalComp.getTitle());
//		draftComp.setDescription(originalComp.getDescription());
//		draftComp.setStudentAllowedToAddActivities(originalComp.isStudentAllowedToAddActivities());
//		draftComp.setDuration(originalComp.getDuration());
//		draftComp.setType(originalComp.getType());
//	    
//		if(originalComp.getTags() != null) {
//			for(Tag tag : originalComp.getTags()) {
//				draftComp.getTags().add(tag);
//			}
//		}
//	    
//		saveEntity(draftComp);	
//
//		List<CompetenceActivity1> activities = activityManager
//				.getCompetenceActivities(originalCompId, true);
//	    if(activities != null) {
//    		for(CompetenceActivity1 ca : activities) {
//    			CompetenceActivity1 ca1 = new CompetenceActivity1();
//				ca1.setOrder(ca.getOrder());
//				ca1.setCompetence(draftComp);
//				ca1.setActivity(ca.getActivity());
//				saveEntity(ca1);
//				draftComp.getActivities().add(ca1);
//    		}	
//	    }
//	    
//		return draftComp;
//	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateDurationForCompetenceWithActivity(long actId, long duration, Operation op) 
			throws DbConnectionException {
		try {
			Optional<Long> compId = getCompetenceIdForActivity(actId);
			
			if(compId.isPresent()) {
				long competenceId = compId.get();
				String opString = op == Operation.Add ? "+" : "-";
				String query = "UPDATE Competence1 comp SET " +
							   "comp.duration = comp.duration " + opString + " :duration " +
							   "WHERE comp.id = :compId";
				
				persistence.currentManager()
					.createQuery(query)
					.setLong("duration", duration)
					.setLong("compId", competenceId)
					.executeUpdate();
				
				credentialManager.updateDurationForCredentialsWithCompetence(competenceId, 
						duration, op);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
	}
	
	private Optional<Long> getCompetenceIdForActivity(long actId) {
		String query = "SELECT comp.id FROM CompetenceActivity1 cAct " +
				"INNER JOIN cAct.competence comp " +
				"WHERE cAct.activity.id = :actId";
		
		Long res = (Long) persistence.currentManager()
			.createQuery(query)
			.setLong("actId", actId)	
			.uniqueResult();

		return res == null ? Optional.empty() : Optional.of(res);
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
			long userId) throws DbConnectionException, ResourceNotFoundException, IllegalArgumentException {
		CompetenceData1 compData = null;
		try {
			compData = getTargetCompetenceData(credId, compId, userId, true, true);
			if (compData == null) {
//				compData = getCompetenceData(compId, true, true, true, userId,
//						LearningResourceReturnResultType.FIRST_TIME_DRAFT_FOR_USER, true);
				//compData = getCompetenceDataForUser(credId, compId, true, true, true, userId, true);
				compData = getCompetenceData(credId, compId, true, true, true, userId, 
						UserGroupPrivilege.Learn, false);
			}
				
			return compData;
		} catch(ResourceNotFoundException rnfe) {
			throw rnfe;
		} catch(IllegalArgumentException iae) {
			throw iae;
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
			//TODO cred-redesign-07
//			String query = "SELECT targetComp " +
//					   "FROM TargetCompetence1 targetComp " +
//					   "INNER JOIN targetComp.targetCredential targetCred " +
//					   		"WITH targetCred.credential.id = :credId " +
//					   		"AND targetCred.user.id = :userId " +
//				   	   "INNER JOIN fetch targetComp.createdBy user " + 
//					   "LEFT JOIN fetch targetComp.tags tags " +
//				   	   "WHERE targetComp.competence.id = :compId";
//
//
//			TargetCompetence1 res = (TargetCompetence1) persistence.currentManager()
//					.createQuery(query)
//					.setLong("userId", userId)
//					.setLong("credId", credId)
//					.setLong("compId", compId)
//					.uniqueResult();
//
//			if (res != null) {
//				compData = competenceFactory.getCompetenceData(res.getCreatedBy(), res, 
//						res.getTags(), null, true);
//				
//				//retrieve user privilege to be able to tell if user can edit this competence
//				UserGroupPrivilege priv = getUserPrivilegeForCompetence(credId, compId, userId);
//				compData.setCanEdit(priv == UserGroupPrivilege.Edit);
//				//target competence can always be accessed
//				compData.setCanAccess(true);
//				
//				if(compData != null && loadActivities) {
//					List<ActivityData> activities = activityManager
//							.getTargetActivitiesData(compData.getTargetCompId());
//					compData.setActivities(activities);
//				}
//				return compData;
//			}
//			return null;
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public List<EventData> publishCompetences(long credId, List<Long> compIds, long creatorId) 
			throws DbConnectionException, CompetenceEmptyException {
		try {
			//get all draft competences
			List<EventData> events = new ArrayList<>();
			List<Competence1> comps = getDraftCompetencesFromList(compIds, creatorId);
			//publish competences that this user can edit only
			User user = new User();
			user.setId(creatorId);
			
			//store ids of competences that can be edited and that are published so list can be 
			//passed to publishActivities method
			List<Long> publishedComps = new ArrayList<>();
			for(Competence1 c : comps) {
				/*
				 * check if competence has at least one activity - if not, it can't be published
				 */
				int numberOfActivities = c.getActivities().size();
				if(numberOfActivities == 0) {
					throw new CompetenceEmptyException();
				}
				//check if user can edit this competence
				UserGroupPrivilege priv = getUserPrivilegeForCompetence(credId, c.getId(), creatorId);
				if(priv == UserGroupPrivilege.Edit) {
					c.setPublished(true);
					EventData ev = new EventData();
					ev.setActorId(creatorId);
					ev.setEventType(EventType.STATUS_CHANGED);
				    ev.setObject(c);
					events.add(ev);
					
					publishedComps.add(c.getId());
				}
			}
			
			List<EventData> actEvents = activityManager.publishActivitiesFromCompetences(credId,
					creatorId, publishedComps);
			persistence.currentManager().flush();
			for(long id : publishedComps) {
				Competence1 comp = (Competence1) persistence.currentManager()
						.load(Competence1.class, id);
				comp.setDuration(getRecalculatedDuration(id));
			}
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
	private List<Competence1> getDraftCompetencesFromList(List<Long> compIds, long userId) {
		StringBuilder queryB = new StringBuilder("SELECT comp FROM Competence1 comp " +
					   "WHERE comp.id IN (:ids) " +
					   "AND comp.published = :published ");
		
		Query q = persistence.currentManager()
				.createQuery(queryB.toString())
				.setParameterList("ids", compIds)
				.setBoolean("published", false);
		
		@SuppressWarnings("unchecked")
		List<Competence1> comps = q.list();
		if(comps == null) {
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
	public List<Competence1> getAllCompetences(Session session) 
			throws DbConnectionException {
		try {
			String query=
					"SELECT comp " +
					"FROM Competence1 comp " +
					"WHERE comp.deleted = :deleted";
			  	
			@SuppressWarnings("unchecked")
			List<Competence1> result = session
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
	
	@Transactional(readOnly = true)
	@Override
	public UserGroupPrivilege getUserPrivilegeForCompetence(long credId, long compId, long userId) 
			throws DbConnectionException {
		try {
			if(credId > 0) {
				return credentialManager.getUserPrivilegeForCredential(credId, userId);
			}
			String query = "SELECT compUserGroup.privilege, comp.createdBy.id, comp.visibleToAll " +
					"FROM CompetenceUserGroup compUserGroup " +
					"INNER JOIN compUserGroup.userGroup userGroup " +
					"RIGHT JOIN compUserGroup.competence comp " +
					"INNER JOIN userGroup.users user " +
						"WITH user.user.id = :userId " +
					"WHERE comp.id = :compId " +
					"ORDER BY CASE WHEN compUserGroup.privilege = :editPriv THEN 1 WHEN compUserGroup.privilege = :viewPriv THEN 2 ELSE 3 END";
			
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setLong("userId", userId)
					.setLong("compId", compId)
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
			throw new DbConnectionException("Error while trying to retrieve user privilege for competence");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isVisibleToAll(long compId) throws DbConnectionException {
		try {
			String query=
					"SELECT comp.visibleToAll " +
					"FROM Competence1 comp " +
					"WHERE comp.id = :compId";
			  	
			Boolean result = (Boolean) persistence.currentManager()
					.createQuery(query)
					.setLong("compId", compId)
				  	.uniqueResult();
			
			return result == null ? false : result;
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving competence visibility");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void updateCompetenceVisibility(long compId, List<ResourceVisibilityMember> groups, 
    		List<ResourceVisibilityMember> users, boolean visibleToAll, boolean visibleToAllChanged) 
    				throws DbConnectionException {
		try {
			if(visibleToAllChanged) {
				Competence1 comp = (Competence1) persistence.currentManager().load(
						Competence1.class, compId);
				comp.setVisibleToAll(visibleToAll);
			}
			userGroupManager.saveCompetenceUsersAndGroups(compId, groups, users);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating credential visibility");
		}
	}
	
	private long getRecalculatedDuration(long compId) {
		String query = "SELECT sum(a.duration) FROM CompetenceActivity1 ca " +
					   "INNER JOIN ca.activity a " +
					   "WHERE ca.competence.id = :compId " +
					   "AND a.published = :published";
		Long res = (Long) persistence.currentManager()
				.createQuery(query)
				.setLong("compId", compId)
				.setBoolean("published", true)
				.uniqueResult();
		
		return res != null ? res : 0;
	}
	
}
