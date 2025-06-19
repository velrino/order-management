-- Clean and Initialize database with sample data

-- ========================================
-- LIMPEZA DO BANCO DE DADOS
-- ========================================

-- Disable foreign key checks temporarily (if using MySQL)
-- SET FOREIGN_KEY_CHECKS = 0;

-- Limpar todas as tabelas na ordem correta (respeitando foreign keys)
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM partners;

-- Reset sequences (PostgreSQL)
-- ALTER SEQUENCE order_items_id_seq RESTART WITH 1;

-- Reset auto increment (MySQL)
-- ALTER TABLE order_items AUTO_INCREMENT = 1;

-- Re-enable foreign key checks (if using MySQL)
-- SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- CRIAÇÃO DE ÍNDICES
-- ========================================

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_orders_partner_created ON orders(partner_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_partner_status ON orders(partner_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- ========================================
-- INSERÇÃO DE DADOS DE EXEMPLO
-- ========================================

-- Insert sample partners
INSERT INTO partners (id, name, credit_limit, available_credit, created_at, updated_at, version)
VALUES
    ('PARTNER001', 'TechCorp Solutions', 50000.00, 50000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('PARTNER002', 'GlobalTrade Inc', 75000.00, 75000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('PARTNER003', 'Innovation Labs', 30000.00, 30000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('PARTNER004', 'Digital Dynamics', 40000.00, 40000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('PARTNER005', 'Future Systems', 60000.00, 60000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
    ON CONFLICT (id) DO NOTHING;

-- Primeiro, vamos verificar quais status são válidos
-- SELECT column_name, data_type, check_clause FROM information_schema.check_constraints WHERE table_name = 'orders';

-- Insert sample orders with valid statuses only
INSERT INTO orders (id, partner_id, status, total_amount, created_at, updated_at, version)
VALUES
-- Pedidos recentes (usando apenas PENDING, APPROVED, PROCESSING)
('550e8400-e29b-41d4-a716-446655440001', 'PARTNER001', 'PENDING', 1500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('550e8400-e29b-41d4-a716-446655440002', 'PARTNER002', 'APPROVED', 2750.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('550e8400-e29b-41d4-a716-446655440003', 'PARTNER001', 'PROCESSING', 950.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('550e8400-e29b-41d4-a716-446655440004', 'PARTNER003', 'APPROVED', 3200.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('550e8400-e29b-41d4-a716-446655440005', 'PARTNER002', 'PENDING', 800.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

-- Pedidos de ontem
('550e8400-e29b-41d4-a716-446655440006', 'PARTNER004', 'PENDING', 1200.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
('550e8400-e29b-41d4-a716-446655440007', 'PARTNER005', 'APPROVED', 4500.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),
('550e8400-e29b-41d4-a716-446655440008', 'PARTNER001', 'PROCESSING', 2100.00, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 0),

-- Pedidos da semana passada
('550e8400-e29b-41d4-a716-446655440009', 'PARTNER003', 'PROCESSING', 1800.00, CURRENT_TIMESTAMP - INTERVAL '7 days', CURRENT_TIMESTAMP - INTERVAL '7 days', 0),
('550e8400-e29b-41d4-a716-446655440010', 'PARTNER002', 'APPROVED', 5200.00, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days', 0),

-- Pedidos do mês passado
('550e8400-e29b-41d4-a716-446655440011', 'PARTNER004', 'PROCESSING', 3800.00, CURRENT_TIMESTAMP - INTERVAL '30 days', CURRENT_TIMESTAMP - INTERVAL '30 days', 0),
('550e8400-e29b-41d4-a716-446655440012', 'PARTNER005', 'PENDING', 1500.00, CURRENT_TIMESTAMP - INTERVAL '25 days', CURRENT_TIMESTAMP - INTERVAL '25 days', 0)
    ON CONFLICT (id) DO NOTHING;

-- Insert sample order items
INSERT INTO order_items (product_id, quantity, unit_price, order_id)
VALUES
-- Items para pedido 1
('PROD001', 10, 150.00, '550e8400-e29b-41d4-a716-446655440001'),

-- Items para pedido 2
('PROD002', 5, 275.00, '550e8400-e29b-41d4-a716-446655440002'),
('PROD003', 3, 200.00, '550e8400-e29b-41d4-a716-446655440002'),
('PROD001', 7, 125.00, '550e8400-e29b-41d4-a716-446655440002'),

-- Items para pedido 3
('PROD001', 19, 50.00, '550e8400-e29b-41d4-a716-446655440003'),

-- Items para pedido 4
('PROD004', 8, 400.00, '550e8400-e29b-41d4-a716-446655440004'),

-- Items para pedido 5
('PROD002', 2, 400.00, '550e8400-e29b-41d4-a716-446655440005'),

-- Items para pedido 6
('PROD005', 6, 200.00, '550e8400-e29b-41d4-a716-446655440006'),

-- Items para pedido 7
('PROD001', 15, 300.00, '550e8400-e29b-41d4-a716-446655440007'),

-- Items para pedido 8
('PROD003', 7, 300.00, '550e8400-e29b-41d4-a716-446655440008'),

-- Items para pedido 9
('PROD002', 4, 450.00, '550e8400-e29b-41d4-a716-446655440009'),

-- Items para pedido 10
('PROD004', 13, 400.00, '550e8400-e29b-41d4-a716-446655440010'),

-- Items para pedido 11
('PROD005', 19, 200.00, '550e8400-e29b-41d4-a716-446655440011'),

-- Items para pedido 12
('PROD001', 15, 100.00, '550e8400-e29b-41d4-a716-446655440012')
    ON CONFLICT DO NOTHING;

-- ========================================
-- VERIFICAÇÃO DOS DADOS INSERIDOS
-- ========================================

-- Verificar quantos registros foram inseridos
SELECT 'Partners inserted' AS info, COUNT(*) AS count FROM partners
UNION ALL
SELECT 'Orders inserted' AS info, COUNT(*) AS count FROM orders
UNION ALL
SELECT 'Order items inserted' AS info, COUNT(*) AS count FROM order_items;

-- Verificar distribuição por status
SELECT
    status,
    COUNT(*) AS count,
    SUM(total_amount) AS total_value
FROM orders
GROUP BY status
ORDER BY count DESC;

-- Verificar pedidos por partner
SELECT
    partner_id,
    COUNT(*) AS order_count,
    SUM(total_amount) AS total_value
FROM orders
GROUP BY partner_id
ORDER BY order_count DESC;