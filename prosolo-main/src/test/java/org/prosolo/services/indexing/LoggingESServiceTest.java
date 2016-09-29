package org.prosolo.services.indexing;/**
 * Created by zoran on 28/09/16.
 */

import org.elasticsearch.client.Client;
import org.junit.BeforeClass;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;

/**
 * zoran 28/09/16
 */
public class LoggingESServiceTest {
    private static Client client;
    @BeforeClass
    public static void initializeClient(){
        String indexName = ESIndexNames.INDEX_LOGS;
        String indexType = ESIndexTypes.LOG;
        client = ElasticSearchFactory.getClient();

        System.out.println("ES CLIENT INITIALIZED");
    }
}
