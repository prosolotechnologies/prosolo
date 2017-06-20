package org.prosolo.services.studentProfile.observations.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.messaging.Message;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.studentProfile.observations.ObservationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.studentProfile.observations.ObservationManager")
public class ObservationManagerImpl extends AbstractManagerImpl implements ObservationManager{

	private static final long serialVersionUID = -7710666335405883922L;
	
	private static Logger logger = Logger.getLogger(ObservationManagerImpl.class);
	
	@Inject
	private MessagingManager msgManager;

	@Override
	@Transactional(readOnly = true)
	public Observation getLastObservationForUser(long userId) throws DbConnectionException{
		try{
			String queryString = 
					"SELECT o " +
					"FROM Observation o " +
					"INNER JOIN fetch o.createdFor student " +
					"INNER JOIN fetch o.createdBy user " +
					"LEFT JOIN fetch o.symptoms sy " +
					"LEFT JOIN fetch o.suggestions su " +
					//"LEFT JOIN o.targetCredential targetCred " +
					"WHERE student.id = :id " +
						//"AND targetCred.id = :targetCredentialId " +
					"ORDER BY o.creationDate desc";
	
			Query query = persistence.currentManager().createQuery(queryString)
					.setLong("id", userId)
					//.setLong("targetCredentialId", targetCredentialId)
					.setMaxResults(1);
			
			return (Observation) query.uniqueResult();	
		} catch (Exception e) {
			throw new DbConnectionException("Observation cannot be loaded at the moment");
		}
	}
	
	@Override
	@Transactional
	public Map<String, Object> saveObservation(long id, Date date, String message, String note, List<Long> symptomIds,
			List<Long> suggestionIds, long creatorId, long studentId) 
					throws DbConnectionException {
		try {
			boolean insert = true;
			Observation observation = new Observation();
			if (id > 0) {
				insert = false;
				observation.setId(id);
				observation.setEdited(true);
			} 
			observation.setCreationDate(date);
			observation.setMessage(message);
			observation.setNote(note);
		    User creator = new User();
		    creator.setId(creatorId);
		    User student = new User();
		    student.setId(studentId);
		    observation.setCreatedBy(creator);
		    observation.setCreatedFor(student);
		    
		    Set<Symptom> symptoms = new HashSet<>();
			for (long sid : symptomIds) {
				Symptom s = new Symptom();
				s.setId(sid);
				symptoms.add(s);
			}
		    observation.setSymptoms(symptoms);
		    
			Set<Suggestion> suggestions = new HashSet<>();
			for (long sid : suggestionIds) {
				Suggestion s = new Suggestion();
				s.setId(sid);
				suggestions.add(s);
			}
		    observation.setSuggestions(suggestions);
		    
			observation =  saveEntity(observation);
			persistence.currentManager().evict(observation);
			
			Map<String, Object> result = new HashMap<>();
			result.put("observationId", observation.getId());
			
			if (insert && message != null && !message.isEmpty() && creatorId != studentId) {
				Message msg = msgManager.sendMessage(creatorId, studentId, message);
				result.put("message", msg);
			}

			return result;
		} catch (DbConnectionException dbce) {
			dbce.printStackTrace();
			throw dbce;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DbConnectionException("Error while saving observation");
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Observation> getObservations(long userId) throws DbConnectionException{
		try {
			String queryString = 
					"SELECT DISTINCT o " +
					"FROM Observation o " +
					"INNER JOIN FETCH o.createdFor student " +
					"INNER JOIN FETCH o.createdBy user " +
					"LEFT JOIN FETCH o.symptoms sy " +
					"LEFT JOIN FETCH o.suggestions su " +
					//"LEFT JOIN o.targetCredential targetCred " +
					"WHERE student.id = :id " +
						//"AND targetCred.id = :targetCredentialId " +
					"ORDER BY o.creationDate desc";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", userId);
			//query.setLong("targetCredentialId", targetCredentialId);
			
			return query.list();	
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw new DbConnectionException("Observations cannot be loaded at the moment");
		}
	}
}
