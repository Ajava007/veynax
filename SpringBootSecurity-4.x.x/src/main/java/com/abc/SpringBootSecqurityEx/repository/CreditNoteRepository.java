package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.CreditNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CreditNoteRepository extends JpaRepository<CreditNote, Long> {
    Page<CreditNote> findByStatus(String status, Pageable pageable);
}