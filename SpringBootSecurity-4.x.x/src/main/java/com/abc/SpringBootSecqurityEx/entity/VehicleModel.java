package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vehicle_model", uniqueConstraints = {
        @UniqueConstraint(name = "uk_vehicle_model_code", columnNames = "model_code")
})
public class VehicleModel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_code", nullable = false, length = 30)
    private String modelCode;

    @Column(name = "model_name", nullable = false, length = 150)
    private String modelName;

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(name = "vehicle_type", length = 40)
    private String vehicleType;

    @Column(length = 40)
    private String segment;

    @Column(name = "model_year", length = 10)
    private String modelYear;

    @Column(name = "fuel_type", length = 30)
    private String fuelType;

    @Column(length = 30)
    private String transmission;

    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    // --- Phase 2: variant / sourcing / line assignment ---
    @Column(length = 60)
    private String variant;

    @Column(length = 60)
    private String trimLevel;

    @Column(name = "color_options", length = 500)
    private String colorOptions; // comma-separated, e.g. "Pearl White,Black,Red"

    @Column(name = "option_packages", length = 1000)
    private String optionPackages; // comma-separated option/accessory codes

    @Column(name = "sourcing_eligibility", length = 10)
    private String sourcingEligibility = "CKD"; // CKD, SKD, BOTH, LOCAL

    @Column(name = "assembly_line_code", length = 30)
    private String assemblyLineCode; // model-to-line assignment

    @Column(name = "production_status", length = 20)
    private String productionStatus = "PLANNED"; // PLANNED, IN_PRODUCTION, PHASED_OUT, DISCONTINUED

}
