package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "forecast", indexes = {
    @Index(name="idx_forecast_period", columnList="period_start,period_end"),
    @Index(name="idx_forecast_model", columnList="model_code"),
    @Index(name="idx_forecast_status", columnList="status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Forecast extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "forecast_number", nullable = false, unique = true, length = 40) private String forecastNumber;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(length = 60) private String variant;
    @Column(name = "historical_demand", nullable = false) private Integer historicalDemand = 0;
    @Column(name = "dealer_forecast", nullable = false) private Integer dealerForecast = 0;
    @Column(name = "manager_adjustment", nullable = false) private Integer managerAdjustment = 0;
    @Column(name = "adjustment_reason", length = 500) private String adjustmentReason;
    @Column(name = "approved_by", length = 100) private String approvedBy;
    @Column(name = "final_forecast", nullable = false) private Integer finalForecast = 0;
    @Column(nullable = false, length = 20) private String status = "DRAFT"; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, SUPERSEDED
    @Column(length = 500) private String remarks;
}
