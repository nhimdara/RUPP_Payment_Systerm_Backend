package com.rupp.payment.service;

import com.rupp.payment.dto.response.AuditLogPageResponse;
import com.rupp.payment.dto.response.AuditLogResponse;
import com.rupp.payment.entity.AuditAction;
import com.rupp.payment.entity.PaymentAuditLog;
import com.rupp.payment.repository.PaymentAuditLogRepository;
import com.rupp.payment.security.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;

// Public audit API for all payment modules
@Service
public class PaymentAuditService {

    private static final int USER_AGENT_MAX = 512;

    private final PaymentAuditLogRepository repository;
    private final CurrentUserProvider currentUser;
    private final TransactionTemplate independentTx;
    private final Clock clock;

    public PaymentAuditService(PaymentAuditLogRepository repository, CurrentUserProvider currentUser,
                               PlatformTransactionManager transactionManager, Clock clock) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.clock = clock;
        this.independentTx = new TransactionTemplate(transactionManager);
        this.independentTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void record(AuditEvent event) {
        PaymentAuditLog entry = toEntity(event);
        if (event.action().isIndependent()) {
            independentTx.executeWithoutResult(status -> repository.save(entry));
        } else {
            repository.save(entry);
        }
    }

    public void record(AuditAction action, Long paymentId, String details) {
        record(AuditEvent.of(action).paymentId(paymentId).details(details).build());
    }

    public void recordStatusChange(Long paymentId, String fromStatus, String toStatus, String details) {
        record(AuditEvent.of(AuditAction.STATUS_CHANGED).paymentId(paymentId)
                .statusChange(fromStatus, toStatus).details(details).build());
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse search(AuditLogFilter filter, Pageable pageable) {
        Page<AuditLogResponse> page = repository.findAll(toSpecification(filter), pageable).map(AuditLogResponse::from);
        return AuditLogPageResponse.from(page);
    }

    private PaymentAuditLog toEntity(AuditEvent event) {
        Long actorUserId = event.actorUserId();
        String actorRole = event.actorRole();
        if (actorUserId == null && currentUser.isAuthenticated()) {
            actorUserId = currentUser.getUserId();
            actorRole = currentUser.getRole();
        }
        HttpServletRequest request = currentRequest();
        String ipAddress = request == null ? null : request.getRemoteAddr();
        String userAgent = request == null ? null : truncate(request.getHeader(HttpHeaders.USER_AGENT));
        return new PaymentAuditLog(event.paymentId(), event.action(), event.fromStatus(), event.toStatus(),
                actorUserId, actorRole, ipAddress, userAgent, event.details(), clock.instant());
    }

    private static Specification<PaymentAuditLog> toSpecification(AuditLogFilter filter) {
        Specification<PaymentAuditLog> spec = Specification.unrestricted();
        if (filter.paymentId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paymentId"), filter.paymentId()));
        }
        if (filter.action() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), filter.action().name()));
        }
        if (filter.actorUserId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actorUserId"), filter.actorUserId()));
        }
        if (filter.from() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.from()));
        }
        if (filter.to() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("createdAt"), filter.to()));
        }
        return spec;
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private static String truncate(String value) {
        return value == null || value.length() <= USER_AGENT_MAX ? value : value.substring(0, USER_AGENT_MAX);
    }
}
