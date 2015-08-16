package org.prosolo.bigdata.dal.cassandra.impl;

import java.util.List;

import org.prosolo.bigdata.common.dal.pojo.ActivityAccessCount;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public interface AnalyzedResultsDBManager {
	@Deprecated
	void insertFrequentCompetenceActivities(long competenceid,
			List<Long> activities);

	List<ActivityAccessCount> findFrequentCompetenceActivities(long competenceId);

}
