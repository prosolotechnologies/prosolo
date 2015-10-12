package org.prosolo.web.lti.validator;

public class EqualValuesValidator extends Validator{
	
	private String compareTo="";
	
	public EqualValuesValidator(Validator v){
	    super(v);
	}
	
	public EqualValuesValidator(Validator v, String compareTo){
		super(v);
		this.compareTo = compareTo;
	}

	@Override
	public Object validate(Object obj, String message) throws Exception {
		if(!compareTo.equals(obj.toString())){
			throw new Exception(getExceptionMessage(message));
		}
		return obj;

	}
	
	public void setCompareTo(String compareTo){
		this.compareTo = compareTo;
	}

	@Override
	protected String getDefaultMessage() {
		return "Values are not equal";
	}
}
