package org.prosolo.web.courses.validator;

import javax.faces.validator.DoubleRangeValidator;

/**
 * @author stefanvuckovic
 * @date 2017-09-27
 * @since 1.0.0
 */
public class NumberValidatorUtil {

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDoubleInRange(double d, double min, double max) {
        return d >= min && d <= max;
    }

    public static int getNumberOfDecimals(double d) {
        String str = String.valueOf(d);
        int dotInd = str.lastIndexOf(".");
        return dotInd > -1 ? str.length() - dotInd - 1 : 0;
    }
}
