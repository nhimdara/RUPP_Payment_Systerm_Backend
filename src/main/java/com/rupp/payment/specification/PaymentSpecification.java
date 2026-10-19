package com.rupp.payment.specification;

import com.rupp.payment.dto.request.PaymentSearchCriteria;
import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentDetail;
import com.rupp.payment.entity.Student;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification implements Specification<Payment> {

    private final PaymentSearchCriteria criteria;

    public PaymentSpecification(PaymentSearchCriteria criteria) {
        this.criteria = criteria;
    }

    public static Specification<Payment> withCriteria(PaymentSearchCriteria criteria) {
        return new PaymentSpecification(criteria);
    }

    @Override
    public Predicate toPredicate(Root<Payment> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (criteria == null) {
            return cb.conjunction();
        }

        List<Predicate> predicates = new ArrayList<>();

        // Join student if needed
        Path<Student> studentPath = root.get("student");

        // 1. Filter by studentId
        if (criteria.getStudentId() != null) {
            predicates.add(cb.equal(studentPath.get("id"), criteria.getStudentId()));
        }

        // 2. Filter by studentName (first name or last name contains)
        if (criteria.getStudentName() != null && !criteria.getStudentName().trim().isEmpty()) {
            String searchPattern = "%" + criteria.getStudentName().trim().toLowerCase() + "%";
            Predicate firstNameMatch = cb.like(cb.lower(studentPath.get("firstName")), searchPattern);
            Predicate lastNameMatch = cb.like(cb.lower(studentPath.get("lastName")), searchPattern);
            predicates.add(cb.or(firstNameMatch, lastNameMatch));
        }

        // 3. Filter by studentEmail
        if (criteria.getStudentEmail() != null && !criteria.getStudentEmail().trim().isEmpty()) {
            String emailPattern = "%" + criteria.getStudentEmail().trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(studentPath.get("user").get("email")), emailPattern));
        }

        // 4. Filter by facultyId
        if (criteria.getFacultyId() != null) {
            predicates.add(cb.equal(studentPath.get("faculty").get("id"), criteria.getFacultyId()));
        }

        // 5. Filter by academicYearId
        if (criteria.getAcademicYearId() != null) {
            predicates.add(cb.equal(studentPath.get("academicYear").get("id"), criteria.getAcademicYearId()));
        }

        // 6. Filter by feeStructureId
        if (criteria.getFeeStructureId() != null) {
            predicates.add(cb.equal(root.get("feeStructure").get("id"), criteria.getFeeStructureId()));
        }

        // 7. Filter by payment status
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }

        // 8. Filter by date range (startDate to endDate)
        if (criteria.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("paymentDate"), criteria.getStartDate().atStartOfDay()));
        }
        if (criteria.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("paymentDate"), criteria.getEndDate().atTime(23, 59, 59, 999999999)));
        }

        // 9. Filter by amount range (minAmount to maxAmount)
        if (criteria.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalPaid"), criteria.getMinAmount()));
        }
        if (criteria.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalPaid"), criteria.getMaxAmount()));
        }

        // 10. Filter by payment method or transaction reference (via PaymentDetail)
        boolean hasMethod = criteria.getPaymentMethod() != null && !criteria.getPaymentMethod().trim().isEmpty();
        boolean hasTxRef = criteria.getTransactionRef() != null && !criteria.getTransactionRef().trim().isEmpty();

        if (hasMethod || hasTxRef) {
            Join<Payment, PaymentDetail> detailJoin = root.join("details", JoinType.LEFT);
            if (query != null && !Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                query.distinct(true);
            }

            if (hasMethod) {
                predicates.add(cb.equal(cb.upper(detailJoin.get("method")), criteria.getPaymentMethod().trim().toUpperCase()));
            }
            if (hasTxRef) {
                String txPattern = "%" + criteria.getTransactionRef().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(detailJoin.get("transactionRef")), txPattern));
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
