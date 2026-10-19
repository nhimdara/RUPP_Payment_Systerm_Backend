package com.rupp.payment.config;

import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Major;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.Semester;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.MajorRepository;
import com.rupp.payment.repository.SemesterRepository;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final AcademicYearRepository academicYearRepository;
    private final MajorRepository majorRepository;
    private final SemesterRepository semesterRepository;
    private final com.rupp.payment.repository.PaymentStatusRepository paymentStatusRepository;
    private final com.rupp.payment.repository.TuitionFeeStructureRepository tuitionFeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AcademicYearRepository academicYearRepository,
                           MajorRepository majorRepository,
                           SemesterRepository semesterRepository,
                           com.rupp.payment.repository.PaymentStatusRepository paymentStatusRepository,
                           com.rupp.payment.repository.TuitionFeeStructureRepository tuitionFeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.majorRepository = majorRepository;
        this.semesterRepository = semesterRepository;
        this.paymentStatusRepository = paymentStatusRepository;
        this.tuitionFeeRepository = tuitionFeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Faculties
        Faculty csFaculty = facultyRepository.findByFacultyName("Computer Science")
                .orElseGet(() -> facultyRepository.save(new Faculty("Computer Science", "Faculty of Science")));
        Faculty teFaculty = facultyRepository.findByFacultyName("Telecommunication Engineering")
                .orElseGet(() -> facultyRepository.save(new Faculty("Telecommunication Engineering", "Faculty of Engineering")));
        Faculty itFaculty = facultyRepository.findByFacultyName("Information Technology Engineering")
                .orElseGet(() -> facultyRepository.save(new Faculty("Information Technology Engineering", "Faculty of Engineering")));
        Faculty mathFaculty = facultyRepository.findByFacultyName("Mathematics")
                .orElseGet(() -> facultyRepository.save(new Faculty("Mathematics", "Faculty of Science")));
        Faculty iflFaculty = facultyRepository.findByFacultyName("Department of English")
                .orElseGet(() -> facultyRepository.save(new Faculty("Department of English", "Institute of Foreign Languages")));

        // 2. Seed Academic Years
        AcademicYear year2024 = academicYearRepository.findByYearLabel("2023-2024")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2023-2024", LocalDate.of(2023, 10, 1), LocalDate.of(2024, 7, 31))));
        AcademicYear year2025 = academicYearRepository.findByYearLabel("2024-2025")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31))));
        AcademicYear year2026 = academicYearRepository.findByYearLabel("2025-2026")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2025-2026", LocalDate.of(2025, 10, 1), LocalDate.of(2026, 7, 31))));

        // 3. Seed Majors if none exist
        if (majorRepository.count() == 0) {
            majorRepository.save(new Major("Computer Science", "CS", csFaculty, "Department of Computer Science"));
            majorRepository.save(new Major("Information Technology Engineering", "ITE", teFaculty, "Department of Telecom & IT"));
            logger.info("Default Majors created: CS, ITE");
        }

        Major csMajor = majorRepository.findByMajorCode("CS")
                .orElseGet(() -> majorRepository.save(new Major("Computer Science", "CS", csFaculty, "Department of Computer Science")));
        Major iteMajor = majorRepository.findByMajorCode("ITE")
                .orElseGet(() -> majorRepository.save(new Major("Information Technology Engineering", "ITE", teFaculty, "Department of Telecom & IT")));

        // 4. Seed Semesters if none exist
        if (semesterRepository.count() == 0) {
            semesterRepository.save(new Semester("Semester 1", 1, year2025, LocalDate.of(2024, 10, 1), LocalDate.of(2025, 2, 28), true));
            semesterRepository.save(new Semester("Semester 2", 2, year2025, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 31), false));
            logger.info("Default Semesters created for 2024-2025: Semester 1 (Active), Semester 2");
        }

        // 5. Seed Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@rupp.edu.kh");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            logger.info("Default ADMIN user created: admin / admin123");
        }

        // 6. Seed Finance Staff Users
        if (!userRepository.existsByUsername("finance")) {
            User finance = new User();
            finance.setUsername("finance");
            finance.setEmail("finance@rupp.edu.kh");
            finance.setPassword(passwordEncoder.encode("finance123"));
            finance.setRole(Role.ROLE_FINANCE_STAFF);
            userRepository.save(finance);
            logger.info("Default FINANCE_STAFF user created: finance / finance123");
        }

        if (!userRepository.existsByUsername("accountant")) {
            User accountant = new User();
            accountant.setUsername("accountant");
            accountant.setEmail("accountant@rupp.edu.kh");
            accountant.setPassword(passwordEncoder.encode("finance123"));
            accountant.setRole(Role.ROLE_FINANCE_STAFF);
            userRepository.save(accountant);
            logger.info("Secondary FINANCE_STAFF user created: accountant / finance123");
        }

        // 7. Seed Legacy student1
        if (!userRepository.existsByUsername("student1")) {
            seedStudentUser("student1", "student1@rupp.edu.kh", "student123", "Dara", "Nhim", csFaculty, year2025, csMajor);
        }

        // 8. Seed All 6 Team Members as Students
        seedStudentUser("dara_nhim", "dara@rupp.edu.kh", "student123", "Dara", "Nhim", csFaculty, year2025, csMajor);
        seedStudentUser("leangkhai", "leangkhai@rupp.edu.kh", "student123", "Leangkhai", "Taing", teFaculty, year2025, iteMajor);
        seedStudentUser("ayutaya", "ayutaya@rupp.edu.kh", "student123", "Ayutaya", "Tab", csFaculty, year2025, csMajor);
        seedStudentUser("chhuntheang", "chhuntheang@rupp.edu.kh", "student123", "Chhuntheang", "Thorn", itFaculty, year2025, iteMajor);
        seedStudentUser("chihong", "chihong@rupp.edu.kh", "student123", "Chihong", "Tang", csFaculty, year2025, csMajor);
        seedStudentUser("vannsonleng", "vannsonleng@rupp.edu.kh", "student123", "Vannsonleng", "Nuon", teFaculty, year2025, iteMajor);
    }

    private void seedStudentUser(String username, String email, String rawPassword,
                                 String firstName, String lastName,
                                 Faculty faculty, AcademicYear academicYear, Major major) {
        if (!userRepository.existsByUsername(username)) {
            User studentUser = new User();
            studentUser.setUsername(username);
            studentUser.setEmail(email);
            studentUser.setPassword(passwordEncoder.encode(rawPassword));
            studentUser.setRole(Role.ROLE_STUDENT);
            User savedUser = userRepository.save(studentUser);

            Student student = new Student();
            student.setUser(savedUser);
            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setFaculty(faculty);
            student.setAcademicYear(academicYear);
            student.setMajor(major);
            studentRepository.save(student);

            logger.info("Seeded Student User: {} ({} {})", username, firstName, lastName);
        }

        // Seed Payment Statuses if none exist
        seedPaymentStatus("PENDING");
        seedPaymentStatus("VERIFIED");
        seedPaymentStatus("CONFIRMED");
        seedPaymentStatus("REJECTED");

        // Seed Tuition Fee Structure for CS and 2024-2025 if none exists
        if (!tuitionFeeRepository.existsByFacultyIdAndAcademicYearId(csFaculty.getId(), year2025.getId())) {
            com.rupp.payment.entity.TuitionFeeStructure csFee = new com.rupp.payment.entity.TuitionFeeStructure(
                    csFaculty,
                    year2025,
                    new java.math.BigDecimal("600.00"),
                    LocalDate.of(2025, 1, 15)
            );
            tuitionFeeRepository.save(csFee);
            logger.info("Default Tuition Fee Structure created for Computer Science 2024-2025: $600.00");
        }

        if (!tuitionFeeRepository.existsByFacultyIdAndAcademicYearId(teFaculty.getId(), year2025.getId())) {
            com.rupp.payment.entity.TuitionFeeStructure teFee = new com.rupp.payment.entity.TuitionFeeStructure(
                    teFaculty,
                    year2025,
                    new java.math.BigDecimal("700.00"),
                    LocalDate.of(2025, 1, 15)
            );
            tuitionFeeRepository.save(teFee);
            logger.info("Default Tuition Fee Structure created for Telecommunication Engineering 2024-2025: $700.00");
        }
    }

    private void seedPaymentStatus(String statusName) {
        if (!paymentStatusRepository.existsByStatusName(statusName)) {
            paymentStatusRepository.save(new com.rupp.payment.entity.PaymentStatus(statusName));
            logger.info("Default PaymentStatus created: {}", statusName);
        }
    }
}
