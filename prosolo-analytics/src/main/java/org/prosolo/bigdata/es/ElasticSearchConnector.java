package org.prosolo.bigdata.es;

//import static org.elasticsearch.client.Requests.clusterHealthRequest;
//import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
/**/
import org.prosolo.bigdata.common.config.ElasticSearchConfig;
import org.prosolo.bigdata.common.config.ElasticSearchHost;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
 
//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
@author Zoran Jeremic May 9, 2015
 *
 */

public class ElasticSearchConnector {
	 private static Client client;
	 private static Logger logger = Logger.getLogger(ElasticSearchConnector.class);

	public static Client getClient() throws IndexingServiceNotAvailable {
		if (client == null) {
			if (Settings.getInstance().config.elasticSearch.type.equals("local")) {
				client = getLocalClient();
			} else if (Settings.getInstance().config.elasticSearch.type.equals("server")){
				client = getESClient();
			}else if (Settings.getInstance().config.elasticSearch.type.equals("cloud-aws")){
				client = getAWSClient();
			}
		}
		return client;
	}

	private static Client getESClient() throws IndexingServiceNotAvailable {
		ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
		ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
		try {
			org.elasticsearch.common.settings.Settings settings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", elasticSearchConfig.clusterName).build();

			
			client = new TransportClient(settings);
			
			for (ElasticSearchHost host : esHosts) {
				((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host.host, host.port));
				
			}
			ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
		} catch (NoNodeAvailableException ex) {
			logger.error("None of the Elasticsearch nodes is available");
			for (ElasticSearchHost host : esHosts) {
			logger.error("Tried:"+host.host+" port:"+host.port);
			}
			throw new IndexingServiceNotAvailable("ElasticSearch node is not available at:"+esHosts.toString() + ex);
		} catch (Exception ex) {
			logger.error("Exception for cluster:" + elasticSearchConfig.clusterName, ex);
			return null;
		}
		return client;
	}

	private static Client getLocalClient() {
		ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
		String dataDirectory = elasticSearchConfig.homePath;
		ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder().put("http.enabled", "false")
				.put("cluster.name", elasticSearchConfig.clusterName).put("path.data", dataDirectory);
		Node node = nodeBuilder().local(true).settings(elasticsearchSettings.build()).client(false).data(true).node();
		client = node.client();
		client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
		return client;
	}
	
	private static Client getAWSClient(){
		ElasticSearchConfig elasticSearchConfig = Settings.getInstance().config.elasticSearch;
		try{
			ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
			settings.put("cluster.name", elasticSearchConfig.clusterName);
			settings.put("cloud.aws.access_key", elasticSearchConfig.awsConfig.accessKey);
			settings.put("cloud.aws.secret_key", elasticSearchConfig.awsConfig.secretKey);
			settings.put("discovery.ec2.availability_zones", elasticSearchConfig.awsConfig.region);
			settings.put("discovery.type", "ec2");
			settings.put("discovery.ec2.groups", elasticSearchConfig.awsConfig.groups);
			List<String> hosts = Lists.newLinkedList();
			ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
			for (ElasticSearchHost host : esHosts) {
				hosts.add(host.host + ":" + host.port);
			}
			settings.put("discovery.zen.ping.multicast.enabled", "false");
			settings.put("discovery.zen.ping.unicast.hosts", StringUtils.join(hosts, ","));
			 
		
			client = new TransportClient(settings);
			for (ElasticSearchHost host : esHosts) {
				((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(host.host, host.port));
				
			}
			ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
		} catch (NoNodeAvailableException ex) {
			logger.error("NoNodeAvailableException for cluster:" + elasticSearchConfig.clusterName, ex);
			return null;
			
		} catch (Exception ex) {
			logger.error("Exception for cluster:" + elasticSearchConfig.clusterName, ex);
			return null;
		}
		return client;
	}
}

