package org.prosolo.bigdata.common.exceptions;

public class OperationForbiddenException extends Exception {

	private static final long serialVersionUID = 7083731827347156284L;

	public OperationForbiddenException(){
		this("Operation forbidden");
	}

	public OperationForbiddenException(String message){
		super(message);
	}
}
