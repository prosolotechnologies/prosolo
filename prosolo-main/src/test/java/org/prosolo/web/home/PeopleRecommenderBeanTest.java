package org.prosolo.web.home;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.prosolo.domainmodel.user.User;
import org.prosolo.core.stress.TestContext;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.web.activitywall.data.UserData;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zoran Jeremic Oct 17, 2014
 *
 */

public class PeopleRecommenderBeanTest  extends TestContext{
	//@Autowired PeopleRecommenderBean peopleRecommender;
	@Autowired private CollaboratorsRecommendation cRecommendation;
	@Autowired private LoggingDBManager loggingDBManager;
	@Autowired private UserManager userManager;
	private List<UserData> activityRecommendedUsers;
	private Logger logger = Logger.getLogger(PeopleRecommenderBeanTest.class);
//	@Autowired private LoggedUserBean loggedUser;
	@Ignore
	@Test
	public void testInitLocationRecommend() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitActivityRecommend() {
		 User cUser=userManager.getUser("zoran.jeremic@gmail.com");
			if (activityRecommendedUsers == null) {
				logger.info("Initializing activity based user recommendation for a user '"+cUser.getLastname()+"'");

				activityRecommendedUsers = new ArrayList<UserData>();
				
				List<User> users=cRecommendation.getMostActiveRecommendedUsers(cUser, 3);
				System.out.println("FOUND USERS:"+users.size());
				if (users != null && !users.isEmpty()) {
					for (User user : users) {
						UserData userData = new UserData(user);
						
						// TODO: Zoran - put here last activity date
						long timestamp=loggingDBManager.getMostActiveUsersLastActivityTimestamp(user.getId());
						userData.setLastAction(new Date(timestamp));
						
						activityRecommendedUsers.add(userData);
					}
					logger.info("Activity based user recommendations initialized '"+cUser+"' found:"+activityRecommendedUsers.size());
				}
		 
		}
	}
@Ignore
	@Test
	public void testInitSimilarityRecommend() {
		fail("Not yet implemented");
	}

}
