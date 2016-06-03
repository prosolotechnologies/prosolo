package org.prosolo.services.studentProfile.observations.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.studentProfile.observations.SuggestionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.studentProfile.observations.SuggestionManager")
public class SuggestionManagerImpl extends AbstractManagerImpl implements SuggestionManager{

	private static final long serialVersionUID = 3794586060152562963L;
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Suggestion> getAllSuggestions() throws DbConnectionException {
		try {
			Session session = persistence.currentManager();
			Criteria criteria = session.createCriteria(Suggestion.class);
			return criteria.list();
		} catch (Exception e) {
			throw new DbConnectionException();
		}
	}
	
	@Override
	@Transactional(readOnly = false)
	public Suggestion saveSuggestion(long id, String description) throws DbConnectionException {
		try {
			Suggestion s = new Suggestion();
			if(id > 0) {
				s.setId(id);
			}
			s.setDescription(description);
			return saveEntity(s);
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			throw new DbConnectionException("Error while saving suggestion");
		}
	}
	
	@Override
	@Transactional (readOnly = false)
	public void deleteSuggestion(long id) throws DbConnectionException {
		try{
			Suggestion s = new Suggestion();
			s.setId(id);
			persistence.currentManager().delete(s);
		}catch(Exception e){
			throw new DbConnectionException("Error while deleting suggestion");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isSuggestionUsed(long suggestionId) throws DbConnectionException {
		try{
			String query = 
				"SELECT COUNT(o) " +
				"FROM Observation o " +
				"INNER JOIN o.suggestions s " +
				"WHERE s.id = :id";
			
			Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("id", suggestionId)
				.uniqueResult();
			
			return result > 0;
		}catch(Exception e){
			throw new DbConnectionException("Error. Please try again.");
		}
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<Long> getSuggestionIdsWithName(String name) throws DbConnectionException {
		try{
			String query = 
				"SELECT s.id " +
				"FROM Suggestion s " +
				"WHERE s.description = :desc";
			
			@SuppressWarnings("unchecked")
			List<Long> result = persistence.currentManager().createQuery(query)
				.setString("desc", name)
				.list();
			
			if (result != null && !result.isEmpty()) {
				return result;
			}
	
			return new ArrayList<Long>();
		}catch(Exception e){
			throw new DbConnectionException("Error while validating suggestion name");
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getSuggestionNameForId(long id) throws DbConnectionException {
		try{
			String query = 
				"SELECT s.description " +
				"FROM Suggestion s " +
				"WHERE s.id = :id";
			
			return (String) persistence.currentManager().createQuery(query)
				.setLong("id", id)
				.uniqueResult();
			
		}catch(Exception e){
			throw new DbConnectionException("Error. Please try again.");
		}
	}
}
