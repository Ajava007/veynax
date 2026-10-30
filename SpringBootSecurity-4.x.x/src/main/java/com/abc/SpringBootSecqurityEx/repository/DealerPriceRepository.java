package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.DealerPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DealerPriceRepository extends JpaRepository<DealerPrice, Long> {
    List<DealerPrice> findByDealerCodeAndStatus(String dealerCode, String status);
    Optional<DealerPrice> findFirstByDealerCodeAndModelCodeAndStatusOrderByCreatedAtDesc(String dealerCode, String modelCode, String status);
}