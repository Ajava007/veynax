package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_claim", indexes = {@Index(name="idx_claim_supplier", columnList="supplier_code")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SupplierClaim extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "claim_number", nullable = false, unique = true, length = 40) private String claimNumber;
    @Column(name = "supplier_code", nullable = false, length = 30) private String supplierCode;
    @Column(name = "damage_record_id", nullable = false) private Long damageRecordId;
    @Column(name = "claimed_amount", precision = 18, scale = 2) private BigDecimal claimedAmount = BigDecimal.ZERO;
    @Column(name = "recovered_amount", precision = 18, scale = 2) private BigDecimal recoveredAmount = BigDecimal.ZERO;
    @Column(name = "credit_note_ref", length = 60) private String creditNoteRef;
    @Column(name = "filed_date") private LocalDate filedDate;
    @Column(name = "resolved_date") private LocalDate resolvedDate;
    @Column(nullable = false, length = 20) private String status = "OPEN"; // OPEN, NEGOTIATING, CREDIT_NOTED, RECOVERED, CLOSED, WRITTEN_OFF
    @Column(length = 500) private String remarks;
}
