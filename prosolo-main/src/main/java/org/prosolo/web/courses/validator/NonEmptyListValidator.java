package org.prosolo.web.courses.validator;

import org.apache.myfaces.view.facelets.component.UIRepeat;
import org.prosolo.common.util.map.CountByKeyMap;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author stefanvuckovic
 * @date 2017-09-26
 * @since 1.0.0
 */
@Component
@Scope("request")
@FacesValidator("nonEmptyListValidator")
public class NonEmptyListValidator implements Validator {

    private static final String DEFAULT_MSG = "List should not be empty";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if (ValidationUtil.shouldValidate(facesContext)) {
            AtomicBoolean valid = new AtomicBoolean(false);
            uiComponent.getParent().visitTree(VisitContext.createVisitContext(facesContext),
                    (VisitContext visitContext, UIComponent uiComponent1) -> {
                        if (uiComponent1 instanceof UIRepeat) {
                            valid.set(((UIRepeat) uiComponent1).getRowCount() > 0);
                            return VisitResult.COMPLETE;
                        } else {
                            return VisitResult.ACCEPT;
                        }
                    });

            if (!valid.get()) {
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
}
