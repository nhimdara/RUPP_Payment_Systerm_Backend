package com.rupp.payment.security;

import com.rupp.payment.exception.InvalidIdempotencyKeyException;
import com.rupp.payment.service.IdempotencyService;
import com.rupp.payment.service.IdempotencyService.Proceed;
import com.rupp.payment.service.IdempotencyService.Replay;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Enforces @Idempotent: claim key -> run handler -> store response; replays/rejects repeated keys
@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String REPLAYED_HEADER = "Idempotent-Replayed";

    private static final String RECORD_ID_ATTRIBUTE = IdempotencyInterceptor.class.getName() + ".recordId";

    private final IdempotencyService idempotencyService;
    private final CurrentUserProvider currentUser;

    public IdempotencyInterceptor(IdempotencyService idempotencyService, CurrentUserProvider currentUser) {
        this.idempotencyService = idempotencyService;
        this.currentUser = currentUser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod method) || !method.hasMethodAnnotation(Idempotent.class)) {
            return true;
        }
        String key = parseKey(request.getHeader(IDEMPOTENCY_KEY_HEADER));
        Long userId = currentUser.getUserId();

        IdempotencyService.Outcome outcome = idempotencyService.begin(userId, key, requestHash(request));
        if (outcome instanceof Replay replay) {
            writeReplay(response, replay);
            return false;
        }
        request.setAttribute(RECORD_ID_ATTRIBUTE, ((Proceed) outcome).recordId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(request.getAttribute(RECORD_ID_ATTRIBUTE) instanceof Long recordId)) {
            return;
        }
        ContentCachingResponseWrapper cached = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (ex != null || cached == null) {
            idempotencyService.release(recordId);
            return;
        }
        String contentType = cached.getContentType();
        String body = new String(cached.getContentAsByteArray(), charsetOf(contentType));
        idempotencyService.complete(recordId, cached.getStatus(), contentType, body);
    }

    static String parseKey(String header) {
        if (header == null || header.isBlank()) {
            throw InvalidIdempotencyKeyException.missing();
        }
        try {
            return UUID.fromString(header.trim()).toString();
        } catch (IllegalArgumentException e) {
            throw InvalidIdempotencyKeyException.malformed();
        }
    }

    static String requestHash(HttpServletRequest request) {
        CachedBodyRequest cached = WebUtils.getNativeRequest(request, CachedBodyRequest.class);
        byte[] body = cached == null ? new byte[0] : cached.getCachedBody();
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
        String target = request.getMethod() + " " + request.getRequestURI() + query + "\n";
        return Sha256.hex(target.getBytes(StandardCharsets.UTF_8), body);
    }

    private static void writeReplay(HttpServletResponse response, Replay replay) throws IOException {
        response.setStatus(replay.status());
        response.setHeader(REPLAYED_HEADER, "true");
        if (replay.contentType() != null) {
            response.setContentType(replay.contentType());
        }
        if (replay.body() != null) {
            response.getOutputStream().write(replay.body().getBytes(charsetOf(replay.contentType())));
        }
    }

    // JSON is written as UTF-8 even when the servlet container reports ISO-8859-1
    private static Charset charsetOf(String contentType) {
        if (contentType != null) {
            try {
                Charset declared = MediaType.parseMediaType(contentType).getCharset();
                if (declared != null) {
                    return declared;
                }
            } catch (InvalidMediaTypeException ignored) {
                // fall through to UTF-8
            }
        }
        return StandardCharsets.UTF_8;
    }
}
