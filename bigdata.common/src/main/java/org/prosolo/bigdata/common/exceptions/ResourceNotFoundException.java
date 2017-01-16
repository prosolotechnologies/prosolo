package org.prosolo.bigdata.common.exceptions;

public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 8309096290309714672L;

	public ResourceNotFoundException(){
		this("Resource not found");
	}
	
	public ResourceNotFoundException (String message){
		super(message);
	}
}
