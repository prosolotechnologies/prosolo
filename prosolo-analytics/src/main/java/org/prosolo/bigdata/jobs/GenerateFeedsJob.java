package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.feeds.DigestManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class GenerateFeedsJob  implements Job {
	private static Logger logger = Logger
			.getLogger(GenerateFeedsJob.class.getName());
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
logger.info("STARTING GENERATION OF FEED DIGESTS");
		
		//digestManager.createFeedDiggestsAndSendEmails();
		DigestManager$ digestManager = DigestManager$.MODULE$;
		digestManager.createFeedDiggestsAndSendEmails();
		logger.info("COMPLETED GENERATION OF FEED DIGESTS");
		
	}

}
