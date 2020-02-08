package org.prosolo.web.lti.message;

import org.prosolo.web.validator.Validator;

public class LtiMessageParameter {

	private String parameterName;
	private String parameter;
	private Validator validator;

	public LtiMessageParameter(String parameterName, Validator validator) {
		this.parameterName = parameterName;
		this.validator = validator;
	}
	
	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) throws Exception {
		if (validator != null) {
			validator.performValidation(parameter, "Required parameter (" + parameterName + ") missing or not valid");
		}
		this.parameter = parameter;
	}

}
