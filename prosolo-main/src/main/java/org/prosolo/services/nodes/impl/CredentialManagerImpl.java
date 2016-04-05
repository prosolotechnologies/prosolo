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

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
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
import org.prosolo.services.nodes.impl.util.CredentialPublishTransition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.CredentialManager")
public class CredentialManagerImpl extends AbstractManagerImpl implements CredentialManager {

	private static final long serialVersionUID = -2783669846949034832L;

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
	@Qualifier("taskExecutor") 
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Override
	@Transactional(readOnly = false)
	public Credential1 saveNewCredential(CredentialData data, User createdBy) throws DbConnectionException {
		Credential1 cred = null;
		try {
			cred = resourceFactory.createCredential(data.getTitle(), data.getDescription(),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getTagsString())),
					new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getHashtagsString())), createdBy,
					data.getType(), data.isMandatoryFlow(), data.isPublished());

			if(data.getCompetences() != null) {
				for(CompetenceData1 cd : data.getCompetences()) {
					CredentialCompetence1 cc = new CredentialCompetence1();
					cc.setOrder(cd.getOrder());
					cc.setCredential(cred);
					Competence1 comp = (Competence1) persistence.currentManager().load(
							Competence1.class, cd.getCompetenceId());
					cc.setCompetence(comp);
					saveEntity(comp);
				}
			}
			eventFactory.generateEvent(EventType.Create, createdBy, cred);

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
	public Credential1 deleteCredential(long credId) throws DbConnectionException {
		try {
			if(credId > 0) {
				Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, credId);
				cred.setDeleted(true);
	
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
						credentialId, res, true);
				
				if(credData != null && loadCompetences) {
					List<CompetenceData1> targetCompData = compManager
							.getTargetCompetencesData(res.getId());
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
			Credential1 cred = getCredential(credentialId, loadCreatorData, loadCompetences);

			if (cred != null) {
				User createdBy = loadCreatorData ? cred.getCreatedBy() : null;
				CredentialData credData = credentialFactory.getCredentialData(createdBy, cred, true);
				
				if(loadCompetences) {
					List<CompetenceData1> compsData = getCredentialCompetencesData(credentialId, 
							false, false , false);
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
	private Credential1 getCredential(long credentialId, boolean loadCreatorData,
			boolean loadCompetences) throws DbConnectionException {
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

	@Transactional(readOnly = true)
	private List<CompetenceData1> getCredentialCompetencesData(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities) throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			List<CredentialCompetence1> res = getCredentialCompetences(credentialId, 
					loadCreator, loadTags, loadActivities);

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
	
	@Transactional(readOnly = true)
	private List<CredentialCompetence1> getCredentialCompetences(long credentialId, boolean loadCreator, 
			boolean loadTags, boolean loadActivities) throws DbConnectionException {
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
				builder.append("LEFT JOIN fetch comp.activities act ");
			}
			builder.append("WHERE credComp.credential = :credential " + 
					   "AND comp.deleted = :deleted " + 
					   "AND (comp.published = :published OR (comp.published = :notPublished " +
					   "AND comp.hasDraft = :hasDraft)) " +
					   "AND comp.draft = :draft " +
					   "AND credComp.deleted = :deleted ");
			if(loadActivities) {
				builder.append("AND (act is null or " +
					   "(act.deleted = :deleted " +
					   "AND (act.published = :published OR (act.published = :notPublished " +
					   "AND act.hasDraft = :hasDraft)) " +
					   "AND act.draft = :draft)) ");
			}
			
			builder.append("ORDER BY credComp.order");

			@SuppressWarnings("unchecked")
			List<CredentialCompetence1> res = persistence.currentManager()
				.createQuery(builder.toString())
				.setEntity("credential", cred)
				.setBoolean("deleted", false)
				.setBoolean("published", true)
				.setBoolean("notPublished", false)
				.setBoolean("draft", false)
				.setBoolean("hasDraft", true)
				.list();

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
						credData = credentialFactory.getCredentialData(null, draftCred, true);
					}	
				} else {
					credData = credentialFactory.getCredentialData(null, res, true);
				}
				if(credData != null && loadCompetences) {
					List<CompetenceData1> compsData = getCredentialCompetencesData(credData.getId(),
							true, false, false);
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
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					data.getId());
			/*
			 * draft should be created if something changed, draft option is chosen 
			 * and credential was published before this update
			*/
			CredentialPublishTransition publishTransition = (!data.isPublished() && data.isPublishedChanged()) ? 
					CredentialPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION :
					CredentialPublishTransition.NO_TRANSITION;
			/*
			 * check if published option was chosen, it was draft before update and 
			 * draft version (and not original credential) of credential is updated.
			 * Last check added because it is possible for original credential to be
			 * draft and that it doesn't have draft version because it was never published.
			*/
			if(publishTransition == CredentialPublishTransition.NO_TRANSITION) {
				publishTransition = (data.isPublished() && data.isPublishedChanged() && data.isDraft()) ? 
						publishTransition = CredentialPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED :
						publishTransition;
			}
			
			Credential1 updatedCredential = updateCredentialData(cred, publishTransition, data);		  
		    
		    /* determine if changes should be propagated
		     * to users enrolled in a credential */
		    boolean shouldPropagateChanges = data.isPublished() && (data.isTitleChanged() 
		    		|| data.isDescriptionChanged() || data.isTagsStringChanged() 
		    		|| data.isHashtagsStringChanged());
		    /*
		     * pass to event shouldPropagateChanges or each attribute that
		     * changed so observer can know if changes should be propagated.
		    */
		    eventFactory.generateEvent(EventType.Edit, user, cred);

		    return updatedCredential;
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
	 * Updates all credential updatable fields and takes into
	 * account transitions between credential publish status
	 * so if status is changed from published to draft, draft
	 * version is inserted and original credential is referencing
	 * this version. If status is changed from draft to published,
	 * it is checked if credential has been published once and if 
	 * it has, that means that draft version exists and this version
	 * is deleted. Also, CredentialData object will be updated with 
	 * new ids for credential and competences and changed data too.
	 * @param cred
	 * @param publishTransition
	 * @param data
	 */
	@Transactional(readOnly = true)
	private Credential1 updateCredentialData(Credential1 cred, CredentialPublishTransition publishTransition,
			CredentialData data) {
		Credential1 credToUpdate = null;
		switch(publishTransition) {
			case FROM_PUBLISHED_TO_DRAFT_VERSION:
				credToUpdate = new Credential1();
				credToUpdate.setDraft(true);
				credToUpdate.setCreatedBy(cred.getCreatedBy());
				cred.setHasDraft(true);
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
	    if(publishTransition == CredentialPublishTransition.NO_TRANSITION) {
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
	    
		if(publishTransition == CredentialPublishTransition.FROM_PUBLISHED_TO_DRAFT_VERSION) {
			saveEntity(credToUpdate);
			cred.setDraftVersion(credToUpdate);
		}
		
		if(publishTransition == CredentialPublishTransition.FROM_DRAFT_VERSION_TO_PUBLISHED) {
			deleteCredentialCompetences(credToUpdate.getId());
		}

		List<CompetenceData1> comps = data.getCompetences();
	    if(comps != null) {
	    	if(publishTransition == CredentialPublishTransition.NO_TRANSITION) {
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
		    		}
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

	private List<TargetCredential1> getTargetCredentialsForCredential(long id) {
//		try {
//			
//			String query = "SELECT targetCred " +
//						   "FROM TargetCredential1 targetCred " + 
//						   "LEFT JOIN fetch cred.tags tags " +
//						   "LEFT JOIN fetch cred.hashtags hashtags " +
//						   "WHERE cred.id = :credentialId " +
//						   "AND cred.deleted = :deleted " +
//						   "AND cred.createdBy = :user";
//
//			Credential1 res = (Credential1) persistence.currentManager()
//					.createQuery(query)
//					.setLong("credentialId", credentialId)
//					.setBoolean("deleted", false)
//					.setEntity("user", user)
//					.uniqueResult();
//			
//			if(res == null) {
//				return null;
//			}
//
//			return credentialFactory.getCredentialData(null, res);
//		} catch (Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//			throw new DbConnectionException("Error while loading credential data");
//		}
		return null;
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
			Credential1 cred = getCredential(credentialId, false, false);
			TargetCredential1 targetCred = createTargetCredential(cred, user);
			
			Map<String, String> params = null;
			
			if(cred.getType() == CredentialType1.UNIVERSITY_CREATED) {
	    		//TODO assign student to instructor automatically if automatic assign is turned on
	    	}
			CredentialData cd = credentialFactory.getFullCredentialData(targetCred, true);
			
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
		
		List<CredentialCompetence1> credComps = getCredentialCompetences(cred.getId(), 
				false, true, true);
		for(CredentialCompetence1 cc : credComps) {
			TargetCompetence1 targetComp = compManager.createTargetCompetence(targetCred, cc);
			targetCred.getTargetCompetences().add(targetComp);
		}
		
		return targetCred;
	}
	
}
