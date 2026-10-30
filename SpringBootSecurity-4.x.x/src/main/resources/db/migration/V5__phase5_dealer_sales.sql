-- Phase 5: Dealer commercial terms, dealer prices, quotations
ALTER TABLE dealer ADD COLUMN IF NOT EXISTS credit_limit DECIMAL(18,2) DEFAULT 0;
ALTER TABLE dealer ADD COLUMN IF NOT EXISTS advance_payment_pct DECIMAL(5,2) DEFAULT 30.00;
ALTER TABLE dealer ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(40) DEFAULT 'NET 90';
ALTER TABLE dealer ADD COLUMN IF NOT EXISTS account_balance DECIMAL(18,2) DEFAULT 0;

ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS quotation_id BIGINT;
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS advance_pct DECIMAL(5,2);
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS advance_required DECIMAL(18,2);
ALTER TABLE sales_order ADD COLUMN IF NOT EXISTS advance_received DECIMAL(18,2) DEFAULT 0;

CREATE TABLE IF NOT EXISTS dealer_price (
    id BIGSERIAL PRIMARY KEY,
    dealer_code VARCHAR(30) NOT NULL,
    model_code VARCHAR(40) NOT NULL,
    variant VARCHAR(60),
    unit_price DECIMAL(18,2) NOT NULL,
    effective_from DATE,
    effective_to DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_price_dealer ON dealer_price (dealer_code, model_code);

CREATE TABLE IF NOT EXISTS quotation (
    id BIGSERIAL PRIMARY KEY,
    quotation_number VARCHAR(40) NOT NULL UNIQUE,
    dealer_code VARCHAR(30) NOT NULL,
    model_code VARCHAR(40) NOT NULL,
    variant VARCHAR(60), color VARCHAR(60),
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL,
    accessories_cost DECIMAL(18,2) DEFAULT 0,
    transport_cost DECIMAL(18,2) DEFAULT 0,
    tax_amount DECIMAL(18,2) DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    valid_until DATE,
    approved_by VARCHAR(100), approved_at DATE,
    reject_reason VARCHAR(500),
    converted_so_number VARCHAR(30),
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_quotation_dealer ON quotation (dealer_code);
CREATE INDEX IF NOT EXISTS idx_quotation_status ON quotation (status);
