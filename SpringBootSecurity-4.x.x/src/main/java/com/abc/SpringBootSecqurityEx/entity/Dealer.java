package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dealer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dealer_code", columnNames = "code")
})
public class Dealer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 120)
    private String contactEmail;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    // --- Phase 5: commercial terms ---
    @Column(name = "credit_limit", precision = 18, scale = 2) private java.math.BigDecimal creditLimit = java.math.BigDecimal.ZERO;
    @Column(name = "advance_payment_pct", precision = 5, scale = 2) private java.math.BigDecimal advancePaymentPct = new java.math.BigDecimal("30.00");
    @Column(name = "payment_terms", length = 40) private String paymentTerms = "NET 90";
    @Column(name = "account_balance", precision = 18, scale = 2) private java.math.BigDecimal accountBalance = java.math.BigDecimal.ZERO;

}
