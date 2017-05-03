package org.prosolo.web.courses.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.prosolo.web.util.HTMLUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @author Bojan
 *
 *         Apr 13, 2017
 */

@Component
@Scope("request")
@FacesValidator("textInputValidator")
public class TextInputValidator implements Validator {

	@SuppressWarnings("static-access")
	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		
		HTMLUtil htmlUtil = new HTMLUtil();
		String inputText = (String)value;
		String inputTextForValidation = null;
		FacesMessage msg = new FacesMessage("Text is required");
		
		if(inputText == null || inputText.trim().isEmpty()){
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}else{
			inputTextForValidation = htmlUtil.cleanHTMLTags(inputText);
			inputTextForValidation = inputTextForValidation.replaceAll("[\u00A0|\\s+]", "").trim();
		}
		if(inputTextForValidation.isEmpty()){
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}

}
