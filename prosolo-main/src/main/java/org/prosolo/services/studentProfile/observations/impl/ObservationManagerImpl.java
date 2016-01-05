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
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.common.domainmodel.user.SimpleOfflineMessage;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.MessagingManager;
import org.prosolo.services.lti.exceptions.DbConnectionException;
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
					"INNER JOIN fetch o.createdBy user "+
					"LEFT JOIN fetch o.symptoms sy "+
					"LEFT JOIN fetch o.suggestions su "+
					"WHERE student.id = :id " +
					"ORDER BY o.creationDate desc";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", userId);
			query.setMaxResults(1);
			
			return (Observation) query.uniqueResult();	
		}catch(Exception e){
			throw new DbConnectionException("Observation cannot be loaded at the moment");
		}
	}
	
	@Override
	@Transactional
	public Map<String, Object> saveObservation(long id, String message, String note, List<Long> symptomIds,
			List<Long> suggestionIds, long creatorId, long studentId) throws DbConnectionException {
		try{
			boolean insert = true;
			Observation observation = new Observation();
			if(id > 0){
				insert = false;
				observation.setId(id);
			}
			observation.setCreationDate(new Date());
			observation.setMessage(message);
			observation.setNote(note);
		    User creator = new User();
		    creator.setId(creatorId);
		    User student = new User();
		    student.setId(studentId);
		    observation.setCreatedBy(creator);
		    observation.setCreatedFor(student);
		    
		    Set<Symptom> symptoms = new HashSet<>();
		    for(long sid:symptomIds){
		    	Symptom s = new Symptom();
		    	s.setId(sid);
		    	symptoms.add(s);
		    }
		    observation.setSymptoms(symptoms);
		    
		    Set<Suggestion> suggestions = new HashSet<>();
		    for(long sid:suggestionIds){
		    	Suggestion s = new Suggestion();
		    	s.setId(sid);
		    	suggestions.add(s);
		    }
		    observation.setSuggestions(suggestions);
		    
			observation =  saveEntity(observation);
			persistence.currentManager().evict(observation);
			
			Map<String, Object> result = new HashMap<>();
			result.put("observationId", observation.getId());
			
			if(insert && message != null && !message.isEmpty() && creatorId != studentId){
				SimpleOfflineMessage msg = msgManager.sendMessage(creatorId, studentId, message);
				result.put("message", msg);
			}

			return result;
		}catch(DbConnectionException dbce){
			dbce.printStackTrace();
			throw dbce;
		}catch(Exception e){
			e.printStackTrace();
			throw new DbConnectionException("Error while saving observation");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Observation> getObservations(long userId) throws DbConnectionException{
		try{
			String queryString = 
					"SELECT distinct o " +
					"FROM Observation o " +
					"INNER JOIN fetch o.createdFor student " +
					"INNER JOIN fetch o.createdBy user "+
					"LEFT JOIN fetch o.symptoms sy "+
					"LEFT JOIN fetch o.suggestions su "+
					"WHERE student.id = :id " +
					"ORDER BY o.creationDate desc";
	
			Query query = persistence.currentManager().createQuery(queryString);
			query.setLong("id", userId);
			
			return query.list();	
		}catch(Exception e){
			throw new DbConnectionException("Observations cannot be loaded at the moment");
		}
	}
}
