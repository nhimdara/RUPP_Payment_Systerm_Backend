package com.rupp.payment.repository;

import com.rupp.payment.entity.Student;
import com.rupp.payment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByFacultyId(Long facultyId);
    List<Student> findByAcademicYearId(Long academicYearId);
    List<Student> findByMajorId(Long majorId);
    boolean existsByUserId(Long userId);
    boolean existsByFacultyId(Long facultyId);
    boolean existsByAcademicYearId(Long academicYearId);
    boolean existsByMajorId(Long majorId);
}
