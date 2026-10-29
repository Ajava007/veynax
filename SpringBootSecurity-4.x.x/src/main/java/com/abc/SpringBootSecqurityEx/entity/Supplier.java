package com.abc.SpringBootSecqurityEx.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplier", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_code", columnNames = "code")
})
public class Supplier extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String company;

    @Column(length = 60)
    private String category;

    @Column(length = 60)
    private String country;

    @Column(precision = 3, scale = 1)
    private java.math.BigDecimal rating;

    @Column(length = 120)
    private String contactEmail;

    @Column(length = 30)
    private String phone;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, BLACKLISTED

    // --- Phase 3: procurement terms ---
    @Column(name = "lead_time_days") private Integer leadTimeDays;
    @Column(name = "payment_terms", length = 40) private String paymentTerms; // e.g. "NET 30", "50% ADVANCE"

}
