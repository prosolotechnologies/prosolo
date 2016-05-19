package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
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
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.Operation;
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
	public Competence1 saveNewCompetence(CompetenceData1 data, User createdBy, long credentialId) 
			throws DbConnectionException {
		Competence1 comp = null;
		try {
			comp = resourceFactory.createCompetence(data.getTitle(), data.getDescription(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getTagsString())),
					createdBy, data.isStudentAllowedToAddActivities(), data.isPublished(), 
					data.getDuration());
			
			if(data.getActivities() != null) {
				for(ActivityData bad : data.getActivities()) {
					CompetenceActivity1 ca = new CompetenceActivity1();
					ca.setOrder(bad.getOrder());
					ca.setCompetence(comp);
					Activity1 act = (Activity1) persistence.currentManager().load(
							Activity1.class, bad.getActivityId());
					ca.setActivity(act);
					saveEntity(ca);
				}
			}
			
			if(credentialId > 0) {
				credentialManager.addCompetenceToCredential(credentialId, comp);
			}

			if(data.isPublished()) {
				eventFactory.generateEvent(EventType.Create, createdBy, comp);
			}

			return comp;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competence");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}

	@Override
	@Transactional(readOnly = false)
	public Competence1 deleteCompetence(long originalCompId, CompetenceData1 data, User user) 
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
				
				deleteAllCredentialCompetencesForCompetence(comp.getId());
	
				if(data.isPublished() || data.isDraft()) {
					eventFactory.generateEvent(EventType.Delete, user, comp);
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
		TargetCompetence1 targetComp = new TargetCompetence1();
		Competence1 comp = cc.getCompetence();
		targetComp.setTitle(comp.getTitle());
		targetComp.setDescription(comp.getDescription());
		targetComp.setTargetCredential(targetCred);
		targetComp.setCompetence(comp);
		targetComp.setDuration(comp.getDuration());
		targetComp.setStudentAllowedToAddActivities(comp.isStudentAllowedToAddActivities());
		targetComp.setOrder(cc.getOrder());
		targetComp.setCreatedBy(comp.getCreatedBy());
		
		if(comp.getTags() != null) {
			Set<Tag> tags = new HashSet<>();
			for(Tag tag : comp.getTags()) {
				tags.add(tag);
			}
			targetComp.setTags(tags);
		}
		saveEntity(targetComp);
		
		List<TargetActivity1> targetActivities = activityManager.createTargetActivities(
				comp.getId(), targetComp);
		targetComp.setTargetActivities(targetActivities);
		
		/*
		 * set first activity as next to learn
		 */
		targetComp.setNextActivityToLearnId(targetActivities.get(0).getActivity().getId());
		return targetComp;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, boolean shouldTrackChanges) throws DbConnectionException {
		try {
			Competence1 comp = getCompetence(compId, loadCreator, loadTags);
		
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, tags, shouldTrackChanges);
			
			if(loadActivities) {
				List<ActivityData> activities = activityManager.getCompetenceActivitiesData(compId);
				compData.setActivities(activities);
			}
			
			return compData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Transactional(readOnly = true)
	private Competence1 getCompetence(long compId, boolean loadCreator, boolean loadTags) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT comp " + 
					   "FROM Competence1 comp ");
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
		builder.append("WHERE comp.id = :compId " +
				   "AND comp.deleted = :deleted " + 
				   "AND (comp.published = :published OR (comp.published = :notPublished " +
				   "AND comp.hasDraft = :hasDraft)) " +
				   "AND comp.draft = :draft ");
//		if(loadActivities) {
//			builder.append("AND (act is null or " +
//				   "(act.deleted = :deleted " +
//				   "AND (act.published = :published OR (act.published = :notPublished " +
//				   "AND act.hasDraft = :hasDraft)) " +
//				   "AND act.draft = :draft))");
//		}

		logger.info("QUERY: " + builder.toString());
		Competence1 res = (Competence1) persistence.currentManager()
			.createQuery(builder.toString())
			.setLong("compId", compId)
			.setBoolean("deleted", false)
			.setBoolean("published", true)
			.setBoolean("notPublished", false)
			.setBoolean("draft", false)
			.setBoolean("hasDraft", true)
			.uniqueResult();

		return res;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceDataForEdit(long competenceId, long creatorId, 
			boolean loadActivities) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, creatorId);
					
			String commonQuery = "SELECT comp " +
						   "FROM Competence1 comp " + 
						   "LEFT JOIN fetch comp.tags tags ";
			
			String query1 = commonQuery + 
					" WHERE comp.id = :compId" +
					" AND comp.deleted = :deleted" +
					" AND comp.createdBy = :user";
						   
			Competence1 res = (Competence1) persistence.currentManager()
					.createQuery(query1)
					.setLong("compId", competenceId)
					.setBoolean("deleted", false)
					.setEntity("user", user)
					.uniqueResult();
			
			if(res != null) {
				CompetenceData1 cd = null;
				if(res.isHasDraft()) {
					String query2 = commonQuery + 
							" WHERE comp = :draftVersion";
					Competence1 draftComp = (Competence1) persistence.currentManager()
							.createQuery(query2)
							.setEntity("draftVersion", res.getDraftVersion())
							.uniqueResult();
					if(draftComp != null) {
						cd = competenceFactory.getCompetenceData(null, draftComp, 
								draftComp.getTags(), true);
					}	
				} else {
					cd = competenceFactory.getCompetenceData(null, res, res.getTags(), true);
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
	public Competence1 updateCompetence(CompetenceData1 data, User user) throws DbConnectionException {
		try {
			Competence1 updatedComp = resourceFactory.updateCompetence(data);		  
		    
			 if(data.isPublished()) {
				//competence remains published
				if(!data.isPublishedChanged()) {
					Map<String, String> params = new HashMap<>();
				    CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
				    		false, data.isTitleChanged(), data.isDescriptionChanged(), false, 
				    		data.isTagsStringChanged(), data.isStudentAllowedToAddActivitiesChanged());
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
				    eventFactory.generateEvent(EventType.Edit, user, updatedComp, params);
				} 
				/*
				 * this means that competence is published for the first time
				 */
				else if(!data.isDraft()) {
					eventFactory.generateEvent(EventType.Create, user, updatedComp);
				}
				/*
				 * Competence becomes published again. Because data can show what has changed
				 * based on draft version, we can't use that. We need to know what has changed based on
				 * original competence, so all fields are treated as changed.
				 */
				else {
					Map<String, String> params = new HashMap<>();
				    CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
				    		true, true, true, true, true, true);
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
				    params.put("draftVersionId", data.getCompetenceId() + "");
				    eventFactory.generateEvent(EventType.Edit, user, updatedComp, params);
				}
			} else {
				/*
				 * if competence remains draft
				 */
				if(!data.isPublishedChanged()) {
					Map<String, String> params = new HashMap<>();
				    CompetenceChangeTracker changeTracker = new CompetenceChangeTracker(data.isPublished(),
				    		false, data.isTitleChanged(), data.isDescriptionChanged(), false, 
				    		data.isTagsStringChanged(), data.isStudentAllowedToAddActivitiesChanged());
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
					eventFactory.generateEvent(EventType.Edit_Draft, user, updatedComp, params);
				} 
				/*
				 * This means that competence was published before so draft version is created.
				 */
				else {
					Map<String, String> params = new HashMap<>();
					params.put("originalVersionId", data.getCompetenceId() + "");
					eventFactory.generateEvent(EventType.Create_Draft, user, updatedComp, params);
				}
			}

		    return updatedComp;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving competence");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
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
		
		return updateComptenceData(comp, publishTransition, data);		   
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
	private Competence1 updateComptenceData(Competence1 comp, EntityPublishTransition publishTransition,
			CompetenceData1 data) {
		Competence1 compToUpdate = null;
		switch(publishTransition) {
			case FROM_PUBLISHED_TO_DRAFT_VERSION:
				compToUpdate = new Competence1();
				compToUpdate.setDraft(true);
				compToUpdate.setCreatedBy(comp.getCreatedBy());
				comp.setHasDraft(true);
				comp.setPublished(false);
				break;
			case FROM_DRAFT_VERSION_TO_PUBLISHED:
				compToUpdate = getOriginalCompetenceForDraft(comp.getId());
				compToUpdate.setHasDraft(false);
				compToUpdate.setDraftVersion(null);
		    	delete(comp);
		    	/*
		    	 * old duration is taken from original competence and new duration is
		    	 * taken from draft version and not from data object because competence
		    	 * duration can be updated from other places (when activity is updated)
		    	 * and data object may not capture last changes
		    	 */
		    	updateCredentialsDuration(compToUpdate.getId(), comp.getDuration(), 
		    			compToUpdate.getDuration());
		    	break;
			case NO_TRANSITION:
				compToUpdate = comp;
				break;
		}
		
		compToUpdate.setTitle(data.getTitle());
		compToUpdate.setDescription(data.getDescription());
		compToUpdate.setPublished(data.isPublished());
		compToUpdate.setStudentAllowedToAddActivities(data.isStudentAllowedToAddActivities());
		compToUpdate.setDuration(data.getDuration());
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
	    	/*
	    	 * if draft version is published or original version becomes published for the 
	    	 * first time, publish all draft activities that were never published.
	    	 */
	    	if(publishTransition == EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED
	    			|| (publishTransition == EntityPublishTransition.NO_TRANSITION
	    			&& data.isPublished() && data.isPublishedChanged())) {
	    		activityManager.publishDraftActivitiesWithoutDraftVersion(actIds);;
    		}
	    }
	    
	    return compToUpdate;
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
			Competence1 comp = (Competence1) persistence.currentManager().load(
					Competence1.class, compId);
			String query = "DELETE CompetenceActivity1 compAct " +
			       	       "WHERE compAct.competence = :comp";
			
			persistence.currentManager()
				.createQuery(query)
				.setEntity("comp", comp)
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
			builder.append("SELECT credComp " + 
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
		try {		
			//if left join is used list with null element would be returned.
			String query = "SELECT tag " +
					       "FROM Competence1 comp " +
					       "INNER JOIN comp.tags tag " +
					       "WHERE comp.id = :compId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = persistence.currentManager()
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

	@Override
	@Transactional(readOnly = false)
	public void publishDraftCompetencesWithoutDraftVersion(List<Long> compIds) 
			throws DbConnectionException {
		try {
			if(compIds == null || compIds.isEmpty()) {
				return;
			}
			
			String query = "UPDATE Competence1 comp " +
						   "SET comp.published = :published " + 
						   "WHERE comp.hasDraft = :hasDraft " +
						   "AND comp.id IN :compIds";
			persistence.currentManager()
				.createQuery(query)
				.setBoolean("published", true)
				.setBoolean("hasDraft", false)
				.setParameterList("compIds", compIds)
				.executeUpdate();
			
			for(Long compId : compIds) {
				activityManager.publishAllCompetenceActivitiesWithoutDraftVersion(compId);
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competences");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public void addActivityToCompetence(long compId, Activity1 act) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(
					Competence1.class, compId);
			
			/*
			 * if credential has draft version, that version is loaded and if credential 
			 * is published draft version will be created and attached to original credential
			 */
			Competence1 draftComp = null;
			if(comp.isHasDraft()) {
				draftComp = comp.getDraftVersion();
			} else if(comp.isPublished()) {
				draftComp = createDraftVersionOfCompetence(compId);
				comp.setHasDraft(true);
				comp.setPublished(false);
				comp.setDraftVersion(draftComp);
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
					updateDuration(compToUpdate.getId(), act.getDuration(), Operation.Add);
				}
			}
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while adding activity to competence");
		}
		
	}
	
	private Competence1 createDraftVersionOfCompetence(long originalCompId) {
		Competence1 originalComp = getCompetence(originalCompId, false, true);
		
		Competence1 draftComp = new Competence1();
		draftComp.setDraft(true);
		draftComp.setPublished(false);
		draftComp.setCreatedBy(originalComp.getCreatedBy());
		draftComp.setTitle(originalComp.getTitle());
		draftComp.setDescription(originalComp.getDescription());
		draftComp.setStudentAllowedToAddActivities(originalComp.isStudentAllowedToAddActivities());
		draftComp.setDuration(originalComp.getDuration());
	    
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
	public void updateDuration(long id, long duration, Operation op) throws DbConnectionException {
		try {
			String opString = op == Operation.Add ? "+" : "-";
			String query = "UPDATE Competence1 comp SET " +
						   "comp.duration = comp.duration " + opString + " :duration " +
						   "WHERE comp.id = :compId";
			
			persistence.currentManager()
				.createQuery(query)
				.setLong("duration", duration)
				.setLong("compId", id)
				.executeUpdate();
			
			credentialManager.updateDurationForCredentialsWithCompetence(id, duration, op);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating competence duration");
		}
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
			String credTitle = null;
			if (compData == null) {
				compData = getCompetenceData(compId, true, true, true, true);
				String query = "SELECT cred.title " +
						   "FROM Credential1 cred " +
						   "WHERE cred.id = :credId";
			
				credTitle = (String) persistence.currentManager()
						.createQuery(query)
						.setLong("credId", credId)
						.uniqueResult();
			} else {
				String query = "SELECT targetCred.title " +
						   "FROM TargetCredential1 targetCred " +
						   "WHERE targetCred.credential.id = :credId " + 
						   "AND targetCred.user.id = :userId";
			
				credTitle = (String) persistence.currentManager()
						.createQuery(query)
						.setLong("credId", credId)
						.setLong("userId", userId)
						.uniqueResult();
			}
			
			compData.setCredentialId(credId);
			compData.setCredentialTitle(credTitle);
				
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
}
