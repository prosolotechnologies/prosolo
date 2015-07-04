/**
 * 
 */
package org.prosolo.core.persistance;

import org.apache.log4j.Logger;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.util.KeepDatabaseAliveService;

/**
 * @author "Nikola Milikic"
 *
 */
public class KeepDatabaseAliveThread extends Thread {
	
	private static Logger logger = Logger.getLogger(KeepDatabaseAliveThread.class);

    private KeepDatabaseAliveService keepDatabaseAliveService;
    
    public KeepDatabaseAliveThread() {
    	keepDatabaseAliveService = ServiceLocator.getInstance().getService(KeepDatabaseAliveService.class);
    }
    
	@Override
	public void run() {
		while (true) {
			try {
				sleep(4 * 60 * 60 * 1000);
			} catch (InterruptedException e) { }
			
			logger.debug("Pinging  database to keep the connection alive");
			keepDatabaseAliveService.pingDatabase();
		}
	}
}
