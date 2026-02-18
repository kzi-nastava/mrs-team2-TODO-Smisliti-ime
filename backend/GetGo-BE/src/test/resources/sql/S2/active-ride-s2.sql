-- S2 active ride test data
-- Driver
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (1, 'driver@gmail.com', 'pass', 'DriverName', 'DriverSurname', 'Addr Driver', '001', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (1, true, true, 45.0, 19.0);

-- Passenger
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (2, 'passenger@gmail.com', 'pass', 'PassengerName', 'PassengerSurname', 'Addr Passenger', '002', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (2, true);

-- Route
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km)
VALUES (3, 'StartPoint','EndPoint',25.0,8.0);

-- Active ride
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id,
                          actual_start_time, needs_baby_seats, needs_pet_friendly, route_id)
VALUES (1, 'ACTIVE', 150.0, 20.0, 1, 2, '2025-12-01 09:00:00', false, false, 3);
