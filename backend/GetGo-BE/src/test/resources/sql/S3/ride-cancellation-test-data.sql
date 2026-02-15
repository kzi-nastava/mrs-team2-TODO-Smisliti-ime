-- Ride cancellation test data
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (30, 'canceluser@test.com', 'pass', 'Can', 'Cell', 'Addr Can', '303', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (30, true);

INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km) VALUES (30, 'CS','CE',10.0,2.0);
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id, actual_start_time, route_id, needs_baby_seats, needs_pet_friendly)
VALUES (30, 'ACTIVE', 90.0, 10.0, NULL, 30, '2026-02-01 09:00:00', 30, false, false);

INSERT INTO ride_cancellations (id, ride_id, canceler_id, role, reason, created_at) VALUES (1, 30, 30, 'PASSENGER', 'test reason', '2026-02-01 09:05:00');
