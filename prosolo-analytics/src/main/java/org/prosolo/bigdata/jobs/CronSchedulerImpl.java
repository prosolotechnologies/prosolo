package org.prosolo.bigdata.jobs;

import static org.quartz.JobKey.jobKey;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

//import org.apache.http.ParseException;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.QuartzJobConfig;
import org.prosolo.bigdata.config.SchedulerConfig;
import org.prosolo.bigdata.config.Settings;
import org.prosolo.bigdata.utils.ScriptRunner;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.MySQLConfig;
import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Zoran Jeremic May 18, 2015
 *
 */

public class CronSchedulerImpl implements CronScheduler {
	public static class CronSchedulerHolder {
		public static final CronSchedulerImpl INSTANCE = new CronSchedulerImpl();
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CronSchedulerImpl.class
			.getName());

	public static CronSchedulerImpl getInstance() {
		return CronSchedulerHolder.INSTANCE;
	}

	Scheduler sched;

	//Set<JobWrapper> jobsList;
	//List<String> executedJobs;

	int startupJobsCounter=0;

	private CronSchedulerImpl() {

		try {
			System.out.println("SHOULD AUTOSTART:"
					+ Settings.getInstance().config.schedulerConfig.autoStart);
			if (Settings.getInstance().config.schedulerConfig.autoStart) {
				startScheduler();
			}
		//} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	/*
	 * @Override public InternalResponse addWrappedJob(JobWrapper jobWrapper) {
	 * InternalResponse response=new InternalResponse();
	 * jobsList.add(jobWrapper); try { if(sched==null || sched.isShutdown()){
	 * response.setSuccess(false); response.setTitle("error");
	 * response.setMessage("Scheduler is not running."); return response; } if
	 * (jobWrapper.getDependencies().size() == 0) {
	 * sched.scheduleJob(jobWrapper.jobDetail, jobWrapper.trigger); } else {
	 * sched.addJob(jobWrapper.jobDetail, true); } //JobDetail
	 * test=sched.getJobDetail(jobWrapper.trigger.getJobKey()); } catch
	 * (SchedulerException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } response.setSuccess(true); return response; }
	 */
	public void removeJobTrigger(String jobId, String groupId) {
		try {
			logger.info("Delete scheduled job:"+jobId);
			sched.deleteJob(jobKey(jobId, groupId));
		}catch(SchedulerException e){
			e.printStackTrace();
		}


	}

	public void rescheduleJobTrigger(String jobId, String groupId, Date newDate) {
		try {
			logger.info("Reschedule job trigger:" + jobId
					+ " with trigger:" + newDate.toString());
			 Trigger oldTrigger = sched.getTrigger(triggerKey(jobId,groupId));
			//Trigger oldTrigger = sched.getTrigger(jobId,groupId);
			if(triggerKey(jobId,groupId)==null){
				System.out.println("TRIGGER KEY IS NULL");
			}
			if(oldTrigger!=null){
				TriggerBuilder tb = oldTrigger.getTriggerBuilder();
				Trigger newTrigger = tb.startAt(newDate)
						.build();
				sched.rescheduleJob(triggerKey(jobId,groupId),newTrigger);
			}else{
				logger.error("Trigger was not found for job:"+jobId+" group:"+groupId);
			}


		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateJobTrigger(String jobId, String groupId, Trigger newTrigger) {
			try {
				logger.info("Replace job trigger:" + jobId
						+ " with " + newTrigger.getKey());
				 sched.rescheduleJob(triggerKey(jobId,groupId),newTrigger);


			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}


	public void startScheduler() throws SchedulerException  {
		// System.getProperties()
		// .put("org.quartz.properties", "quartz.properties");
		MySQLConfig mySQLConfig=CommonSettings.getInstance().config.mysqlConfig;
		String username = mySQLConfig.user;
		String password = mySQLConfig.password;
		String host = mySQLConfig.host;
		int port = mySQLConfig.port;
		 String database = mySQLConfig.database;
		//String database="prosolo2";
		String url="jdbc:mysql://"+ host + ":" + port + "/" + database;

		// String
		// mongoUri="mongodb://"+serverConfig.dbHost+":"+serverConfig.dbPort;
		SchedulerConfig schedConfig = Settings.getInstance().config.schedulerConfig;
		// Main Quartz configuration
		logger.info("STARTING CRON SCHEDULER:");
		System.setProperty("org.quartz.scheduler.instanceName",
				schedConfig.instanceName);
		//System.setProperty("org.quartz.threadPool.threadCount",
		//		String.valueOf(schedConfig.threadCount));
		//System.setProperty("org.quartz.jobStore.class",
			//	schedConfig.jobStoreClass);
		 System.setProperty("org.quartz.scheduler.skipUpdateCheck","true");
		System.setProperty("org.quartz.scheduler.instanceName","DatabaseScheduler");
		System.setProperty("org.quartz.scheduler.instanceId","NON_CLUSTERED");
		System.setProperty("org.quartz.scheduler.jobFactory.class","org.quartz.simpl.SimpleJobFactory");
		System.setProperty("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
		System.setProperty("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
		System.setProperty("org.quartz.jobStore.dataSource","quartzDataSource");
		System.setProperty("org.quartz.jobStore.tablePrefix","QRTZ_");
		System.setProperty("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
		System.setProperty("org.quartz.threadPool.threadCount","5");

// JobStore: JDBC jobStoreTX
		System.setProperty("org.quartz.dataSource.quartzDataSource.driver","com.mysql.jdbc.Driver");
		System.setProperty("org.quartz.dataSource.quartzDataSource.URL", url);
		System.setProperty("org.quartz.dataSource.quartzDataSource.user",mySQLConfig.user);
		System.setProperty("org.quartz.dataSource.quartzDataSource.password",mySQLConfig.password);
		System.setProperty("org.quartz.dataSource.quartzDataSource.maxConnections","8");
if(Settings.getInstance().config.initConfig.formatDB || Settings.getInstance().config.schedulerConfig.createTables){
	try{
		Connection con = DriverManager.getConnection(url, mySQLConfig.user, mySQLConfig.password);
		ScriptRunner runner = new ScriptRunner(con, true, true);
		System.out.println("CREATE QUARTZ TABLES...");
		InputStream inpStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("config/quartz_tables_mysql_innodb.sql");
		runner.runScript(new InputStreamReader(inpStream));
	}catch(SQLException ex){
		logger.error(ex);
	}catch(FileNotFoundException fex){
		logger.error(fex);
	}catch(IOException ioex){
		logger.error(ioex);
	}
}

		SchedulerFactory sf = new StdSchedulerFactory();

		sched = sf.getScheduler();
		// Create a listener for all the jobs
		sched.getListenerManager()
				.addJobListener(new JobsListener(), allJobs());
		// Initialize scheduler to run after 15 minutes
		// sched.startDelayed(900);
		sched.start();

	}

	public void shutdownScheduler() {
		try {
			sched.shutdown();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isSchedulerStarted() {
		boolean isStarted = false;
		try {
			if (sched != null) {
				isStarted = !sched.isShutdown();
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isStarted;
	}

	@Override
	public <T extends Job> void startJobForSpecificJobClass(Class<T> clazz) {
		// JobBuilder
		// jobBuilder=JobBuilder.newJob(AssociationRulesForCompetencesDiscoveryJob.class);
		JobBuilder jobBuilder = JobBuilder.newJob(clazz);
		jobBuilder.withIdentity(clazz.getSimpleName().toLowerCase(), "job");
		jobBuilder.storeDurably(false);
		JobDetail jobDetails = jobBuilder.build();
		SimpleScheduleBuilder scheduleBuilder = null;

		scheduleBuilder = simpleSchedule().withIntervalInMinutes(2);
		scheduleBuilder.repeatForever();
		TriggerBuilder tb = TriggerBuilder.newTrigger();
		tb.withSchedule(scheduleBuilder);
		Calendar calendar = Calendar.getInstance();
		// calendar.add(Calendar.HOUR, 1);
		calendar.add(Calendar.MINUTE, 1);
		tb.startAt(calendar.getTime());
		Trigger trigger = tb.build();

		JobWrapper jobWrapper = JobWrapper.createJob(jobDetails, trigger, clazz
				.getSimpleName().toLowerCase());
		jobWrapper.setWaitInterval("10s");
		CronSchedulerImpl.getInstance().addWrappedJob(jobWrapper);
	}

	public void addWrappedJob(org.prosolo.bigdata.jobs.JobWrapper jobWrapper) {
 		try {
			if (sched == null || sched.isShutdown()) {
				logger.error("Scheduler is not running");
			}

				sched.scheduleJob(jobWrapper.jobDetail, jobWrapper.trigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void checkAndActivateJob(String jobClassName, QuartzJobConfig jobConfig)
			throws SchedulerException, ClassNotFoundException {
		//String jobClassName = jobConfig.className;
		System.out.println("JOB CLASS NAME:" + jobClassName);

		Class<? extends Job> jobClass = (Class<? extends Job>) Class
				.forName(jobClassName);
		JobKey jobKey = jobKey(jobClassName, "job");
		if(sched.checkExists(jobKey)) {
			sched.deleteJob(jobKey);
		}
		if (jobConfig.activated) {
			JobBuilder jobBuilder = JobBuilder.newJob(jobClass);
			jobBuilder.withIdentity(jobKey);
			jobBuilder.storeDurably();
			JobDetail jobDetails = jobBuilder.build();
			sched.addJob(jobDetails, true);

			TriggerBuilder tb = TriggerBuilder.newTrigger();
			tb.forJob(jobKey);
			tb.withIdentity(jobClassName,"job");
			String cronExpression=jobConfig.schedule;
			CronScheduleBuilder.cronSchedule(cronExpression);
			tb.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression));
			Trigger trigger=tb.build();
			sched.scheduleJob(trigger);
		}
		JobKey startupJobKey = jobKey(jobClassName+"_startup", "job");
		if(sched.checkExists(startupJobKey)) {
			sched.deleteJob(startupJobKey);
		}
		if (jobConfig.onStartup) {
			this.startupJobsCounter++;
			JobBuilder jobBuilder = JobBuilder.newJob(jobClass);

			jobBuilder.withIdentity(startupJobKey);
			System.out.println("RUNNING ON startup JOB:"+jobClassName);
			jobBuilder.storeDurably();
			JobDetail jobDetails = jobBuilder.build();

			TriggerBuilder tb = TriggerBuilder.newTrigger();
			tb.forJob(startupJobKey);
			tb.withIdentity(jobClassName+"_startup","job");
			tb.startAt(DateBuilder.futureDate(this.startupJobsCounter*30, DateBuilder.IntervalUnit.SECOND));
			Trigger trigger=tb.build();

			sched.addJob(jobDetails, true);
			sched.scheduleJob(trigger);

		}

	}



	@Override
	public boolean isJobAlreadyRunning(String jobId, String groupId) throws SchedulerException {
		return sched.checkExists(jobKey(jobId, groupId));
	}
}
