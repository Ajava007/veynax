package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    Page<Receipt> findByStatus(String status, Pageable pageable);
}