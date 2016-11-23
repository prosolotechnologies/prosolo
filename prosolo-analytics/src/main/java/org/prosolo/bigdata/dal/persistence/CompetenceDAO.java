package org.prosolo.bigdata.dal.persistence;

import java.util.Date;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;

public interface CompetenceDAO {

	void setPublicVisibilityForCompetence(long compId) throws DbConnectionException;

	Date getScheduledVisibilityUpdateDate(long compId);
}