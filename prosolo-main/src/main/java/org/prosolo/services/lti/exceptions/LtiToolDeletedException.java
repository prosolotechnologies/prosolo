package org.prosolo.services.lti.exceptions;

public class LtiToolDeletedException extends RuntimeException{

	private static final long serialVersionUID = 4558565651099872169L;

	public LtiToolDeletedException(){
		this("This tool is deleted");
	}
	
	public LtiToolDeletedException (String message){
		super(message);
	}
}
