package org.prosolo.bigdata.twitter;

import static org.junit.Assert.*;

import org.junit.Test;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class TwitterHashtagsStreamsManagerImplTest {

	@Test
	public void testInitialize() {
		TwitterHashtagsStreamsManagerImpl manager=new TwitterHashtagsStreamsManagerImpl();
		manager.initialize();
	}

}

