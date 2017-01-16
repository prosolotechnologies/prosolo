package org.prosolo.bigdata.dal.persistence;

import java.util.Date;

import org.prosolo.bigdata.common.exceptions.CompetenceEmptyException;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;

public interface CompetenceDAO {

	void changeVisibilityForCompetence(long compId) throws DbConnectionException;

	Date getScheduledVisibilityUpdateDate(long compId);
	
	void publishCompetences(long credId, long userId) 
			throws DbConnectionException, CompetenceEmptyException;
}