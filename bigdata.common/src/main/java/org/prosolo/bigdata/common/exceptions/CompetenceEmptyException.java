package org.prosolo.bigdata.common.exceptions;

public class CompetenceEmptyException extends RuntimeException {

	private static final long serialVersionUID = 5449882157520806683L;

	public CompetenceEmptyException(){
		this("Competence must have at least one activity");
	}
	
	public CompetenceEmptyException (String message){
		super(message);
	}
}
