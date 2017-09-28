package org.prosolo.web.courses.validator;

import javax.faces.context.FacesContext;

/**
 * @author stefanvuckovic
 * @date 2017-09-27
 * @since 1.0.0
 */
public class ValidationUtil {

    public static boolean shouldValidate(FacesContext context) {
         /*
        check which button click submitted the form: if the button which id ends with DoValidate
        triggered form submission, validation should be performed
         */
        for (final String requestParameter : context.getExternalContext().getRequestParameterValuesMap().keySet()) {
            if (requestParameter.endsWith("DoValidate")) {
                return true;
            }
        }
        return false;
    }
}
