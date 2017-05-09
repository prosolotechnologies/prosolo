package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.instructorEmails.InstructorEmailManager$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class InstructorEmailSenderJob implements Job {

	private static Logger logger = Logger
			.getLogger(InstructorEmailSenderJob.class.getName());

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Instructor email sender job starting");
		InstructorEmailManager$ emailSender = InstructorEmailManager$.MODULE$;
		emailSender.runJob();;
		logger.info("Instructor email sender job completed");
	}
}
