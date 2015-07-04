package org.prosolo.services.feeds;

import org.junit.Test;
import org.prosolo.core.stress.TestContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zoran Jeremic Sep 20, 2014
 *
 */

public class CheckFeedsJobBeanTest extends TestContext {
	
	@Autowired
	FeedsManager feedsManager;
	@Autowired
	FeedsAgregator feedsAgregator;
	
	@Test
	public void testExecute() {
//		TimeFrame timeFrame = TimeFrame.DAILY;
//		Collection<User> users = userManager.getAllUsers();
//		String systemUserEmail = Settings.getInstance().config.init.defaultUser.email;
//		User systemUser = userManager.getUser(systemUserEmail);
//		PersonalFeed systemFeed = feedsAgregator.aggregateSystemFeeds(systemUser.getId(), timeFrame);
//		int counter = 0;
//		for (User user : users) {
//			FeedsPreferences feedsPreferences = feedsManager.getFeedsPreferences(user);
//			if (feedsPreferences.getUpdatePeriod() == null) {
//				feedsPreferences.setUpdatePeriod(TimeFrame.DAILY);
//				feedsManager.saveEntity(feedsPreferences);
//			}
//			
//			if (feedsPreferences.getUpdatePeriod() != null && feedsPreferences.getUpdatePeriod().equals(timeFrame)) {
//				System.out.println("***************");
//				System.out.println("Agregate feed");
//				System.out.println("time period is good:" + user.getLastname() + " " + user.getName());
//				counter++;
//				if (counter < 4 && counter > 2) {
//					DailyUpdate dUpdate = new DailyUpdate();
//					try {
//						dUpdate.createUpdates(user, systemFeed, null);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//				counter++;
//			}
//		}
	}
	
}
