package org.prosolo.web.courses.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@FacesValidator("urlValidator")
public class UrlValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		org.prosolo.web.validators.UrlValidator urlValidator = 
				new org.prosolo.web.validators.UrlValidator(null);
		try {
			urlValidator.performValidation(value, "URL is not valid");
		} catch(Exception e) {
			FacesMessage msg = new FacesMessage(e.getMessage());
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}

}