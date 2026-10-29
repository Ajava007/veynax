package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.CustomsDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CustomsDeclarationRepository extends JpaRepository<CustomsDeclaration, Long> {
    Optional<CustomsDeclaration> findByShipmentId(Long shipmentId);
}
