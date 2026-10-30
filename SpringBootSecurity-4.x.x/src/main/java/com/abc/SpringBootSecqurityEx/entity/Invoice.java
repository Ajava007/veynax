package com.abc.SpringBootSecqurityEx.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoice", indexes = {@Index(name="idx_invoice_so", columnList="sales_order_id"), @Index(name="idx_invoice_dealer", columnList="dealer_code"), @Index(name="idx_invoice_status", columnList="status")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "invoice_number", nullable = false, unique = true, length = 40) private String invoiceNumber;
    @Column(name = "sales_order_id") private Long salesOrderId;
    @Column(name = "dealer_code", nullable = false, length = 30) private String dealerCode;
    @Column(name = "invoice_date", nullable = false) private LocalDate invoiceDate;
    @Column(name = "due_date", nullable = false) private LocalDate dueDate;
    @Column(name = "subtotal", precision = 18, scale = 2) private BigDecimal subtotal = BigDecimal.ZERO;
    @Column(name = "tax_amount", precision = 18, scale = 2) private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2) private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(name = "amount_paid", precision = 18, scale = 2) private BigDecimal amountPaid = BigDecimal.ZERO;
    @Column(name = "credit_applied", precision = 18, scale = 2) private BigDecimal creditApplied = BigDecimal.ZERO;
    @Column(name = "balance_due", precision = 18, scale = 2) private BigDecimal balanceDue = BigDecimal.ZERO;
    @Column(nullable = false, length = 20) private String status = "DRAFT"; // DRAFT, ISSUED, PARTIAL_PAID, PAID, OVERDUE, VOID
    @Column(length = 500) private String remarks;
}