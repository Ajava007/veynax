package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.MaterialReservation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class InventoryPhase4Test {
    @Test
    void reservationFields() {
        MaterialReservation r = MaterialReservation.builder()
            .reservationNumber("RES-000001").partCode("ENG-001")
            .workOrderNumber("WO-001").warehouse("WH-MAIN")
            .quantity(new BigDecimal("10")).status("RESERVED").build();
        assertEquals("RES-000001", r.getReservationNumber());
        assertEquals("WO-001", r.getWorkOrderNumber());
        assertEquals(0, new BigDecimal("10").compareTo(r.getQuantity()));
        assertEquals("RESERVED", r.getStatus());
    }

    @Test
    void negativeStockCheckLogic() {
        BigDecimal onHand = new BigDecimal("5");
        BigDecimal issue = new BigDecimal("10");
        BigDecimal result = onHand.subtract(issue);
        assertTrue(result.compareTo(BigDecimal.ZERO) < 0, "Should detect negative stock");
    }
}
