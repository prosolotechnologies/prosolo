package org.prosolo.services.studentProfile.observations;

import java.util.Date;
import java.util.List;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;

public interface ObservationManager {

	Observation getLastObservationForUser(long userId) throws DbConnectionException;

	void saveObservation(long id, Date date, String message, String note, List<Long> symptomIds,
											   List<Long> suggestionIds, UserContextData context, long studentId)
			throws DbConnectionException;

	Result<Void> saveObservationAndGetEvents(long id, Date date, String message, String note, List<Long> symptomIds,
											   List<Long> suggestionIds, UserContextData context, long studentId)
			throws DbConnectionException;
	
	List<Observation> getObservations(long userId) throws DbConnectionException;
}