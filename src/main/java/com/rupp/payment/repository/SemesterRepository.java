package com.rupp.payment.repository;

import com.rupp.payment.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
<<<<<<< HEAD
import java.util.Optional;
=======
>>>>>>> origin/feature/search

@Repository
public interface SemesterRepository extends JpaRepository<Semester, Long> {
    List<Semester> findByAcademicYearId(Long academicYearId);
<<<<<<< HEAD
    Optional<Semester> findByAcademicYearIdAndSemesterNumber(Long academicYearId, Integer semesterNumber);
    Optional<Semester> findFirstByIsActiveTrue();
    boolean existsByAcademicYearIdAndSemesterNumber(Long academicYearId, Integer semesterNumber);
    boolean existsByAcademicYearIdAndSemesterNumberAndIdNot(Long academicYearId, Integer semesterNumber, Long id);
=======
>>>>>>> origin/feature/search
}
