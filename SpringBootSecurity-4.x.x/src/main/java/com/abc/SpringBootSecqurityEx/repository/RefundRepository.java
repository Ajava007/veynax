package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Page<Refund> findByStatus(String status, Pageable pageable);
}