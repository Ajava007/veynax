package com.abc.SpringBootSecqurityEx.repository;

import com.abc.SpringBootSecqurityEx.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    boolean existsByWoNumber(String woNumber);
    List<WorkOrder> findByLineCodeAndStatus(String lineCode, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wo from WorkOrder wo where wo.id = :id")
    Optional<WorkOrder> findByIdForUpdate(@Param("id") Long id);
}
