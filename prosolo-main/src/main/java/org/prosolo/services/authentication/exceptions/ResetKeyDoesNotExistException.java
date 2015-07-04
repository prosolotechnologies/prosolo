/**
 * 
 */
package org.prosolo.services.authentication.exceptions;

/**
 * @author "Nikola Milikic"
 *
 */
public class ResetKeyDoesNotExistException extends Exception {

	private static final long serialVersionUID = 1968045973508102455L;

	public ResetKeyDoesNotExistException(String message) {
		super(message);
	}
}
