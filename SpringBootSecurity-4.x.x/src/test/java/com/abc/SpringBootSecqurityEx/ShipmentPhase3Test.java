package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentPhase3Test {

    @Test
    void shipmentWithContainers() {
        Shipment s = Shipment.builder().shipmentNumber("SHP-2026-00001")
            .poNumber("PO-001").supplierCode("SUP-001").vesselName("MV Ocean")
            .status("IN_TRANSIT").build();
        Container c = Container.builder().containerNumber("ABCD1234567")
            .sealNumber("SEAL123").containerType("40HQ").status("SEALED").build();
        assertEquals("SHP-2026-00001", s.getShipmentNumber());
        assertEquals("ABCD1234567", c.getContainerNumber());
    }

    @Test
    void landedCostTotals() {
        LandedCost lc = LandedCost.builder().freightCost(new BigDecimal("1000"))
            .insuranceCost(new BigDecimal("200")).dutyCost(new BigDecimal("500"))
            .handlingCost(new BigDecimal("100")).otherCost(BigDecimal.ZERO).build();
        BigDecimal total = lc.getFreightCost().add(lc.getInsuranceCost())
            .add(lc.getDutyCost()).add(lc.getHandlingCost()).add(lc.getOtherCost());
        assertEquals(new BigDecimal("1800"), total);
    }

    @Test
    void damageRecordLinksToClaim() {
        DamageRecord dmg = DamageRecord.builder().damageNumber("DMG-000001")
            .poNumber("PO-001").partCode("ENG-001").damagedQuantity(5)
            .damageType("CRUSHED").responsibleParty("CARRIER").status("QUARANTINED").build();
        SupplierClaim claim = SupplierClaim.builder().claimNumber("CLM-000001")
            .supplierCode("SUP-001").damageRecordId(1L).claimedAmount(new BigDecimal("500"))
            .status("OPEN").build();
        dmg.setClaimId(claim.getId());
        assertEquals("QUARANTINED", dmg.getStatus());
        assertEquals("OPEN", claim.getStatus());
    }
}
