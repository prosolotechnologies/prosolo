
package org.prosolo.recommendation.util;

import java.util.concurrent.Callable;

/**
 * @author Zoran Jeremic
 * @date May 1, 2012
 */
public interface ReadOnlyCommandService   {
	 
	/**
     * Execute a command within a read only transaction.
     *
     * @param command the command to execute (not null)
     */
    void inReadOnlyTransaction(Runnable command);

    /**
     * Execute a command within a read only transaction.
     *
     * @param <T> result type
     * @param command the command to execute (not null)
     * @return result of command
     * @throws RuntimeException when command throws an exceptie,
     *   message is "CommandService#inTransaction(s)" where s is the toString of command
     */
    <T> T inReadOnlyTransaction(Callable<T> command);
}


