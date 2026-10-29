-- Phase 2: Vehicle master data enhancements + audit log table (from Phase 1)
-- Run after V1 baseline. Safe for existing databases (IF NOT EXISTS / ADD COLUMN IF NOT EXISTS).

-- Audit log table (added in Phase 1, captured here for fresh DBs)
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    ip_address VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON audit_log (actor);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_log (timestamp);

-- Vehicle model: variant / sourcing / line assignment
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS variant VARCHAR(60);
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS trim_level VARCHAR(60);
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS color_options VARCHAR(500);
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS option_packages VARCHAR(1000);
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS sourcing_eligibility VARCHAR(10) DEFAULT 'CKD';
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS assembly_line_code VARCHAR(30);
ALTER TABLE vehicle_model ADD COLUMN IF NOT EXISTS production_status VARCHAR(20) DEFAULT 'PLANNED';

-- BOM row: revision / approval / substitute
ALTER TABLE bom_row ADD COLUMN IF NOT EXISTS bom_revision VARCHAR(10) DEFAULT 'A';
ALTER TABLE bom_row ADD COLUMN IF NOT EXISTS approved BOOLEAN DEFAULT FALSE;
ALTER TABLE bom_row ADD COLUMN IF NOT EXISTS substitute_part_code VARCHAR(40);

-- Routing operation: line / workstation
ALTER TABLE routing_operation ADD COLUMN IF NOT EXISTS line_code VARCHAR(30);
ALTER TABLE routing_operation ADD COLUMN IF NOT EXISTS workstation VARCHAR(60);

-- Part: approved substitute
ALTER TABLE part ADD COLUMN IF NOT EXISTS substitute_part_code VARCHAR(40);
