package com.rupp.payment.repository;

import com.rupp.payment.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByYearLabel(String yearLabel);
    boolean existsByYearLabel(String yearLabel);
    boolean existsByYearLabelAndIdNot(String yearLabel, Long id);
}
