package com.rupp.payment.security;

import com.rupp.payment.exception.InvalidIdempotencyKeyException;
import com.rupp.payment.service.IdempotencyService;
import com.rupp.payment.service.IdempotencyService.Proceed;
import com.rupp.payment.service.IdempotencyService.Replay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyInterceptorTest {

    private static final String KEY = "3f2b7c1e-5a4d-4e6f-9b8a-1c2d3e4f5a6b";

    @Mock
    private IdempotencyService idempotencyService;
    @Mock
    private CurrentUserProvider currentUser;

    private IdempotencyInterceptor interceptor;
    private HandlerMethod idempotentHandler;
    private HandlerMethod plainHandler;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        interceptor = new IdempotencyInterceptor(idempotencyService, currentUser);
        idempotentHandler = new HandlerMethod(new SampleController(), SampleController.class.getMethod("create"));
        plainHandler = new HandlerMethod(new SampleController(), SampleController.class.getMethod("list"));
    }

    @Test
    void handlersWithoutAnnotationAreIgnored() throws Exception {
        assertThat(interceptor.preHandle(new MockHttpServletRequest("POST", "/api/x"), new MockHttpServletResponse(), plainHandler)).isTrue();
        verifyNoInteractions(idempotencyService);
    }

    @Test
    void missingKeyIsRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/x");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), idempotentHandler))
                .isInstanceOf(InvalidIdempotencyKeyException.class)
                .hasMessageContaining("required");
    }

    @Test
    void nonUuidKeyIsRejected() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/x");
        request.addHeader(IdempotencyInterceptor.IDEMPOTENCY_KEY_HEADER, "not-a-uuid");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), idempotentHandler))
                .isInstanceOf(InvalidIdempotencyKeyException.class)
                .hasMessageContaining("UUID");
    }

    @Test
    void replayWritesStoredResponseAndSkipsHandler() throws Exception {
        MockHttpServletRequest request = requestWithKey("{\"a\":1}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(currentUser.getUserId()).thenReturn(7L);
        when(idempotencyService.begin(eq(7L), eq(KEY), anyString()))
                .thenReturn(new Replay(201, "application/json", "{\"paymentId\":1}"));

        assertThat(interceptor.preHandle(request, response, idempotentHandler)).isFalse();
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader(IdempotencyInterceptor.REPLAYED_HEADER)).isEqualTo("true");
        assertThat(response.getContentAsString()).isEqualTo("{\"paymentId\":1}");
    }

    @Test
    void completedHandlerResponseIsStored() throws Exception {
        MockHttpServletRequest request = requestWithKey("{\"a\":1}");
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(new MockHttpServletResponse());
        when(currentUser.getUserId()).thenReturn(7L);
        when(idempotencyService.begin(eq(7L), eq(KEY), anyString())).thenReturn(new Proceed(3L));

        assertThat(interceptor.preHandle(request, response, idempotentHandler)).isTrue();
        response.setStatus(201);
        response.setContentType("application/json");
        response.getOutputStream().write("{\"ok\":true}".getBytes(StandardCharsets.UTF_8));
        interceptor.afterCompletion(request, response, idempotentHandler, null);

        verify(idempotencyService).complete(3L, 201, "application/json", "{\"ok\":true}");
    }

    @Test
    void unhandledExceptionReleasesKey() throws Exception {
        MockHttpServletRequest request = requestWithKey("{}");
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(new MockHttpServletResponse());
        when(currentUser.getUserId()).thenReturn(7L);
        when(idempotencyService.begin(eq(7L), eq(KEY), anyString())).thenReturn(new Proceed(3L));

        interceptor.preHandle(request, response, idempotentHandler);
        interceptor.afterCompletion(request, response, idempotentHandler, new IllegalStateException("boom"));

        verify(idempotencyService).release(3L);
    }

    @Test
    void requestHashDependsOnBodyAndPath() throws Exception {
        String first = IdempotencyInterceptor.requestHash(new CachedBodyRequest(post("/api/a", "{\"amount\":100}")));
        String sameAgain = IdempotencyInterceptor.requestHash(new CachedBodyRequest(post("/api/a", "{\"amount\":100}")));
        String otherBody = IdempotencyInterceptor.requestHash(new CachedBodyRequest(post("/api/a", "{\"amount\":999}")));
        String otherPath = IdempotencyInterceptor.requestHash(new CachedBodyRequest(post("/api/b", "{\"amount\":100}")));

        assertThat(first).isEqualTo(sameAgain).hasSize(64);
        assertThat(first).isNotEqualTo(otherBody).isNotEqualTo(otherPath);
    }

    private static MockHttpServletRequest requestWithKey(String body) {
        MockHttpServletRequest request = post("/api/x", body);
        request.addHeader(IdempotencyInterceptor.IDEMPOTENCY_KEY_HEADER, KEY);
        return request;
    }

    private static MockHttpServletRequest post(String uri, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    static class SampleController {

        @Idempotent
        public void create() {
        }

        public void list() {
        }
    }
}
