package org.prosolo.web.validator;


public abstract class Validator {
    private Validator next;
 
    public Validator(Validator v){
        next=v;
    }
    
    protected abstract Object validate(Object obj, String message) throws Exception;
    protected abstract String getDefaultMessage();
    
    public Object performValidation(Object o, String message) throws Exception{
        Object obj=null;
        try{
            obj=validate(o, message);
            if(next!=null){
                obj=next.performValidation(o, message);
            }
            
            return obj;
        }catch(Exception e){
            throw e;
        }
    }
    
    protected String getExceptionMessage(String message) {
    	if(message != null){
    		return message;
    	}
		return getDefaultMessage();
	}
    
    
}
