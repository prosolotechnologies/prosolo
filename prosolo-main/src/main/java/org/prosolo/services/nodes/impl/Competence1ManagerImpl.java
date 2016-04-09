package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.prosolo.services.nodes.data.BasicActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.impl.util.EntityPublishTransition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
				for(BasicActivityData bad : data.getActivities()) {
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

			eventFactory.generateEvent(EventType.Create, createdBy, comp);

			return comp;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving credential");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
	}

	

	@Override
	@Transactional(readOnly = false)
	public Competence1 deleteCompetence(long compId) throws DbConnectionException {
		try {
			if(compId > 0) {
				Competence1 comp = (Competence1) persistence.currentManager()
						.load(Competence1.class, compId);
				comp.setDeleted(true);
				
				deleteAllCredentialCompetencesForCompetence(comp.getId());
	
				return comp;
			}
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential");
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
					CompetenceData1 compData = competenceFactory.getCompetenceData(
							targetComp.getCreatedBy(), targetComp, targetComp.getTags(), true);
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
	public TargetCompetence1 createTargetCompetence(TargetCredential1 targetCred, 
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
		
		List<CompetenceActivity1> compActivities = comp.getActivities();
		if(compActivities != null) {
			for(CompetenceActivity1 act : compActivities) {
				TargetActivity1 ta = activityManager.createTargetActivity(targetComp, act);
				targetComp.getTargetActivities().add(ta);
			}
		}
		
		return targetComp;
	}
	
	@Override
	@Transactional(readOnly = true)
	public CompetenceData1 getCompetenceData(long compId, boolean loadCreator, boolean loadTags, 
			boolean loadActivities, boolean shouldTrackChanges) throws DbConnectionException {
		try {
			Competence1 comp = getCompetence(compId, loadCreator, loadTags, loadActivities);
			
			User creator = loadCreator ? comp.getCreatedBy() : null;
			Set<Tag> tags = loadTags ? comp.getTags() : null;
			
			CompetenceData1 compData = competenceFactory.getCompetenceData(
					creator, comp, tags, shouldTrackChanges);
			return compData;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading competence data");
		}
	}
	
	@Transactional(readOnly = true)
	private Competence1 getCompetence(long compId, boolean loadCreator, boolean loadTags,
			boolean loadActivities) {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT comp " + 
					   "FROM Competence1 comp ");
		if(loadCreator) {
			builder.append("INNER JOIN fetch comp.createdBy user ");
		}
		if(loadTags) {
			builder.append("LEFT JOIN fetch comp.tags tags ");
		}
		if(loadActivities) {
			builder.append("LEFT JOIN fetch comp.activities compAct " +
					       "LEFT JOIN fetch compAct.activity act ");
		}
		builder.append("WHERE comp.id = :compId " +
				   "comp.deleted = :deleted " + 
				   "AND (comp.published = :published OR (comp.published = :notPublished " +
				   "AND comp.hasDraft = :hasDraft)) " +
				   "AND comp.draft = :draft ");
		if(loadActivities) {
			builder.append("AND (act is null or " +
				   "(act.deleted = :deleted " +
				   "AND (act.published = :published OR (act.published = :notPublished " +
				   "AND act.hasDraft = :hasDraft)) " +
				   "AND act.draft = :draft))");
		}

		Competence1 res = (Competence1) persistence.currentManager()
			.createQuery(builder.toString())
			.setEntity("compId", compId)
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
					List<BasicActivityData> activities = activityManager
							.getCompetenceActivities(cd.getCompetenceId());
					cd.setActivities(activities);
				}
				return cd;
			}
			
			return null;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Competence1 updateCompetence(CompetenceData1 data, User user) throws DbConnectionException {
		try {
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
			
			Competence1 updatedComp = updateComeptenceData(comp, publishTransition, data);		  
		    
		    /* determine if changes should be propagated
		     * to users enrolled in a credential */
		    boolean shouldPropagateChanges = data.isPublished() && (data.isTitleChanged() 
		    		|| data.isDescriptionChanged() || data.isTagsStringChanged());
		    
		    /*
		     * TODO generate event and implement observer that will update
		     * all users target credentials if needed
		     * pass to event shouldPropagateChanges or each attribute that
		     * changed so observer can know if changes should be propagated.
		    */
		    eventFactory.generateEvent(EventType.Edit, user, comp);

		    return updatedComp;
		} catch (EventException e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving credential");
		} catch (DbConnectionException dbe) {
			logger.error(dbe);
			dbe.printStackTrace();
			throw dbe;
		}
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
	private Competence1 updateComeptenceData(Competence1 comp, EntityPublishTransition publishTransition,
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

		List<BasicActivityData> activities = data.getActivities();
	    if(activities != null) {
	    	if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
	    		Iterator<BasicActivityData> actIterator = activities.iterator();
	    		while(actIterator.hasNext()) {
	    			BasicActivityData bad = actIterator.next();
		    		switch(bad.getObjectStatus()) {
		    			case CREATED:
		    				CompetenceActivity1 ca1 = new CompetenceActivity1();
		    				ca1.setOrder(bad.getOrder());
		    				ca1.setCompetence(compToUpdate);
		    				Activity1 act = (Activity1) persistence.currentManager().load(
		    						Activity1.class, bad.getActivityId());
		    				ca1.setActivity(act);
		    				saveEntity(ca1);
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
		    				break;
		    			case UP_TO_DATE:
		    				break;
		    		}
		    	}
	    	} else {
	    		Iterator<BasicActivityData> actIterator = activities.iterator();
	    		while(actIterator.hasNext()) {
	    			BasicActivityData bad = actIterator.next();
		    		if(bad.getObjectStatus() != ObjectStatus.REMOVED) {
		    			CompetenceActivity1 ca = new CompetenceActivity1();
	    				ca.setOrder(bad.getOrder());
	    				ca.setCompetence(compToUpdate);
	    				Activity1 act = (Activity1) persistence.currentManager().load(
	    						Activity1.class, bad.getActivityId());
	    				ca.setActivity(act);
	    				saveEntity(ca);
		    		}
	    		}
	    	}
	    }
	    
	    return compToUpdate;
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
			throw new DbConnectionException("Error while retrieving credential data");
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
					loadCreator, loadTags, loadActivities, includeNotPublished);

			for (CredentialCompetence1 credComp : res) {
				User creator = loadCreator ? credComp.getCompetence().getCreatedBy() : null;
				Set<Tag> tags = loadTags ? credComp.getCompetence().getTags() : null;
				
				CompetenceData1 compData = competenceFactory.getCompetenceData(
						creator, credComp, tags, true);
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
			boolean loadTags, boolean loadActivities, boolean includeNotPublished) 
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
			if(loadActivities) {
				builder.append("LEFT JOIN fetch comp.activities compAct " +
							   "LEFT JOIN fetch compAct.activity act ");
			}
			builder.append("WHERE credComp.credential = :credential " + 
					   "AND comp.deleted = :deleted " + 
					   "AND comp.draft = :draft " +
					   "AND credComp.deleted = :deleted ");
			
			if(!includeNotPublished) {
				builder.append("AND (comp.published = :published OR (comp.published = :notPublished " +
						   	   "AND comp.hasDraft = :hasDraft)) ");
			}
			
			if(loadActivities) {
				builder.append("AND (act is null or " +
					   "(act.deleted = :deleted " +
					   "AND act.draft = :draft ");
				
				if(!includeNotPublished) {
					builder.append("AND (act.published = :published OR (act.published = :notPublished " +
							       "AND act.hasDraft = :hasDraft))");
				}
				builder.append("))");
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

}
