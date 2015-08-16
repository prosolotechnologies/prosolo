package org.prosolo.bigdata.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * @author Zoran Jeremic May 18, 2015
 *
 */

public class JobsListener implements JobListener {
	public static final String LISTENER_NAME = JobsListener.class
			.getCanonicalName();

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return LISTENER_NAME;
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobToBeExecuted(JobExecutionContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobWasExecuted(JobExecutionContext arg0,
			JobExecutionException arg1) {
		// TODO Auto-generated method stub

	}

}
