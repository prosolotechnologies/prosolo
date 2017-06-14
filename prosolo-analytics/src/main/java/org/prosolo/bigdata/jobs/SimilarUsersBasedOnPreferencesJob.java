package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 30/07/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.recommendations.SimilarUsersBasedOnPreferences$;
import org.prosolo.bigdata.scala.recommendations.SimilarUsersBasedOnPreferencesManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * zoran 30/07/16
 */
public class SimilarUsersBasedOnPreferencesJob implements Job {

    private static Logger logger = Logger
            .getLogger(SimilarUsersBasedOnPreferencesJob.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("STARTING SIMILAR USERS BASED ON PREFERENCES JOB");
        //SimilarUsersBasedOnPreferences$ similarUsersBasedOnPreferences=SimilarUsersBasedOnPreferences$.MODULE$;
        //similarUsersBasedOnPreferences.runJob();
        SimilarUsersBasedOnPreferencesManager$ similarUsersBasedOnPreferences=SimilarUsersBasedOnPreferencesManager$.MODULE$;
        similarUsersBasedOnPreferences.runJob();
        logger.info("SIMILAR USERS BASED ON PREFERENCES JOB FINISHED");

    }
}
