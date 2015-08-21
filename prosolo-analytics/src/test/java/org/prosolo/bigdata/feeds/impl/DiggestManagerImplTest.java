package org.prosolo.bigdata.feeds.impl;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.prosolo.bigdata.jobs.GenerateFeedsJob;
import org.prosolo.bigdata.scala.feeds.DigestManager$;

public class DiggestManagerImplTest {
	private static Logger logger = Logger
			.getLogger(DiggestManagerImplTest.class.getName());
	@Test
	public void testCreateFeedDiggestsAndSendEmails() {
		System.out.println("STARTING TEST");
logger.info("STARTING GENERATION OF FEED DIGESTS");
		
		//digestManager.createFeedDiggestsAndSendEmails();
		DigestManager$ digestManager = DigestManager$.MODULE$;
		digestManager.createFeedDiggestsAndSendEmails();
		logger.info("COMPLETED GENERATION OF FEED DIGESTS");
	}

}
