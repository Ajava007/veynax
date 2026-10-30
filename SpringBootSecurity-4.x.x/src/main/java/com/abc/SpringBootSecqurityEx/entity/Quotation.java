package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "quotation", indexes = {
    @Index(name="idx_quotation_dealer", columnList="dealer_code"),
    @Index(name="idx_quotation_status", columnList="status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quotation extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "quotation_number", nullable = false, unique = true, length = 40) private String quotationNumber;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "model_code", nullable = false, length = 40) private String modelCode;
    @Column(length = 60) private String variant;
    @Column(length = 60) private String color;
    @Column(nullable = false) private Integer quantity = 1;
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2) private BigDecimal unitPrice;
    @Column(name = "accessories_cost", precision = 18, scale = 2) private BigDecimal accessoriesCost = BigDecimal.ZERO;
    @Column(name = "transport_cost", precision = 18, scale = 2) private BigDecimal transportCost = BigDecimal.ZERO;
    @Column(name = "tax_amount", precision = 18, scale = 2) private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2) private BigDecimal totalAmount;
    @Column(nullable = false, length = 20) private String status = "DRAFT"; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CONVERTED, EXPIRED
    @Column(name = "valid_until") private LocalDate validUntil;
    @Column(name = "approved_by", length = 100) private String approvedBy;
    @Column(name = "approved_at") private LocalDate approvedAt;
    @Column(name = "reject_reason", length = 500) private String rejectReason;
    @Column(name = "converted_so_number", length = 30) private String convertedSoNumber;
    @Column(length = 500) private String remarks;
}
