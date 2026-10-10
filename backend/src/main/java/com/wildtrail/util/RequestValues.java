package com.wildtrail.util;

import com.wildtrail.exception.ApiException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class RequestValues {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private RequestValues() {
    }

    public static String password(Map<String, Object> body) {
        Object value = body.get("password");
        if (!(value instanceof String password) || password.isEmpty() || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw ApiException.badRequest("Password must contain 1 to 72 UTF-8 bytes");
        }
        return password;
    }

    public static String text(Map<String, Object> body, String key, int max) {
        String value = RequestValues.optionalText(body, key);
        if (value.isBlank()) {
            throw ApiException.badRequest(RequestValues.label(key) + " is required");
        }
        if (value.length() > max) {
            throw ApiException.badRequest(RequestValues.label(key) + " is too long");
        }
        return value;
    }

    public static String optionalText(Map<String, Object> body, String key) {
        Object raw = body.get(key);
        return raw == null ? "" : raw.toString().trim();
    }

    public static String email(Map<String, Object> body) {
        String value = RequestValues.text(body, "email", 160).toLowerCase();
        if (!EMAIL.matcher(value).matches()) {
            throw ApiException.badRequest("Email address is invalid");
        }
        return value;
    }

    public static int integer(Map<String, Object> body, String key, int min, int max) {
        double value = RequestValues.number(body, key);
        if (value != Math.rint(value)) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be a whole number");
        }
        if (value < min || value > max) throw ApiException.badRequest(RequestValues.label(key) + " is out of range");
        int result = (int)value;
        if (result < min) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be at least " + min);
        }
        if (result > max) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be at most " + max);
        }
        return result;
    }

    public static double number(Map<String, Object> body, String key) {
        Object raw = body.get(key);
        if (raw == null || raw.toString().isBlank()) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be a number");
        }
        try {
            double value = Double.parseDouble(raw.toString());
            if (!Double.isFinite(value)) {
                throw new NumberFormatException();
            }
            return value;
        }
        catch (NumberFormatException exception) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be a number");
        }
    }

    public static double nonNegative(Map<String, Object> body, String key) {
        double value = RequestValues.number(body, key);
        if (value < 0.0) {
            throw ApiException.badRequest(RequestValues.label(key) + " must be at least 0");
        }
        return value;
    }

    public static boolean bool(Map<String, Object> body, String key, boolean fallback) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return value.equals(true) || value.equals("true") || value.equals("on") || value.equals(1) || value.equals("1");
    }

    public static List<String> stringList(Map<String, Object> body, String key) {
        Object raw = body.get(key);
        if (raw instanceof List) {
            List<?> list = (List<?>)raw;
            return list.stream().map(Object::toString).map(String::trim).filter(s -> !s.isBlank()).toList();
        }
        return RequestValues.optionalText(body, key).lines().flatMap(line -> Arrays.stream(line.split(","))).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private static String label(String key) {
        String spaced = key.replaceAll("([A-Z])", " $1");
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
