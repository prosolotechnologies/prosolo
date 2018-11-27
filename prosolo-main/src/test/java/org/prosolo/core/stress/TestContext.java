package org.prosolo.core.stress;

import org.junit.runner.RunWith;
import org.prosolo.services.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author Zoran Jeremic, May 16, 2014
 *
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/core/hibernate/testcontext.xml","classpath:/core/spring/testcontext.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
public abstract class TestContext {
	protected @Autowired UserManager userManager;

}
