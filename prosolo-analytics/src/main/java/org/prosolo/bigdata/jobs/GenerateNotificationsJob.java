package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.emails.NotificationsEmailManager$;
import org.prosolo.common.util.date.DateEpochUtil;
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
       Long date= DateEpochUtil.getDaysSinceEpoch();

        emailManager.runAnalyser(date-1);
        logger.info("GENERATION OF NOTIFICATIONS JOB FINISHED");
    }
}
