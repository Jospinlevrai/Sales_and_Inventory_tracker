package service;

import exception.BusinessRuleException;

import java.util.Locale;
import java.util.regex.Pattern;

/** Small input checks shared by the services. */
final class Text {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");
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

    static int nonNegativeInteger(int value, int max, String label) {
        if (value < 0 || value > max) {
            throw new BusinessRuleException(label + " must be between 0 and " + max);
        }
        return value;
    }

    static java.math.BigDecimal money(java.math.BigDecimal value, String label) {
        if (value == null) {
            throw new BusinessRuleException(label + " is required");
        }
        if (value.signum() < 0) {
            throw new BusinessRuleException(label + " cannot be negative");
        }
        java.math.BigDecimal rounded = value.setScale(2, java.math.RoundingMode.HALF_UP);
        if (rounded.compareTo(new java.math.BigDecimal("9999999999.99")) > 0) {
            throw new BusinessRuleException(label + " is too large");
        }
        return rounded;
    }

    static java.time.LocalDate deliveryDate(java.time.LocalDate value) {
        if (value == null) {
            throw new BusinessRuleException("The delivery date is required");
        }
        if (value.isAfter(java.time.LocalDate.now())) {
            throw new BusinessRuleException("The delivery date cannot be in the future");
        }
        return value;
    }
}
