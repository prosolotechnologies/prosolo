package org.prosolo.bigdata.common.exceptions;

public class IllegalDataStateException extends Exception {

	private static final long serialVersionUID = -581926045749453346L;

	public IllegalDataStateException(){
		this("Illegal data state");
	}
	
	public IllegalDataStateException (String message){
		super(message);
	}
}
