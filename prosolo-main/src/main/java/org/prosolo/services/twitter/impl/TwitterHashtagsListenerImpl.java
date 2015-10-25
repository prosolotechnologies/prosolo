package org.prosolo.services.twitter.impl;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.twitter.TwitterHashtagsListener;
import org.prosolo.services.twitter.TwitterHashtagsQueueHandler;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

/**
 * @author Zoran Jeremic 2013-09-25
 * 
 */
@Deprecated
//@Service("org.prosolo.services.twitter.TwitterHashtagListener")
public class TwitterHashtagsListenerImpl implements StatusListener, TwitterHashtagsListener {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TwitterHashtagsListenerImpl.class);
	 
	@Override
	public void onStatus(Status status) {
		//System.out.println("TwitterHashTagsListener on status:"+status.getText());
		ServiceLocator.getInstance().getService(TwitterHashtagsQueueHandler.class).enqueue(status);
	}

	@Override
	public void onException(Exception arg0) { }

	@Override
	public void onDeletionNotice(StatusDeletionNotice arg0) { }

	@Override
	public void onScrubGeo(long arg0, long arg1) { }

	@Override
	public void onStallWarning(StallWarning arg0) { }

	@Override
	public void onTrackLimitationNotice(int arg0) { }

}
