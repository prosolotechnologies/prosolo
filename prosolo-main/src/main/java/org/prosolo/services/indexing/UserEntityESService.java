package org.prosolo.services.indexing;

import org.hibernate.Session;
import org.prosolo.domainmodel.user.User;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface UserEntityESService  extends AbstractBaseEntityESService {

	void saveUserNode(User user, Session session);

}
