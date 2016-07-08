package org.prosolo.services.lti;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.common.domainmodel.lti.LtiToolSet;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.core.spring.SpringConfig;
import org.prosolo.services.lti.exceptions.ConsumerAlreadyRegisteredException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ SpringConfig.class })
public class ToolSetManagerTest {

	@Inject private ToolSetManager tsManager;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSaveToolSet() {
		LtiTool tool = new LtiTool();
		tool.setName("Tool 1");
		tool.setActivityId(1);
		tool.setLearningGoalId(1);
		tool.setCompetenceId(1);
		User u = new User();
		u.setId(2);
	    tool.setCreatedBy(u);
	    LtiToolSet ts = tsManager.saveToolSet(tool);
	    assertNotNull("Error while saving tool set", ts);
	}
	
	@Test(expected = DataIntegrityViolationException.class)
	public void testSaveToolSetNonExistingUser() {
		LtiTool tool = new LtiTool();
		tool.setName("Tool 1");
		tool.setActivityId(1);
		tool.setLearningGoalId(1);
		tool.setCompetenceId(1);
		User u = new User();
		u.setId(12365);
	    tool.setCreatedBy(u);
	    tsManager.saveToolSet(tool);
	}

	@Test
	public void testCheckIfToolSetExists() throws Exception{
		boolean exists = tsManager.checkIfToolSetExists(131072);
		assertTrue("Tool set does not exist error ", exists);
	}
	
	@Test
	public void testCheckIfToolSetExistsNonExistingId() throws Exception{
		boolean exists = tsManager.checkIfToolSetExists(131072378);
		assertFalse("Tool set exists error ", exists);
	}
	
	@Test(expected = ConsumerAlreadyRegisteredException.class)
	public void testCheckIfToolSetExistsAlreadyRegisteredConsumer() {
		tsManager.checkIfToolSetExists(196608);
	}

}
