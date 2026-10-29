package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "landed_cost", indexes = {@Index(name="idx_landed_shipment", columnList="shipment_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LandedCost extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shipment_id", nullable = false) private Long shipmentId;
    @Column(name = "freight_cost", precision = 18, scale = 2) private BigDecimal freightCost = BigDecimal.ZERO;
    @Column(name = "insurance_cost", precision = 18, scale = 2) private BigDecimal insuranceCost = BigDecimal.ZERO;
    @Column(name = "duty_cost", precision = 18, scale = 2) private BigDecimal dutyCost = BigDecimal.ZERO;
    @Column(name = "handling_cost", precision = 18, scale = 2) private BigDecimal handlingCost = BigDecimal.ZERO;
    @Column(name = "other_cost", precision = 18, scale = 2) private BigDecimal otherCost = BigDecimal.ZERO;
    @Column(name = "currency", length = 3) private String currency = "USD";
    @Column(name = "allocation_method", length = 20) private String allocationMethod = "BY_VALUE"; // BY_VALUE, BY_WEIGHT, BY_QUANTITY
    @Column(name = "total_cost", precision = 18, scale = 2) private BigDecimal totalCost = BigDecimal.ZERO;
    @Column(nullable = false) private boolean reconciled = false;
}
