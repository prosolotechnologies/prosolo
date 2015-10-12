package org.prosolo.web.lti.validator;


public class LongValidator extends Validator{
    public LongValidator(Validator v){
        super(v);
    }
    @Override
    protected Object validate(Object obj, String message) throws Exception {
        String s=obj.toString();
        try{
        	Long.parseLong(s);
        }catch(Exception e){
        	throw new Exception(getExceptionMessage(message));
        }
        
        return obj;
    }
	@Override
	protected String getDefaultMessage() {
		return "This field should contain only numbers";
	}
	
	
}
