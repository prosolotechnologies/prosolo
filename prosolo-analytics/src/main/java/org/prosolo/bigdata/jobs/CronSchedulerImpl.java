package org.prosolo.bigdata.jobs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.http.ParseException;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.config.DBConfig;
import org.prosolo.bigdata.config.DBServerConfig;
import org.prosolo.bigdata.config.QuartzJobConfig;
import org.prosolo.bigdata.config.SchedulerConfig;
import org.prosolo.bigdata.config.Settings;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;

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

	Set<JobWrapper> jobsList;
	List<String> executedJobs;

	private CronSchedulerImpl() {
		jobsList = new HashSet<JobWrapper>();
		executedJobs = new ArrayList<String>();
		try {
			System.out.println("SHOULD AUTOSTART:"
					+ Settings.getInstance().config.schedulerConfig.autoStart);
			if (Settings.getInstance().config.schedulerConfig.autoStart) {
				startScheduler();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addExecutedJob(String id) {
		if (!this.executedJobs.contains(id)) {
			this.executedJobs.add(id);
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
	public void removeJobTriggerAndWrapper(String jobId) {
		JobWrapper jobWrapper = findJobWrapper(jobId);
		if (jobWrapper != null) {
			Trigger oldTrigger = jobWrapper.getTrigger();
			try {
				sched.deleteJob(oldTrigger.getJobKey());
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		jobsList.remove(jobWrapper);
	}

	private void removeJobWrapper(String jobId) {
		Iterator<JobWrapper> iter = jobsList.iterator();
		while (iter.hasNext()) {
			JobWrapper job = iter.next();
			if (job.jobId.equals(jobId)) {
				iter.remove();
			}
		}
	}

	public void replaceJobTrigger(String jobId, Date newTime) {
		JobWrapper jobWrapper = findJobWrapper(jobId);
		if (jobWrapper != null) {
			Trigger oldTrigger = jobWrapper.getTrigger();
			TriggerBuilder tb = oldTrigger.getTriggerBuilder();
			Trigger newTrigger = tb.startAt(newTime).build();
			try {
				logger.info("Replace job trigger:" + oldTrigger.getKey()
						+ " with " + newTrigger.getKey());
				sched.deleteJob(oldTrigger.getJobKey());
				sched.scheduleJob(jobWrapper.jobDetail, newTrigger);
				jobWrapper.setTrigger(newTrigger);
				this.replaceJobWrapper(jobId, jobWrapper);

			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void rescheduleJobTrigger(String jobId, Date newTime) {
		JobWrapper jobWrapper = findJobWrapper(jobId);
		if (jobWrapper != null) {
			Trigger oldTrigger = jobWrapper.getTrigger();
			TriggerBuilder tb = oldTrigger.getTriggerBuilder();
			Trigger newTrigger = tb.startAt(newTime).build();
			try {
				sched.rescheduleJob(oldTrigger.getKey(), newTrigger);
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public boolean isJobExecuted(String id) {
		if (executedJobs.contains(id)) {
			return true;
		} else {
			return false;
		}
	}

	public JobWrapper findJobWrapper(String jobId) {
		for (JobWrapper job : jobsList) {
			if (job.jobId.equals(jobId)) {
				return job;
			}
		}
		return null;
	}

	public void replaceJobWrapper(String jobId, JobWrapper newWrapper) {
		Iterator<JobWrapper> iter = jobsList.iterator();
		while (iter.hasNext()) {
			JobWrapper job = iter.next();
			if (job.jobId.equals(jobId)) {
				iter.remove();
			}
		}
		jobsList.add(newWrapper);
	}

	public boolean isJobExistsInJobListById(String id) {
		for (JobWrapper job : jobsList) {
			if (job.jobId.equals(id)) {
				return true;
			}

		}
		return false;
	}

	public void removeExecutedJob(String id) {
		if (this.executedJobs.contains(id)) {
			this.executedJobs.remove(id);
		}
	}

	public void startScheduler() throws SchedulerException, ParseException {
		// System.getProperties()
		// .put("org.quartz.properties", "quartz.properties");
		// DBConfig dbConfig=Settings.getInstance().config.dbConfig;

		// String
		// mongoUri="mongodb://"+serverConfig.dbHost+":"+serverConfig.dbPort;
		SchedulerConfig schedConfig = Settings.getInstance().config.schedulerConfig;
		logger.info("STARTING CRON SCHEDULER:");
		System.setProperty("org.quartz.scheduler.instanceName",
				schedConfig.instanceName);
		System.setProperty("org.quartz.threadPool.threadCount",
				String.valueOf(schedConfig.threadCount));
		System.setProperty("org.quartz.jobStore.class",
				schedConfig.jobStoreClass);
		// System.setProperty("org.quartz.jobStore.mongoUri",mongoUri);
		// System.setProperty("org.quartz.jobStore.dbName",dbConfig.dbQuartzName);
		// System.setProperty("org.quartz.jobStore.collectionPrefix",schedConfig.collectionPrefix);
		// # comma separated list of mongodb hosts/replica set seeds (optional
		// if 'org.quartz.jobStore.mongoUri' is set)
		// org.quartz.jobStore.addresses=host1,host2
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

	private void addWrappedJob(org.prosolo.bigdata.jobs.JobWrapper jobWrapper) {
		jobsList.add(jobWrapper);
		try {
			if (sched == null || sched.isShutdown()) {
				logger.error("Scheduler is not running");
			}
			if (jobWrapper.getDependencies().size() == 0) {
				sched.scheduleJob(jobWrapper.jobDetail, jobWrapper.trigger);
			} else {
				sched.addJob(jobWrapper.jobDetail, true);
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void checkAndActivateJob(QuartzJobConfig jobConfig)
			throws SchedulerException, ClassNotFoundException {
		String jobClassName = jobConfig.className;
		System.out.println("JOB CLASS NAME:" + jobClassName);
		Class<? extends Job> jobClass = (Class<? extends Job>) Class
				.forName(jobClassName);

		if (jobConfig.activated) {
			JobKey jobKey = JobKey.jobKey(jobClassName, "job");

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
		if (jobConfig.onStartup) {
			JobBuilder jobBuilder = JobBuilder.newJob(jobClass);
			JobKey jobKey = JobKey.jobKey(jobClassName+"_startup", "job");
			jobBuilder.withIdentity(jobKey);
			System.out.println("RUNNING ON startup JOB:"+jobClassName);
			jobBuilder.storeDurably();
			JobDetail jobDetails = jobBuilder.build();

			TriggerBuilder tb = TriggerBuilder.newTrigger();
			tb.forJob(jobKey);
			tb.withIdentity(jobClassName+"_startup","job");
			tb.startAt(DateBuilder.futureDate(2, DateBuilder.IntervalUnit.MINUTE));
			Trigger trigger=tb.build();

			sched.addJob(jobDetails, true);
			sched.scheduleJob(trigger);

		}

	}
}
