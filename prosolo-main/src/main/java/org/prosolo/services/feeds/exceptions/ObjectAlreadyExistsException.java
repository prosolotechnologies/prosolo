package org.prosolo.services.feeds.exceptions;

public class ObjectAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = -4615914614006079482L;

	public ObjectAlreadyExistsException(){
		this("Object alreadyn exists in database");
	}
	
	public ObjectAlreadyExistsException (String message){
		super(message);
	}
}
