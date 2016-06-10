package org.prosolo.services.common.exception;

public class EntityAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 5449882157520806683L;

	public EntityAlreadyExistsException(){
		this("Entity already exists");
	}
	
	public EntityAlreadyExistsException (String message){
		super(message);
	}
}
