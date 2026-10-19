package com.rupp.payment.repository;

import com.rupp.payment.entity.TuitionFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionFeeStructureRepository extends JpaRepository<TuitionFeeStructure, Long> {
<<<<<<< HEAD

    List<TuitionFeeStructure> findByFacultyId(Long facultyId);

    List<TuitionFeeStructure> findByAcademicYearId(Long academicYearId);

    Optional<TuitionFeeStructure> findByFacultyIdAndAcademicYearId(Long facultyId, Long academicYearId);

    boolean existsByFacultyIdAndAcademicYearId(Long facultyId, Long academicYearId);

    boolean existsByFacultyIdAndAcademicYearIdAndIdNot(Long facultyId, Long academicYearId, Long id);

    boolean existsByFacultyId(Long facultyId);

    boolean existsByAcademicYearId(Long academicYearId);
=======
    List<TuitionFeeStructure> findByFacultyId(Long facultyId);
    List<TuitionFeeStructure> findByAcademicYearId(Long academicYearId);
    Optional<TuitionFeeStructure> findByFacultyIdAndAcademicYearId(Long facultyId, Long academicYearId);
>>>>>>> origin/feature/search
}
