package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.WarrantyPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, Long> {
    Page<WarrantyPolicy> findByStatus(String status, Pageable pageable);
}