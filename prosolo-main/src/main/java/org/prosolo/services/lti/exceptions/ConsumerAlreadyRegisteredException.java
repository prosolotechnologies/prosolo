package org.prosolo.services.lti.exceptions;

public class ConsumerAlreadyRegisteredException extends RuntimeException{

	private static final long serialVersionUID = -2469299723875369649L;

	public ConsumerAlreadyRegisteredException(){
		this("LTI Consumer already registered");
	}
	
	public ConsumerAlreadyRegisteredException (String message){
		super(message);
	}
}
