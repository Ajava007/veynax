package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.Invoice;
import com.abc.SpringBootSecqurityEx.entity.WarrantyClaim;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FinancePhase8Test {
    @Test
    void invoicePaymentReconciliation() {
        Invoice inv = Invoice.builder().invoiceNumber("INV-2026-000001")
            .totalAmount(new BigDecimal("100000")).amountPaid(BigDecimal.ZERO)
            .creditApplied(BigDecimal.ZERO).balanceDue(new BigDecimal("100000"))
            .status("ISSUED").dueDate(LocalDate.now().plusDays(90)).build();
        BigDecimal payment = new BigDecimal("40000");
        inv.setAmountPaid(inv.getAmountPaid().add(payment));
        BigDecimal balance = inv.getTotalAmount().subtract(inv.getAmountPaid()).subtract(inv.getCreditApplied());
        inv.setBalanceDue(balance);
        assertEquals(new BigDecimal("60000"), balance);
        assertEquals("ISSUED", inv.getStatus());
        inv.setStatus("PARTIAL_PAID");
        assertEquals("PARTIAL_PAID", inv.getStatus());
    }

    @Test
    void creditNoteReducesBalance() {
        Invoice inv = Invoice.builder().totalAmount(new BigDecimal("100000"))
            .amountPaid(new BigDecimal("50000")).creditApplied(BigDecimal.ZERO)
            .balanceDue(new BigDecimal("50000")).build();
        BigDecimal credit = new BigDecimal("10000");
        inv.setCreditApplied(inv.getCreditApplied().add(credit));
        BigDecimal balance = inv.getTotalAmount().subtract(inv.getAmountPaid()).subtract(inv.getCreditApplied());
        assertEquals(new BigDecimal("40000"), balance);
    }

    @Test
    void warrantyClaimLinksToVin() {
        WarrantyClaim c = WarrantyClaim.builder().claimNumber("WC-000001")
            .vin("JTD12345678901234").partCode("ENG-001")
            .claimAmount(new BigDecimal("2500")).status("OPEN").build();
        assertEquals("JTD12345678901234", c.getVin());
        assertEquals("ENG-001", c.getPartCode());
        c.setApprovedAmount(new BigDecimal("2000"));
        c.setStatus("APPROVED");
        assertEquals("APPROVED", c.getStatus());
        assertEquals(new BigDecimal("2000"), c.getApprovedAmount());
    }
}
