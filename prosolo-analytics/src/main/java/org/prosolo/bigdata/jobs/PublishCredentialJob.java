package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 27/08/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.dal.persistence.impl.CourseDAOImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * zoran 27/08/16
 */
public class PublishCredentialJob  implements Job {
    private static Logger logger = Logger
            .getLogger(PublishCredentialJob.class.getName());
    
    CourseDAOImpl courseDao = new CourseDAOImpl();
    
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long credentialId=dataMap.getLong("credentialId");
        courseDao.publishCredential(credentialId);
        logger.info("PUBLISHING CREDENTIAL ID:"+credentialId);
        updateElasticearchIndex(credentialId);
    }
	
    
    private void updateElasticearchIndex(Long credentialId) {
		// TODO Auto-generated method stub
		
	}
}
