package org.prosolo.bigdata.common.exceptions;

public class StaleDataException extends RuntimeException {

	private static final long serialVersionUID = 1969008486179946148L;

	public StaleDataException(){
		this("Stale data");
	}
	
	public StaleDataException (String message){
		super(message);
	}
}
