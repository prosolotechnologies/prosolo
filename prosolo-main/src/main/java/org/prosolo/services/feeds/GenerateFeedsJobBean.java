package org.prosolo.services.feeds;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-08-30
 * 
 */
public class GenerateFeedsJobBean implements JobInterface {
	
	private static Logger logger = Logger.getLogger(GenerateFeedsJobBean.class);
	
	@Autowired private DiggestManager digestManager;
	
	@Override
	@Transactional
	public void execute() throws JobExecutionException {
		logger.info("STARTING GENERATION OF FEED DIGESTS");
		
		digestManager.createFeedDiggestsAndSendEmails();

		logger.info("COMPLETED GENERATION OF FEED DIGESTS");
	}
	
}
