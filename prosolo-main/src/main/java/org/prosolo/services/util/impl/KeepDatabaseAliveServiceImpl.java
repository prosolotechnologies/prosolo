/**
 * 
 */
package org.prosolo.services.util.impl;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.util.KeepDatabaseAliveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author "Nikola Milikic"
 *
 */
@Service("org.prosolo.services.util.KeepDatabaseAliveService")
public class KeepDatabaseAliveServiceImpl extends AbstractManagerImpl implements KeepDatabaseAliveService {

	private static final long serialVersionUID = 1393992402890925356L;
	
	private static Logger logger = Logger.getLogger(KeepDatabaseAliveService.class);
	
	@Override
	@Transactional
	public boolean pingDatabase() {
		Session session = persistence.currentManager();
		
		String queryString = 
			"FROM Role role ";
		
		logger.debug("hb query:"+queryString);
		
		session.createQuery(queryString).setMaxResults(1)
				.setMaxResults(1)
				.list();
		
		return true;
	}
}
