package org.prosolo.core.spring.security.exceptions;

public class SessionInitializationException extends RuntimeException {

	private static final long serialVersionUID = 5027669832891900548L;

	public SessionInitializationException(){
		this("Error initializing user session data");
	}
	
	public SessionInitializationException (String message){
		super(message);
	}
}
