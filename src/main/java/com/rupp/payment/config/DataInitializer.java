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
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository,
                           AcademicYearRepository academicYearRepository,
                           MajorRepository majorRepository,
                           SemesterRepository semesterRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.majorRepository = majorRepository;
        this.semesterRepository = semesterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Faculties if none exist
        Faculty csFaculty = facultyRepository.findByFacultyName("Computer Science")
                .orElseGet(() -> facultyRepository.save(new Faculty("Computer Science", "Faculty of Science")));
        Faculty teFaculty = facultyRepository.findByFacultyName("Telecommunication Engineering")
                .orElseGet(() -> facultyRepository.save(new Faculty("Telecommunication Engineering", "Faculty of Engineering")));

        // Seed Academic Years if none exist
        AcademicYear year2025 = academicYearRepository.findByYearLabel("2024-2025")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2024-2025", LocalDate.of(2024, 10, 1), LocalDate.of(2025, 7, 31))));
        academicYearRepository.findByYearLabel("2025-2026")
                .orElseGet(() -> academicYearRepository.save(
                        new AcademicYear("2025-2026", LocalDate.of(2025, 10, 1), LocalDate.of(2026, 7, 31))));

        // Seed Majors if none exist
        if (majorRepository.count() == 0) {
            majorRepository.save(new Major("Computer Science", "CS", csFaculty, "Department of Computer Science"));
            majorRepository.save(new Major("Information Technology Engineering", "ITE", teFaculty, "Department of Telecom & IT"));
            logger.info("Default Majors created: CS, ITE");
        }

        // Seed Semesters if none exist
        if (semesterRepository.count() == 0) {
            semesterRepository.save(new Semester("Semester 1", 1, year2025, LocalDate.of(2024, 10, 1), LocalDate.of(2025, 2, 28), true));
            semesterRepository.save(new Semester("Semester 2", 2, year2025, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 7, 31), false));
            logger.info("Default Semesters created for 2024-2025: Semester 1 (Active), Semester 2");
        }

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

        Major csMajor = majorRepository.findByMajorCode("CS")
                .orElseGet(() -> majorRepository.save(new Major("Computer Science", "CS", csFaculty, "Department of Computer Science")));

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
    }
}
