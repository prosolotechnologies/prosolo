package org.prosolo.web.lti.validator;

import org.prosolo.web.validator.Validator;

public class EmptyValidator extends Validator {

    public EmptyValidator(Validator v){
        super(v);
    }
    @Override
    public Object validate(Object obj, String message) throws Exception {
            if(obj.toString().isEmpty()){
                throw new Exception(getExceptionMessage(message));
            }else{
                return obj;
            }
                
           
        
    }
	@Override
	protected String getDefaultMessage() {
		return "This field should not be empty";
	}
    
}
