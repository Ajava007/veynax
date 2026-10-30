package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "receipt", indexes = {@Index(name="idx_receipt_dealer", columnList="dealer_code"), @Index(name="idx_receipt_invoice", columnList="invoice_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Receipt extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "receipt_number", nullable = false, unique = true, length = 40) private String receiptNumber;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "invoice_id") private Long invoiceId;
    @Column(name = "receipt_date", nullable = false) private LocalDate receiptDate;
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal amount = BigDecimal.ZERO;
    @Column(nullable = false, length = 30) private String method; // CASH, BANK_TRANSFER, CHEQUE, CREDIT_NOTE
    @Column(length = 100) private String reference;
    @Column(nullable = false, length = 20) private String status = "RECORDED"; // RECORDED, APPLIED, BOUNCED
}