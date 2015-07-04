/**
 * 
 */
package org.prosolo.services.authentication.exceptions;

/**
 * @author "Nikola Milikic"
 *
 */
public class ResetKeyExpiredException extends Exception {

	private static final long serialVersionUID = 1L;

	public ResetKeyExpiredException(String message) {
		super(message);
	}
}
