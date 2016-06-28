package org.prosolo.services.indexing;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.common.config.ElasticSearchHost;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.junit.Assert.*;

/**
 * Created by zoran on 27/06/16.
 */
public class ElasticSearchFactoryTest {

    @Test
    public void testGetClient() throws Exception {
        System.out.println("TEST GET CLIENT");
       Client client;
        System.out.println("KT-es1");
        ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
        try {
            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", elasticSearchConfig.clusterName).build();
            System.out.println("KT-es2:"+elasticSearchConfig.clusterName);
            ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
            System.out.println("KT-es3");
            //client = new TransportClient(settings);
            client= TransportClient.builder().settings(settings).build();
            System.out.println("KT-es4");
            for (ElasticSearchHost host : esHosts) {
                System.out.println("KT-es5");
                ((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host.host, host.port)));

            }
            System.out.println("KT-es61");
            ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
        } catch (NoNodeAvailableException ex) {
            System.out.println("ElasticSearch node is not available. " + ex);
            //throw new IndexingServiceNotAvailable("ElasticSearch node is not available. " + ex);
        } catch (Exception ex) {
            System.out.println("Exception for cluster:" + elasticSearchConfig.clusterName);

        }
    }
}