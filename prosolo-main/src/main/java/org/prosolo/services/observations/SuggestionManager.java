package org.prosolo.services.observations;

import java.util.List;

import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.common.domainmodel.observations.Symptom;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.springframework.transaction.annotation.Transactional;

public interface SuggestionManager {

	List<Suggestion> getAllSuggestions() throws DbConnectionException;

	public Suggestion saveSuggestion(long id, String description) throws DbConnectionException;
	
	public void deleteSuggestion(long id) throws DbConnectionException;
	
	public boolean isSuggestionUsed(long suggestionId) throws DbConnectionException;
	
	public List<Long> getSuggestionIdsWithName(String name) throws DbConnectionException;
	
	public String getSuggestionNameForId(long id) throws DbConnectionException;
}