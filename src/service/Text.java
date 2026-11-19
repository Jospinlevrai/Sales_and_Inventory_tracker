package service;

import exception.BusinessRuleException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Small input checks shared by the services. */
final class Text {
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final Pattern PHONE = Pattern.compile("[0-9+() -]{7,20}");

    private Text() {
    }

    static String required(String value, int max, String label) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new BusinessRuleException(label + " is required");
        }
        if (text.length() > max) {
            throw new BusinessRuleException(label + " is too long (at most " + max + " characters)");
        }
        return text;
    }

    /** Trims the text; blank becomes null. */
    static String optional(String value, int max, String label) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.length() > max) {
            throw new BusinessRuleException(label + " is too long (at most " + max + " characters)");
        }
        return text;
    }

    static String optionalEmail(String value) {
        String text = optional(value, 100, "Email");
        if (text == null) {
            return null;
        }
        if (!EMAIL.matcher(text).matches()) {
            throw new BusinessRuleException("The email address is not valid");
        }
        return text.toLowerCase(Locale.ROOT);
    }

    static String optionalPhone(String value) {
        String text = optional(value, 20, "Phone number");
        if (text == null) {
            return null;
        }
        if (!PHONE.matcher(text).matches()) {
            throw new BusinessRuleException("The phone number is not valid");
        }

        int digitCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                digitCount++;
            }
        }
        if (digitCount < 7 || digitCount > 15) {
            throw new BusinessRuleException("The phone number must contain between 7 and 15 digits");
        }
        return text;
    }
}
