package org.prosolo.services.oauth.exceptions;

public class OauthException extends RuntimeException{

	private static final long serialVersionUID = -2469299723875369649L;

	public OauthException(){
		this("Request not valid");
	}
	
	public OauthException (String message){
		super(message);
	}
}
