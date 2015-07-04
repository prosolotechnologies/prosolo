package org.prosolo.core.importing;

 

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prosolo.services.importing.ResourcesImporter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Zoran Jeremic 2013-07-14
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:core/*/context.xml"})
public class CompetencesImporterTest {

	@Test 
	public void testReadCompetencesFromJson() {
		ResourcesImporter ci=new ResourcesImporter();
		ci.batchImportExternalCompetences();
	}

}
