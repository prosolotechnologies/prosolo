package org.prosolo.services.session;

import org.prosolo.web.SessionCountBean;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class SessionCountJobBean extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		((SessionCountBean) context.getJobDetail().getJobDataMap().get("sessioncountbean")).storeCount();
	}

}
