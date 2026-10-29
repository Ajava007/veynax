-- Phase 3: Import & shipment tracking + receiving + damage claims
-- Safe for existing DBs (IF NOT EXISTS / ADD COLUMN IF NOT EXISTS)

CREATE TABLE IF NOT EXISTS shipment (
    id BIGSERIAL PRIMARY KEY,
    shipment_number VARCHAR(40) NOT NULL UNIQUE,
    po_number VARCHAR(30) NOT NULL,
    supplier_code VARCHAR(30) NOT NULL,
    vessel_name VARCHAR(120),
    voyage_number VARCHAR(40),
    port_of_loading VARCHAR(80),
    port_of_discharge VARCHAR(80),
    etd DATE, eta DATE, actual_departure DATE, actual_arrival DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    commercial_invoice_ref VARCHAR(60),
    packing_list_ref VARCHAR(60),
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_shipment_po ON shipment (po_number);
CREATE INDEX IF NOT EXISTS idx_shipment_status ON shipment (status);

CREATE TABLE IF NOT EXISTS container (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipment(id) ON DELETE CASCADE,
    container_number VARCHAR(20) NOT NULL,
    seal_number VARCHAR(30),
    container_type VARCHAR(20),
    gross_weight_kg DECIMAL(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'SEALED',
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_container_shipment ON container (shipment_id);

CREATE TABLE IF NOT EXISTS customs_declaration (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    declaration_number VARCHAR(60) NOT NULL,
    declaration_date DATE,
    release_date DATE,
    duty_amount DECIMAL(18,2) DEFAULT 0,
    vat_amount DECIMAL(18,2) DEFAULT 0,
    other_charges DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    held_reason VARCHAR(500),
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_customs_shipment ON customs_declaration (shipment_id);

CREATE TABLE IF NOT EXISTS landed_cost (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    freight_cost DECIMAL(18,2) DEFAULT 0,
    insurance_cost DECIMAL(18,2) DEFAULT 0,
    duty_cost DECIMAL(18,2) DEFAULT 0,
    handling_cost DECIMAL(18,2) DEFAULT 0,
    other_cost DECIMAL(18,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    allocation_method VARCHAR(20) DEFAULT 'BY_VALUE',
    total_cost DECIMAL(18,2) DEFAULT 0,
    reconciled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_landed_shipment ON landed_cost (shipment_id);

CREATE TABLE IF NOT EXISTS damage_record (
    id BIGSERIAL PRIMARY KEY,
    damage_number VARCHAR(40) NOT NULL UNIQUE,
    po_number VARCHAR(30) NOT NULL,
    shipment_id BIGINT,
    container_number VARCHAR(20),
    part_code VARCHAR(40) NOT NULL,
    damaged_quantity INTEGER NOT NULL DEFAULT 0,
    damage_type VARCHAR(40),
    responsible_party VARCHAR(30),
    estimated_loss DECIMAL(18,2) DEFAULT 0,
    evidence_url VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'REPORTED',
    claim_id BIGINT,
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_damage_po ON damage_record (po_number);
CREATE INDEX IF NOT EXISTS idx_damage_shipment ON damage_record (shipment_id);

CREATE TABLE IF NOT EXISTS supplier_claim (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(40) NOT NULL UNIQUE,
    supplier_code VARCHAR(30) NOT NULL,
    damage_record_id BIGINT NOT NULL,
    claimed_amount DECIMAL(18,2) DEFAULT 0,
    recovered_amount DECIMAL(18,2) DEFAULT 0,
    credit_note_ref VARCHAR(60),
    filed_date DATE,
    resolved_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_claim_supplier ON supplier_claim (supplier_code);

-- Receiving tracking on PO items
ALTER TABLE purchase_order_item ADD COLUMN IF NOT EXISTS received_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE purchase_order_item ADD COLUMN IF NOT EXISTS rejected_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE purchase_order_item ADD COLUMN IF NOT EXISTS damaged_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE purchase_order_item ADD COLUMN IF NOT EXISTS missing_quantity INTEGER NOT NULL DEFAULT 0;

-- Supplier procurement terms
ALTER TABLE supplier ADD COLUMN IF NOT EXISTS lead_time_days INTEGER;
ALTER TABLE supplier ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(40);
