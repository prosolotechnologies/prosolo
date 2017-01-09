package org.prosolo.services.nodes.factory;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceActivity1;
import org.prosolo.common.domainmodel.credential.CredentialCompetence1;
import org.prosolo.services.nodes.DefaultManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompetenceCloneFactory {
	
	private static Logger logger = Logger.getLogger(CompetenceCloneFactory.class);
	
	@Inject 
	private DefaultManager defaultManager;
	@Inject 
	private ActivityCloneFactory activityCloneFactory;
	
	/**
	 * Creates a new {@link CredentialCompetence1} instance that is a clone of the given original. The method internally 
	 * calls the {@link #clone(Competence1)} method to duplicate a competence. Newly created CredentialCompetence1 is not 
	 * persisted as it does not have a reference to a credential at this point.
	 * 
	 * @param credComp CredentialCompetence1 instance to be cloned
	 * @return newly created instance of the CredentialCompetence1 class
	 */
	@Transactional (readOnly = false)
	public CredentialCompetence1 clone(CredentialCompetence1 original) {
		try {
			Competence1 comp = clone(original.getCompetence());
			
			CredentialCompetence1 credComp = new CredentialCompetence1();
			credComp.setCompetence(comp);
			credComp.setOrder(original.getOrder());
			return credComp;
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning credential competence");
		}
	}

	/**
	 * Creates a new {@link Comeptence1} instance that is a clone of the given instance. This method also makes a copy of each 
	 * activity where the new competence will reference the copies of activities. 
	 * 
	 * @param original the original competence to be cloned
	 * @return newly created competence
	 */
	@Transactional (readOnly = false)
	public Competence1 clone(Competence1 original) {
		try {
			Competence1 competence = new Competence1();
			
			competence.setTitle(original.getTitle());
			competence.setDescription(original.getDescription());
			competence.setDateCreated(new Date());
			competence.setTags(new HashSet<Tag>(original.getTags()));
			competence.setCreatedBy(original.getCreatedBy());
			competence.setDuration(original.getDuration());
			competence.setStudentAllowedToAddActivities(original.isStudentAllowedToAddActivities());
			competence.setType(original.getType());
			competence = defaultManager.saveEntity(competence);
			
			List<CompetenceActivity1> activities = original.getActivities();
			
			for (CompetenceActivity1 compActivity : activities) {
				CompetenceActivity1 clone = activityCloneFactory.clone(compActivity);
				clone.setCompetence(competence);
				clone = defaultManager.saveEntity(clone);
				
				competence.getActivities().add(clone);
			}
			
			return defaultManager.saveEntity(competence);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while cloning competence");
		}
	}
	
}
