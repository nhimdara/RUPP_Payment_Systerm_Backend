package com.rupp.payment.service;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDetailRepository paymentDetailRepository;
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
    }
}
