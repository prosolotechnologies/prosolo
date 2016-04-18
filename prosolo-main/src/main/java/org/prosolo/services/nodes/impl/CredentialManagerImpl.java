package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.CredentialType1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.CredentialDataFactory;
import org.prosolo.services.nodes.impl.util.EntityPublishTransition;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	@Override
	@Transactional(readOnly = false)
	public Credential1 saveNewCredential(CredentialData data, User createdBy) throws DbConnectionException {
		Credential1 cred = null;
		try {
			cred = resourceFactory.createCredential(data.getTitle(), data.getDescription(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getTagsString())),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getHashtagsString())), createdBy,
					data.getType(), data.isMandatoryFlow(), data.isPublished(), data.getDuration());

			if(data.getCompetences() != null) {
				for(CompetenceData1 cd : data.getCompetences()) {
					CredentialCompetence1 cc = new CredentialCompetence1();
					cc.setOrder(cd.getOrder());
					cc.setCredential(cred);
					Competence1 comp = (Competence1) persistence.currentManager().load(
							Competence1.class, cd.getCompetenceId());
					cc.setCompetence(comp);
					saveEntity(cc);
				}
			}
			//generate create event only if credential is published
			if(data.isPublished()) {
				eventFactory.generateEvent(EventType.Create, createdBy, cred);
			}

			return cred;
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
	public Credential1 deleteCredential(long originalCredId, CredentialData data, User user) throws DbConnectionException {
		try {
			if(originalCredId > 0) {
				Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, originalCredId);
				cred.setDeleted(true);
				
				if(data.isDraft()) {
					Credential1 draftVersion = (Credential1) persistence.currentManager()
							.load(Credential1.class, data.getId());
					cred.setDraftVersion(null);
					delete(draftVersion);
				}
	
				if(data.isPublished() || data.isDraft()) {
					eventFactory.generateEvent(EventType.Delete, user, cred);
				}
				
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
	public CredentialData getAllCredentialDataForUser(long credentialId, long userId)
			throws DbConnectionException {
		CredentialData credData = null;
		try {
			credData = getTargetCredentialData(credentialId, userId, true);
			if (credData == null) {
				credData = getCredentialData(credentialId, true, true);
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
		User user = (User) persistence.currentManager().load(User.class, userId);
		Credential1 cred = (Credential1) persistence.currentManager().load(
				Credential1.class, credentialId);
		try {
			String query = "SELECT targetCred " +
						   "FROM TargetCredential1 targetCred " + 
						   "INNER JOIN fetch targetCred.createdBy user " + 
						   "LEFT JOIN fetch targetCred.tags tags " +
						   "LEFT JOIN fetch targetCred.hashtags hashtags " +
						   "WHERE targetCred.credential = :cred " +
						   "AND targetCred.user = :student";

			TargetCredential1 res = (TargetCredential1) persistence.currentManager()
					.createQuery(query)
					.setEntity("cred", cred)
					.setEntity("student", user)
					.uniqueResult();

			if (res != null) {
				credData = credentialFactory.getCredentialData(res.getCreatedBy(), 
						res, res.getTags(), res.getHashtags(), true);
				
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

	@Transactional(readOnly = true)
	private CredentialData getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences) throws DbConnectionException {
		try {
			Credential1 cred = getCredential(credentialId, loadCreatorData);

			if (cred != null) {
				User createdBy = loadCreatorData ? cred.getCreatedBy() : null;
				CredentialData credData = credentialFactory.getCredentialData(createdBy, cred,
						cred.getTags(), cred.getHashtags(), true);
				
				if(loadCompetences) {
					List<CompetenceData1> compsData = compManager.getCredentialCompetencesData(
							credentialId, false, false , false, false);
					credData.setCompetences(compsData);
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
	
	@Transactional(readOnly = true)
	private Credential1 getCredential(long credentialId, boolean loadCreatorData) throws DbConnectionException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT cred FROM Credential1 cred ");

			if(loadCreatorData) {
				builder.append("INNER JOIN fetch cred.createdBy user ");
			}
			builder.append("LEFT JOIN fetch cred.tags tags "); 
			builder.append("LEFT JOIN fetch cred.hashtags hashtags ");
			builder.append("WHERE cred.id = :credentialId AND cred.deleted = :deleted "
					+ "AND cred.draft = :draft "
					+ "AND (cred.published = :published  OR (cred.published = :notPublished "
					+ "AND cred.hasDraft = :hasDraft))");

			logger.info("GET CREDENTIAL DATA QUERY: " + builder.toString());
			Credential1 cred = (Credential1) persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false)
					.setBoolean("published", true)
					.setBoolean("notPublished", false)
					.setBoolean("draft", false)
					.setBoolean("hasDraft", true)
					.uniqueResult();

			return cred;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while loading credential data");
		}
	}

	
	@Override
	@Transactional(readOnly = true)
	public CredentialData getCredentialDataForEdit(long credentialId, long creatorId, 
			boolean loadCompetences) throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, creatorId);
					
			String commonQuery = "SELECT cred " +
						   "FROM Credential1 cred " + 
						   "LEFT JOIN fetch cred.tags tags " +
						   "LEFT JOIN fetch cred.hashtags hashtags ";
			
			String query1 = commonQuery + 
					" WHERE cred.id = :credentialId" +
					" AND cred.deleted = :deleted" +
					" AND cred.createdBy = :user";
						   
			Credential1 res = (Credential1) persistence.currentManager()
					.createQuery(query1)
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false)
					.setEntity("user", user)
					.uniqueResult();
			
			if(res != null) {
				CredentialData credData = null;
				if(res.isHasDraft()) {
					String query2 = commonQuery + 
							" WHERE cred = :draftVersion";
					Credential1 draftCred = (Credential1) persistence.currentManager()
							.createQuery(query2)
							.setEntity("draftVersion", res.getDraftVersion())
							.uniqueResult();
					if(draftCred != null) {
						credData = credentialFactory.getCredentialData(null, draftCred, 
								draftCred.getTags(), draftCred.getHashtags(), true);
					}	
				} else {
					credData = credentialFactory.getCredentialData(null, res, res.getTags(),
							res.getHashtags(), true);
				}
				if(credData != null && loadCompetences) {
					List<CompetenceData1> compsData = compManager.getCredentialCompetencesData(
							credData.getId(), true, false, false, true);
					credData.setCompetences(compsData);
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
	@Transactional(readOnly = false)
	public Credential1 updateCredential(CredentialData data, User user) throws DbConnectionException {
		try {
			Credential1 cred = resourceFactory.updateCredential(data);

			if(data.isPublished()) {
				if(data.isPublishedChanged() && !data.isDraft()) {
					eventFactory.generateEvent(EventType.Create, user, cred);
				} else {
					Map<String, String> params = new HashMap<>();
				    CredentialChangeTracker changeTracker = new CredentialChangeTracker(data.isPublished(),
				    		data.isTitleChanged(), data.isDescriptionChanged(), data.isTagsStringChanged(), 
				    		data.isHashtagsStringChanged());
				    Gson gson = new GsonBuilder().create();
				    String jsonChangeTracker = gson.toJson(changeTracker);
				    params.put("changes", jsonChangeTracker);
				    eventFactory.generateEvent(EventType.Edit, user, cred, params);
				}
			}

		    return cred;
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
	public Credential1 updateCredential(CredentialData data) {
		Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
				data.getId());
		/*
		 * draft should be created if something changed, draft option is chosen 
		 * and credential was published before this update
		*/
		EntityPublishTransition publishTransition = (!data.isPublished() && data.isPublishedChanged()) ? 
				EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION :
				EntityPublishTransition.NO_TRANSITION;
		/*
		 * check if published option was chosen, it was draft before update and 
		 * draft version (and not original credential) of credential is updated.
		 * Last check added because it is possible for original credential to be
		 * draft and that it doesn't have draft version because it was never published.
		*/
		if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
			publishTransition = (data.isPublished() && data.isPublishedChanged() && data.isDraft()) ? 
					publishTransition = EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED :
					publishTransition;
		}
		
		return updateCredentialData(cred, publishTransition, data);
	}
	
	/**
	 * Updates all credential updatable fields and takes into
	 * account transitions between credential publish status
	 * so if status is changed from published to draft, draft
	 * version is inserted and original credential is referencing
	 * this version. If status is changed from draft to published,
	 * it is checked if credential has been published once and if 
	 * it has, that means that draft version exists and this version
	 * is deleted.
	 * @param cred
	 * @param publishTransition
	 * @param data
	 */
	@Transactional(readOnly = true)
	private Credential1 updateCredentialData(Credential1 cred, EntityPublishTransition publishTransition,
			CredentialData data) {
		Credential1 credToUpdate = null;
		switch(publishTransition) {
			case FROM_PUBLISHED_TO_DRAFT_VERSION:
				credToUpdate = new Credential1();
				credToUpdate.setDraft(true);
				credToUpdate.setCreatedBy(cred.getCreatedBy());
				cred.setHasDraft(true);
				cred.setPublished(false);
				break;
			case FROM_DRAFT_VERSION_TO_PUBLISHED:
				credToUpdate = getOriginalCredentialForDraft(cred.getId());
				credToUpdate.setHasDraft(false);
		    	credToUpdate.setDraftVersion(null);
		    	delete(cred);
		    	break;
			case NO_TRANSITION:
				credToUpdate = cred;
				break;
		}
		
		credToUpdate.setTitle(data.getTitle());
		credToUpdate.setDescription(data.getDescription());
		credToUpdate.setCompetenceOrderMandatory(data.isMandatoryFlow());
		credToUpdate.setPublished(data.isPublished());
		credToUpdate.setStudentsCanAddCompetences(data.isStudentsCanAddCompetences());
		credToUpdate.setManuallyAssignStudents(data.isManuallyAssingStudents());
		credToUpdate.setDefaultNumberOfStudentsPerInstructor(data.getDefaultNumberOfStudentsPerInstructor());
		credToUpdate.setDuration(data.getDuration());
	    if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
	    	if(data.isTagsStringChanged()) {
	    		credToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    				data.getTagsString())));		     
	    	}
	    	if(data.isHashtagsStringChanged()) {
	    		credToUpdate.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    				data.getHashtagsString())));
	    	}
	    } else {
	    	credToUpdate.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    			data.getTagsString())));
	    	credToUpdate.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(
	    			data.getHashtagsString())));
	    }
	    
		if(publishTransition == EntityPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION) {
			saveEntity(credToUpdate);
			cred.setDraftVersion(credToUpdate);
		}
		
		if(publishTransition == EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED) {
			deleteCredentialCompetences(credToUpdate.getId());
		}

		List<CompetenceData1> comps = data.getCompetences();
	    if(comps != null) {
	    	if(publishTransition == EntityPublishTransition.NO_TRANSITION) {
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
		    				break;
		    			case CHANGED:
		    				CredentialCompetence1 cc2 = (CredentialCompetence1) persistence.currentManager().load(
				    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
		    				cc2.setOrder(cd.getOrder());
		    				break;
		    			case REMOVED:
		    				CredentialCompetence1 cc3 = (CredentialCompetence1) persistence.currentManager().load(
				    				CredentialCompetence1.class, cd.getCredentialCompetenceId());
		    				delete(cc3);
		    				break;
		    			case UP_TO_DATE:
		    				break;
		    		}
		    	}
	    	} else {
	    		Iterator<CompetenceData1> compIterator = comps.iterator();
	    		/*
	    		 * List of competence ids so we can call method that will publish all draft
	    		 * competences
	    		 */
	    		List<Long> compIds = new ArrayList<>();
	    		while(compIterator.hasNext()) {
	    			CompetenceData1 cd = compIterator.next();
		    		if(cd.getObjectStatus() != ObjectStatus.REMOVED) {
		    			CredentialCompetence1 cc1 = new CredentialCompetence1();
	    				cc1.setOrder(cd.getOrder());
	    				cc1.setCredential(credToUpdate);
	    				Competence1 comp = (Competence1) persistence.currentManager().load(
	    						Competence1.class, cd.getCompetenceId());
	    				cc1.setCompetence(comp);
	    				saveEntity(cc1);
	    				compIds.add(cd.getCompetenceId());
		    		}
	    		}
	    		if(publishTransition == EntityPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED) {
	    			compManager.publishDraftCompetencesWithoutDraftVersion(compIds);
	    		}
	    	}
	    }
	    
	    return credToUpdate;
	}

	@Transactional(readOnly = true)
	private Credential1 getOriginalCredentialForDraft(long draftCredId) {
		try {
			Credential1 draftCred = (Credential1) persistence.currentManager().load(
					Credential1.class, draftCredId);
			String query = "SELECT cred " +
					   "FROM Credential1 cred " + 
					   "WHERE cred.draftVersion = :draft";
			
			Credential1 original = (Credential1) persistence.currentManager()
					.createQuery(query)
					.setEntity("draft", draftCred)
					.uniqueResult();
			return original;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential data");
		}
	}

	@Transactional(readOnly = false)
	private void deleteCredentialCompetences(long credentialId) {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(
					Credential1.class, credentialId);
			String query = "DELETE CredentialCompetence1 comp " +
						   "WHERE comp.credential = :cred";
			
			persistence.currentManager()
				.createQuery(query)
				.setEntity("cred", cred)
				.executeUpdate();
			
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while deleting credential competences");
		}
	}

	@Override
	@Transactional(readOnly = false)
	public CredentialData enrollInCredential(long credentialId, User user, LearningContextData context) 
			throws DbConnectionException {
		try {
			Credential1 cred = getCredential(credentialId, false);
			TargetCredential1 targetCred = createTargetCredential(cred, user);
			
			Map<String, String> params = null;
			
			if(cred.getType() == CredentialType1.UNIVERSITY_CREATED) {
	    		//TODO assign student to instructor automatically if automatic assign is turned on
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
			
			String page = null;
			String lContext = null;
			String service = null;
			if(context != null) {
				page = context.getPage();
				lContext = context.getLearningContext();
				service = context.getService();
			}
			eventFactory.generateEvent(EventType.ENROLL_COURSE, user, cred, null, 
					page, lContext, service, params);
			
			return cd;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while enrolling in a credential");
		}
	}
	
	@Transactional(readOnly = false)
	private TargetCredential1 createTargetCredential(Credential1 cred, User user) {
		TargetCredential1 targetCred = new TargetCredential1();
		targetCred.setTitle(cred.getTitle());
		targetCred.setDescription(cred.getDescription());
		targetCred.setCredential(cred);
		targetCred.setUser(user);
		targetCred.setDateStarted(new Date());
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
		
		return targetCred;
	}
	
	@Override
	@Transactional(readOnly = false)
	public void addCompetenceToCredential(long credentialId, Competence1 comp) 
			throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(
					Credential1.class, credentialId);
			
			/*
			 * if credential has draft version, that version is loaded and if credential 
			 * is published draft version will be created and attached to original credential
			 */
			Credential1 draftCred = null;
			if(cred.isHasDraft()) {
				draftCred = cred.getDraftVersion();
			} else if(cred.isPublished()) {
				draftCred = createDraftVersionOfCredential(credentialId);
				cred.setHasDraft(true);
				cred.setPublished(false);
				cred.setDraftVersion(draftCred);
			}
			
			Credential1 credToUpdate = draftCred != null ? draftCred : cred;
			CredentialCompetence1 cc = new CredentialCompetence1();
			cc.setCompetence(comp);
			cc.setCredential(credToUpdate);
			cc.setOrder(credToUpdate.getCompetences().size() + 1);
			saveEntity(cc);
			/* 
			 * If duration of added competence is greater than 0,
			 * update credential duration
			*/
			//TODO check if this requires select + update and if so, use hql update instead
			if(comp.getDuration() > 0) {
				credToUpdate.setDuration(credToUpdate.getDuration() + comp.getDuration());
			}
		} catch(Exception e) {
			throw new DbConnectionException("Error while adding competence to credential");
		}
		
	}
	
	private Credential1 createDraftVersionOfCredential(long originalCredentialId) {
		Credential1 originalCred = getCredential(originalCredentialId, false);
		
		Credential1 draftCred = new Credential1();
		draftCred.setDraft(true);
		draftCred.setPublished(false);
		draftCred.setCreatedBy(originalCred.getCreatedBy());
		draftCred.setTitle(originalCred.getTitle());
		draftCred.setDescription(originalCred.getDescription());
		draftCred.setCompetenceOrderMandatory(originalCred.isCompetenceOrderMandatory());
		draftCred.setStudentsCanAddCompetences(originalCred.isStudentsCanAddCompetences());
		draftCred.setManuallyAssignStudents(originalCred.isManuallyAssignStudents());
		draftCred.setDefaultNumberOfStudentsPerInstructor(originalCred.getDefaultNumberOfStudentsPerInstructor());
		draftCred.setDuration(originalCred.getDuration());
	    
		if(originalCred.getTags() != null) {
			for(Tag tag : originalCred.getTags()) {
				draftCred.getTags().add(tag);
			}
		}
		if(originalCred.getHashtags() != null) {
			for(Tag hashtag : originalCred.getHashtags()) {
				draftCred.getHashtags().add(hashtag);
			}
		}
	    
		saveEntity(draftCred);	

		List<CredentialCompetence1> comps = compManager.getCredentialCompetences(originalCredentialId, 
				false, false, true);
	    if(comps != null) {
    		for(CredentialCompetence1 cc : comps) {
    			CredentialCompetence1 cc1 = new CredentialCompetence1();
				cc1.setOrder(cc.getOrder());
				cc1.setCredential(draftCred);
				cc1.setCompetence(cc.getCompetence());
				saveEntity(cc1);
				draftCred.getCompetences().add(cc1);
    		}	
	    }
	    
		return draftCred;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CredentialData> getCredentialsWithIncludedCompetenceBasicData(long compId) 
			throws DbConnectionException {
		try {
			Competence1 comp = (Competence1) persistence.currentManager().load(Competence1.class, compId);
			String query = "SELECT cred.id, cred.title " +
					       "FROM CredentialCompetence1 credComp " +
					       "INNER JOIN credComp.credential cred " +
					       "WHERE credComp.competence = :comp " +
					       "AND cred.draft = :draft " +
					       "AND cred.deleted = :deleted";
			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
									.createQuery(query)
									.setEntity("comp", comp)
									.setBoolean("draft", false)
									.setBoolean("deleted", false)
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
		try {			
			String query = "SELECT tag " +
					       "FROM Credential1 cred " +
					       "INNER JOIN cred.tags tag " +
					       "WHERE cred.id = :credentialId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = persistence.currentManager()
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
		try {			
			String query = "SELECT hashtag " +
					       "FROM Credential1 cred " +
					       "INNER JOIN cred.hashtags hashtag " +
					       "WHERE cred.id = :credentialId";					    
			@SuppressWarnings("unchecked")
			List<Tag> res = persistence.currentManager()
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
			
	@Transactional(readOnly = false)
	private void updateTitleAndDescriptionForTargetCredentials(long credentialId) 
			throws DbConnectionException {
		try {	
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			String query = "UPDATE TargetCredential1 targetCred " +
					       "SET targetCred.title = :title, " +
					       "targetCred.description = :description " +
					       "WHERE targetCred.credential = :cred";					    

			persistence.currentManager()
				.createQuery(query)
				.setString("title", cred.getTitle())
				.setString("description", cred.getDescription())
				.setEntity("cred", cred)
				.executeUpdate();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while updating user credentials");
		}
	}
	
	@Transactional(readOnly = true)
	private List<TargetCredential1> getTargetCredentialsForCredential(long credentialId) 
			throws DbConnectionException {
		try {		
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			String query = "SELECT cred " +
					       "FROM TargetCredential1 cred " +
					       "WHERE cred.credential = :cred";					    
			@SuppressWarnings("unchecked")
			List<TargetCredential1> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("cred", cred)
				.list();
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
	private void updateTargetCredentialTags(long credentialId) 
			throws DbConnectionException {
		try {
			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(credentialId);
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
	private void updateTargetCredentialHashtags(long credentialId) 
			throws DbConnectionException {
		try {
			List<TargetCredential1> targetCredentials = getTargetCredentialsForCredential(credentialId);
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
	
	@Override
	@Transactional(readOnly = false)
	public void updateTargetCredentialsWithChangedData(long credentialId, CredentialChangeTracker changeTracker) 
			throws DbConnectionException {
		if(changeTracker.isPublished()) {
			if(changeTracker.isTagsChanged()) {
				updateTargetCredentialTags(credentialId);
			}
			if(changeTracker.isHashtagsChanged()) {
				updateTargetCredentialHashtags(credentialId);
			}
			if(changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged()) {
				updateTitleAndDescriptionForTargetCredentials(credentialId);
			}
		}
	}
	
	public List<CredentialBookmark> getBookmarkedByIds(long credId) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
			String query = "SELECT bookmark " +
						   "FROM CredentialBookmark bookmark " +
						   "WHERE bookmark.credential = :cred";
			
			@SuppressWarnings("unchecked")
			List<CredentialBookmark> bookmarks = persistence.currentManager()
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
	
}
