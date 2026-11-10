package com.teamnative.moss.global.logging;

import java.util.List;
import java.util.Locale;

public final class LogSanitizer {

    public static final String MASKED_VALUE = "***";
    public static final List<String> SENSITIVE_KEYS = List.of(
            "authorization",
            "password",
            "pwd",
            "code",
            "email",
            "name",
            "phone",
            "mobile",
            "telephone",
            "tel",
            "number",
            "token",
            "secret",
            "cookie"
    );

    private LogSanitizer() {
    }

    public static boolean isSensitive(String key) {
        if (key == null) {
            return false;
        }

        String lowerKey = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.stream().anyMatch(lowerKey::contains);
    }
}
