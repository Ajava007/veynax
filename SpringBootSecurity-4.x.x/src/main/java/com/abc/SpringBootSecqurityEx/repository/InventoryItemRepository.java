package com.abc.SpringBootSecqurityEx.repository;

import com.abc.SpringBootSecqurityEx.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByPartCode(String partCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<InventoryItem> findByPartCodeAndWarehouseAndLotNumber(String partCode, String warehouse, String lotNumber);
}
