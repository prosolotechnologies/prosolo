package org.prosolo.bigdata.common.exceptions;

public class DbConnectionException extends RuntimeException{

	private static final long serialVersionUID = -46055949522760294L;
	
	public DbConnectionException(){
		this("Database connection error");
	}
	
	public DbConnectionException (String message){
		super(message);
	}
}
