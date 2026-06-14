-- Auto-create default ADMIN user on first run
-- Password: admin123 (BCrypt hash below)
INSERT IGNORE INTO users (username, password, email, role, approved)
VALUES (
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh..',
  'admin@cricketpro.com',
  'ADMIN',
  true
);