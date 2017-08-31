package org.prosolo.services.studentProfile.observations;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.event.context.data.UserContextData;

public interface ObservationManager {

	Observation getLastObservationForUser(long userId) throws DbConnectionException;

	public Map<String, Object> saveObservation(long id, Date date, String message, String note, List<Long> symptomIds,
											   List<Long> suggestionIds, UserContextData context, long studentId) throws DbConnectionException;
	
	public List<Observation> getObservations(long userId) throws DbConnectionException;
}