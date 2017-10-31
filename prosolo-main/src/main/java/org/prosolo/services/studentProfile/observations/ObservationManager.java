package org.prosolo.services.studentProfile.observations;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.observations.Observation;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.data.Result;
import org.prosolo.services.event.EventException;

public interface ObservationManager {

	Observation getLastObservationForUser(long userId) throws DbConnectionException;

	void saveObservation(long id, Date date, String message, String note, List<Long> symptomIds,
											   List<Long> suggestionIds, UserContextData context, long studentId)
			throws DbConnectionException, EventException;

	Result<Void> saveObservationAndGetEvents(long id, Date date, String message, String note, List<Long> symptomIds,
											   List<Long> suggestionIds, UserContextData context, long studentId)
			throws DbConnectionException, EventException;
	
	List<Observation> getObservations(long userId) throws DbConnectionException;
}