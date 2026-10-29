package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.audit.AuditLog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    @Test
    void builderSetsFields() {
        AuditLog log = AuditLog.builder()
            .actor("admin").entityType("WorkOrder").entityId("WO-001")
            .action("CREATE").afterState("{}").ipAddress("127.0.0.1").build();
        assertEquals("admin", log.getActor());
        assertEquals("CREATE", log.getAction());
        assertEquals("WO-001", log.getEntityId());
    }
}
