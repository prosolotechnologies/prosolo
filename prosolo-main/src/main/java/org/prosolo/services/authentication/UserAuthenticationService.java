package org.prosolo.services.authentication;

import org.prosolo.common.domainmodel.user.User;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-08-30
 * @since 1.0.0
 */
public interface UserAuthenticationService extends Serializable {

    UserDetails authenticateUser(User user) throws LockedException;
}
