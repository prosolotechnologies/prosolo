package org.prosolo.services.lti;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.prosolo.core.spring.SpringConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ SpringConfig.class })
public class LtiUserManagerTest {
	
	@Inject private LtiUserManager userManager;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*@Test
	public void testGetUserForLaunch() {
		User user = userManager.getUserForLaunch(196608, "3", "Stefan", "Vuckovic", 
				"stefan.vuckovic10@gmail.com");
		System.out.println("USER NAME "+user.getName());
		assertNotNull("Error returning user for launch", user);
	}*/

}
