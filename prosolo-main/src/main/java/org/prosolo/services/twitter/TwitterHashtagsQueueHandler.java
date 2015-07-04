package org.prosolo.services.twitter;

import java.util.List;

import twitter4j.Status;

/**
 * @author Zoran Jeremic 2013-10-14
 *
 */

public interface TwitterHashtagsQueueHandler {

	public abstract void enqueue(Status object);

	public abstract Status dequeue();

	List<String> getHashtagsBlackList();

	boolean isHashTagBlacklisted(String tag);

}