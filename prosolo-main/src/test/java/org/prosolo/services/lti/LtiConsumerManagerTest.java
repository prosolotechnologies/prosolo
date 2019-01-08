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
public class LtiConsumerManagerTest {

	@Inject private LtiConsumerManager consumerManager;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/*@Test
	public void testRegisterLTIConsumer() throws Exception{
		String key = UUID.randomUUID().toString();
		String secret = UUID.randomUUID().toString();
		LtiConsumer cons = new LtiConsumer();
		cons.setKeyLtiTwo(key);
		cons.setSecretLtiTwo(secret);
		long toolSetId = 294912;
		LtiConsumer consumer = consumerManager.registerLTIConsumer(cons, toolSetId);
		assertNotNull("Error registering consumer", consumer);
	}*/
	
	
	/*@Test
	public void testRegisterLTIConsumerCapabilities() throws Exception{
		LtiConsumer cons = new LtiConsumer();
		List<String> caps = new ArrayList<>();
		caps.add("test1");
		caps.add("test2");
		caps.add("test3");
		cons.setCapabilitieList(caps);
		String key = UUID.randomUUID().toString();
		String secret = UUID.randomUUID().toString();
		cons.setKeyLtiTwo(key);
		cons.setSecretLtiTwo(secret);
		long toolSetId = 196608;
		LtiConsumer consumer = consumerManager.registerLTIConsumer(cons, toolSetId);
		assertEquals("Error registering consumer", 3, consumer.getCapabilitieList().size());
	}*/
	
}
