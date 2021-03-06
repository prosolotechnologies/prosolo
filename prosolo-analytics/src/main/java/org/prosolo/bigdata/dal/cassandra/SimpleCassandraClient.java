package org.prosolo.bigdata.dal.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.prosolo.bigdata.dal.cassandra.impl.SimpleCassandraClientImpl;
import org.prosolo.bigdata.dal.cassandra.impl.TableNames;

/**
 * @author Zoran Jeremic Apr 3, 2015
 *
 */

public interface SimpleCassandraClient {

	void connect(String node, int port, String keyspace, int replicationFactor);

	void createSchemaIfNotExists(Session session, String schemaName,
			int replicationFactor);

	void updateCurrentTimestamp(TableNames tablename, Long timestamp);

	Long getCurrentTimestampForTable(TableNames tablename);

	void close();

	Session getSession();

	Cluster getCluster();
	String getSchemaName();

}
