package com.laundry.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+() -]{7,20}$");

    private ValidationUtil() { }

    public static List<String> customer(String name, String email, String phone, String address) {
        List<String> errors = new ArrayList<>();
        if (blank(name) || name.trim().length() < 2) errors.add("Full name must contain at least 2 characters.");
        if (blank(email) || !EMAIL.matcher(email.trim()).matches()) errors.add("Enter a valid email address.");
        if (blank(phone) || !PHONE.matcher(phone.trim()).matches()) errors.add("Enter a valid phone number.");
        if (blank(address)) errors.add("Address is required.");
        return errors;
    }

    public static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " cannot be negative.");
        }
    }

    public static void requireDateOrder(LocalDate first, LocalDate second, String message) {
        if (first != null && second != null && second.isBefore(first)) throw new IllegalArgumentException(message);
    }

    public static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
