package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warranty_policy", indexes = {@Index(name="idx_warranty_model", columnList="model_code")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarrantyPolicy extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(name = "coverage_months", nullable = false) private Integer coverageMonths = 36;
    @Column(name = "coverage_km", nullable = false) private Integer coverageKm = 100000;
    @Column(name = "coverage_items", length = 1000) private String coverageItems; // comma-separated: ENGINE,TRANSMISSION,...
    @Column(nullable = false, length = 20) private String status = "ACTIVE"; // ACTIVE, SUPERSEDED
}