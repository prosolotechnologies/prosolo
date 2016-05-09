package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;

import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileInteractionsManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by zoran on 30/03/16.
 */
public class GenerateUserProfileInteractions  implements Job {
    private static Logger logger = Logger
            .getLogger(GenerateUserProfileClusters.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("STARTING GENERATION OF USER PROFILES INTERACTIONS EXECUTED JOB");
        UserProfileInteractionsManager$ userProfileClusteringManager=UserProfileInteractionsManager$.MODULE$;
        userProfileClusteringManager.runAnalyser();
        logger.info("GENERATION OF USER PROFILES INTERACTIONS JOB FINISHED");
    }
}
