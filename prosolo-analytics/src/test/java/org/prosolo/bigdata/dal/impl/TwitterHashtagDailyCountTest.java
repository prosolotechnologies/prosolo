package org.prosolo.bigdata.dal.impl;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.prosolo.bigdata.dal.cassandra.impl.TwitterHashtagStatisticsDBManagerImpl;
import org.prosolo.common.util.date.DateEpochUtil;

public class TwitterHashtagDailyCountTest {

	@Ignore
	@Test
	public void generateEntries() {
		// Generates 10000 hashtag counts for last two weeks.
		TwitterHashtagStatisticsDBManagerImpl manager =  TwitterHashtagStatisticsDBManagerImpl.getInstance();
		long to = DateEpochUtil.getDaysSinceEpoch();
		long from = to - 15;

		for (int hashtagIndex = 0; hashtagIndex < 10000; hashtagIndex++) {
			String hashtag = UUID.randomUUID().toString();
			for (long day = from; day <= to; day++) {
				for (int count = random(1, 100); count > 0; count--) {
					manager.updateTwitterHashtagDailyCount(hashtag, day);
				}
			}
		}
	}

	private int random(int lo, int hi) {
		return (int) (lo + Math.round(Math.random() * (hi - lo)));
	}

}
