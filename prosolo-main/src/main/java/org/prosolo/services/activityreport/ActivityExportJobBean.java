package org.prosolo.services.activityreport;

import org.apache.log4j.Logger;
import org.prosolo.services.feeds.JobInterface;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is a scheduled bean which triggers the generation of Prosolo activity
 * reports for each user.
 * 
 * @author Vitomir Kovanovic 2014-10-22
 */
public class ActivityExportJobBean implements JobInterface {
	private static Logger logger = Logger.getLogger(ActivityExportJobBean.class);
	
	@Autowired
	private ActivityExportManager exportManager;
	
	@Override
	@Transactional
	public void execute() throws JobExecutionException {
		logger.info("EXECUTING ACTIVITY EXPORT JOB BEAN");
		exportManager.runActivityExport();
	}
}
