package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.LandedCost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface LandedCostRepository extends JpaRepository<LandedCost, Long> {
    Optional<LandedCost> findByShipmentId(Long shipmentId);
}
