package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.VinRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface VinRecordRepository extends JpaRepository<VinRecord, Long> {
    Optional<VinRecord> findByVin(String vin);
    Page<VinRecord> findByWoNumber(String woNumber, Pageable pageable);
    Page<VinRecord> findByStatus(String status, Pageable pageable);
    Page<VinRecord> findByModelCode(String modelCode, Pageable pageable);
}