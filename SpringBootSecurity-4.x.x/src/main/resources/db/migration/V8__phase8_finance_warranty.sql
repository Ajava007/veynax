-- Phase 8: Finance (invoice/credit-note/receipt/refund) + Warranty
CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL UNIQUE,
    sales_order_id BIGINT,
    dealer_code VARCHAR(30) NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(18,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    amount_paid DECIMAL(18,2) DEFAULT 0,
    credit_applied DECIMAL(18,2) DEFAULT 0,
    balance_due DECIMAL(18,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_invoice_so ON invoice (sales_order_id);
CREATE INDEX IF NOT EXISTS idx_invoice_dealer ON invoice (dealer_code);
CREATE INDEX IF NOT EXISTS idx_invoice_status ON invoice (status);

CREATE TABLE IF NOT EXISTS credit_note (
    id BIGSERIAL PRIMARY KEY,
    credit_note_number VARCHAR(40) NOT NULL UNIQUE,
    invoice_id BIGINT,
    dealer_code VARCHAR(30) NOT NULL,
    credit_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_credit_invoice ON credit_note (invoice_id);
CREATE INDEX IF NOT EXISTS idx_credit_dealer ON credit_note (dealer_code);

CREATE TABLE IF NOT EXISTS receipt (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    dealer_code VARCHAR(30) NOT NULL,
    invoice_id BIGINT,
    receipt_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    method VARCHAR(30) NOT NULL,
    reference VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'RECORDED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_receipt_dealer ON receipt (dealer_code);
CREATE INDEX IF NOT EXISTS idx_receipt_invoice ON receipt (invoice_id);

CREATE TABLE IF NOT EXISTS refund (
    id BIGSERIAL PRIMARY KEY,
    refund_number VARCHAR(40) NOT NULL UNIQUE,
    dealer_code VARCHAR(30) NOT NULL,
    refund_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_refund_dealer ON refund (dealer_code);

CREATE TABLE IF NOT EXISTS warranty_policy (
    id BIGSERIAL PRIMARY KEY,
    model_code VARCHAR(40) NOT NULL,
    coverage_months INTEGER NOT NULL DEFAULT 36,
    coverage_km INTEGER NOT NULL DEFAULT 100000,
    coverage_items VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_warranty_model ON warranty_policy (model_code);

CREATE TABLE IF NOT EXISTS warranty_claim (
    id BIGSERIAL PRIMARY KEY,
    claim_number VARCHAR(40) NOT NULL UNIQUE,
    vin VARCHAR(17) NOT NULL,
    part_code VARCHAR(40),
    defect_description VARCHAR(1000),
    claim_amount DECIMAL(18,2) DEFAULT 0,
    approved_amount DECIMAL(18,2) DEFAULT 0,
    claim_date DATE NOT NULL,
    resolved_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reject_reason VARCHAR(500),
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_wc_vin ON warranty_claim (vin);
CREATE INDEX IF NOT EXISTS idx_wc_status ON warranty_claim (status);
