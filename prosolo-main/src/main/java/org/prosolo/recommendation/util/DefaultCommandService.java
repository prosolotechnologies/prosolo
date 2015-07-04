
package org.prosolo.recommendation.util;

import java.util.concurrent.Callable;

import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic
 * @date May 1, 2012
 */
@Service("org.prosolo.recommendation.util.CommandService")
@Transactional
public class DefaultCommandService extends AbstractManagerImpl implements CommandService {
    
	private static final long serialVersionUID = 2289252437416204624L;

	@Transactional(readOnly = true)
    public void inReadOnlyTransaction(Runnable command) {
        command.run();
    }

    public void inTransaction(Runnable command) {
        command.run();
    }
    
    private <T> T doCall(Callable<T> command) {
        try {
            return command.call();
        } catch (Exception e) {
            throw new RuntimeException("CommandService#inTransaction(" + command.toString() + ")", e);
        }
    }

    @Transactional(readOnly = true)
	@Override
	public <T> T inReadOnlyTransaction(Callable<T> command) {
		 return doCall(command);
	}

	@Override
	public <T> T inTransaction(Callable<T> command) {
		return doCall(command);
	}
	
}


