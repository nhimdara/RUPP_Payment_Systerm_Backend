package com.rupp.payment.repository;

import com.rupp.payment.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByMajorCode(String majorCode);
    boolean existsByMajorCode(String majorCode);
    boolean existsByMajorCodeAndIdNot(String majorCode, Long id);
    List<Major> findByFacultyId(Long facultyId);
}
