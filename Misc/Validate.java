package icn.icmyas.Misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static icn.icmyas.Misc.Constants.EMAIL;
import static icn.icmyas.Misc.Constants.NAME;
import static icn.icmyas.Misc.Constants.PASSWORD;
import static icn.icmyas.Misc.Constants.USERNAME;
import static icn.icmyas.Misc.Constants.USERNAME_INPUT;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas.Misc
 * Project Name: ICMYAS
 */

public class Validate {
    public static boolean isValid(String type, String inputToValidate) {
        String PATTERN = "";
        Pattern pattern;
        switch (type) {
            case NAME:
                PATTERN = "^[\\p{L} .'-]+$";
                pattern = Pattern.compile(PATTERN);
                break;
            case USERNAME_INPUT:
                PATTERN = "^(?!.*[-_]{2,})(?=^[^-_].*[^-_]$)[\\w\\s-]{3,9}$";
                pattern = Pattern.compile(PATTERN);
                break;
            case USERNAME:
                pattern = android.util.Patterns.EMAIL_ADDRESS;
                break;
            case EMAIL:
                PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
                pattern = Pattern.compile(PATTERN);
                break;
            case PASSWORD:
                PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{6,45}$";
                pattern = Pattern.compile(PATTERN);
                break;
            default:
                PATTERN = "";
                pattern = Pattern.compile(PATTERN);
                break;
        }
        Matcher matcher = pattern.matcher(inputToValidate);
        return matcher.matches();
    }
}