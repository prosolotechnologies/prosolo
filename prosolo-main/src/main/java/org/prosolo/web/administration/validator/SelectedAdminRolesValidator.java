package org.prosolo.web.administration.validator;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bojan on 6/12/2017.
 */
@Component
@Scope("request")
@FacesValidator("selectedAdminRolesValidator")
public class SelectedAdminRolesValidator implements Validator {

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        List<Boolean> checkBoxComponentValues = new ArrayList<>();

        facesContext.getViewRoot().findComponent("userEdit:formMain").visitTree(VisitContext.createVisitContext(facesContext), new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext visitContext, UIComponent uiComponent) {
                if(uiComponent instanceof HtmlSelectBooleanCheckbox){
                    checkBoxComponentValues.add(((HtmlSelectBooleanCheckbox)uiComponent).isSelected());
                    return VisitResult.REJECT;
                }else {
                    return VisitResult.ACCEPT;
                }
            }
        });
        for(Boolean ob :checkBoxComponentValues){
            if(ob){
                return;
            }
        }
        FacesMessage msg = new FacesMessage("At least one role should be selected");
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        facesContext.addMessage("formMain:hiddenInput", msg);
        throw new ValidatorException(msg);

    }
}
