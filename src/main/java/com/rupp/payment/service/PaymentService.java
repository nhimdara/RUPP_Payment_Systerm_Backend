package com.rupp.payment.service;

<<<<<<< HEAD
import com.rupp.payment.dto.request.MakePaymentRequest;
import com.rupp.payment.dto.request.VerifyPaymentRequest;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.PaymentStatusResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ConflictException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
=======
import com.rupp.payment.dto.request.CreatePaymentRequest;
import com.rupp.payment.dto.request.PaymentSearchCriteria;
import com.rupp.payment.dto.request.UpdatePaymentStatusRequest;
import com.rupp.payment.dto.response.PagedResponse;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ReceiptResponse;
import com.rupp.payment.entity.*;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.*;
import com.rupp.payment.specification.PaymentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

>>>>>>> origin/feature/search
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
<<<<<<< HEAD
    private final PaymentStatusRepository paymentStatusRepository;
    private final TuitionFeeStructureRepository tuitionFeeRepository;
    private final StudentRepository studentRepository;
    private final ReceiptRepository receiptRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentDetailRepository paymentDetailRepository,
                          PaymentStatusRepository paymentStatusRepository,
                          TuitionFeeStructureRepository tuitionFeeRepository,
                          StudentRepository studentRepository,
                          ReceiptRepository receiptRepository) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.tuitionFeeRepository = tuitionFeeRepository;
        this.studentRepository = studentRepository;
        this.receiptRepository = receiptRepository;
    }

    @Transactional
    public PaymentResponse makePayment(User currentUser, MakePaymentRequest request) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for current user: " + currentUser.getUsername()));

        TuitionFeeStructure feeStructure = tuitionFeeRepository.findById(request.getFeeStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("TuitionFeeStructure", "id", request.getFeeStructureId()));

        if (paymentDetailRepository.existsByTransactionRef(request.getTransactionRef().trim())) {
            throw new ConflictException("Transaction reference '" + request.getTransactionRef().trim() + "' has already been submitted.");
        }

        PaymentStatus pendingStatus = paymentStatusRepository.findByStatusName(PaymentStatus.PENDING)
                .orElseGet(() -> paymentStatusRepository.save(new PaymentStatus(PaymentStatus.PENDING)));

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setFeeStructure(feeStructure);
        payment.setPaymentStatus(pendingStatus);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTotalPaid(request.getAmount());

        Payment savedPayment = paymentRepository.save(payment);

        PaymentDetail detail = new PaymentDetail();
        detail.setPayment(savedPayment);
        detail.setMethod(request.getMethod().trim());
        detail.setTransactionRef(request.getTransactionRef().trim());
        detail.setAmount(request.getAmount());

        paymentDetailRepository.save(detail);
        savedPayment.getPaymentDetails().add(detail);

        return PaymentResponse.fromEntity(savedPayment);
    }

    @Transactional
    public PaymentResponse verifyPayment(Long paymentId, VerifyPaymentRequest request, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        PaymentStatus verifiedStatus = paymentStatusRepository.findByStatusName(PaymentStatus.VERIFIED)
                .orElseGet(() -> paymentStatusRepository.save(new PaymentStatus(PaymentStatus.VERIFIED)));

        payment.setPaymentStatus(verifiedStatus);
        Payment updated = paymentRepository.save(payment);

        Long receiptId = receiptRepository.findByPaymentId(payment.getId()).map(Receipt::getId).orElse(null);
        return PaymentResponse.fromEntity(updated, receiptId);
    }

    @Transactional
    public PaymentResponse confirmPayment(Long paymentId, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (PaymentStatus.CONFIRMED.equalsIgnoreCase(payment.getPaymentStatus().getStatusName())) {
            throw new BadRequestException("Payment ID " + paymentId + " is already confirmed.");
        }

        PaymentStatus confirmedStatus = paymentStatusRepository.findByStatusName(PaymentStatus.CONFIRMED)
                .orElseGet(() -> paymentStatusRepository.save(new PaymentStatus(PaymentStatus.CONFIRMED)));

        payment.setPaymentStatus(confirmedStatus);
        Payment updated = paymentRepository.save(payment);

        Receipt receipt = receiptRepository.findByPaymentId(payment.getId()).orElse(null);
        if (receipt == null) {
            String receiptNumber = "REC-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now())
                    + "-" + String.format("%04d", payment.getId());
            receipt = new Receipt(payment, receiptNumber, LocalDateTime.now(), payment.getTotalPaid());
            receipt = receiptRepository.save(receipt);
        }

        return PaymentResponse.fromEntity(updated, receipt.getId());
    }

    @Transactional
    public PaymentResponse rejectPayment(Long paymentId, String reason, User currentUser) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        PaymentStatus rejectedStatus = paymentStatusRepository.findByStatusName(PaymentStatus.REJECTED)
                .orElseGet(() -> paymentStatusRepository.save(new PaymentStatus(PaymentStatus.REJECTED)));

        payment.setPaymentStatus(rejectedStatus);
        Payment updated = paymentRepository.save(payment);

        return PaymentResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(User currentUser) {
        List<Payment> payments = paymentRepository.findByStudentUserIdOrderByPaymentDateDesc(currentUser.getId());
        return payments.stream()
                .map(p -> {
                    Long receiptId = receiptRepository.findByPaymentId(p.getId()).map(Receipt::getId).orElse(null);
                    return PaymentResponse.fromEntity(p, receiptId);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id, User currentUser) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        if (Role.ROLE_STUDENT.equals(currentUser.getRole())) {
            if (!payment.getStudent().getUser().getId().equals(currentUser.getId())) {
                throw new BadRequestException("Access denied: You can only view your own payment details.");
            }
        }

        Long receiptId = receiptRepository.findByPaymentId(payment.getId()).map(Receipt::getId).orElse(null);
        return PaymentResponse.fromEntity(payment, receiptId);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(Long studentId, Long statusId, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : null;

        org.springframework.data.jpa.domain.Specification<Payment> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (studentId != null) {
                predicates.add(cb.equal(root.get("student").get("id"), studentId));
            }
            if (statusId != null) {
                predicates.add(cb.equal(root.get("paymentStatus").get("id"), statusId));
            }
            if (fromDateTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), fromDateTime));
            }
            if (toDateTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), toDateTime));
            }
            query.orderBy(cb.desc(root.get("paymentDate")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<Payment> payments = paymentRepository.findAll(spec);
        return payments.stream()
                .map(p -> {
                    Long receiptId = receiptRepository.findByPaymentId(p.getId()).map(Receipt::getId).orElse(null);
                    return PaymentResponse.fromEntity(p, receiptId);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentStatusResponse> getAllPaymentStatuses() {
        return paymentStatusRepository.findAll().stream()
                .map(PaymentStatusResponse::fromEntity)
                .collect(Collectors.toList());
=======
    private final ReceiptRepository receiptRepository;
    private final StudentRepository studentRepository;
    private final TuitionFeeStructureRepository feeStructureRepository;
    private final PaymentStateMachine paymentStateMachine;

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentDetailRepository paymentDetailRepository,
                          ReceiptRepository receiptRepository,
                          StudentRepository studentRepository,
                          TuitionFeeStructureRepository feeStructureRepository,
                          PaymentStateMachine paymentStateMachine) {
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.receiptRepository = receiptRepository;
        this.studentRepository = studentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.paymentStateMachine = paymentStateMachine;
    }

    @Transactional
    public PaymentResponse createPayment(User currentUser, CreatePaymentRequest request) {
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUser.getUsername()));

        TuitionFeeStructure feeStructure = feeStructureRepository.findById(request.getFeeStructureId())
                .orElseThrow(() -> new ResourceNotFoundException("Tuition fee structure not found with id: " + request.getFeeStructureId()));

        if (paymentDetailRepository.existsByTransactionRef(request.getTransactionRef().trim())) {
            throw new BadRequestException("Transaction reference '" + request.getTransactionRef() + "' already exists");
        }

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setFeeStructure(feeStructure);
        payment.setTotalPaid(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setRemarks(request.getRemarks());

        PaymentDetail detail = new PaymentDetail(
                request.getPaymentMethod().trim().toUpperCase(),
                request.getTransactionRef().trim(),
                request.getAmount()
        );
        payment.addDetail(detail);

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(savedPayment);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> searchPayments(PaymentSearchCriteria criteria, Pageable pageable) {
        Page<Payment> page = paymentRepository.findAll(PaymentSpecification.withCriteria(criteria), pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return PagedResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getStudentPayments(User currentUser, Pageable pageable) {
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + currentUser.getUsername()));

        Page<Payment> page = paymentRepository.findByStudentId(student.getId(), pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
        return PagedResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, UpdatePaymentStatusRequest request) {
        Payment payment = paymentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        PaymentStatus currentStatus = payment.getStatus();
        PaymentStatus targetStatus = request.getStatus();

        // Validate state transition through State Machine
        paymentStateMachine.validateTransition(currentStatus, targetStatus);

        payment.setStatus(targetStatus);
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            payment.setRemarks(request.getRemarks().trim());
        }

        // Automatic receipt generation upon confirmation
        if (targetStatus == PaymentStatus.CONFIRMED && payment.getReceipt() == null) {
            String receiptNumber = generateReceiptNumber(payment.getId());
            Receipt receipt = new Receipt(payment, receiptNumber, LocalDateTime.now(), payment.getTotalPaid());
            payment.setReceipt(receipt);
            receiptRepository.save(receipt);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return PaymentResponse.from(updatedPayment);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptByPaymentId(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BadRequestException("Receipt is only available for CONFIRMED payments. Current status: " + payment.getStatus());
        }

        Receipt receipt = receiptRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not yet generated for payment id: " + paymentId));

        return new ReceiptResponse(
                receipt.getId(),
                payment.getId(),
                receipt.getReceiptNumber(),
                receipt.getIssueDate(),
                receipt.getTotalAmount()
        );
    }

    private String generateReceiptNumber(Long paymentId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String suffix = String.format("%04d", paymentId % 10000);
        return "RCP-" + dateStr + "-" + suffix;
>>>>>>> origin/feature/search
    }
}
