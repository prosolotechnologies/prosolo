package org.prosolo.services.indexing;

//import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.node.Node;

public class ElasticSearchNodeManager {
	public static class ElasticSearchNodeManagerHolder {
		public static final ElasticSearchNodeManager INSTANCE = new ElasticSearchNodeManager();
	}
	
	public static ElasticSearchNodeManager getInstance() {
		return ElasticSearchNodeManagerHolder.INSTANCE;
	}
	
	public Node getLocalNode() {
		// TODO Should be replaced with distributed version of the ElasticSearch
		//TODO es migration - if this method is necessary, reimplement it, otherwise it can be removed
//		Settings.Builder settings = Settings.builder();
//		ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
//		String homePath = elasticSearchConfig.homePath;
//		settings.put("cluster.name", elasticSearchConfig.clusterName);
//		settings.put("path.conf", homePath);
//		settings.put("path.data", homePath + "/data");
//		settings.put("path.work", homePath + "/work");
//		settings.put("path.logs", homePath + "/logs");
//		settings.put("index.number_of_replicas", elasticSearchConfig.replicasNumber);
//		settings.put("index.number_of_shards", elasticSearchConfig.shardsNumber);
//		settings.build();
//
//		settings.put("node.data", true)
//				//TODO es migration - this should be equivalent of nodebuilder().local(true)
//				.put("transport.type", "local");
//
//		Node node = NodeBuilder.nodeBuilder().settings(settings).local(true).build().start();
//		return node;
		return null;
	}
	
}
