package org.prosolo;

import org.junit.Assert;
import org.junit.Test;


//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:core/*/context.xml" })
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
//@Transactional
public class BaseTest  {

//	@BeforeSuite(alwaysRun=true)
//	protected void pupulateTestDatabase() throws Exception {
		// Load a batch of test data in the transaction
//		executeSqlScript("classpath:test_bc_10.sql.gz", false);
//	}

	@Test
	public void testIfItWorks() {
		Assert.assertEquals(2, 2);
	}
}
