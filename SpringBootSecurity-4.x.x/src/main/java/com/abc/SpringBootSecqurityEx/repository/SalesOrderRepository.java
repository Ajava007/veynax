package com.abc.SpringBootSecqurityEx.repository;

import com.abc.SpringBootSecqurityEx.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsBySoNumber(String soNumber);
    Optional<SalesOrder> findByVinAndStatusNot(String vin, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select so from SalesOrder so where so.id = :id")
    Optional<SalesOrder> findByIdForUpdate(@Param("id") Long id);
}
