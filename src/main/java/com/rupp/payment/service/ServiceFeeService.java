package com.rupp.payment.service;

import com.rupp.payment.dto.request.PayServiceFeeRequest;
import com.rupp.payment.dto.request.ServiceFeeRequest;
import com.rupp.payment.dto.response.PaymentResponse;
import com.rupp.payment.dto.response.ServiceFeeResponse;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentDetail;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.entity.ServiceFee;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ConflictException;
import com.rupp.payment.exception.DuplicateResourceException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.PaymentDetailRepository;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.ServiceFeeRepository;
import com.rupp.payment.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Campus service fees (moto parking, library, lab...) and student payments for them. */
@Service
public class ServiceFeeService {

    private static final Set<PaymentStatus> ACTIVE_STATUSES =
            EnumSet.of(PaymentStatus.PENDING, PaymentStatus.VERIFIED, PaymentStatus.CONFIRMED);

    private final ServiceFeeRepository serviceFeeRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
    private final StudentRepository studentRepository;

    public ServiceFeeService(ServiceFeeRepository serviceFeeRepository,
                             PaymentRepository paymentRepository,
                             PaymentDetailRepository paymentDetailRepository,
                             StudentRepository studentRepository) {
        this.serviceFeeRepository = serviceFeeRepository;
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceFeeResponse> listActive() {
        return serviceFeeRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream()
                .map(ServiceFeeResponse::from)
                .collect(Collectors.toList());
    }

    /** Active fees plus the logged-in student's payment status for each one. */
    @Transactional(readOnly = true)
    public List<ServiceFeeResponse> listForStudent(User currentUser) {
        Student student = studentOf(currentUser);
        Map<Long, List<Payment>> byFee = paymentRepository.findByStudentIdAndServiceFeeIsNotNull(student.getId()).stream()
                .collect(Collectors.groupingBy(p -> p.getServiceFee().getId()));

        return serviceFeeRepository.findByActiveTrueOrderByCategoryAscNameAsc().stream()
                .map(fee -> {
                    Optional<Payment> latest = byFee.getOrDefault(fee.getId(), List.of()).stream()
                            .filter(p -> ACTIVE_STATUSES.contains(p.getStatus()))
                            .max(Comparator.comparing(Payment::getPaymentDate));
                    if (latest.isEmpty()) {
                        return ServiceFeeResponse.withStatus(fee, "NOT_PAID", null, null, null);
                    }
                    Payment p = latest.get();
                    String receiptNo = p.getReceipt() != null ? p.getReceipt().getReceiptNumber() : null;
                    String status = switch (p.getStatus()) {
                        case CONFIRMED -> fee.isRepeatable() ? "NOT_PAID" : "PAID";
                        case VERIFIED -> "VERIFIED";
                        default -> "PENDING";
                    };
                    return ServiceFeeResponse.withStatus(fee, status, p.getId(), receiptNo, p.getPaymentDate());
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates a PENDING payment for a service fee. The student then verifies it with the usual
     * OTP endpoints, and Finance confirms it (which issues the receipt), exactly like tuition.
     */
    @Transactional
    public PaymentResponse pay(User currentUser, Long serviceFeeId, PayServiceFeeRequest request) {
        Student student = studentOf(currentUser);
        ServiceFee fee = serviceFeeRepository.findById(serviceFeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Service fee not found with id: " + serviceFeeId));

        if (!fee.isActive()) {
            throw new BadRequestException("This service is no longer available");
        }
        if (fee.getValidUntil() != null && fee.getValidUntil().isBefore(LocalDate.now())) {
            throw new BadRequestException("This service fee period has ended");
        }

        List<Payment> existing = paymentRepository.findByStudentIdAndServiceFeeId(student.getId(), fee.getId());
        boolean inProgress = existing.stream().anyMatch(p ->
                p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.VERIFIED);
        if (inProgress) {
            throw new ConflictException("You already have a payment in progress for " + fee.getName());
        }
        if (!fee.isRepeatable() && existing.stream().anyMatch(p -> p.getStatus() == PaymentStatus.CONFIRMED)) {
            throw new ConflictException("You have already paid for " + fee.getName());
        }

        String ref = request.transactionRef().trim();
        if (paymentDetailRepository.existsByTransactionRef(ref)) {
            throw new BadRequestException("Transaction reference already exists: " + ref);
        }

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setServiceFee(fee);
        payment.setTotalPaid(fee.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setRemarks(request.remarks() != null && !request.remarks().isBlank()
                ? request.remarks().trim()
                : fee.getName());
        payment.addDetail(new PaymentDetail(request.paymentMethod().trim().toUpperCase(), ref, fee.getAmount()));

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ---- Admin ----

    @Transactional
    public ServiceFeeResponse create(ServiceFeeRequest request) {
        String code = request.code().trim().toUpperCase();
        if (serviceFeeRepository.existsByCode(code)) {
            throw new DuplicateResourceException("A service fee with code " + code + " already exists");
        }
        ServiceFee fee = new ServiceFee();
        fee.setCode(code);
        apply(fee, request);
        return ServiceFeeResponse.from(serviceFeeRepository.save(fee));
    }

    @Transactional
    public ServiceFeeResponse update(Long id, ServiceFeeRequest request) {
        ServiceFee fee = serviceFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service fee not found with id: " + id));
        String code = request.code().trim().toUpperCase();
        if (!code.equals(fee.getCode()) && serviceFeeRepository.existsByCode(code)) {
            throw new DuplicateResourceException("A service fee with code " + code + " already exists");
        }
        fee.setCode(code);
        apply(fee, request);
        return ServiceFeeResponse.from(serviceFeeRepository.save(fee));
    }

    /** Soft delete, so past payments keep pointing at the fee. */
    @Transactional
    public void deactivate(Long id) {
        ServiceFee fee = serviceFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service fee not found with id: " + id));
        fee.setActive(false);
        serviceFeeRepository.save(fee);
    }

    private void apply(ServiceFee fee, ServiceFeeRequest r) {
        if (r.validFrom() != null && r.validUntil() != null && r.validUntil().isBefore(r.validFrom())) {
            throw new BadRequestException("validUntil must be on or after validFrom");
        }
        fee.setName(r.name().trim());
        fee.setCategory(r.category());
        fee.setDescription(r.description());
        fee.setAmount(r.amount());
        fee.setValidityLabel(r.validityLabel());
        fee.setValidFrom(r.validFrom());
        fee.setValidUntil(r.validUntil());
        fee.setRepeatable(Boolean.TRUE.equals(r.repeatable()));
        fee.setActive(true);
    }

    private Student studentOf(User user) {
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + user.getUsername()));
    }
}
