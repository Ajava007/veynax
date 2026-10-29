package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.DamageRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DamageRecordRepository extends JpaRepository<DamageRecord, Long> {
    Page<DamageRecord> findByPoNumber(String poNumber, Pageable pageable);
}
