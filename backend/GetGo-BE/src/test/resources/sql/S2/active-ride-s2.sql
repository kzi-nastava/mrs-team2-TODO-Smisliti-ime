-- S2 active ride test data
-- Driver
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (201, 's2-driver@test.com', 'pass', 'S2', 'Driver', 'Addr S2', '201', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (201, true, true, 45.0, 19.0);

-- Passenger
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (203, 's2-passenger@test.com', 'pass', 'S2', 'Passenger', 'Addr P', '203', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (203, true);

-- Route
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km)
VALUES (210, 'S2Start','S2End',25.0,8.0);

-- Active ride for S2
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id,
                          actual_start_time, needs_baby_seats, needs_pet_friendly, route_id)
VALUES (101, 'ACTIVE', 150.0, 20.0, 201, 203, '2025-12-01 09:00:00', false, false, 210);

