package org.prosolo.web.courses.validator;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * @author stefanvuckovic
 * @date 2017-09-26
 * @since 1.0.0
 */
@Component
@Scope("request")
@FacesValidator("greaterThanZeroValidator")
public class GreaterThanZeroValidator implements Validator {

    private static Logger logger = Logger.getLogger(GreaterThanZeroValidator.class);

    private static final String DEFAULT_MSG = "One option must be selected";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        long val = (Long) o;
        if (val <= 0) {
            Object message = uiComponent.getAttributes().get("msg");
            if (message == null || message.toString().isEmpty()) {
                message = DEFAULT_MSG;
            }
            FacesMessage msg = new FacesMessage(message.toString());
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
