package com.abc.SpringBootSecqurityEx.repository;
import com.abc.SpringBootSecqurityEx.entity.Forecast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    Optional<Forecast> findByForecastNumber(String forecastNumber);
    Page<Forecast> findByModelCode(String modelCode, Pageable pageable);
    Page<Forecast> findByStatus(String status, Pageable pageable);
}