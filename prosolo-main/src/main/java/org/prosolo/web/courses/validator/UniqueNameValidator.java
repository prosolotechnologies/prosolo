package org.prosolo.web.courses.validator;

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
@FacesValidator("uniqueNameValidator")
public class UniqueNameValidator implements Validator {

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if (ValidationUtil.shouldValidate(facesContext)) {
            CountByKeyMap<String> names = new CountByKeyMap<>();
            List<Object[]> inputs = new ArrayList<>();
            uiComponent.getParent().visitTree(VisitContext.createVisitContext(facesContext),
                    (VisitContext visitContext, UIComponent uiComponent1) -> {
                        if (uiComponent1 instanceof HtmlInputText
                                && uiComponent1.getPassThroughAttributes().get("data-unique-name") != null) {
                            Object val = ((HtmlInputText) uiComponent1).getSubmittedValue();
                            //if value is null or empty let required validator validate this input
                            if (val != null && !val.toString().trim().isEmpty()) {
                                names.put(val.toString().trim());
                                Object[] input = new Object[3];
                                input[0] = uiComponent1;
                                input[1] = val.toString().trim();
                                input[2] = uiComponent1.getClientId();
                                inputs.add(input);
                            }
                            return VisitResult.REJECT;
                        } else {
                            return VisitResult.ACCEPT;
                        }
                    });

            AtomicBoolean valid = new AtomicBoolean(true);
            List<FacesMessage> msgs = new ArrayList<>();
            for (Object[] inputObj : inputs) {
                HtmlInputText input = (HtmlInputText) inputObj[0];
                if (names.get(inputObj[1]) > 1) {
                    input.setValid(false);
                    valid.set(false);
                    FacesMessage msg = new FacesMessage("Name must be unique");
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    facesContext.addMessage(inputObj[2].toString(), msg);
                    msgs.add(msg);
                }
            }
            if (!valid.get()) {
                facesContext.validationFailed();
                FacesMessage msg = new FacesMessage("");
                throw new ValidatorException(msgs);
            }
        }
    }
}
