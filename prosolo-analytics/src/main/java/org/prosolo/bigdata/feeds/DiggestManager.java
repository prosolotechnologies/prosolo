package org.prosolo.bigdata.feeds;

/**
 * @author Zoran Jeremic 2013-08-29
 * 
 */
public interface DiggestManager {
	
	void initializeDiggestJobs();
	
	void createFeedDiggestsAndSendEmails();
	
}