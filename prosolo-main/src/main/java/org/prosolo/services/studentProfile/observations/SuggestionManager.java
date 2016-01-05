package org.prosolo.services.studentProfile.observations;

import java.util.List;

import org.prosolo.common.domainmodel.observations.Suggestion;
import org.prosolo.services.lti.exceptions.DbConnectionException;

public interface SuggestionManager {

	List<Suggestion> getAllSuggestions() throws DbConnectionException;

	public Suggestion saveSuggestion(long id, String description) throws DbConnectionException;
	
	public void deleteSuggestion(long id) throws DbConnectionException;
	
	public boolean isSuggestionUsed(long suggestionId) throws DbConnectionException;
	
	public List<Long> getSuggestionIdsWithName(String name) throws DbConnectionException;
	
	public String getSuggestionNameForId(long id) throws DbConnectionException;
}