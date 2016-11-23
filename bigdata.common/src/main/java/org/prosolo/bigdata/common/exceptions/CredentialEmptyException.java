package org.prosolo.bigdata.common.exceptions;

public class CredentialEmptyException extends RuntimeException {

	private static final long serialVersionUID = 1362414713351213998L;

	public CredentialEmptyException(){
		this("Credential must have at least one competence");
	}
	
	public CredentialEmptyException (String message){
		super(message);
	}
}
