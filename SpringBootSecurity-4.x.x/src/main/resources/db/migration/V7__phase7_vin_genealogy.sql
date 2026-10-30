-- Phase 7: VIN genealogy + work order actuals
CREATE TABLE IF NOT EXISTS vin_record (
    id BIGSERIAL PRIMARY KEY,
    vin VARCHAR(17) NOT NULL UNIQUE,
    wo_number VARCHAR(30) NOT NULL,
    model_code VARCHAR(40) NOT NULL,
    variant VARCHAR(60),
    color VARCHAR(30),
    bom_revision VARCHAR(10),
    line_code VARCHAR(30),
    build_date DATE,
    completed_at TIMESTAMP WITH TIME ZONE,
    quality_hold BOOLEAN NOT NULL DEFAULT FALSE,
    hold_reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_BUILD',
    released_by VARCHAR(100),
    released_at TIMESTAMP WITH TIME ZONE,
    allocated_to_dealer VARCHAR(30),
    delivered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_vin_wo ON vin_record (wo_number);
CREATE INDEX IF NOT EXISTS idx_vin_model ON vin_record (model_code);
CREATE INDEX IF NOT EXISTS idx_vin_status ON vin_record (status);

CREATE TABLE IF NOT EXISTS vin_component (
    id BIGSERIAL PRIMARY KEY,
    vin VARCHAR(17) NOT NULL,
    part_code VARCHAR(40) NOT NULL,
    part_name VARCHAR(150),
    serial_number VARCHAR(60),
    batch_number VARCHAR(40),
    supplier_code VARCHAR(30),
    po_number VARCHAR(30),
    shipment_number VARCHAR(40),
    container_number VARCHAR(20),
    installed_at_workstation VARCHAR(60),
    installed_by VARCHAR(100),
    installed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_vincomp_vin ON vin_component (vin);
CREATE INDEX IF NOT EXISTS idx_vincomp_part ON vin_component (part_code);
CREATE INDEX IF NOT EXISTS idx_vincomp_serial ON vin_component (serial_number);

ALTER TABLE work_order ADD COLUMN IF NOT EXISTS scrap_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS rework_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS actual_start TIMESTAMP WITH TIME ZONE;
ALTER TABLE work_order ADD COLUMN IF NOT EXISTS actual_finish TIMESTAMP WITH TIME ZONE;
