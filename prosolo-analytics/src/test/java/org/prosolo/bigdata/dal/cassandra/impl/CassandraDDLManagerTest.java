package org.prosolo.bigdata.dal.cassandra.impl;/**
 * Created by zoran on 01/08/16.
 */

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.TableMetadata;
import org.junit.Test;
import org.prosolo.bigdata.config.Settings;

import java.util.Collection;

/**
 * zoran 01/08/16
 */
public class CassandraDDLManagerTest {
    @Test
    public void listAllTablesTest(){
        Cluster cluster=CassandraDDLManagerImpl.getInstance().getCluster();
        Metadata metadata =cluster.getMetadata();
        String keyspacename= CassandraDDLManagerImpl.getInstance().getSchemaName();

       Collection<TableMetadata> tablesMetadata= metadata.getKeyspace(keyspacename).getTables();
        for(TableMetadata tm:tablesMetadata){
            Collection<ColumnMetadata> columnsMetadata=tm.getColumns();
            System.out.println("Table:"+tm.getName());
            for(ColumnMetadata cm:columnsMetadata){
                String columnName=cm.getName();
                System.out.println("Column name:"+columnName);
            }
        }
    }

    @Test
    public void testCassandraConnection(){
        Cluster cluster=CassandraDDLManagerImpl.getInstance().getCluster();
        Metadata metadata =cluster.getMetadata();
        String keyspacename= CassandraDDLManagerImpl.getInstance().getSchemaName();
        CassandraDDLManagerImpl.getInstance().connect("54.197.119.54",9042,keyspacename,3);
        System.out.println("FINISHED CONNECT");
       // CassandraDDLManagerImpl.getInstance().getSession().execute("SELECT * FROM ")

        CassandraDDLManagerImpl.getInstance().dropSchemaIfExists(keyspacename);
        System.out.println("FINISHED DROP");

        CassandraDDLManagerImpl.getInstance().checkIfTablesExistsAndCreate(keyspacename);
        System.out.println("FINISHED CREATE");

    }
}
