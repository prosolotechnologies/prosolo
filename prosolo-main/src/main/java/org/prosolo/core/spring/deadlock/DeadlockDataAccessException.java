/**
 * 
 */
package org.prosolo.core.spring.deadlock;

/**
 * @author "Nikola Milikic"
 *
 */
public class DeadlockDataAccessException extends Exception {

	private static final long serialVersionUID = -8601661556919784656L;

	public DeadlockDataAccessException(String msg) {
		super(msg);
	}
}
