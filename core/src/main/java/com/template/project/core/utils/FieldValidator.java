package com.template.project.core.utils;

import android.util.Patterns;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to check fields (email, birthdate, etc.) is valid
 */
public class FieldValidator {

    public static final String REGEX_EMAIL = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
    public static final String REGEX_USERNAME = "^[a-zA-Z0-9][a-zA-Z0-9]*$";

    /**
     * Check if String value for name is valid.
     */
    public static boolean isNameValid(String name) {
        boolean isValid = false;
        if (name != null) {
            if(name.matches(REGEX_USERNAME)) {
                isValid = true;
            }
            if (name.length() > 0 && name.length() <= 50) {
                // \\p{L} Unicode Character Property that matches any kind of letter from any language
                String regx = "^[\\p{L} .'-]+$";
                Pattern validNamePattern = Pattern.compile(regx);
                if (validNamePattern.matcher(name).matches()) {
                    boolean isAllWhiteSpace = true;
                    for (char c : name.toCharArray()) {
                        if (!Character.isWhitespace(c)) {
                            isAllWhiteSpace = false;
                        }
                    }
                    if (!isAllWhiteSpace)
                        isValid = true;
                }
                // name should not contain numbers
                char[] nameCharArr = name.toCharArray();
                for(char x : nameCharArr) {
                    if(Character.isDigit(x)) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    /**
     * Check if email address is valid
     * MUST be 64 character max
     * @return true if email is valid, false if not.
     */
    public static boolean isEmailValid(String emailAdd) {
        boolean isValid = false;
        if(emailAdd.matches(REGEX_EMAIL)) {
            isValid = true;
        } else if(FieldValidator.isStringValid(emailAdd)) {
            boolean isValidPatterOfEmail = Patterns.EMAIL_ADDRESS.matcher(emailAdd).matches();

            // check if email has whitespace character
            boolean hasWhiteSpace = false;
            for(char c : emailAdd.toCharArray()) {
                if(Character.isWhitespace(c)) {
                    hasWhiteSpace = true;
                }
            }

            isValid = hasNameAndDomain(emailAdd) && !hasPlusOneEmailValue(emailAdd)
                    && !hasWhiteSpace && isValidPatterOfEmail;
        }
        return isValid;
    }

    /*
     * Check if email is in form of "abc@abc"
     *
     * @param email The email address string to validate
     * @return true if email address is valid, false otherwise
     */
    private static boolean hasNameAndDomain(String email) {
        String[] tokens = email.split("@");
        return tokens.length == 2 && isStringValid(tokens[0]) && isStringValid(tokens[1]);
    }

    /*
     * Check if email has "+1" String, this will spam email message if it has the value.
     */
    private static boolean hasPlusOneEmailValue(String value) {
        return value.contains("+1");
    }

    /**
     * Check if date is a valid birthday
     * MUST not be a future date and should be at least 5 yrs earlier
     */
    public static boolean isBdayValid(Calendar calBday) {
        boolean isValid = false;
        Calendar calNow = Calendar.getInstance();
	    // TODO create validation based on defined contract validation for birthday.
        return isValid;
    }

    /**
     * Check if date is a valid birthday
     * MUST not be a future date and should be at least 5 yrs earlier
     */
    public static boolean isGenderValid(String gender) {
        boolean isValid = true;
        if (gender.equalsIgnoreCase("Male") || gender.equalsIgnoreCase("Female")) {
            isValid = true;
        }
        return isValid;
    }

    /**
     * Check if desired password of user is valid
     * character is 8 minimum and 16 maximum, can have symbol but not whitespace
     */
    public static boolean isDesiredPasswdValid(String password) {
        boolean isValid = false;
	// TODO create validation based on defined contract validation for password.
        return isValid;
    }

    /**
     * Check if mobile num is a valid
     */
    public static boolean isMobileNumValid(String mobileNum) {
        boolean isValid = false;
        //Pattern patternPhone = Patterns.PHONE;
        //isValid = patternPhone.matcher(mobileNum).matches();
        if (mobileNum != null) {
            Pattern mobilePattern = Pattern.compile("^[\\+]\\d{12}$");
            Matcher mobileMatcher = mobilePattern.matcher(mobileNum);
            isValid = mobileMatcher.matches();
        }
        return isValid;
    }

    /**
     * Checks wether the string has character, not null, not equal to "", and not equal to " "(space).
     */
    public static boolean isStringValid(String strVal) {
        boolean isValid = false;
        if (strVal != null && !strVal.isEmpty() && strVal.length() > 0 && !strVal.equals("")) {
            isValid = true;
        }
        return isValid;
    }

}
