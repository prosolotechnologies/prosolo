/**
 * 
 */
package org.prosolo.services.authentication;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.authentication.exceptions.ResetKeyDoesNotExistException;
import org.prosolo.services.authentication.exceptions.ResetKeyExpiredException;
import org.prosolo.services.authentication.exceptions.ResetKeyInvalidatedException;

/**
 * @author "Nikola Milikic"
 *
 */
public interface PasswordResetManager {

	boolean initiatePasswordReset(User user, String email, String serverAddress);

	/**
	 * @param resetKey
	 * @return
	 * @throws ResetKeyDoesNotExistException
	 * @throws ResetKeyInvalidatedException 
	 * @throws ResetKeyExpiredException 
	 */
	boolean checkIfResetKeyIsValid(String resetKey) throws ResetKeyDoesNotExistException, ResetKeyInvalidatedException, ResetKeyExpiredException;

	User getResetKeyUser(String resetKey);

	/**
	 * @param resetKey
	 */
	void invalidateResetKey(String resetKey);
}
