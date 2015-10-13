package org.prosolo.services.lti.exceptions;

public class LtiToolDisabledException extends RuntimeException{

	private static final long serialVersionUID = 7842580051842538781L;

	public LtiToolDisabledException(){
		this("This tool is disabled");
	}
	
	public LtiToolDisabledException (String message){
		super(message);
	}
}
