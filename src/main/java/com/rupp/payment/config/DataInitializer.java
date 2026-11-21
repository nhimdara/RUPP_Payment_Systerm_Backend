package com.rupp.payment.config;

import com.rupp.payment.entity.*;
import com.rupp.payment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final MajorRepository majorRepository;
    private final SemesterRepository semesterRepository;
    private final TuitionFeeStructureRepository feeStructureRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServiceFeeRepository serviceFeeRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AcademicYearRepository academicYearRepository,
                           MajorRepository majorRepository,
                           SemesterRepository semesterRepository,
                           TuitionFeeStructureRepository feeStructureRepository,
                           PaymentRepository paymentRepository,
                           ReceiptRepository receiptRepository,
                           PasswordEncoder passwordEncoder,
                           ServiceFeeRepository serviceFeeRepository,
                           JdbcTemplate jdbcTemplate) {
        this.serviceFeeRepository = serviceFeeRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.majorRepository = majorRepository;
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

        // Seed Majors if none exist
        Major csMajor = majorRepository.findByMajorCode("CS-01")
                .orElseGet(() -> {
                    Major m = new Major();
                    m.setMajorName("Computer Science");
                    m.setMajorCode("CS-01");
                    m.setFaculty(csFaculty);
                    m.setDescription("Core Computer Science Program");
                    return majorRepository.save(m);
                });

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
            student.setMajor(csMajor);
            studentRepository.save(student);

            logger.info("Default STUDENT user created: student1 / student123");
        }

        if (!userRepository.existsByUsername("ayutaya")) {
            User studentUser = new User();
            studentUser.setUsername("ayutaya");
            studentUser.setEmail("ayutaya@rupp.edu.kh");
            studentUser.setPassword(passwordEncoder.encode("student123"));
            studentUser.setRole(Role.ROLE_STUDENT);
            User savedUser = userRepository.save(studentUser);

            Student student = new Student();
            student.setUser(savedUser);
            student.setFirstName("Ayutaya");
            student.setLastName("Tab");
            student.setFaculty(csFaculty);
            student.setAcademicYear(year2025);
            student.setMajor(csMajor);
            studentRepository.save(student);

            logger.info("Default STUDENT user created: ayutaya / student123");
        }

        // Seed Semester if none exist
        Semester semester1 = semesterRepository.findAll().stream().findFirst()
                .orElseGet(() -> semesterRepository.save(new Semester(
                        "Semester 1", 1, year2025,
                        LocalDate.of(2024, 10, 1), LocalDate.of(2025, 2, 28), true)));

        // Seed Tuition Fee Structures if none exist
        TuitionFeeStructure feeStructure = feeStructureRepository.findByFacultyIdAndAcademicYearId(csFaculty.getId(), year2025.getId())
                .orElseGet(() -> feeStructureRepository.save(new TuitionFeeStructure(
                        csFaculty,
                        year2025,
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

        allowServicePaymentsInExistingSchema();
        seedServiceFees();
        backfillMissingReceipts();
    }

    /**
     * Payments for campus services have no tuition fee structure. Hibernate's ddl-auto=update adds
     * new columns but never relaxes NOT NULL, so older PostgreSQL databases still have
     * payments.fee_structure_id NOT NULL. Drop it once (safe to run repeatedly).
     */
    private void allowServicePaymentsInExistingSchema() {
        try {
            String product = jdbcTemplate.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
            if (product != null && product.toLowerCase().contains("postgres")) {
                jdbcTemplate.execute("ALTER TABLE payments ALTER COLUMN fee_structure_id DROP NOT NULL");
            }
        } catch (Exception e) {
            logger.warn("Could not relax payments.fee_structure_id NOT NULL: {}", e.getMessage());
        }
    }

    /** Campus service fees (parking, library, lab...) valid from the start of the current month. */
    private void seedServiceFees() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        java.time.format.DateTimeFormatter label = java.time.format.DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDate semesterEnd = start.plusMonths(6).minusDays(1);
        LocalDate yearEnd = start.plusMonths(12).minusDays(1);
        String semesterLabel = start.format(label) + " – " + semesterEnd.format(label);
        String yearLabel = start.format(label) + " – " + yearEnd.format(label);

        seedFee(new ServiceFee("MOTO-SEM", "Moto Parking Pass (Semester)", ServiceFeeCategory.MOTO_PARKING,
                "Unlimited motorbike parking at Campus 1 and Campus 2 for one semester.",
                new BigDecimal("15.00"), semesterLabel, start, semesterEnd, false));
        seedFee(new ServiceFee("LIB-YEAR", "Library Pass (Academic Year)", ServiceFeeCategory.LIBRARY,
                "Borrowing and reading-room access at the RUPP library for one year.",
                new BigDecimal("10.00"), yearLabel, start, yearEnd, false));
        seedFee(new ServiceFee("LAB-SEM", "Computer Lab Access (Semester)", ServiceFeeCategory.LABORATORY,
                "STEM computer lab and Wi-Fi lab access for one semester.",
                new BigDecimal("25.00"), semesterLabel, start, semesterEnd, false));
        seedFee(new ServiceFee("IDC-REPL", "Student ID Card Replacement", ServiceFeeCategory.ID_CARD,
                "Reprint of a lost or damaged student ID card.",
                new BigDecimal("5.00"), null, null, null, true));
        seedFee(new ServiceFee("EXAM-RETAKE", "Exam Retake Fee (per subject)", ServiceFeeCategory.EXAM,
                "Registration fee to retake one final exam.",
                new BigDecimal("20.00"), null, null, null, true));
    }

    /** Earlier, POST /payments/{id}/confirm did not issue receipts. Give those confirmed payments one. */
    private void backfillMissingReceipts() {
        paymentRepository.findByStatus(PaymentStatus.CONFIRMED).stream()
                .filter(p -> receiptRepository.findByPaymentId(p.getId()).isEmpty())
                .forEach(p -> {
                    String number = "RCP-" + p.getPaymentDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                            + "-" + String.format("%04d", p.getId() % 10000);
                    receiptRepository.save(new Receipt(p, number, LocalDateTime.now(), p.getTotalPaid()));
                    logger.info("Backfilled missing receipt {} for confirmed payment {}", number, p.getId());
                });
    }

    private void seedFee(ServiceFee fee) {
        if (!serviceFeeRepository.existsByCode(fee.getCode())) {
            serviceFeeRepository.save(fee);
            logger.info("Service fee seeded: {} (${})", fee.getName(), fee.getAmount());
        }
    }
}
