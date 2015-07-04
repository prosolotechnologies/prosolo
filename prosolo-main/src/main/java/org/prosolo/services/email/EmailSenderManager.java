/**
 * 
 */
package org.prosolo.services.email;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.prosolo.domainmodel.user.Email;
import org.prosolo.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
public interface EmailSenderManager {

	boolean sendEmailAboutNewAccount(User user, String email) throws FileNotFoundException, IOException;

	boolean sendEmailVerificationEmailForNewUser(User user, Email email) throws FileNotFoundException, IOException;

}
