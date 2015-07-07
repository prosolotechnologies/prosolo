package org.prosolo.recommendation.impl;



import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.stress.TestContext;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.logging.LoggingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Zoran Jeremic, Aug 20, 2014
 *
 */
public class CollaboratorsRecommendationImplTest extends TestContext {

	@Autowired private CollaboratorsRecommendation collaboratorsRecommendation;
	@Autowired private LoggingService loggingService;
	
	@Test
	public void testGetMostActiveRecommendedUsers() {
		collaboratorsRecommendation.getMostActiveRecommendedUsers(new User(), 10);
	}
	 @Ignore
	@Test
	public void generateRandomLogs(){
		System.out.println("generate randome logs started");
		int since=16305;
		for(int i=since;i>16290;i--){
			System.out.println("since:"+i);
			for(int x=0;x<1000;x++){
				Random random=new Random();
				Long userid=(long) random.nextInt(28);
				if(userid>0)
				loggingService.increaseUserActivityLog(userid,i);
			}
		}
		
	}

}
