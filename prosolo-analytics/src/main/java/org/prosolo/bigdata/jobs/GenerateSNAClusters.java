package org.prosolo.bigdata.jobs;/**
 * Created by zoran on 09/01/16.
 */

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.clustering.sna.SNAclusterManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * zoran 09/01/16
 */
public class GenerateSNAClusters  implements Job {
    private static Logger logger = Logger
            .getLogger(GenerateSNAClusters.class.getName());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("STARTING GENERATION OF SNA CLUSTERS EXECUTED JOB");
        SNAclusterManager$ snAclusterManager= SNAclusterManager$.MODULE$;
        snAclusterManager.identifyClusters();
        logger.info("GENERATION OF SNA CLUSTERS JOB FINISHED");
    }
}
