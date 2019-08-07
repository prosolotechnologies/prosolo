package org.prosolo.services.lti;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prosolo.common.domainmodel.lti.LtiTool;
import org.prosolo.services.lti.data.ExternalToolFormData;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:core/spring/testcontext.xml")
public class LtiToolManagerTest {

	@Inject private LtiToolManager toolManager;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = DataIntegrityViolationException.class) 
	public void testSaveLtiToolNullRelationship() {
		LtiTool tool = new LtiTool();
		tool.setName("Tool");
		tool.setActivityId(1);
		tool.setCredentialId(1);
		tool.setCompetenceId(1);
		tool = toolManager.saveLtiTool(tool);
	}

	@Test
	public void testUpdateLtiTool() throws Exception{
		ExternalToolFormData t = new ExternalToolFormData();
		t.setTitle("Competence 1");
		t.setDescription("This is a tool for competence 1");
		t.setToolId(425984);
		LtiTool tool = toolManager.updateLtiTool(t);
		assertEquals("Error updating tool", "Competence 1", tool.getName());
	}

	@Test
	public void testChangeEnabled() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteLtiTool() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetToolDetails() {
		fail("Not yet implemented");
	}

//	@Test
//	public void testSearchToolsGeneral() {
//		Map<String, Object> parameters= new HashMap<>();
//		parameters.put(EntityConstants.CREDENTIAL_ID, 1);
//		parameters.put(EntityConstants.COMPETENCE_ID, 1);
//		parameters.put(EntityConstants.ACTIVITY_ID, 1);
//		List<LtiTool> tools = toolManager.searchTools(2, "t", parameters, new ToolSearchGeneralFilter());
//		for(LtiTool t:tools){
//			System.out.println("TOOL "+t.getId());
//		}
//		System.out.println("BROJ REZULTATA "+ tools.size());
//		assertEquals("Error searching tools", 4, tools.size());
//	}
//	
//	@Test
//	public void testSearchToolsCompetence() {
//		Map<String, Object> parameters= new HashMap<>();
//		parameters.put(EntityConstants.CREDENTIAL_ID, 1);
//		parameters.put(EntityConstants.COMPETENCE_ID, 1);
//		parameters.put(EntityConstants.ACTIVITY_ID, 1);
//		List<LtiTool> tools = toolManager.searchTools(2, "t", parameters, new ToolSearchCompetenceFilter());
//		for(LtiTool t:tools){
//			System.out.println("TOOL "+t.getId());
//		}
//		System.out.println("BROJ REZULTATA "+ tools.size());
//		assertEquals("Error searching tools", 0, tools.size());
//	}

	/*@Test
	public void testGetLtiToolForLaunch() {
		LtiTool tool = toolManager.getLtiToolForLaunch(425984);
		System.out.println("TOOL FOUND "+tool.getId());
		System.out.println("Consumer key "+tool.getToolSet().getConsumer().getKeyLtiOne());
		assertNotNull("Error searching tool", tool);
	}*/
	
	
}
