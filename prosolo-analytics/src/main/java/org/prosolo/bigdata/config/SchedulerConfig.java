package org.prosolo.bigdata.config;



import org.simpleframework.xml.Element;


/**
@author Zoran Jeremic May 18, 2015
 *
 */

public class SchedulerConfig {
	@Element(name = "auto-start", required = true)
	public boolean autoStart;
	
	@Element(name = "instance-name", required = true)
	public String instanceName;
	
	@Element(name = "thread-count", required = true)
	public int threadCount;
	
	@Element(name = "job-store-class", required = true)
	public String jobStoreClass;
	
	@Element(name = "collection-prefix", required = true)
	public String collectionPrefix;
	
	@Element(name="quartz-jobs", required=true)
	public JobsMap jobs;
}

