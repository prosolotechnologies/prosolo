package org.prosolo.services.authentication.exceptions;

/**
 * Exception thrown when a certain password does not match the one for a certain
 * user
 */
public class InvalidPasswordException extends AccountNotFoundException {

	private static final long serialVersionUID = -4379597444602496250L;

	public InvalidPasswordException(String message) {
		super(message);
	}
}
