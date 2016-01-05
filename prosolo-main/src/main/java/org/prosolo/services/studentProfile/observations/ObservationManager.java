package org.prosolo.services.studentProfile.observations;

import java.util.List;
import java.util.Map;

import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.services.lti.exceptions.DbConnectionException;

public interface ObservationManager {

	Observation getLastObservationForUser(long userId) throws DbConnectionException;

	public Map<String, Object> saveObservation(long id, String message, String note, List<Long> symptomIds,
			List<Long> suggestionIds, long creatorId, long studentId) throws DbConnectionException;
	
	public List<Observation> getObservations(long userId) throws DbConnectionException;
}