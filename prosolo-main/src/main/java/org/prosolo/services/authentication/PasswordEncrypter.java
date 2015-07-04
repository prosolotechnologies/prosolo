package org.prosolo.services.authentication;

import org.springframework.security.authentication.encoding.PasswordEncoder;

public interface PasswordEncrypter extends PasswordEncoder {

	String encodePassword(String password);
}
