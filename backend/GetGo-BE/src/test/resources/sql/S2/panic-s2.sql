-- Panic data attached to active ride 1
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (4, 'panicuser@gmail.com', 'pass', 'PanicName', 'PanicSurname', 'Addr', '004', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (4, true);

INSERT INTO panic (id, ride_id, triggered_by_user_id, triggered_at, is_read) VALUES (5, 1, 4, '2025-12-01 09:05:00', false);
