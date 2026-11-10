package com.teamnative.moss.global.logging;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamnative.moss.global.logging.AppLogDto.ErrorLogData;
import com.teamnative.moss.global.logging.AppLogDto.HttpLogData;
import com.teamnative.moss.global.logging.AppLogDto.PayloadLogData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {
    public static final String TRACE_ID_ATTRIBUTE = "backend.traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final int MAX_BODY_LENGTH = 2_000;
    private static final String REDACTED_BODY_VALUE = "[REDACTED_BODY_CONTAINS_SENSITIVE_FIELD]";
    private static final String TRUNCATED_BODY_VALUE = "[JSON_BODY_TRUNCATED]";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final List<String> LOGGABLE_HEADERS = List.of(
            "Content-Type",
            "Content-Length",
            "Accept",
            TRACE_ID_HEADER
    );

    private final AppLogger appLogger;
    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(AppLogger appLogger, ObjectMapper objectMapper) {
        this.appLogger = appLogger;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = traceId(request);
        LimitedRequestWrapper wrappedRequest = new LimitedRequestWrapper(request, MAX_BODY_LENGTH);
        JsonResponseCaptureWrapper wrappedResponse = new JsonResponseCaptureWrapper(response, MAX_BODY_LENGTH);
        long startedAt = System.currentTimeMillis();
        wrappedRequest.setAttribute(TRACE_ID_ATTRIBUTE, traceId);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (IOException | ServletException | RuntimeException exception) {
            logError(traceId, wrappedRequest, startedAt, exception);
            throw exception;
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            wrappedResponse.setHeader(TRACE_ID_HEADER, traceId);
            logRequest(traceId, wrappedRequest);
            logResponse(traceId, wrappedRequest, wrappedResponse, durationMs);
        }
    }

    private void logRequest(String traceId, LimitedRequestWrapper request) {
        appLogger.info(new AppLogDto(
                LogLevel.INFO,
                LogEvent.EXTERNAL_REQUEST,
                "Incoming HTTP request.",
                traceId,
                null,
                new HttpLogData(
                        request.getMethod(),
                        request.getRequestURI(),
                        sanitizeQuery(request.getQueryString()),
                        null,
                        null,
                        clientIp(request),
                        new PayloadLogData(
                                loggableHeaders(Collections.list(request.getHeaderNames()).stream()
                                        .collect(Collectors.toMap(
                                                header -> header,
                                                request::getHeader,
                                                (left, right) -> right,
                                                LinkedHashMap::new
                                        ))),
                                parseBody(request.getContentAsByteArray(), request.getCharacterEncoding(),
                                        request.getContentType(), request.isContentOverflowed())
                        ),
                        null
                ),
                null
        ));
    }

    private void logResponse(String traceId, HttpServletRequest request,
                             JsonResponseCaptureWrapper response, long durationMs) {
        int status = response.getStatus();
        AppLogDto log = new AppLogDto(
                status >= 500 ? LogLevel.ERROR : LogLevel.INFO,
                LogEvent.RESPONSE_RETURNED,
                "HTTP response completed.",
                traceId,
                null,
                new HttpLogData(
                        request.getMethod(),
                        request.getRequestURI(),
                        sanitizeQuery(request.getQueryString()),
                        status,
                        durationMs,
                        clientIp(request),
                        null,
                        new PayloadLogData(
                                loggableHeaders(response.getHeaderNames().stream()
                                        .collect(Collectors.toMap(
                                                header -> header,
                                                header -> response.getHeader(header) == null ? "" : response.getHeader(header),
                                                (left, right) -> right,
                                                LinkedHashMap::new
                                        ))),
                                parseBody(response.capturedJsonBody(), response.getCharacterEncoding(),
                                        response.getContentType(), response.isTruncated())
                        )
                ),
                null
        );

        if (status >= 500) {
            appLogger.error(log);
        } else {
            appLogger.info(log);
        }
    }

    private void logError(String traceId, HttpServletRequest request, long startedAt, Exception exception) {
        appLogger.error(new AppLogDto(
                LogLevel.ERROR,
                LogEvent.ERROR_OCCURRED,
                "Unhandled HTTP request error.",
                traceId,
                null,
                new HttpLogData(
                        request.getMethod(),
                        request.getRequestURI(),
                        sanitizeQuery(request.getQueryString()),
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        System.currentTimeMillis() - startedAt,
                        clientIp(request),
                        null,
                        null
                ),
                new ErrorLogData(
                        exception.getClass().getName(),
                        "Request processing failed.",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                )
        ));
    }

    private String traceId(HttpServletRequest request) {
        String headerTraceId = request.getHeader(TRACE_ID_HEADER);
        if (headerTraceId != null && TRACE_ID_PATTERN.matcher(headerTraceId).matches()) {
            return headerTraceId.toLowerCase(Locale.ROOT);
        }
        return UUID.randomUUID().toString();
    }

    private Map<String, String> loggableHeaders(Map<String, String> headers) {
        Map<String, String> loggable = new LinkedHashMap<>();
        headers.forEach((key, value) -> {
            boolean allowed = LOGGABLE_HEADERS.stream().anyMatch(header -> header.equalsIgnoreCase(key));
            if (allowed) {
                loggable.put(key, LogSanitizer.isSensitive(key) ? LogSanitizer.MASKED_VALUE : value);
            }
        });
        return loggable;
    }

    private Object parseBody(byte[] bytes, String encoding, String contentType, boolean truncated) {
        if (!isJsonContentType(contentType) || bytes == null || bytes.length == 0) {
            return null;
        }
        if (truncated) {
            return TRUNCATED_BODY_VALUE;
        }

        String body = new String(bytes, charset(encoding));
        try {
            return sanitizeBody(objectMapper.readValue(body, Object.class));
        } catch (JacksonException ignored) {
            return sanitizeRawBody(body);
        }
    }

    private boolean isJsonContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return mediaType.equals("application/json") || mediaType.endsWith("+json");
    }

    private Object sanitizeBody(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> {
                String keyText = key == null ? "" : key.toString();
                sanitized.put(keyText, LogSanitizer.isSensitive(keyText)
                        ? LogSanitizer.MASKED_VALUE
                        : sanitizeBody(entryValue));
            });
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::sanitizeBody).toList();
        }
        return value;
    }

    private String sanitizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return query;
        }
        return List.of(query.split("&")).stream()
                .map(this::sanitizeQueryPart)
                .collect(Collectors.joining("&"));
    }

    private String sanitizeQueryPart(String part) {
        String key = part.contains("=") ? part.substring(0, part.indexOf("=")) : part;
        String decodedKey = decodeUrlValue(key);
        return LogSanitizer.isSensitive(decodedKey) ? key + "=" + LogSanitizer.MASKED_VALUE : part;
    }

    private String sanitizeRawBody(String body) {
        return LogSanitizer.SENSITIVE_KEYS.stream()
                .anyMatch(key -> body.toLowerCase(Locale.ROOT).contains(key.toLowerCase(Locale.ROOT)))
                ? REDACTED_BODY_VALUE
                : body;
    }

    private String decodeUrlValue(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return value;
        }
    }

    private Charset charset(String encoding) {
        try {
            return Charset.forName(encoding == null ? StandardCharsets.UTF_8.name() : encoding);
        } catch (Exception ignored) {
            return StandardCharsets.UTF_8;
        }
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private static final class LimitedRequestWrapper extends ContentCachingRequestWrapper {
        private boolean contentOverflowed;

        private LimitedRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
            super(request, contentCacheLimit);
        }

        @Override
        protected void handleContentOverflow(int contentCacheLimit) {
            contentOverflowed = true;
        }

        private boolean isContentOverflowed() {
            return contentOverflowed;
        }
    }
}
