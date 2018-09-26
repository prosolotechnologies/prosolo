package org.prosolo.services.authentication.exceptions;

/**
 * Exception thrown when an account with the information could not be found
 */
public class AuthenticationException extends RuntimeException {

	private static final long serialVersionUID = -6199558055473548513L;

	/**
	 * 
	 * @param message
	 */
	public AuthenticationException(String message) {
		super(message);
	}
}
