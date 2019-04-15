package org.prosolo.services.studentProfile.observations;

import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.observations.Suggestion;

public interface SuggestionManager {

	List<Suggestion> getAllSuggestions() throws DbConnectionException;

	Suggestion saveSuggestion(long id, String description) throws DbConnectionException;
	
	void deleteSuggestion(long id) throws DbConnectionException;
	
	boolean isSuggestionUsed(long suggestionId) throws DbConnectionException;
	
	List<Long> getSuggestionIdsWithName(String name) throws DbConnectionException;
	
	String getSuggestionNameForId(long id) throws DbConnectionException;
	
	void saveSuggestions(List<String> suggestions) throws DbConnectionException;
}