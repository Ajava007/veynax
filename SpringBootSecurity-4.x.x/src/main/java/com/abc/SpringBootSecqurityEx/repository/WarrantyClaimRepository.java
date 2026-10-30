package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.WarrantyClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
    Page<WarrantyClaim> findByStatus(String status, Pageable pageable);
}