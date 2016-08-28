package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 27/08/16.
 */

import org.apache.log4j.Logger;
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
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long credentialId=dataMap.getLong("credentialId");
        logger.info("PUBLISHING CREDENTIAL ID:"+credentialId);



    }
}
