package com.rupp.payment.service;

import com.rupp.payment.dto.response.SystemMonitoringResponse;
import com.rupp.payment.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.LocalDateTime;

@Service
public class SystemMonitoringService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final MajorRepository majorRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final DataSource dataSource;

    public SystemMonitoringService(UserRepository userRepository,
                                   StudentRepository studentRepository,
                                   FacultyRepository facultyRepository,
                                   MajorRepository majorRepository,
                                   AcademicYearRepository academicYearRepository,
                                   SemesterRepository semesterRepository,
                                   PaymentRepository paymentRepository,
                                   ReceiptRepository receiptRepository,
                                   DataSource dataSource) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.majorRepository = majorRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public SystemMonitoringResponse getSystemMetrics() {
        String dbStatus = "UP";
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(2)) {
                dbStatus = "DEGRADED";
            }
        } catch (Exception e) {
            dbStatus = "DOWN: " + e.getMessage();
        }

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        return new SystemMonitoringResponse(
                "HEALTHY",
                dbStatus,
                LocalDateTime.now(),
                uptime,
                totalMemory,
                freeMemory,
                usedMemory,
                runtime.availableProcessors(),
                userRepository.count(),
                studentRepository.count(),
                facultyRepository.count(),
                majorRepository.count(),
                academicYearRepository.count(),
                semesterRepository.count(),
                paymentRepository.count(),
                receiptRepository.count()
        );
    }
}
