package org.prosolo.services.authentication;

import org.prosolo.services.authentication.exceptions.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthenticationService {

	void login(HttpServletRequest req, HttpServletResponse resp, String email)
			throws AuthenticationException;

	void logout();

	boolean loginUser(String email) throws AuthenticationException;

	boolean checkPassword(String rawPassword, String encodedPassword);
}
