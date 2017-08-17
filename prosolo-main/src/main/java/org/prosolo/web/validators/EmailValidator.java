package org.prosolo.web.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.prosolo.services.authentication.RegistrationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Zoran Jeremic 2013-10-25
 * 
 */
@Component
@Scope("request")
@FacesValidator("emailValidator")
public class EmailValidator implements Validator {
	
	@Autowired
	RegistrationManager registrationManager;

	public EmailValidator() { }

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		if (!EmailValidatorUtil.isValid(value.toString())) {
			FacesMessage msg = new FacesMessage("Invalid email format.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
}