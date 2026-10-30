package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.MaterialReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MaterialReservationRepository extends JpaRepository<MaterialReservation, Long> {
    Optional<MaterialReservation> findByReservationNumber(String reservationNumber);
    Page<MaterialReservation> findByPartCode(String partCode, Pageable pageable);
    Page<MaterialReservation> findByWorkOrderNumber(String workOrderNumber, Pageable pageable);
    Page<MaterialReservation> findByStatus(String status, Pageable pageable);
}
