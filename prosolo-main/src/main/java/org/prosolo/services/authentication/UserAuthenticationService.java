package org.prosolo.services.authentication;

import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.core.spring.security.authentication.sessiondata.ProsoloUserDetails;
import org.prosolo.services.user.data.UserData;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-08-30
 * @since 1.0.0
 */
public interface UserAuthenticationService extends Serializable {

    ProsoloUserDetails authenticateUser(UserData user) throws UsernameNotFoundException, LockedException, DbConnectionException;
}
