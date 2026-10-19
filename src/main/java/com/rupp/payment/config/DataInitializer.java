package com.rupp.payment.config;

import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AcademicYearRepository academicYearRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Seed Faculties
        Faculty csFaculty = facultyRepository.findByFacultyName("Computer Science")
                .orElseGet(() -> facultyRepository.save(new Faculty("Computer Science", "Faculty of Science")));
        facultyRepository.findByFacultyName("Telecommunication Engineering")
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

        // 3. Seed Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@rupp.edu.kh");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            logger.info("Default ADMIN user created: admin / admin123");
        }

        // 4. Seed Finance Staff Users
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

        // 5. Seed Legacy student1
        if (!userRepository.existsByUsername("student1")) {
            seedStudentUser("student1", "student1@rupp.edu.kh", "student123", "Dara", "Nhim", csFaculty, year2025);
        }

        // 6. Seed All 6 Team Members as Students
        seedStudentUser("dara_nhim", "dara@rupp.edu.kh", "student123", "Dara", "Nhim", csFaculty, year2025);
        seedStudentUser("leangkhai", "leangkhai@rupp.edu.kh", "student123", "Leangkhai", "Taing", teFaculty, year2025);
        seedStudentUser("ayutaya", "ayutaya@rupp.edu.kh", "student123", "Ayutaya", "Tab", csFaculty, year2025);
        seedStudentUser("chhuntheang", "chhuntheang@rupp.edu.kh", "student123", "Chhuntheang", "Thorn", itFaculty, year2025);
        seedStudentUser("chihong", "chihong@rupp.edu.kh", "student123", "Chihong", "Tang", csFaculty, year2025);
        seedStudentUser("vannsonleng", "vannsonleng@rupp.edu.kh", "student123", "Vannsonleng", "Nuon", teFaculty, year2025);
    }

    private void seedStudentUser(String username, String email, String rawPassword,
                                 String firstName, String lastName,
                                 Faculty faculty, AcademicYear academicYear) {
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
            studentRepository.save(student);

            logger.info("Seeded Student User: {} ({} {})", username, firstName, lastName);
        }
    }
}
