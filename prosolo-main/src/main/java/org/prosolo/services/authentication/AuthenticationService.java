package org.prosolo.services.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.prosolo.services.authentication.exceptions.AuthenticationException;

public interface AuthenticationService {

	boolean login(String email, String password) throws AuthenticationException;
	
	void login(HttpServletRequest req, HttpServletResponse resp, String email) 
			throws AuthenticationException;

	void logout();

	boolean loginOpenId(String email) throws AuthenticationException;

	boolean checkPassword(String rawPassword, String encodedPassword);
}
