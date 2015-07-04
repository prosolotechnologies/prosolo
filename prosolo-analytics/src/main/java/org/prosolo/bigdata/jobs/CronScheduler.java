package org.prosolo.bigdata.jobs;

import org.prosolo.bigdata.config.QuartzJobConfig;
import org.quartz.Job;
import org.quartz.SchedulerException;

/**
@author Zoran Jeremic May 18, 2015
 *
 */

public interface CronScheduler {

	//void startAssociationRulesForCompetenceDiscovery();

	void checkAndActivateJob(QuartzJobConfig jobConfig) throws SchedulerException, ClassNotFoundException;

 

	//<T extends Job> void startAssociationRulesForCompetenceDiscovery(T clazz);

	//<T extends Job>  void startAssociationRulesForCompetenceDiscovery(Class<T> clazz);

	<T extends Job>  void startJobForSpecificJobClass(Class<T> clazz);

}

