package org.prosolo.bigdata.dal.cassandra.impl;/**
 * Created by zoran on 22/11/16.
 */

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.common.config.CommonSettings;

/**
 * zoran 22/11/16
 */
public class CassandraAdminImpl {
    protected final static Logger logger = Logger
            .getLogger(CassandraAdminImpl.class.getName());
    public void dropSchema(){
        DBServerConfig dbConfig = Settings.getInstance().config.dbConfig.dbServerConfig;
        String keyspace = Settings.getInstance().config.dbConfig.dbServerConfig.dbName
                + CommonSettings.getInstance().config.getNamespaceSufix();
        PoolingOptions poolingOpts=new PoolingOptions();
        poolingOpts.setCoreConnectionsPerHost(HostDistance.REMOTE, 8);
        poolingOpts.setMaxConnectionsPerHost(HostDistance.REMOTE, 200);
        poolingOpts.setConnectionsPerHost(HostDistance.REMOTE, 8, 8);
        poolingOpts.setMaxRequestsPerConnection(HostDistance.REMOTE, 128);
        poolingOpts.setNewConnectionThreshold(HostDistance.REMOTE,100);
        Cluster cluster = Cluster.builder()
                .withPoolingOptions( poolingOpts)
                .withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
                .withPort(dbConfig.dbPort)
                .addContactPoint(dbConfig.dbHost).build();
        Metadata metadata = cluster.getMetadata();
        System.out.printf("Connected to cluster: %s\n",
                metadata.getClusterName());
        for ( Host host : metadata.getAllHosts() ) {
            System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
                    host.getDatacenter(), host.getAddress(), host.getRack());
        }
        if (keyspace != null) {
            Session session =null;
            try {
                session = cluster.connect(keyspace);

            } catch (InvalidQueryException iqu) {

                session = cluster.connect();

            }

            System.out.println("BEFORE DROP:"+keyspace);
            String selectQuery = "SELECT keyspace_name FROM system_schema.keyspaces where keyspace_name='" + keyspace + "'";
            ResultSet keyspaceQueryResult = session.execute(selectQuery);
            System.out.println("SELECT FINISHED");
            if (keyspaceQueryResult.iterator().hasNext()) {
                String dropQuery = "DROP KEYSPACE IF EXISTS " + keyspace+";";
                Statement s=new SimpleStatement(dropQuery);
                s.enableTracing();
                int retries=0;
                while(retries<3){
                    try{
                        System.out.println("executing : " + dropQuery);
                        ResultSet results = session.execute(s);
                        /*ExecutionInfo executionInfo = results.getExecutionInfo();

                        QueryTrace queryTrace = executionInfo.getQueryTrace();
                        System.out.printf("Trace id: %s\n\n", queryTrace.getTraceId());
                        System.out
                                .println("---------------------------------------+--------------+------------+--------------");
                        for (QueryTrace.Event event : queryTrace.getEvents()) {
                            System.out.printf("%38s | %12s | %10s | %12s\n",
                                    event.getDescription(),event.getTimestamp(),
                                    event.getSource(), event.getSourceElapsedMicros());
                        }*/
                        retries=3;
                    }catch(NoHostAvailableException ex){
                       logger.error(ex);
                        try {
                            Thread.sleep(10000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    ++retries;
                }

            }
            session.close();
            cluster.close();
            //this.getSession().execute("DROP KEYSPACE " + schemaName);
           System.out.println("KEYSPACE DROPPED");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
