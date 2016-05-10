package org.prosolo.web.validators;

import org.prosolo.web.validator.Validator;

public class UrlValidator extends Validator {

	public UrlValidator(Validator v) {
		super(v);
	}

	@Override
	protected Object validate(Object obj, String message) throws Exception {
		org.apache.commons.validator.routines.UrlValidator urlValidator = 
				new org.apache.commons.validator.routines.UrlValidator();
		boolean valid = urlValidator.isValid(obj.toString());
		if(valid) {
			return obj;
		}
		throw new Exception(getExceptionMessage(message));
	}

	@Override
	protected String getDefaultMessage() {
		return "Url not valid";
	}

}
