package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContainerRepository extends JpaRepository<Container, Long> {
    List<Container> findByShipmentId(Long shipmentId);
}
