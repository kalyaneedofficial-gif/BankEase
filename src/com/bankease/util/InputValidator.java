package com.bankease.util;

import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,49}$");
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^[6-9]\\d{9}$");

    private InputValidator() { }

    public static boolean isValidName(String s) {
        return s != null && NAME.matcher(s.trim()).matches();
    }

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    public static boolean isValidPhone(String s) {
        return s != null && PHONE.matcher(s.trim()).matches();
    }

    public static boolean isValidAccountType(String s) {
        return "SAVINGS".equalsIgnoreCase(s) || "CURRENT".equalsIgnoreCase(s);
    }

    public static boolean isPositiveAmount(double amount) {
        return amount > 0 && !Double.isNaN(amount) && !Double.isInfinite(amount);
    }

    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Please enter a valid whole number.");
        }
    }

    public static double parseAmount(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Please enter a valid amount.");
        }
    }
}
