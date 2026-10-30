package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "warranty_claim", indexes = {@Index(name="idx_wc_vin", columnList="vin"), @Index(name="idx_wc_status", columnList="status")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WarrantyClaim extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "claim_number", nullable = false, unique = true, length = 40) private String claimNumber;
    @Column(name = "vin", nullable = false, length = 17) private String vin;
    @Column(name = "part_code", length = 40) private String partCode;
    @Column(name = "defect_description", length = 1000) private String defectDescription;
    @Column(name = "claim_amount", precision = 18, scale = 2) private BigDecimal claimAmount = BigDecimal.ZERO;
    @Column(name = "approved_amount", precision = 18, scale = 2) private BigDecimal approvedAmount = BigDecimal.ZERO;
    @Column(name = "claim_date", nullable = false) private LocalDate claimDate;
    @Column(name = "resolved_date") private LocalDate resolvedDate;
    @Column(nullable = false, length = 20) private String status = "OPEN"; // OPEN, UNDER_REVIEW, APPROVED, REJECTED, PAID, CLOSED
    @Column(name = "reject_reason", length = 500) private String rejectReason;
    @Column(length = 500) private String remarks;
}