package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.VehicleModel;
import com.abc.SpringBootSecqurityEx.entity.BomRow;
import com.abc.SpringBootSecqurityEx.entity.Part;
import com.abc.SpringBootSecqurityEx.entity.RoutingOperation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MasterDataPhase2Test {

    @Test
    void vehicleModelHasSourcingAndLine() {
        VehicleModel m = new VehicleModel();
        m.setSourcingEligibility("CKD");
        m.setAssemblyLineCode("LINE-A");
        m.setProductionStatus("IN_PRODUCTION");
        m.setVariant("GLI");
        m.setTrimLevel("High");
        assertEquals("CKD", m.getSourcingEligibility());
        assertEquals("LINE-A", m.getAssemblyLineCode());
        assertEquals("IN_PRODUCTION", m.getProductionStatus());
        assertEquals("GLI", m.getVariant());
        assertEquals("High", m.getTrimLevel());
    }

    @Test
    void bomRowHasRevisionAndApproval() {
        BomRow b = new BomRow();
        b.setBomRevision("B");
        b.setApproved(true);
        b.setSubstitutePartCode("SUB-001");
        assertEquals("B", b.getBomRevision());
        assertTrue(b.isApproved());
        assertEquals("SUB-001", b.getSubstitutePartCode());
    }

    @Test
    void partAndRoutingHaveNewFields() {
        Part p = new Part();
        p.setSubstitutePartCode("ALT-001");
        assertEquals("ALT-001", p.getSubstitutePartCode());

        RoutingOperation r = new RoutingOperation();
        r.setLineCode("LINE-A");
        r.setWorkstation("WS-12");
        assertEquals("LINE-A", r.getLineCode());
        assertEquals("WS-12", r.getWorkstation());
    }
}
