package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "refund", indexes = {@Index(name="idx_refund_dealer", columnList="dealer_code")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "refund_number", nullable = false, unique = true, length = 40) private String refundNumber;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "refund_date", nullable = false) private LocalDate refundDate;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount = BigDecimal.ZERO;
    @Column(length = 500) private String reason;
    @Column(nullable = false, length = 20) private String status = "PENDING"; // PENDING, APPROVED, PAID, REJECTED
}