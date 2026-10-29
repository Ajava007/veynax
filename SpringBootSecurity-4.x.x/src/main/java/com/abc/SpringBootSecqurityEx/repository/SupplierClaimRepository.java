package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.SupplierClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SupplierClaimRepository extends JpaRepository<SupplierClaim, Long> {
    Page<SupplierClaim> findBySupplierCode(String supplierCode, Pageable pageable);
    Page<SupplierClaim> findByStatus(String status, Pageable pageable);
}
