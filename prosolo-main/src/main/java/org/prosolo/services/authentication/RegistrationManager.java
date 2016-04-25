package org.prosolo.services.authentication;

import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.oauth.OpenIDAccount;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Zoran Jeremic 2013-10-25
 *
 */

public interface RegistrationManager extends AbstractManager{

	public abstract boolean isEmailAlreadyExists(String emailAddress);

	User getUserByVerificationKey(String verificationKey);

	RegistrationKey getRegistrationKeyById(String registrationKey);

	boolean setUserAsVerified(String emailAddress, boolean verified);

	OpenIDAccount findOpenIDAccount(String verificationId);

}