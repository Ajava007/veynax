-- Phase 6: Forecasts + daily line targets + plan-vs-actual
CREATE TABLE IF NOT EXISTS forecast (
    id BIGSERIAL PRIMARY KEY,
    forecast_number VARCHAR(40) NOT NULL UNIQUE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    model_code VARCHAR(40) NOT NULL,
    variant VARCHAR(60),
    historical_demand INTEGER NOT NULL DEFAULT 0,
    dealer_forecast INTEGER NOT NULL DEFAULT 0,
    manager_adjustment INTEGER NOT NULL DEFAULT 0,
    adjustment_reason VARCHAR(500),
    approved_by VARCHAR(100),
    final_forecast INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_forecast_period ON forecast (period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_forecast_model ON forecast (model_code);
CREATE INDEX IF NOT EXISTS idx_forecast_status ON forecast (status);

CREATE TABLE IF NOT EXISTS line_target (
    id BIGSERIAL PRIMARY KEY,
    target_date DATE NOT NULL,
    line_code VARCHAR(30) NOT NULL,
    model_code VARCHAR(40) NOT NULL,
    target_quantity INTEGER NOT NULL DEFAULT 0,
    actual_quantity INTEGER NOT NULL DEFAULT 0,
    rejected_quantity INTEGER NOT NULL DEFAULT 0,
    downtime_minutes INTEGER NOT NULL DEFAULT 0,
    remarks VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_line_target_date ON line_target (target_date, line_code);
CREATE INDEX IF NOT EXISTS idx_line_target_line ON line_target (line_code);

ALTER TABLE production_plan ADD COLUMN IF NOT EXISTS actual_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE production_plan ADD COLUMN IF NOT EXISTS revision_reason VARCHAR(500);
ALTER TABLE production_plan ADD COLUMN IF NOT EXISTS revised_by VARCHAR(100);
