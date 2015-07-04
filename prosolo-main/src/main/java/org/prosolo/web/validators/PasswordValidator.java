package org.prosolo.web.validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
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
public class PasswordValidator implements Validator {
	
	@Autowired
	RegistrationManager registrationManager;

	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		// Cast the value of the entered password to String.
		String password = (String) value;

		// Obtain the component and submitted value of the confirm password
		// component.
		UIInput confirmComponent = (UIInput) component.getAttributes().get(
				"confirm");
		String confirm = (String) confirmComponent.getSubmittedValue();

		// Check if they both are filled in.
		if (password == null || password.isEmpty() || confirm == null
				|| confirm.isEmpty()) {
			return; // Let required="true" do its job.
		}

		// Compare the password with the confirm password.
		if (!password.equals(confirm)) {
			confirmComponent.setValid(false); // So that it's marked invalid.
			FacesMessage msg = new FacesMessage("Passwords do not match.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
		if (password.length() < 7) {
			confirmComponent.setValid(false); // So that it's marked invalid.
			FacesMessage msg = new FacesMessage(
					"Password is too short. It should be at least seven characters and include at least one number, one uppercase and one lowercase letter.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
		char[] aC = password.toCharArray();
		int upper = 0, lower = 0, digit = 0;
		for (char c : aC) {
			if (Character.isUpperCase(c)) {
				upper++;
			} else if (Character.isLowerCase(c)) {
				lower++;
			} else if (Character.isDigit(c)) {
				digit++;
			}
		}
		if (upper == 0 || lower == 0 || digit == 0) {
			confirmComponent.setValid(false); // So that it's marked invalid.
			FacesMessage msg = new FacesMessage(
					"Password is not strong enough. It should be at least seven characters and include at least one number, one uppercase and one lowercase letter.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}

		// You can even validate the minimum password length here and throw
		// accordingly.
		// Or, if you're smart, calculate the password strength and throw
		// accordingly ;)
	}

}