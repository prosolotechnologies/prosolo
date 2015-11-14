package org.prosolo.bigdata.dal.cassandra;

import java.util.List;

import org.prosolo.bigdata.events.analyzers.ObservationType;

import com.datastax.driver.core.Row;

/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public interface UserObservationsDBManager {

	boolean updateUserObservationsCounter(Long date, Long userid, long login,
			long lmsuse, long resourceview, long discussionview);

	List<Row> findAllUsersObservationsForDate(Long date);

	boolean updateUserProfileActionsObservationCounter(Long date, Long userid, ObservationType observationType);

}
