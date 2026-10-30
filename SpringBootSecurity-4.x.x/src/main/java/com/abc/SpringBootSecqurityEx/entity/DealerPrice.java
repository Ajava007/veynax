package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "dealer_price", indexes = {@Index(name="idx_price_dealer", columnList="dealer_code,model_code")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DealerPrice extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(length = 60) private String variant;
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2) private BigDecimal unitPrice;
    @Column(name = "effective_from") private LocalDate effectiveFrom;
    @Column(name = "effective_to") private LocalDate effectiveTo;
    @Column(nullable = false, length = 20) private String status = "ACTIVE"; // ACTIVE, EXPIRED
}
