-- Blocked user
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (200, 'blocked@test.com', 'pass', 'Blocked', 'User', 'Addr', '666', 2, true);
INSERT INTO passengers (id, can_access_system) VALUES (200, false);

-- User that never got blocked
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (201, 'nonblocked@test.com', 'pass', 'Non-blocked', 'User', 'Addr', '777', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (201, true);

-- User with expired block
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (202, 'previously-blocked@test.com', 'pass', 'Previously-blocked', 'User', 'Addr', '999', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (202, true);

-- Admin (for block note 'blocked by' field)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (203, 'admin@test.com', 'pass', 'Admin', 'User', 'Addr', '888', 1, false);
INSERT INTO administrators (id) VALUES (203);

-- Active block note (user blocked)
INSERT INTO block_notes (id, user_id, admin_id, reason, blocked_at, unblocked_at)
VALUES (1, 200, 203, 'Misbehavior', '2025-06-01 10:00:00', NULL);

-- Expired block note (user unblocked)
INSERT INTO block_notes (id, user_id, admin_id, reason, blocked_at, unblocked_at)
VALUES (2, 202, 203, 'Old offense', '2025-01-01 10:00:00', '2025-02-01 10:00:00');