/**
 * 
 */
package org.prosolo.services.email;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.User;

/**
 * @author "Nikola Milikic"
 *
 */
public interface EmailSenderManager {

	boolean sendEmailAboutNewAccount(User user, String email) throws IOException;

	boolean sendEmailAboutNewAccount(User user, String email, Session session) throws IOException;

	boolean sendEmailVerificationEmailForNewUser(User user) throws FileNotFoundException, IOException;

}
