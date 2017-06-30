package org.prosolo.web.courses.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Bojan
 *
 * Apr 18, 2017
 */

@Component
@Scope("request")
@FacesValidator("difficultyLevelValidator")
public class DifficultyLevelValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer selectedValue = (Integer)value;
		
		if(selectedValue == -1){
			FacesMessage msg = new FacesMessage("Difficulty level needs to be set");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			context.addMessage("createActivity:formMain:difficultyLevelSelect", msg);
			throw new ValidatorException(msg);
		}
	}

}
