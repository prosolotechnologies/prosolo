package org.prosolo.services.authentication;

public interface AuthenticationService {

	boolean loginUserOpenID(String email);

	void logout();

	boolean checkPassword(String rawPassword, String encodedPassword);
}
