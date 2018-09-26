package org.prosolo.services.authentication;

import org.prosolo.services.authentication.exceptions.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthenticationService {

	void loginAs(HttpServletRequest req, HttpServletResponse resp, String email)
			throws AuthenticationException;

	boolean loginUserOpenID(String email);

	boolean loginUserLTI(HttpServletRequest request, HttpServletResponse response, String email);


	void logout();

	boolean checkPassword(String rawPassword, String encodedPassword);
}
