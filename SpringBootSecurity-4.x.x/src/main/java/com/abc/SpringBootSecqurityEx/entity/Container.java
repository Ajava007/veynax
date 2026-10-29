package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "container", indexes = {@Index(name="idx_container_shipment", columnList="shipment_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Container extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "shipment_id", nullable = false) private Shipment shipment;
    @Column(name = "container_number", nullable = false, length = 20) private String containerNumber;
    @Column(name = "seal_number", length = 30) private String sealNumber;
    @Column(name = "container_type", length = 20) private String containerType; // 20GP, 40HQ, etc.
    @Column(name = "gross_weight_kg", precision = 12, scale = 2) private java.math.BigDecimal grossWeightKg;
    @Column(nullable = false, length = 20) private String status = "SEALED"; // SEALED, IN_TRANSIT, ARRIVED, UNLOADED, INSPECTED, DAMAGED, RETURNED
    @Column(length = 500) private String remarks;
}
