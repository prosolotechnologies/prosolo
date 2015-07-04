package org.prosolo.services.feeds;

/**
 * @author Zoran Jeremic 2013-08-29
 * 
 */
public interface DiggestManager {
	
	void initializeDiggestJobs();
	
	void createFeedDiggestsAndSendEmails();
	
}