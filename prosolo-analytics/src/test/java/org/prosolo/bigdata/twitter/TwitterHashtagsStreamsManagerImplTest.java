package org.prosolo.bigdata.twitter;


import org.junit.Test;
import org.prosolo.bigdata.scala.twitter.TwitterHashtagsStreamsManager$;

/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class TwitterHashtagsStreamsManagerImplTest {

	@Test
	public void testInitialize() {
		//TwitterHashtagsStreamsManagerImpl manager=new TwitterHashtagsStreamsManagerImpl();
		//manager.initialize();
		TwitterHashtagsStreamsManager$ twitterManager=TwitterHashtagsStreamsManager$.MODULE$;
		System.out.println("ENABLE THIS");
		 twitterManager.initialize();
		// twitterManager.startStreamsForHashTags();
		 try {
			Thread.sleep(100000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

