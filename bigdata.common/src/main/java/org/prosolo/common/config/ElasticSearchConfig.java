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

	@Element(name = "credentials-index")
	public String credentialsIndex;

	@Element(name = "competences-index")
	public String competencesIndex;

	@Element(name = "users-index")
	public String usersIndex;
	
	@Element(name = "associationrules-index")
	public  String associationrulesIndex;

	@Element(name = "recommendationdata-index")
	public   String recommendationdataIndex;

	@Element(name = "logs-index")
	public   String logsIndex;
	
	@Element(name = "userGroup-index")
	public String userGroupIndex;

	@Element(name = "rubrics-index")
	public String rubricsIndex;

	@Element(name = "jobsLogs-index")
	public String jobsLogsIndex;

	
}