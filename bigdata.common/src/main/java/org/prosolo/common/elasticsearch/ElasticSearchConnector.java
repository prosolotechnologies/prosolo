package org.prosolo.common.elasticsearch;

//import static org.elasticsearch.client.Requests.clusterHealthRequest;
//import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.common.config.ElasticSearchHost;
import org.prosolo.common.elasticsearch.client.ESRestClient;
import org.prosolo.common.elasticsearch.client.impl.ESRestClientImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**/
//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class ElasticSearchConnector {
	private static ESRestClient client;
	private static Logger logger = Logger
			.getLogger(ElasticSearchConnector.class);

	public static ESRestClient getClient() {
		if (client == null) {
//			if (CommonSettings.getInstance().config.elasticSearch.type.equals("local")) {
//				 client = getLocalClient();
//			} else if (CommonSettings.getInstance().config.elasticSearch.type.equals("server")){
//				client = getESClient();
//			}else if (CommonSettings.getInstance().config.elasticSearch.type.equals("cloud-aws")){
//				client = getAWSClient();
//			}
			client = getESClient();
		}

		return client;
	}

	private static ESRestClient getESClient() {
		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
		ESRestClient cl = null;
		try {
//			org.elasticsearch.common.settings.Settings settings = org.elasticsearch.common.settings.Settings.builder()
//					.put("cluster.name", elasticSearchConfig.clusterName).build();

			cl = getRestClient(elasticSearchConfig);
			cl.waitForGreenStatus();
		} catch (NoNodeAvailableException ex) {
			logger.error("ElasticSearch node is not available. " + ex);
 		} catch (IOException e) {
			logger.error("Error", e);
		}
 		return cl;
	}

	private static ESRestClient getRestClient(ElasticSearchConfig esConfig) {
		ArrayList<ElasticSearchHost> esHosts = esConfig.esHostsConfig.esHosts;
		List<HttpHost> httpHosts = new ArrayList<>();
		for (ElasticSearchHost eshost : esHosts) {
			httpHosts.add(new HttpHost(eshost.host, eshost.httpPort, "http"));
		}

		RestClient lowLevelRestClient = RestClient.builder(
				httpHosts.toArray(new HttpHost[httpHosts.size()])).build();

		return ESRestClientImpl.of(lowLevelRestClient);
	}

	//TODO es migration test local client
	private static ESRestClient getLocalClient() {
//		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
//		String dataDirectory = elasticSearchConfig.homePath;
//		org.elasticsearch.common.settings.Settings.Builder elasticsearchSettings = org.elasticsearch.common.settings.Settings.builder()
//				.put("http.enabled", "false")
//				.put("cluster.name", elasticSearchConfig.clusterName)
//				.put("path.data", dataDirectory)
//				.put("node.data", true)
//				//TODO es migration - this should be equivalent of nodebuilder().local(true)
//				.put("transport.type", "local");
//		Node node = new Node(elasticsearchSettings.build());
		//TODO how to create rest client from local node
//		client = node.client();
//		client.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
//		return client;
		return null;
	}

	//TODO check if this is needed
	private static ESRestClient getAWSClient(){
//		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
//		ESRestClient cl = null;
//		try{
////			org.elasticsearch.common.settings.Settings.Builder settings =  org.elasticsearch.common.settings.Settings.builder();
////			settings.put("cluster.name", elasticSearchConfig.clusterName);
////			settings.put("cloud.aws.access_key", elasticSearchConfig.awsConfig.accessKey);
////			settings.put("cloud.aws.secret_key", elasticSearchConfig.awsConfig.secretKey);
////			settings.put("discovery.ec2.availability_zones", elasticSearchConfig.awsConfig.region);
////			settings.put("discovery.type", "ec2");
////			settings.put("discovery.ec2.groups", elasticSearchConfig.awsConfig.groups);
////			List<String> hosts = new LinkedList<String>();
////			ArrayList<ElasticSearchHost> esHosts = elasticSearchConfig.esHostsConfig.esHosts;
////			for (ElasticSearchHost host : esHosts) {
////				hosts.add(host.host + ":" + host.port);
////			}
////			settings.put("discovery.zen.ping.multicast.enabled", "false");
////			settings.put("discovery.zen.ping.unicast.hosts", StringUtils.join(hosts, ","));
//
//
//			cl = getRestClient(elasticSearchConfig);
//			cl.waitForGreenStatus();
//		} catch (NoNodeAvailableException ex) {
//			logger.error("NoNodeAvailableException for cluster:" + elasticSearchConfig.clusterName, ex);
//		} catch (Exception ex) {
//			logger.error("Exception for cluster:" + elasticSearchConfig.clusterName, ex);
//		}
//		return cl;
		return null;
	}
}
