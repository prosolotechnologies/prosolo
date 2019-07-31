package org.prosolo.web.validators;

import org.apache.commons.validator.routines.EmailValidator;

import javax.faces.validator.ValidatorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author stefanvuckovic
 * @date 2017-08-09
 * @since 1.0.0
 */
public class EmailValidatorUtil {

//    private static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static boolean isEmailValid(String email) throws ValidatorException {
//        Pattern pattern = Pattern.compile(EMAIL_REGEX);
//        Matcher matcher = pattern.matcher(email);
//
//        if (matcher.matches()) {
//           return true;
//        }
//        return false;
        return EmailValidator.getInstance().isValid(email);
    }
}
