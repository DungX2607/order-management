-- Initial seed data for Water Order Management System
-- Password for all accounts: 123456aA@ (BCrypt hash)

-- Insert default admin account
INSERT INTO users (username, password, full_name, role, is_active)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Administrator', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- Insert sample categories
INSERT INTO categories (name, display_order)
VALUES
    ('Highland Coffee', 1),
    ('The Coffee House', 2),
    ('Phúc Long', 3)
ON CONFLICT (name) DO NOTHING;

-- Insert sample menu items
INSERT INTO menu_items (category_id, name, display_order, is_active)
SELECT c.id, 'Trà sữa trân châu', 1, true
FROM categories c WHERE c.name = 'Highland Coffee'
ON CONFLICT DO NOTHING;

INSERT INTO menu_items (category_id, name, display_order, is_active)
SELECT c.id, 'Cà phê sữa đá', 2, true
FROM categories c WHERE c.name = 'Highland Coffee'
ON CONFLICT DO NOTHING;

INSERT INTO menu_items (category_id, name, display_order, is_active)
SELECT c.id, 'Trà đào cam sả', 1, true
FROM categories c WHERE c.name = 'The Coffee House'
ON CONFLICT DO NOTHING;

INSERT INTO menu_items (category_id, name, display_order, is_active)
SELECT c.id, 'Trà sữa ô long', 1, true
FROM categories c WHERE c.name = 'Phúc Long'
ON CONFLICT DO NOTHING;
