package com.rupp.payment.repository;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    @Override
    @EntityGraph(attributePaths = {"student", "student.user", "student.faculty", "student.academicYear", "feeStructure"})
    Page<Payment> findAll(Specification<Payment> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.user", "student.faculty", "student.academicYear", "feeStructure", "details", "receipt"})
    Optional<Payment> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"student", "student.user", "student.faculty", "student.academicYear", "feeStructure"})
    Page<Payment> findByStudentId(Long studentId, Pageable pageable);

    List<Payment> findByStudentIdAndFeeStructureId(Long studentId, Long feeStructureId);

    boolean existsByFeeStructureId(Long feeStructureId);

    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.totalPaid), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumTotalPaidByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.totalPaid), 0) FROM Payment p WHERE p.status = 'CONFIRMED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumConfirmedPaidBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.student.faculty.facultyName, COUNT(p), COALESCE(SUM(p.totalPaid), 0) " +
           "FROM Payment p WHERE p.status = 'CONFIRMED' " +
           "GROUP BY p.student.faculty.facultyName")
    List<Object[]> findRevenueByFaculty();

    @Query("SELECT pd.method, COUNT(pd), COALESCE(SUM(pd.amount), 0) " +
           "FROM PaymentDetail pd " +
           "GROUP BY pd.method")
    List<Object[]> findStatsByPaymentMethod();
}
