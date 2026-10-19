package com.rupp.payment.config;

import com.rupp.payment.entity.Payment;
import com.rupp.payment.entity.PaymentStatus;
import com.rupp.payment.entity.Student;
import com.rupp.payment.repository.PaymentRepository;
import com.rupp.payment.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Inserts sample students/payments so you can try the dashboard and receipt endpoints.
 * Runs ONLY with the "dev" profile and only when the payments table is empty.
 * Delete this file once Tab's real seed scripts are in.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final StudentRepository students;
    private final PaymentRepository payments;

    public DevDataSeeder(StudentRepository students, PaymentRepository payments) {
        this.students = students;
        this.payments = payments;
    }

    @Override
    public void run(String... args) {
        if (payments.count() > 0) {
            return;
        }

        List<Student> list = students.saveAll(List.of(
                new Student("S001", "Sok Dara"),
                new Student("S002", "Chan Sreyneang"),
                new Student("S003", "Vann Piseth")));

        Random rnd = new Random(42);
        BigDecimal[] fees = {new BigDecimal("250.00"), new BigDecimal("300.00"),
                new BigDecimal("450.00"), new BigDecimal("500.00")};
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd");

        List<Payment> batch = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            LocalDateTime paidAt = LocalDate.now().minusDays(rnd.nextInt(120))
                    .atTime(8 + rnd.nextInt(9), rnd.nextInt(60));
            int roll = rnd.nextInt(10);
            PaymentStatus status = roll < 7 ? PaymentStatus.COMPLETED
                    : roll < 9 ? PaymentStatus.PENDING : PaymentStatus.FAILED;

            Payment p = new Payment();
            p.setReceiptNumber("RCP-" + paidAt.format(f) + "-" + String.format("%04d", i + 1));
            p.setStudent(list.get(rnd.nextInt(list.size())));
            p.setAmount(fees[rnd.nextInt(fees.length)]);
            p.setStatus(status);
            p.setPaidAt(paidAt);
            batch.add(p);
        }
        payments.saveAll(batch);
    }
}
