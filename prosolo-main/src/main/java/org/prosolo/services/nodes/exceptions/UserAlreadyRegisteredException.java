/**
 * 
 */
package org.prosolo.services.nodes.exceptions;

/**
 * @author "Nikola Milikic"
 *
 */
public class UserAlreadyRegisteredException extends Exception {

	private static final long serialVersionUID = 1707505363942396793L;

	public UserAlreadyRegisteredException(String msg) {
		super(msg);
	}
}
