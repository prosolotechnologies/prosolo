package org.prosolo.bigdata.dal.cassandra;
/**
 * @author Zoran Jeremic, Oct 11, 2015
 *
 */
public interface UserObservationsDBManager {

	boolean updateUserObservationsCounter(Long date, Long userid, long login,
			long lmsuse, long resourceview, long discussionview);

}
