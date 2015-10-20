package org.prosolo.config.security.exceptions;

public class NonexistentRoleException extends RuntimeException{

	private static final long serialVersionUID = -2469299723875369649L;

	public NonexistentRoleException(){
		this("Role does not exist");
	}
	
	public NonexistentRoleException (String message){
		super(message);
	}
}
