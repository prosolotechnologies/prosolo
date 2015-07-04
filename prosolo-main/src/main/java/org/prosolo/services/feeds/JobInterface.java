package org.prosolo.services.feeds;

import org.quartz.JobExecutionException;

/**
 * @author Zoran Jeremic 2013-08-30
 *
 */
public interface JobInterface {
	
	void execute() throws JobExecutionException;
}
