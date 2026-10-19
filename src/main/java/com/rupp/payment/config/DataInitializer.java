package com.rupp.payment.config;

import com.rupp.payment.entity.*;
import com.rupp.payment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SemesterRepository semesterRepository;
    private final TuitionFeeStructureRepository feeStructureRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AcademicYearRepository academicYearRepository,
                           SemesterRepository semesterRepository,
                           TuitionFeeStructureRepository feeStructureRepository,
                           PaymentRepository paymentRepository,
                           ReceiptRepository receiptRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.semesterRepository = semesterRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.paymentRepository = paymentRepository;
        this.receiptRepository = receiptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Faculties if none exist
        Faculty csFaculty = facultyRepository.findByFacultyName("Computer Science")
                .orElseGet(() -> facultyRepository.save(new Faculty("Computer Science", "Faculty of Science")));
        facultyRepository.findByFacultyName("Telecommunication Engineering")
                .orElseGet(() -> facultyRepository.save(new Faculty("Telecommunication Engineering", "Faculty of Engineering")));

        // Seed Academic Years if none exist
        AcademicYear year2025 = academicYearRepository.findByYearLabel("2024-2025")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31))));
        academicYearRepository.findByYearLabel("2025-2026")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2025-2026", LocalDate.of(2025, 10, 1), LocalDate.of(2026, 7, 31))));

        // Seed Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@rupp.edu.kh");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            logger.info("Default ADMIN user created: admin / admin123");
        }

        // Seed Finance Staff User
        if (!userRepository.existsByUsername("finance")) {
            User finance = new User();
            finance.setUsername("finance");
            finance.setEmail("finance@rupp.edu.kh");
            finance.setPassword(passwordEncoder.encode("finance123"));
            finance.setRole(Role.ROLE_FINANCE_STAFF);
            userRepository.save(finance);
            logger.info("Default FINANCE_STAFF user created: finance / finance123");
        }

        // Seed Student User and Student Profile
        if (!userRepository.existsByUsername("student1")) {
            User studentUser = new User();
            studentUser.setUsername("student1");
            studentUser.setEmail("student1@rupp.edu.kh");
            studentUser.setPassword(passwordEncoder.encode("student123"));
            studentUser.setRole(Role.ROLE_STUDENT);
            User savedUser = userRepository.save(studentUser);

            Student student = new Student();
            student.setUser(savedUser);
            student.setFirstName("Dara");
            student.setLastName("Nhim");
            student.setFaculty(csFaculty);
            student.setAcademicYear(year2025);
            studentRepository.save(student);

            logger.info("Default STUDENT user created: student1 / student123");
        }

        // Seed Semester if none exist
        Semester semester1 = semesterRepository.findAll().stream().findFirst()
                .orElseGet(() -> semesterRepository.save(new Semester("Semester 1", year2025)));

        // Seed Tuition Fee Structures if none exist
        TuitionFeeStructure feeStructure = feeStructureRepository.findByFacultyIdAndAcademicYearId(csFaculty.getId(), year2025.getId())
                .orElseGet(() -> feeStructureRepository.save(new TuitionFeeStructure(
                        csFaculty,
                        year2025,
                        semester1,
                        new BigDecimal("600.00"),
                        LocalDate.of(2025, 11, 30)
                )));

        // Seed Sample Payment and Receipt if none exist
        if (paymentRepository.count() == 0) {
            studentRepository.findAll().stream().findFirst().ifPresent(student -> {
                // 1. Confirmed payment with receipt
                Payment confirmedPayment = new Payment();
                confirmedPayment.setStudent(student);
                confirmedPayment.setFeeStructure(feeStructure);
                confirmedPayment.setTotalPaid(new BigDecimal("600.00"));
                confirmedPayment.setStatus(PaymentStatus.CONFIRMED);
                confirmedPayment.setPaymentDate(LocalDateTime.now().minusDays(3));
                confirmedPayment.setRemarks("Full semester tuition fee paid via ABA");

                PaymentDetail detail1 = new PaymentDetail("ABA_PAY", "ABA-TXN-889123", new BigDecimal("600.00"));
                confirmedPayment.addDetail(detail1);

                Payment savedConfirmed = paymentRepository.save(confirmedPayment);

                Receipt receipt = new Receipt(
                        savedConfirmed,
                        "RCP-20261019-0001",
                        LocalDateTime.now().minusDays(3),
                        new BigDecimal("600.00")
                );
                savedConfirmed.setReceipt(receipt);
                receiptRepository.save(receipt);
                paymentRepository.save(savedConfirmed);

                // 2. Pending payment for testing verification
                Payment pendingPayment = new Payment();
                pendingPayment.setStudent(student);
                pendingPayment.setFeeStructure(feeStructure);
                pendingPayment.setTotalPaid(new BigDecimal("300.00"));
                pendingPayment.setStatus(PaymentStatus.PENDING);
                pendingPayment.setPaymentDate(LocalDateTime.now().minusHours(4));
                pendingPayment.setRemarks("First installment paid via Wing");

                PaymentDetail detail2 = new PaymentDetail("WING", "WING-TXN-441299", new BigDecimal("300.00"));
                pendingPayment.addDetail(detail2);
                paymentRepository.save(pendingPayment);

                logger.info("Sample payments initialized: 1 CONFIRMED with receipt, 1 PENDING");
            });
        }
    }
}
