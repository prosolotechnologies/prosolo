package org.prosolo.services.openid.exception;

public class OpenIdException extends Exception {

	private static final long serialVersionUID = 5390995183455828563L;

	public OpenIdException() {
		this("OpenId error");
	}
	
	public OpenIdException (String message){
		super(message);
	}
}
