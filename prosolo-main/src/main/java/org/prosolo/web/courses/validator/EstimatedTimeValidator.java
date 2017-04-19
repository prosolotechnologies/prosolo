package org.prosolo.web.courses.validator;


import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlSelectOneMenu;

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
@FacesValidator("estimatedTimeValidator")
public class EstimatedTimeValidator implements Validator {
	
	UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
	
	
	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		Integer selectedValueHours = (Integer)value;
		HtmlSelectOneMenu estimatedTimeMinutes = (HtmlSelectOneMenu) context.getViewRoot().findComponent("createActivity:formMain:estimatedTimeMinutes");
		
		String selectedMinutes = (String)estimatedTimeMinutes.getSubmittedValue();
		Integer selectedValueMinutes = Integer.parseInt(selectedMinutes);
		
		if(selectedValueHours == 0 && selectedValueMinutes == 0){
			FacesMessage msg = new FacesMessage("Estimated time needs to be set");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			context.addMessage("createActivity:formMain:estimatedTimeHours", msg);
			throw new ValidatorException(msg);
		}
	}
	

}
