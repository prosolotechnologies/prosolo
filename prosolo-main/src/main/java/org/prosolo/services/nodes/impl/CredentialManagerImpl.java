package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Activity1;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.common.domainmodel.credential.CredentialType1;
import org.prosolo.common.domainmodel.credential.TargetActivity1;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.event.context.data.LearningContextData;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.services.nodes.factory.CredentialDataFactory;
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
					data.getType(), data.isMandatoryFlow(), data.isPublished());

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
	public CredentialData getTargetCredentialData(long credentialId, long userId, 
			boolean loadCompetences) throws DbConnectionException {
		CredentialData credData = null;
		User user = (User) persistence.currentManager().load(User.class, userId);
		Credential1 cred = (Credential1) persistence.currentManager().load(
				Credential1.class, credentialId);
	    Credential1 cr1 = new Credential1();
	    cr1.setCreatedBy(cred.getCreatedBy());
		try {
			String query = "SELECT targetCred, user.id, user.name, user.lastname, user.avatarUrl " +
						   "FROM TargetCredential1 targetCred " + 
						   "INNER JOIN targetCred.createdBy user " + 
						   //"LEFT JOIN fetch targetCred.tags tags " +
						   //"LEFT JOIN fetch targetCred.hashtags hashtags " +
						   "WHERE targetCred.credential = :cred " +
						   "AND targetCred.user = :student";

			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(query)
					.setEntity("cred", cred)
					.setEntity("student", user)
					.uniqueResult();

			if (res != null) {
				TargetCredential1 targetCred = (TargetCredential1) res[0];

				long usrId = (long) res[1];
				String firstName = (String) res[2];
				String lastName = (String) res[3];
				String avatar = (String) res[4];
				User creator = new User();
				creator.setId(usrId);
				creator.setName(firstName);
				creator.setLastname(lastName);
				creator.setAvatarUrl(avatar);
				
				credData = credentialFactory.getCredentialData(creator, 
						credentialId, targetCred);
				
				if(loadCompetences) {
					List<CompetenceData1> targetCompData = getTargetCompetencesData(targetCred.getId());
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
	public CredentialData getCredentialData(long credentialId, boolean loadCreatorData,
			boolean loadCompetences) throws DbConnectionException {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("SELECT cred");
			if(loadCreatorData) {
				builder.append(", user.id, user.name, user.lastname, user.avatarUrl");
			}
			builder.append(" FROM Credential1 cred");
			if(loadCreatorData) {
				builder.append(" INNER JOIN cred.createdBy user");
			}
			//builder.append(" LEFT JOIN fetch cred.tags tags"); 
			//builder.append(" LEFT JOIN fetch cred.hashtags hashtags");
			builder.append(" WHERE cred.id = :credentialId AND cred.deleted = :deleted ");
//			String query = "SELECT cred, user.id, user.name, user.lastname, user.avatarUrl " +
//						   "FROM Credential1 cred " + 
//						   "INNER JOIN cred.createdBy user " + 
//						   "WHERE cred.id = :credentialId " +
//						   "AND cred.deleted = :deleted";

			logger.info("GET CREDENTIAL DATA QUERY: " + builder.toString());
			Object[] res = (Object[]) persistence.currentManager()
					.createQuery(builder.toString())
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false)
					.uniqueResult();

			if (res != null) {
				Credential1 cred = (Credential1) res[0];
				User user = null;
				if(loadCreatorData) {
					long usrId = (long) res[1];
					String firstName = (String) res[2];
					String lastName = (String) res[3];
					String avatar = (String) res[4];
					user = new User();
					user.setId(usrId);
					user.setName(firstName);
					user.setLastname(lastName);
					user.setAvatarUrl(avatar);
				}
				
				CredentialData credData = credentialFactory.getCredentialData(user, cred);
				
				if(loadCompetences) {
					List<CompetenceData1> compsData = getCompetencesData(credentialId);
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
	private List<CompetenceData1> getTargetCompetencesData(long targetCredentialId) throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			TargetCredential1 targetCred = (TargetCredential1) persistence.currentManager().load(
					TargetCredential1.class, targetCredentialId);
			String query = "SELECT targetComp, user.id, user.name, user.lastname, user.avatarUrl " +
					       "FROM TargetCompetence1 targetComp " + 
					       "INNER JOIN targetComp.createdBy user " +
					       //"LEFT JOIN fetch comp.tags tags " +
					       "WHERE targetComp.targetCredential = :targetCred";

			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("targetCred", targetCred)
				.list();

			if (res != null && !res.isEmpty()) {
				for (Object[] row : res) {
					TargetCompetence1 targetComp = (TargetCompetence1) row[0];
					
					long usrId = (long) row[1];
					String firstName = (String) row[2];
					String lastName = (String) row[3];
					String avatar = (String) row[4];
					User creator = new User();
					creator.setId(usrId);
					creator.setName(firstName);
					creator.setLastname(lastName);
					creator.setAvatarUrl(avatar);

					CompetenceData1 compData = competenceFactory.getCompetenceData(creator, targetComp);
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

	@Transactional(readOnly = true)
	private List<CompetenceData1> getCompetencesData(long credentialId) throws DbConnectionException {
		List<CompetenceData1> result = new ArrayList<>();
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			
			String query = "SELECT comp, credComp.id, credComp.order, user.id, user.name, user.lastname, user.avatarUrl " + 
						   "FROM Competence1 comp " +
						   "INNER JOIN comp.credentialCompetence credComp " + 
						   "INNER JOIN comp.createdBy user " +
						   //"LEFT JOIN fetch comp.tags tags " +
						   "WHERE credComp.credential = :credential " + 
						   "AND comp.deleted = :deleted " + 
						   "AND credComp.deleted = :deleted";

			@SuppressWarnings("unchecked")
			List<Object[]> res = persistence.currentManager()
				.createQuery(query)
				.setEntity("credential", cred)
				.setBoolean("deleted", false)
				.list();

			if (res != null && !res.isEmpty()) {
				for (Object[] row : res) {
					Competence1 comp = (Competence1) row[0];
					long credCompId = (long) row[1];
					int order = (int) row[2];
					long usrId = (long) row[3];
					String firstName = (String) row[4];
					String lastName = (String) row[5];
					String avatar = (String) row[6];
					User creator = new User();
					creator.setId(usrId);
					creator.setName(firstName);
					creator.setLastname(lastName);
					creator.setAvatarUrl(avatar);
					
					CompetenceData1 compData = competenceFactory.getCompetenceData(creator, comp, 
							credCompId, order);
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
	@Transactional(readOnly = true)
	public CredentialData getCredentialDataForCreator(long credentialId, long creatorId) 
			throws DbConnectionException {
		try {
			User user = (User) persistence.currentManager().load(User.class, creatorId);
			
			String query = "SELECT cred " +
						   "FROM Credential1 cred " + 
						   "LEFT JOIN fetch cred.tags tags " +
						   "LEFT JOIN fetch cred.hashtags hashtags " +
						   "WHERE cred.id = :credentialId " +
						   "AND cred.deleted = :deleted " +
						   "AND cred.createdBy = :user";

			Credential1 res = (Credential1) persistence.currentManager()
					.createQuery(query)
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false)
					.setEntity("user", user)
					.uniqueResult();
			
			if(res == null) {
				return null;
			}

			return credentialFactory.getCredentialData(null, res);
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
			cred.setTitle(data.getTitle());
			cred.setDescription(data.getDescription());
		    cred.setTags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getTagsString())));		     
		    cred.setHashtags(new HashSet<Tag>(tagManager.parseCSVTagsAndSave(data.getHashtagsString())));
		    cred.setCompetenceOrderMandatory(data.isMandatoryFlow());
		    cred.setPublished(data.isPublished());
		    
		    saveEntity(cred);

		    eventFactory.generateEvent(EventType.Edit, user, cred);

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
	@Transactional
	public CredentialData enrollInCredential(long credentialId, User user, LearningContextData context) 
			throws DbConnectionException {
		try {
			Credential1 cred = getFullCredentialData(credentialId);
			TargetCredential1 targetCred = createTargetCredential(cred, user);
			
			Map<String, String> params = null;
			
			if(cred.getType() == CredentialType1.UNIVERSITY_CREATED) {
	    		//TODO assign student to instructor automatically if automatic assign is turned on
	    	}
			CredentialData cd = credentialFactory.getFullCredentialData(targetCred);
			
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
		
		List<CredentialCompetence1> credComps = getFullCredentialCompetencesData(cred.getId());
		for(CredentialCompetence1 cc : credComps) {
			TargetCompetence1 targetComp = createTargetCompetence(targetCred, cc);
			targetCred.getTargetCompetences().add(targetComp);
		}
		
		return targetCred;
	}

	private TargetCompetence1 createTargetCompetence(TargetCredential1 targetCred, 
			CredentialCompetence1 cc) {
		TargetCompetence1 targetComp = new TargetCompetence1();
		Competence1 comp = cc.getCompetence();
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
		
		List<Activity1> activities = comp.getActivities();
		if(activities != null) {
			for(Activity1 act : activities) {
				TargetActivity1 ta = createTargetActivity(targetComp, act);
				targetComp.getTargetActivities().add(ta);
			}
		}
		
		return targetComp;
	}

	private TargetActivity1 createTargetActivity(TargetCompetence1 targetComp, Activity1 act) {
		TargetActivity1 targetAct = new TargetActivity1();
		targetAct.setTargetCompetence(targetComp);
		targetAct.setActivity(act);
		targetAct.setOrder(act.getOrderInCompetence());
		targetAct.setDuration(act.getDuration());
		return saveEntity(targetAct);
	}

	@Transactional(readOnly = true)
	private Credential1 getFullCredentialData(long credentialId) throws DbConnectionException {
		try {
			String query = "SELECT cred " +
					   "FROM Credential1 cred " + 
					   "LEFT JOIN fetch cred.tags tags " +
					   "LEFT JOIN fetch cred.hashtags hashtags " +
					   "WHERE cred.id = :credentialId " +
					   "AND cred.deleted = :deleted " +
					   "AND cred.published = :published " +
					   "AND cred.draft = :draft " ;
			
			Credential1 res = (Credential1) persistence.currentManager()
					.createQuery(query)
					.setLong("credentialId", credentialId)
					.setBoolean("deleted", false)
					.setBoolean("published", true)
					.setBoolean("draft", false)
					.uniqueResult();
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential data");
		}
	}

	private List<CredentialCompetence1> getFullCredentialCompetencesData(long credentialId) throws DbConnectionException {
		try {
			Credential1 cred = (Credential1) persistence.currentManager().load(Credential1.class, 
					credentialId);
			String query = "SELECT credComp " +
					   "FROM CredentialCompetence1 credComp " + 
					   "INNER JOIN fetch credComp.competence comp " +
					   "LEFT JOIN fetch comp.tags tags " +
					   "LEFT JOIN fetch comp.activities act " +
					   "WHERE credComp.credential = :cred " +
					   "AND comp.deleted = :deleted " +
					   "AND comp.published = :published " +
					   "AND comp.draft = :draft " +
					   "AND (act is null or " +
					   "(act.deleted = :deleted " +
					   "AND act.published = :published " +
					   "AND act.draft = :draft))";
			
			@SuppressWarnings("unchecked")
			List<CredentialCompetence1> res = persistence.currentManager()
					.createQuery(query)
					.setEntity("cred", cred)
					.setBoolean("deleted", false)
					.setBoolean("published", true)
					.setBoolean("draft", false)
					.list();
			
			if(res == null) {
				return new ArrayList<>();	
			}
			
			return res;
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while retrieving credential data");
		}
	}
	
}
