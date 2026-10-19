package com.rupp.payment.repository;

import com.rupp.payment.entity.TuitionFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionFeeStructureRepository extends JpaRepository<TuitionFeeStructure, Long> {
    List<TuitionFeeStructure> findByFacultyId(Long facultyId);
    List<TuitionFeeStructure> findByAcademicYearId(Long academicYearId);
    Optional<TuitionFeeStructure> findByFacultyIdAndAcademicYearId(Long facultyId, Long academicYearId);
}
