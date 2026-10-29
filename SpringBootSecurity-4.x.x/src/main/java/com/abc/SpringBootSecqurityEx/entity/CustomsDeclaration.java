package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customs_declaration", indexes = {@Index(name="idx_customs_shipment", columnList="shipment_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomsDeclaration extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shipment_id", nullable = false) private Long shipmentId;
    @Column(name = "declaration_number", nullable = false, length = 60) private String declarationNumber;
    @Column(name = "declaration_date") private LocalDate declarationDate;
    @Column(name = "release_date") private LocalDate releaseDate;
    @Column(name = "duty_amount", precision = 18, scale = 2) private BigDecimal dutyAmount = BigDecimal.ZERO;
    @Column(name = "vat_amount", precision = 18, scale = 2) private BigDecimal vatAmount = BigDecimal.ZERO;
    @Column(name = "other_charges", precision = 18, scale = 2) private BigDecimal otherCharges = BigDecimal.ZERO;
    @Column(nullable = false, length = 20) private String status = "SUBMITTED"; // SUBMITTED, UNDER_REVIEW, HELD, RELEASED, REJECTED
    @Column(name = "held_reason", length = 500) private String heldReason;
    @Column(length = 500) private String remarks;
}
