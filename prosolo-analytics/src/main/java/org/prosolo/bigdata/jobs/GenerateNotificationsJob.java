package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.emails.NotificationsEmailManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GenerateNotificationsJob implements Job {
    private static Logger logger = Logger
            .getLogger(GenerateNotificationsJob.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("STARTING GENERATION OF NOTIFICATIONS EXECUTED JOB");
        NotificationsEmailManager$ emailManager=NotificationsEmailManager$.MODULE$;
        emailManager.runAnalyser();
        logger.info("GENERATION OF NOTIFICATIONS JOB FINISHED");
    }
}
