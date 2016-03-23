package org.prosolo.bigdata.jobs;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.scala.instructorEmails.InstructorEmailSender$;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class InstructorEmailSenderJob implements Job {

	private static Logger logger = Logger
			.getLogger(InstructorEmailSenderJob.class.getName());

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("Instructor email sender job starting");
		InstructorEmailSender$ emailSender = InstructorEmailSender$.MODULE$;
		emailSender.sendEmailsToInstructors();
		logger.info("Instructor email sender job completed");
	}
	
//	public void testJavaSpark() {
//		try {
//			JavaSparkContext javaSparkContext = SparkLauncher.getSparkContext();
//			System.out.println("GOT JAVA SPARK CONTEXT");
//			
//			JavaRDD<CassandraRow> testRdd = javaFunctions(javaSparkContext).cassandraTable("prosolo_logs_stefan2", "student_assign_events");
//			testRdd.cache();
//			System.out.println("Count");
//			System.out.println(testRdd.count());
//			System.out.println("Results");
//			testRdd.collect().forEach(x -> System.out.println(x));
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
}
