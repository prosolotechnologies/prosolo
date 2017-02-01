package org.prosolo.bigdata.common.exceptions;

public class AccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -1559240390605506572L;

	public AccessDeniedException(){
		this("You are not allowed to access this resource");
	}
	
	public AccessDeniedException (String message){
		super(message);
	}
}
