package com.rupp.payment.service;

import com.rupp.payment.dto.response.ReceiptResponse;
import com.rupp.payment.entity.Receipt;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;

    public ReceiptService(ReceiptRepository receiptRepository) {
        this.receiptRepository = receiptRepository;
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptById(Long id, User currentUser) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", id));
        validateAccess(receipt, currentUser);
        return ReceiptResponse.fromEntity(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByPaymentId(Long paymentId, User currentUser) {
        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "paymentId", paymentId));
        validateAccess(receipt, currentUser);
        return ReceiptResponse.fromEntity(receipt);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByNumber(String receiptNumber, User currentUser) {
        Receipt receipt = receiptRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "receiptNumber", receiptNumber));
        validateAccess(receipt, currentUser);
        return ReceiptResponse.fromEntity(receipt);
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAllReceipts(Long studentId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        org.springframework.data.jpa.domain.Specification<Receipt> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (studentId != null) {
                predicates.add(cb.equal(root.get("payment").get("student").get("id"), studentId));
            }
            if (fromDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), fromDateTime));
            }
            if (toDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), toDateTime));
            }
            query.orderBy(cb.desc(root.get("issueDate")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return receiptRepository.findAll(spec).stream()
                .map(ReceiptResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void validateAccess(Receipt receipt, User currentUser) {
        if (Role.ROLE_STUDENT.equals(currentUser.getRole())) {
            if (receipt.getPayment() != null &&
                receipt.getPayment().getStudent() != null &&
                receipt.getPayment().getStudent().getUser() != null) {
                if (!receipt.getPayment().getStudent().getUser().getId().equals(currentUser.getId())) {
                    throw new BadRequestException("Access denied: You are only allowed to view your own receipts.");
                }
            }
        }
    }
}
