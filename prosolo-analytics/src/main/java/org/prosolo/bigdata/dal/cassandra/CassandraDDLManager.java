package org.prosolo.bigdata.dal.cassandra;
/**
@author Zoran Jeremic Apr 3, 2015
 *
 */

public interface CassandraDDLManager {

	void checkIfTablesExistsAndCreate(String keyspacename);

}

