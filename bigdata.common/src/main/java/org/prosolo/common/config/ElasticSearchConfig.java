package org.prosolo.common.config;



import org.simpleframework.xml.Element;


/**
 * @author Zoran Jeremic 2013-06-10
 */
public class ElasticSearchConfig {
	

	@Element(name="type")
	public String type;
	
	@Element(name="aws")
	public AWSConfig awsConfig;
	
	 @Element(name = "es-hosts")
	 public ElasticSearchHostsConfig esHostsConfig; 
	
	
	
	@Element(name = "home-path")
	public String homePath;

	@Element(name = "cluster-name")
	public String clusterName;
	
	@Element(name = "replicas-number")
	public int replicasNumber;
	
	@Element(name = "shards-number")
	public int shardsNumber;
	
	@Element(name = "documents-index")
	public String documentsIndex;
	@Element(name = "nodes-index")
	public String nodesIndex;
	@Element(name = "users-index")
	public String usersIndex;
	
	@Element(name = "associationrules-index")
	public  String associationrulesIndex;

	@Element(name = "recommendationdata-index")
	public   String recommendationdataIndex;
	
	@Element(name = "userGroup-index")
	public String userGroupIndex;

	@Element(name = "jobsLogs-index")
	public String jobsLogsIndex;
	
	
}