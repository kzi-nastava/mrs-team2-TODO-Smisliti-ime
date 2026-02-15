-- S2 panic data attached to active ride 101
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (204, 's2-panicuser@test.com', 'pass', 'Panic', 'User', 'Addr', '204', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (204, true);

INSERT INTO panic (id, ride_id, triggered_by_user_id, triggered_at, is_read) VALUES (51, 101, 204, '2025-12-01 09:05:00', false);

