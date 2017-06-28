package org.prosolo.web.administration.validator;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * @author Bojan
 * @date 2017-06-27
 * @since 0.7
 */
@Component
@Scope("request")
@FacesValidator("selectUserForOrganizationValidator")
public class SelectUserForOrganizationValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        final Boolean[] rendered = {new Boolean(false)};

        if (context.getViewRoot().findComponent("formMain:selectedOwner").isRendered()) {
            return;
        }

        FacesMessage msg = new FacesMessage("At least one user should be selected");
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        context.addMessage("formMain:selectNewOwner:hiddenInput", msg);
        throw new ValidatorException(msg);

    }
}
