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
 * @date 2017-09-27
 * @since 1.0.0
 */
@Component
@Scope("request")
@FacesValidator("percentValidator")
public class PercentValidator implements Validator {

    private static final int NUMBER_OF_DECIMALS_ALLOWED = 2;
    private static final String TYPE_ERROR = "Only numbers allowed";
    private static final String RANGE_ERROR = "Entered number should be between %1$s and %2$s";
    private static final String NUMBER_OF_DECIMALS_ERROR = "Number can contain at most two digits after the decimal point";

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if (ValidationUtil.shouldValidate(facesContext)) {
            String validatorMsg;
            if (NumberValidatorUtil.isDouble(o.toString())) {
                double d = Double.parseDouble(o.toString());
                double min = Double.parseDouble(uiComponent.getAttributes().get("minValue").toString());
                if (NumberValidatorUtil.isDoubleInRange(d, min, 100d)) {
                    if (NumberValidatorUtil.getNumberOfDecimals(d) <= NUMBER_OF_DECIMALS_ALLOWED) {
                        return;
                    } else {
                        validatorMsg = NUMBER_OF_DECIMALS_ERROR;
                    }
                } else {
                    validatorMsg = String.format(RANGE_ERROR, min, 100);
                }
            } else {
                validatorMsg = TYPE_ERROR;
            }

            FacesMessage msg = new FacesMessage(validatorMsg);
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
