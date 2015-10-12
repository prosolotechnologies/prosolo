package org.prosolo.web.lti.message;

import org.prosolo.web.lti.validator.Validator;

public class LtiMessageParameter {

	private String parameter;
	private Validator validator;

	public LtiMessageParameter(Validator validator){
		this.validator = validator;
	}
	
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) throws Exception {
		if(validator != null){
			validator.performValidation(parameter, "Required parameter missing or not valid");
		}
		this.parameter = parameter;
	}

}
