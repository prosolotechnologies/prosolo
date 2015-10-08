package org.prosolo.web.lti.validator;

public class NullValidator extends Validator{
	public NullValidator(Validator v) {
		super(v);
	}

	@Override
	public Object validate(Object obj, String message) throws Exception {
		if (obj == null) {
			throw new Exception(getExceptionMessage(message));
		} else {
			return obj;
		}

	}

	@Override
	protected String getDefaultMessage() {
		return "This field should not be null";
	}
}
