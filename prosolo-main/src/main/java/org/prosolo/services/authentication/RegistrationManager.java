package org.prosolo.services.authentication;

import org.prosolo.common.domainmodel.app.RegistrationKey;
import org.prosolo.common.domainmodel.user.Email;
import org.prosolo.common.domainmodel.user.oauth.OpenIDAccount;
import org.prosolo.services.general.AbstractManager;

/**
 * @author Zoran Jeremic 2013-10-25
 *
 */

public interface RegistrationManager extends AbstractManager{

	public abstract boolean isEmailAlreadyExists(String emailAddress);

	Email getEmailByVerificationKey(String verificationKey);

	RegistrationKey getRegistrationKeyById(String registrationKey);

	boolean setEmailAsVerified(String emailAddress, boolean verified);

	OpenIDAccount findOpenIDAccount(String verificationId);

}