package org.prosolo.web.courses.validator;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.log4j.Logger;
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

/**
 * @author stefanvuckovic
 * @date 2017-09-26
 * @since 1.0.0
 */
@Component
@Scope("request")
@FacesValidator("percentSumValidator")
public class PercentSumValidator implements Validator {

    private static Logger logger = Logger.getLogger(PercentSumValidator.class);

    private static final String DEFAULT_MSG = "Sum of percentages must be 100";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if (ValidationUtil.shouldValidate(facesContext)) {
            AtomicDouble sum = new AtomicDouble();
            uiComponent.getParent().visitTree(VisitContext.createVisitContext(facesContext),
                    (VisitContext visitContext, UIComponent uiComponent1) -> {
                        if (uiComponent1.getPassThroughAttributes().get("data-percent") != null) {
                            double percent = 0D;
                            HtmlInputText input = (HtmlInputText) uiComponent1;
                            if (input.isValid()) {
                                percent = Double.parseDouble(input.getValue().toString());
                            }
                            sum.getAndAdd(percent);
                            return VisitResult.REJECT;
                        } else {
                            return VisitResult.ACCEPT;
                        }
                    });

            /*
            equality check for doubles would not always work because of the way doubles are stored.
            Instead, error tolerance is used and it is 0.001 because user is allowed to enter two decimals
            so if he makes a mistake it will be by at least 0.01 but our tolerance is one order smaller
            because points sum being a double is approximated so in some cases tolerance of 0.01 would lead
            to errors
             */
            double tolerance = 0.001;
            if (Math.abs(sum.get() - 100) > tolerance) {
                String message = uiComponent.getAttributes().get("msg").toString();
                if (message == null || message.isEmpty()) {
                    message = DEFAULT_MSG;
                }
                FacesMessage msg = new FacesMessage(message);
                msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(msg);
            }
        }
    }
}
