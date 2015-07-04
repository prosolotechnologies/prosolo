
package org.prosolo.recommendation.util;

import java.util.concurrent.Callable;


/**
 * @author Zoran Jeremic
 * @date May 1, 2012
 */

public interface CommandService extends ReadOnlyCommandService{
	 /**
     * Execute a command within a transaction.
     *
     * @param command the command to execute (not null)
     */
    void inTransaction(Runnable command);

    /**
     * Execute a command within a transaction.
     *
     * @param <T> result type
     * @param command the command to execute (not null)
     * @return result of command
     * @throws RuntimeException when command throws an exceptie,
     *   message is "CommandService#inTransaction(s)" where s is the toString of command
     */
    <T> T inTransaction(Callable<T> command);
}


