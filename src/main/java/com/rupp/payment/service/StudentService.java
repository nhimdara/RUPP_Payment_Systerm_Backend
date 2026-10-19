package com.rupp.payment.service;

import com.rupp.payment.dto.request.CreateStudentRequest;
import com.rupp.payment.dto.request.UpdateStudentRequest;
import com.rupp.payment.dto.response.StudentResponse;
import com.rupp.payment.entity.AcademicYear;
import com.rupp.payment.entity.Faculty;
import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.BadRequestException;
import com.rupp.payment.exception.ResourceNotFoundException;
import com.rupp.payment.repository.AcademicYearRepository;
import com.rupp.payment.repository.FacultyRepository;
import com.rupp.payment.repository.StudentRepository;
import com.rupp.payment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final AcademicYearRepository academicYearRepository;
    private final com.rupp.payment.repository.MajorRepository majorRepository;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          FacultyRepository facultyRepository,
                          AcademicYearRepository academicYearRepository,
                          com.rupp.payment.repository.MajorRepository majorRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.academicYearRepository = academicYearRepository;
        this.majorRepository = majorRepository;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents(Long facultyId, Long academicYearId, Long majorId) {
        List<Student> students;
        if (majorId != null) {
            students = studentRepository.findByMajorId(majorId);
        } else if (facultyId != null) {
            students = studentRepository.findByFacultyId(facultyId);
        } else if (academicYearId != null) {
            students = studentRepository.findByAcademicYearId(academicYearId);
        } else {
            students = studentRepository.findAll();
        }

        return students.stream()
                .map(StudentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return StudentResponse.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + userId));
        return StudentResponse.fromEntity(student);
    }

    @Transactional(readOnly = true)
    public StudentResponse getMyStudentProfile(User currentUser) {
        Student student = studentRepository.findByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("No student profile found for current user: " + currentUser.getUsername()));
        return StudentResponse.fromEntity(student);
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByUserId(request.getUserId())) {
            throw new BadRequestException("Student record already exists for user ID: " + request.getUserId());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Student student = new Student();
        student.setUser(user);
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
            student.setFaculty(faculty);
        }

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            student.setAcademicYear(academicYear);
        }

        if (request.getMajorId() != null) {
            com.rupp.payment.entity.Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Major", "id", request.getMajorId()));
            student.setMajor(major);
        }

        Student saved = studentRepository.save(student);
        return StudentResponse.fromEntity(saved);
    }

    @Transactional
    public StudentResponse updateStudent(Long id, UpdateStudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            student.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            student.setLastName(request.getLastName());
        }

        if (request.getFacultyId() != null) {
            Faculty faculty = facultyRepository.findById(request.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty", "id", request.getFacultyId()));
            student.setFaculty(faculty);
        }

        if (request.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(request.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", "id", request.getAcademicYearId()));
            student.setAcademicYear(academicYear);
        }

        if (request.getMajorId() != null) {
            com.rupp.payment.entity.Major major = majorRepository.findById(request.getMajorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Major", "id", request.getMajorId()));
            student.setMajor(major);
        }

        Student updated = studentRepository.save(student);
        return StudentResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        studentRepository.delete(student);
    }
}
