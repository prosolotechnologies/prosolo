package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 09/01/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.clustering.userprofiling.UserProfileClusteringManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * zoran 09/01/16
 */
public class GenerateUserProfileClusters  implements Job {
    private static Logger logger = Logger
            .getLogger(GenerateUserProfileClusters.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("STARTING GENERATION OF USER PROFILES CLUSTERS EXECUTED JOB");
        UserProfileClusteringManager$ userProfileClusteringManager=UserProfileClusteringManager$.MODULE$;
        userProfileClusteringManager.runClustering();
        logger.info("GENERATION OF USER PROFILES CLUSTERS JOB FINISHED");
    }
}
