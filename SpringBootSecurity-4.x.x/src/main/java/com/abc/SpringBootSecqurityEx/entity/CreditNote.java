package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "credit_note", indexes = {@Index(name="idx_credit_invoice", columnList="invoice_id"), @Index(name="idx_credit_dealer", columnList="dealer_code")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditNote extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "credit_note_number", nullable = false, unique = true, length = 40) private String creditNoteNumber;
    @Column(name = "invoice_id") private Long invoiceId;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "credit_date", nullable = false) private LocalDate creditDate;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount = BigDecimal.ZERO;
    @Column(length = 500) private String reason;
    @Column(nullable = false, length = 20) private String status = "DRAFT"; // DRAFT, ISSUED, APPLIED, VOID
}