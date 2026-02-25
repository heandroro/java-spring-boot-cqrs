-- Orders Service Database Initialization Script
-- PostgreSQL 15+
-- Executed automatically by Flyway on startup

-- 1. Orders table (main entity)
CREATE TABLE IF NOT EXISTS orders (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'pending'
    CHECK (status IN ('pending', 'confirmed', 'shipped', 'delivered')),
  total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

COMMENT ON TABLE orders IS 'Main orders table for Orders Service';
COMMENT ON COLUMN orders.id IS 'Unique identifier for the order';
COMMENT ON COLUMN orders.customer_id IS 'ID of the customer who owns this order';
COMMENT ON COLUMN orders.status IS 'Current status: pending, confirmed, shipped, delivered';
COMMENT ON COLUMN orders.total IS 'Total order value (sum of all item subtotals)';
COMMENT ON COLUMN orders.created_at IS 'Timestamp when order was created';
COMMENT ON COLUMN orders.updated_at IS 'Timestamp when order was last updated';

-- 2. Order items (child entities)
CREATE TABLE IF NOT EXISTS order_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  product_id VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL CHECK (quantity > 0),
  price_per_unit NUMERIC(10, 2) NOT NULL CHECK (price_per_unit > 0),
  subtotal NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

COMMENT ON TABLE order_items IS 'Line items for each order';
COMMENT ON COLUMN order_items.id IS 'Unique identifier for the order item';
COMMENT ON COLUMN order_items.order_id IS 'Foreign key to orders table';
COMMENT ON COLUMN order_items.product_id IS 'Product identifier';
COMMENT ON COLUMN order_items.quantity IS 'Quantity of this product in the order';
COMMENT ON COLUMN order_items.price_per_unit IS 'Price per unit at time of order';
COMMENT ON COLUMN order_items.subtotal IS 'Calculated subtotal (quantity × price_per_unit)';

-- 3. Audit log (for compliance & debugging)
CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  entity_type VARCHAR(50) NOT NULL,       -- 'order', 'order_item'
  entity_id UUID NOT NULL,                -- order.id or order_item.id
  operation VARCHAR(20) NOT NULL,         -- 'INSERT', 'UPDATE', 'DELETE'
  customer_id UUID,                       -- masked PII in logs
  details JSONB,                          -- operation details (no sensitive data)
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_customer ON audit_logs(customer_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

COMMENT ON TABLE audit_logs IS 'Audit trail for compliance and debugging';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type of entity being audited';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID of the entity being audited';
COMMENT ON COLUMN audit_logs.operation IS 'Operation performed (INSERT, UPDATE, DELETE)';
COMMENT ON COLUMN audit_logs.details IS 'JSON details of the operation';
