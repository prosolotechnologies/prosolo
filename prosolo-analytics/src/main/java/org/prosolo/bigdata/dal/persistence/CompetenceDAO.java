package org.prosolo.bigdata.dal.persistence;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.IllegalDataStateException;

import java.util.Date;

public interface CompetenceDAO {

	void changeVisibilityForCompetence(long compId) throws DbConnectionException;

	Date getScheduledVisibilityUpdateDate(long compId);
	
	void publishCompetences(long credId, long userId) throws DbConnectionException, IllegalDataStateException;
}