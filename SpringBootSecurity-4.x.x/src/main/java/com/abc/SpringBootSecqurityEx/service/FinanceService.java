package com.abc.SpringBootSecqurityEx.service;

import com.abc.SpringBootSecqurityEx.entity.*;
import com.abc.SpringBootSecqurityEx.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FinanceService {

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    private final InvoiceRepository invoiceRepo;
    private final CreditNoteRepository creditRepo;
    private final ReceiptRepository receiptRepo;
    private final RefundRepository refundRepo;
    private final WarrantyClaimRepository warrantyRepo;
    private final SalesOrderRepository salesOrderRepo;

    public FinanceService(InvoiceRepository invoiceRepo, CreditNoteRepository creditRepo,
            ReceiptRepository receiptRepo, RefundRepository refundRepo,
            WarrantyClaimRepository warrantyRepo, SalesOrderRepository salesOrderRepo) {
        this.invoiceRepo = invoiceRepo; this.creditRepo = creditRepo;
        this.receiptRepo = receiptRepo; this.refundRepo = refundRepo;
        this.warrantyRepo = warrantyRepo; this.salesOrderRepo = salesOrderRepo;
    }

    public Page<Invoice> invoices(Pageable p) { return invoiceRepo.findAll(p); }
    public Page<Invoice> invoicesByDealer(String dealer, Pageable p) { return invoiceRepo.findByDealerCode(dealer, p); }
    public Invoice invoiceByNumber(String n) { return invoiceRepo.findByInvoiceNumber(n).orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + n)); }
    public Page<WarrantyClaim> warrantyClaims(Pageable p) { return warrantyRepo.findAll(p); }
    public Page<WarrantyClaim> warrantyByVin(String vin, Pageable p) { return warrantyRepo.findByVin(vin, p); }

    @Transactional
    public Invoice generateInvoice(Long salesOrderId) {
        SalesOrder so = salesOrderRepo.findById(salesOrderId)
            .orElseThrow(() -> new EntityNotFoundException("Sales order not found: " + salesOrderId));
        String invNum = "INV-" + LocalDate.now().getYear() + "-" + String.format("%06d", SEQ.incrementAndGet());
        BigDecimal total = so.getTotalAmount() != null ? so.getTotalAmount() : BigDecimal.ZERO;
        Invoice inv = Invoice.builder()
            .invoiceNumber(invNum).salesOrderId(so.getId()).dealerCode(so.getDealerCode())
            .invoiceDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(90))
            .subtotal(total).taxAmount(BigDecimal.ZERO).totalAmount(total)
            .amountPaid(so.getAdvanceReceived() != null ? so.getAdvanceReceived() : BigDecimal.ZERO)
            .balanceDue(total.subtract(so.getAdvanceReceived() != null ? so.getAdvanceReceived() : BigDecimal.ZERO))
            .status("ISSUED").build();
        return invoiceRepo.save(inv);
    }

    @Transactional
    public Receipt applyPayment(String invoiceNumber, BigDecimal amount, String method, String reference) {
        Invoice inv = invoiceByNumber(invoiceNumber);
        if ("PAID".equals(inv.getStatus()) || "VOID".equals(inv.getStatus())) {
            throw new IllegalStateException("Invoice is " + inv.getStatus() + " - cannot apply payment");
        }
        BigDecimal newPaid = inv.getAmountPaid().add(amount);
        inv.setAmountPaid(newPaid);
        BigDecimal balance = inv.getTotalAmount().subtract(newPaid).subtract(inv.getCreditApplied());
        inv.setBalanceDue(balance.max(BigDecimal.ZERO));
        if (balance.compareTo(BigDecimal.ZERO) <= 0) inv.setStatus("PAID");
        else if (newPaid.compareTo(BigDecimal.ZERO) > 0) inv.setStatus("PARTIAL_PAID");
        if (inv.getDueDate().isBefore(LocalDate.now()) && balance.compareTo(BigDecimal.ZERO) > 0) inv.setStatus("OVERDUE");
        invoiceRepo.save(inv);

        Receipt r = Receipt.builder()
            .receiptNumber("RCP-" + String.format("%06d", SEQ.incrementAndGet()))
            .dealerCode(inv.getDealerCode()).invoiceId(inv.getId())
            .receiptDate(LocalDate.now()).amount(amount).method(method).reference(reference)
            .status("APPLIED").build();
        return receiptRepo.save(r);
    }

    @Transactional
    public CreditNote applyCreditNote(String invoiceNumber, BigDecimal amount, String reason) {
        Invoice inv = invoiceByNumber(invoiceNumber);
        CreditNote cn = CreditNote.builder()
            .creditNoteNumber("CN-" + String.format("%06d", SEQ.incrementAndGet()))
            .invoiceId(inv.getId()).dealerCode(inv.getDealerCode())
            .creditDate(LocalDate.now()).amount(amount).reason(reason).status("APPLIED").build();
        cn = creditRepo.save(cn);
        inv.setCreditApplied(inv.getCreditApplied().add(amount));
        BigDecimal balance = inv.getTotalAmount().subtract(inv.getAmountPaid()).subtract(inv.getCreditApplied());
        inv.setBalanceDue(balance.max(BigDecimal.ZERO));
        if (balance.compareTo(BigDecimal.ZERO) <= 0) inv.setStatus("PAID");
        invoiceRepo.save(inv);
        return cn;
    }

    @Transactional
    public Refund issueRefund(String dealerCode, BigDecimal amount, String reason) {
        Refund r = Refund.builder()
            .refundNumber("RFD-" + String.format("%06d", SEQ.incrementAndGet()))
            .dealerCode(dealerCode).refundDate(LocalDate.now())
            .amount(amount).reason(reason).status("PENDING").build();
        return refundRepo.save(r);
    }

    @Transactional
    public WarrantyClaim fileWarrantyClaim(String vin, String partCode, String defect, BigDecimal amount) {
        WarrantyClaim c = WarrantyClaim.builder()
            .claimNumber("WC-" + String.format("%06d", SEQ.incrementAndGet()))
            .vin(vin).partCode(partCode).defectDescription(defect)
            .claimAmount(amount).claimDate(LocalDate.now()).status("OPEN").build();
        return warrantyRepo.save(c);
    }

    @Transactional
    public WarrantyClaim approveWarrantyClaim(Long claimId, BigDecimal approvedAmount) {
        WarrantyClaim c = warrantyRepo.findById(claimId)
            .orElseThrow(() -> new EntityNotFoundException("Claim not found: " + claimId));
        if (!"OPEN".equals(c.getStatus()) && !"UNDER_REVIEW".equals(c.getStatus())) {
            throw new IllegalStateException("Claim is " + c.getStatus() + " - cannot approve");
        }
        c.setApprovedAmount(approvedAmount);
        c.setStatus("APPROVED");
        c.setResolvedDate(LocalDate.now());
        return warrantyRepo.save(c);
    }
}
