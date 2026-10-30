package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByStatus(String status, Pageable pageable);
}