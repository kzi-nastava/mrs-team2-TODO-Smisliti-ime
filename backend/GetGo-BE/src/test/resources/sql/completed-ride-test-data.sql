-- Users for completed rides
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (11, 'completed1@test.com', 'pass', 'Compl', 'Eted', 'Addr C1', '101', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (11, true);

-- Completed ride rows
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km) VALUES (11, 'S11','E11',12.0,3.0);
INSERT INTO completed_rides (
  id, route_id, start_time, end_time, estimated_price, est_time, est_distance_km, vehicle_type, paying_passenger_id,
  actual_distance_km, actual_price, is_cancelled, is_completed_normally, is_panic_pressed, is_stopped_early, needs_baby_seats, needs_pet_friendly
)
VALUES (11, 11, '2026-01-01 09:00:00', '2026-01-01 09:20:00', 150.0, 12.0, 3.0, 'STANDARD', 11,
        3.2, 140.0, false, true, false, false, false, false);
