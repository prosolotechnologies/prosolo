package org.prosolo.web.validators;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * @author Nikola Milikic
 * @date 2019-04-11
 * @since 1.3.2
 */
@Component
@Scope("request")
@FacesValidator("customProfileUrlValidator")
public class CustomProfileUrlValidator implements Validator {

	public CustomProfileUrlValidator() { }

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		String customProfileURL = value.toString();

		if (customProfileURL.length() < 5 || customProfileURL.length() > 50) {
			FacesMessage msg = new FacesMessage("Custom URL must contain 5 and 50 characters.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}

		if (!customProfileURL.matches("^[a-zA-Z0-9-]+$")) {
			FacesMessage msg = new FacesMessage("Only letters and numbers are supported");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}