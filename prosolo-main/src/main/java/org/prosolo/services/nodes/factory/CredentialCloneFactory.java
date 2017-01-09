package org.prosolo.services.nodes.factory;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CredentialCloneFactory {
	
	private static Logger logger = Logger.getLogger(CredentialCloneFactory.class);
	
	@Inject 
	private DefaultManager defaultManager;
	@Inject 
	private CompetenceCloneFactory competenceCloneFactory;
	
	/**
	 * This method creates a clone of the original credential in terms of its structure: competences and activities.
	 * It will also create appropriate copies of all competences and activities.
	 * 
	 * @param credentialData id of the credential to be cloned
	 * @return id of the newly created credential
	 */
	@Transactional (readOnly = false)
	public long clone(long credentialId) {
		try {
			Credential1 original = defaultManager.loadResource(Credential1.class, credentialId);
	
			Credential1 cred = new Credential1();
			cred.setTitle("Copy of " + original.getTitle());
			cred.setDescription(original.getDescription());
			cred.setCreatedBy(original.getCreatedBy());
			cred.setDateCreated(new Date());
			cred.setTags(new HashSet<Tag>(original.getTags()));
			cred.setHashtags(new HashSet<Tag>(original.getHashtags()));
			cred.setCompetenceOrderMandatory(original.isCompetenceOrderMandatory());
			cred.setDuration(original.getDuration());
			cred.setStudentsCanAddCompetences(original.isStudentsCanAddCompetences());
			cred.setManuallyAssignStudents(original.isManuallyAssignStudents());
			cred.setDefaultNumberOfStudentsPerInstructor(original.getDefaultNumberOfStudentsPerInstructor());
			cred.setType(original.getType());
			cred = defaultManager.saveEntity(cred);
			
			List<CredentialCompetence1> competences = original.getCompetences();
			
			for (CredentialCompetence1 credComp : competences) {
				CredentialCompetence1 clone = competenceCloneFactory.clone(credComp);
				clone.setCredential(cred);
				clone = defaultManager.saveEntity(clone);
				
				cred.getCompetences().add(clone);
			}
			return cred.getId();
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning credential");
		}
	}
	
}
