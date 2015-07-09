package org.prosolo.services.indexing;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;

 


public class ElasticSearchNodeManager {
	public static class ElasticSearchNodeManagerHolder {
		public static final ElasticSearchNodeManager INSTANCE = new ElasticSearchNodeManager();
	}

	public static ElasticSearchNodeManager getInstance() {
		return ElasticSearchNodeManagerHolder.INSTANCE;
	}
	public  Node getLocalNode(){
		//TODO Should be replaced with distributed version of the ElasticSearch
		ImmutableSettings.Builder settings = ImmutableSettings.settingsBuilder();
		ElasticSearchConfig elasticSearchConfig=CommonSettings.getInstance().config.elasticSearch;
		String homePath=elasticSearchConfig.homePath;
		 settings.put("cluster.name", elasticSearchConfig.clusterName);
         settings.put("path.conf", homePath);
         settings.put("path.data", homePath + "/data");
         settings.put("path.work", homePath + "/work");
         settings.put("path.logs", homePath + "/logs") ;
 		 settings.put("index.number_of_replicas", elasticSearchConfig.replicasNumber);
		 settings.put("index.number_of_shards", elasticSearchConfig.shardsNumber);
		 settings.build();

		Node node = NodeBuilder.nodeBuilder().settings(settings).local(true).build().start(); 
		return node;
	}

}
