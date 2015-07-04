package org.prosolo.services.twitter.impl;

import org.junit.Test;
import org.prosolo.core.stress.TestContext;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Zoran Jeremic, Aug 30, 2014
 *
 */
public class TwitterStreamsManagerImplTest  extends TestContext  {

	@Autowired private TwitterStreamsManager twitterStreamsManager;
	@Test
	public void startTest() {
		twitterStreamsManager.start();
	}

}
