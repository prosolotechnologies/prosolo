package org.prosolo.bigdata.dal.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public interface SimpleCassandraClient {

	void connect(String node, String keyspace, int replicationFactor);

	void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor);

	void close();

	Session getSession();

	Cluster getCluster();

}
