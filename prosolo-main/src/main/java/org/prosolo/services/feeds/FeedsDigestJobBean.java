package org.prosolo.services.feeds;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Zoran Jeremic 2013-08-30
 * 
 */
public class FeedsDigestJobBean extends QuartzJobBean {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FeedsDigestJobBean.class);

	private static final String APPLICATION_CONTEXT_KEY = "applicationContext";
	private static final String JOB_BEAN_NAME_KEY = "job.bean.name";

	@Override
	protected final void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		boolean distributed = Settings.getInstance().config.rabbitmq.distributed;
		boolean master = Settings.getInstance().config.rabbitmq.masterNode;
	
		if ((master && distributed) || !distributed) {
			SchedulerContext schedulerContext = null;
			
			try {
				schedulerContext = jobExecutionContext.getScheduler().getContext();
			} catch (SchedulerException e) {
				throw new JobExecutionException("Failure accessing scheduler context", e);
			}
			
			ApplicationContext appContext = (ApplicationContext) schedulerContext.get(APPLICATION_CONTEXT_KEY);
			String jobBeanName = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(JOB_BEAN_NAME_KEY);
//			String updatePeriod = (String) jobExecutionContext.getJobDetail().getJobDataMap().get("update.period");
//			TimeFrame timeFrame = TimeFrame.valueOf(updatePeriod);
			
			JobInterface jobBean = (JobInterface) appContext.getBean(jobBeanName);
			jobBean.execute();
		}
	}

}
