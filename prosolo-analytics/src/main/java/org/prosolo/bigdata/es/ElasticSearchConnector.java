package org.prosolo.bigdata.es;

//import static org.elasticsearch.client.Requests.clusterHealthRequest;
//import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**/
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.common.config.ElasticSearchHost;

//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class ElasticSearchConnector {
	private static Client client;
	private static Logger logger = Logger
			.getLogger(ElasticSearchConnector.class);

	public static Client getClient() {
		if (client == null) {
			if (CommonSettings.getInstance().config.elasticSearch.type.equals("local")) {
				client = getLocalClient();
			} else if (CommonSettings.getInstance().config.elasticSearch.type.equals("server")){
				client = getESClient();
			}else if (CommonSettings.getInstance().config.elasticSearch.type.equals("cloud-aws")){
				client = getAWSClient();
			}
		}

		return client;
	}

	private static Client getESClient() {
		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
 		try {
		org.elasticsearch.common.settings.Settings settings = org.elasticsearch.common.settings.Settings.settingsBuilder()
				.put("cluster.name", elasticSearchConfig.clusterName).build();

		ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
		//client = new TransportClient(settings);
		client=TransportClient.builder().settings(settings).build();

		for (ElasticSearchHost host : esHosts) {
			((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host.host, host.port)));

		}
		ClusterHealthResponse clusterHealth = client.admin().cluster().health(clusterHealthRequest().waitForGreenStatus()).actionGet();
 		} catch (NoNodeAvailableException ex) {
			logger.error("ElasticSearch node is not available. " + ex);
 			//throw new IndexingServiceNotAvailable("ElasticSearch node is not available. " + ex);
//		} catch (Exception ex) {
//			logger.error("Exception for cluster:" + elasticSearchConfig.clusterName, ex);
//			return null;
 		}
		return client;
	}

	private static Client getLocalClient() {
		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
		String dataDirectory = elasticSearchConfig.homePath;
		org.elasticsearch.common.settings.Settings.Builder elasticsearchSettings = org.elasticsearch.common.settings.Settings.settingsBuilder().put("http.enabled", "false")
				.put("cluster.name", elasticSearchConfig.clusterName).put("path.data", dataDirectory);
		Node node = nodeBuilder().local(true).settings(elasticsearchSettings.build()).client(false).data(true).node();
		client = node.client();
		client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
		return client;
	}

	private static Client getAWSClient(){
		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
		try{
			org.elasticsearch.common.settings.Settings.Builder settings =  org.elasticsearch.common.settings.Settings.settingsBuilder();
			settings.put("cluster.name", elasticSearchConfig.clusterName);
			settings.put("cloud.aws.access_key", elasticSearchConfig.awsConfig.accessKey);
			settings.put("cloud.aws.secret_key", elasticSearchConfig.awsConfig.secretKey);
			settings.put("discovery.ec2.availability_zones", elasticSearchConfig.awsConfig.region);
			settings.put("discovery.type", "ec2");
			settings.put("discovery.ec2.groups", elasticSearchConfig.awsConfig.groups);
			List<String> hosts = new LinkedList<String>();
			ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
			for (ElasticSearchHost host : esHosts) {
				hosts.add(host.host + ":" + host.port);
			}
			settings.put("discovery.zen.ping.multicast.enabled", "false");
			settings.put("discovery.zen.ping.unicast.hosts", StringUtils.join(hosts, ","));


			client =   TransportClient.builder().settings(settings).build();
			for (ElasticSearchHost host : esHosts) {
				((TransportClient) client).addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(host.host, host.port)));

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
