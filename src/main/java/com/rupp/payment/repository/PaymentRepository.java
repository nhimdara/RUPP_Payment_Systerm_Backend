package com.rupp.payment.repository;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    long countByStatus(PaymentStatus status);

    /** Total of payments with the given status paid in [start, end). Returns 0 when none. */
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.status = :status AND p.paidAt >= :start AND p.paidAt < :end
            """)
    BigDecimal sumAmountByStatusBetween(@Param("status") PaymentStatus status,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    /** Rows: [date, total]. Only days that have payments are returned. */
    @Query(value = """
            SELECT CAST(paid_at AS date) AS revenue_date, SUM(amount) AS total
            FROM payments
            WHERE status = :status AND paid_at >= :start AND paid_at < :end
            GROUP BY CAST(paid_at AS date)
            ORDER BY CAST(paid_at AS date)
            """, nativeQuery = true)
    List<Object[]> dailyRevenue(@Param("status") String status,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    /** Rows: [month number 1-12, total]. Only months that have payments are returned. */
    @Query(value = """
            SELECT EXTRACT(MONTH FROM paid_at) AS month_no, SUM(amount) AS total
            FROM payments
            WHERE status = :status AND paid_at >= :start AND paid_at < :end
            GROUP BY EXTRACT(MONTH FROM paid_at)
            ORDER BY EXTRACT(MONTH FROM paid_at)
            """, nativeQuery = true)
    List<Object[]> monthlyRevenue(@Param("status") String status,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}
