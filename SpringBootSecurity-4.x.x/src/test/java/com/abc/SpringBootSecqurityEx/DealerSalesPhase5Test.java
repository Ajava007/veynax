package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.Quotation;
import com.abc.SpringBootSecqurityEx.entity.Dealer;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class DealerSalesPhase5Test {
    @Test
    void quotationWorkflowStates() {
        Quotation q = Quotation.builder().quotationNumber("QTN-2026-00001")
            .dealerCode("DLR-001").modelCode("ALLION").quantity(2)
            .unitPrice(new BigDecimal("25000")).totalAmount(new BigDecimal("50000"))
            .status("DRAFT").build();
        assertEquals("DRAFT", q.getStatus());
        q.setStatus("PENDING_APPROVAL");
        q.setStatus("APPROVED");
        q.setConvertedSoNumber("SO-000001");
        q.setStatus("CONVERTED");
        assertEquals("CONVERTED", q.getStatus());
        assertEquals("SO-000001", q.getConvertedSoNumber());
    }

    @Test
    void advancePaymentCalculation() {
        BigDecimal total = new BigDecimal("100000");
        BigDecimal pct = new BigDecimal("30");
        BigDecimal advance = total.multiply(pct).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("30000.00"), advance);
    }

    @Test
    void dealerCommercialTerms() {
        Dealer d = new Dealer();
        d.setCode("DLR-001");
        d.setAdvancePaymentPct(new BigDecimal("50"));
        d.setCreditLimit(new BigDecimal("500000"));
        d.setPaymentTerms("NET 60");
        assertEquals(new BigDecimal("50"), d.getAdvancePaymentPct());
        assertEquals("NET 60", d.getPaymentTerms());
    }
}
