package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "damage_record", indexes = {@Index(name="idx_damage_po", columnList="po_number"), @Index(name="idx_damage_shipment", columnList="shipment_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DamageRecord extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "damage_number", nullable = false, unique = true, length = 40) private String damageNumber;
    @Column(name = "po_number", nullable = false, length = 30) private String poNumber;
    @Column(name = "shipment_id") private Long shipmentId;
    @Column(name = "container_number", length = 20) private String containerNumber;
    @Column(name = "part_code", nullable = false, length = 40) private String partCode;
    @Column(nullable = false) private Integer damagedQuantity = 0;
    @Column(name = "damage_type", length = 40) private String damageType; // CRUSHED, WET, STOLEN, SHORTAGE, WRONG_PART
    @Column(name = "responsible_party", length = 30) private String responsibleParty; // SUPPLIER, CARRIER, INSURANCE, PORT
    @Column(name = "estimated_loss", precision = 18, scale = 2) private BigDecimal estimatedLoss = BigDecimal.ZERO;
    @Column(name = "evidence_url", length = 500) private String evidenceUrl; // photo reference
    @Column(nullable = false, length = 20) private String status = "REPORTED"; // REPORTED, QUARANTINED, CLAIM_FILED, RESOLVED, WRITTEN_OFF
    @Column(name = "claim_id") private Long claimId; // auto-linked to SupplierClaim
    @Column(length = 500) private String remarks;
}
