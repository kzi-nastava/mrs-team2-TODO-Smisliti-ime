-- Users for panics
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (10, 'panicuser@test.com', 'pass', 'Pan', 'Ic', 'Addr P', '999', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (10, true);

-- Active ride to attach panic to
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km) VALUES (10, 'S1','E1',5.0,1.0);
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id, actual_start_time, route_id, needs_baby_seats, needs_pet_friendly)
VALUES (10, 'ACTIVE', 100.0, 10.0, NULL, 10, '2026-02-01 10:00:00', 10, false, false);

-- Panic record (include is_read boolean because column is NOT NULL)
INSERT INTO panic (id, ride_id, triggered_by_user_id, triggered_at, is_read) VALUES (1, 10, 10, '2026-02-01 10:05:00', false);
