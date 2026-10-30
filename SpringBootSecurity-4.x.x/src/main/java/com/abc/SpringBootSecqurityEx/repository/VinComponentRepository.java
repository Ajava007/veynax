package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.VinComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface VinComponentRepository extends JpaRepository<VinComponent, Long> {
    List<VinComponent> findByVin(String vin);
    List<VinComponent> findBySerialNumber(String serialNumber);
}