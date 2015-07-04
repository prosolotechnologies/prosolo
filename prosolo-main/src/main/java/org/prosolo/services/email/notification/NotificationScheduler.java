package org.prosolo.services.email.notification;



public class NotificationScheduler {
//	private static Logger logger = Logger.getLogger(NotificationScheduler.class.getName());
//	private EmailNotifierConfig notifierConfig=Settings.getInstance().config.emailNotifier;
//	public void startScheduler() throws SchedulerException, ParseException{
//		SchedulerFactory sf=new StdSchedulerFactory();
//		Scheduler sched=sf.getScheduler();
//		
//		// don't schedule this --> should be done by DataModelManager and
//		// HibernateUtil (through c3p0), respectively
////		scheduleHourlyUpdates(sched);
//		
//		if(notifierConfig.activated){
//			if(notifierConfig.debug){
//				scheduleOneTimeUpdate(sched);
//			}else{
//				if(notifierConfig.daily){
//					scheduleDailyUpdates(sched);
//				}
//				if(notifierConfig.weekly){
//					scheduleWeeklyUpdates(sched);
//				}
//				if(notifierConfig.monthly){
//					scheduleMonthlyUpdates(sched);
//				}
//				
//			}
//	       
//		}
//		 sched.start();
//	}
//	@SuppressWarnings("unused")
//	private void scheduleHourlyUpdates(Scheduler sched) throws ParseException, SchedulerException{
// 		logger.debug("scheduleHourlyJob");
//		JobDetail job = JobBuilder.newJob(HourlyJob.class).withIdentity("hourlyJob", "cron_scheduler").build();
//		SimpleScheduleBuilder cSched=SimpleScheduleBuilder.repeatHourlyForever(1);
//		 SimpleTrigger  trigger=  TriggerBuilder.newTrigger().withIdentity("hourlyJob","cron_scheduler").withSchedule(cSched).build();
//		 sched.scheduleJob(job,trigger);
//	}
//	
//	/**
//	 * This is for testing purposes. 
//	 * Should not be used in productive release.
//	 * @param sched
//	 * @throws SchedulerException 
//	 */
//	private void scheduleOneTimeUpdate(Scheduler sched) throws SchedulerException{
//		logger.debug("------- Scheduling Jobs ----------------");
//	      
//        // get a "nice round" time a few seconds in the future...
//        Date startTime = DateBuilder.nextGivenSecondDate(null, 15);
//        
// 
//        // job1 will only fire once at date/time "ts"
//        JobDetail job=(JobDetail) JobBuilder.newJob(DailyUpdatesJob.class).withIdentity("testjob","email_notification").build();
//        SimpleTrigger trigger=(SimpleTrigger) TriggerBuilder.newTrigger().withIdentity("testjob","email_notification").startAt(startTime).build();
//        // schedule it to run!
//        sched.scheduleJob(job, trigger);
//     
//	}
//	private void scheduleDailyUpdates(Scheduler sched) throws ParseException, SchedulerException{
//		JobDetail job = JobBuilder.newJob(DailyUpdatesJob.class).withIdentity("dailyUpdate", "email_notification").build();
//		CronScheduleBuilder cSched=CronScheduleBuilder.dailyAtHourAndMinute(0, 15);
//		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("dailyUpdate", "email_notification").withSchedule(cSched)
//			    .build();
//			sched.scheduleJob(job, trigger);
//	}
//	private void scheduleWeeklyUpdates(Scheduler sched) throws ParseException, SchedulerException{
//		JobDetail job = JobBuilder.newJob(DailyUpdatesJob.class).withIdentity("weeklyUpdate", "email_notification").build();
//		CronScheduleBuilder cSched=CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.SUNDAY, 0, 15);
//			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("weeklyUpdate", "email_notification").withSchedule(cSched)
//			    .build();
//			sched.scheduleJob(job, trigger);
//	}
//	private void scheduleMonthlyUpdates(Scheduler sched) throws ParseException, SchedulerException{
//		JobDetail job = JobBuilder.newJob(DailyUpdatesJob.class).withIdentity("weeklyUpdate", "email_notification").build();
//		CronScheduleBuilder cSched=CronScheduleBuilder.monthlyOnDayAndHourAndMinute(1, 2, 45);
//			CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity("weeklyUpdate", "email_notification").withSchedule(cSched)
//			    .build();
//			sched.scheduleJob(job, trigger);
//	}


}
