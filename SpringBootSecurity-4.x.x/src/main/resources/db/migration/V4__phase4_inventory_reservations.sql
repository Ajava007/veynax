-- Phase 4: Material reservations + inventory enhancements
CREATE TABLE IF NOT EXISTS material_reservation (
    id BIGSERIAL PRIMARY KEY,
    reservation_number VARCHAR(40) NOT NULL UNIQUE,
    part_code VARCHAR(40) NOT NULL,
    work_order_number VARCHAR(30),
    warehouse VARCHAR(60) NOT NULL,
    lot_number VARCHAR(40),
    quantity DECIMAL(18,4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    required_by DATE,
    reserved_by VARCHAR(100),
    remark VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_reservation_part ON material_reservation (part_code);
CREATE INDEX IF NOT EXISTS idx_reservation_wo ON material_reservation (work_order_number);
CREATE INDEX IF NOT EXISTS idx_reservation_status ON material_reservation (status);
