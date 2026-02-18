-- Inconsistency reports and passenger
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked) VALUES (50,'rep@test.com','pass','Rep','User','AddrR','505',2,false);
INSERT INTO passengers (id, can_access_system) VALUES (50, true);

INSERT INTO inconsistency_reports (passenger_id, text, created_at) VALUES (50, 'Report text', '2026-02-01 12:00:00');
