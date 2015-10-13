package org.prosolo.services.lti.exceptions;

public class LtiToolAccessDeniedException extends RuntimeException{

	private static final long serialVersionUID = 5828931977301038253L;

	public LtiToolAccessDeniedException(){
		this("You are not allowed to access this tool");
	}
	
	public LtiToolAccessDeniedException (String message){
		super(message);
	}
}
