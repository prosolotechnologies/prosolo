package org.prosolo.services.authentication;

import org.prosolo.services.authentication.exceptions.AuthenticationException;

public interface AuthenticationService {

	boolean login(String email, String password) throws AuthenticationException;

	void logout();

	boolean loginOpenId(String email) throws AuthenticationException;

	boolean checkPassword(String oldPassword, String newPassword);
}
