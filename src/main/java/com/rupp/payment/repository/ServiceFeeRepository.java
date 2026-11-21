package com.rupp.payment.repository;

import com.rupp.payment.entity.ServiceFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceFeeRepository extends JpaRepository<ServiceFee, Long> {
    List<ServiceFee> findByActiveTrueOrderByCategoryAscNameAsc();
    Optional<ServiceFee> findByCode(String code);
    boolean existsByCode(String code);
}
