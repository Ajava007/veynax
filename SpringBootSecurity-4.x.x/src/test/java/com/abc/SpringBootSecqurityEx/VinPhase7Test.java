package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.VinRecord;
import com.abc.SpringBootSecqurityEx.entity.VinComponent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VinPhase7Test {
    @Test
    void vinLifecycleStates() {
        VinRecord v = VinRecord.builder().vin("JTD12345678901234")
            .woNumber("WO-001").modelCode("ALLION").status("IN_BUILD").build();
        assertEquals(17, v.getVin().length());
        v.setStatus("BUILT");
        v.setQualityHold(true);
        v.setHoldReason("Failed brake test");
        v.setStatus("QUALITY_HOLD");
        assertTrue(v.isQualityHold());
        assertEquals("QUALITY_HOLD", v.getStatus());
        v.setQualityHold(false);
        v.setStatus("RELEASED");
        v.setAllocatedToDealer("DLR-001");
        v.setStatus("ALLOCATED");
        v.setStatus("DELIVERED");
        assertEquals("DELIVERED", v.getStatus());
    }

    @Test
    void componentGenealogyTracesToSupplier() {
        VinComponent c = VinComponent.builder().vin("JTD12345678901234")
            .partCode("ENG-001").serialNumber("SN-ENG-999")
            .supplierCode("SUP-001").poNumber("PO-001")
            .shipmentNumber("SHP-2026-00001").containerNumber("ABCD1234567")
            .installedAtWorkstation("WS-ENG-01").build();
        assertEquals("SUP-001", c.getSupplierCode());
        assertEquals("SHP-2026-00001", c.getShipmentNumber());
        assertEquals("ABCD1234567", c.getContainerNumber());
    }

    @Test
    void qualityHoldBlocksDelivery() {
        VinRecord v = new VinRecord();
        v.setQualityHold(true);
        v.setStatus("QUALITY_HOLD");
        assertTrue(v.isQualityHold(), "Vehicle on hold cannot be delivered");
        assertThrows(IllegalStateException.class, () -> {
            if (v.isQualityHold()) throw new IllegalStateException("Vehicle on quality hold - cannot deliver");
        });
    }
}
