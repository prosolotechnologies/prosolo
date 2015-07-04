/**
 * 
 */
package org.prosolo.services.authentication.exceptions;

/**
 * @author "Nikola Milikic"
 *
 */
public class ResetKeyInvalidatedException extends Exception {

	private static final long serialVersionUID = 454947187033350568L;

	public ResetKeyInvalidatedException(String msg) {
		super(msg);
	}
}
