-- Driver 1 (Has active ride)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (1, 'driver1@test.com', 'pass', 'John', 'Driver', 'Addr 1', '111', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (1, true, true, 45.0, 19.0);

-- Driver 2 (Has no active rides)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (2, 'driver2@test.com', 'pass', 'Jane', 'Driver2', 'Addr 2', '222', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (2, true, true, 45.1, 19.1);

-- Driver 3 (Has scheduled ride with linked passenger)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (6, 'driver3@test.com', 'pass', 'Mike', 'Driver3', 'Addr 6', '666', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (6, true, true, 45.2, 19.2);

-- Passenger 1 (Paying passenger on ACTIVE ride)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (3, 'passenger1@test.com', 'pass', 'Bob', 'Passenger', 'Addr 3', '333', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (3, true);

-- Passenger 2 (Paying passenger on SCHEDULED ride, also linked passenger on ACTIVE ride)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (4, 'passenger2@test.com', 'pass', 'Alice', 'Passenger2', 'Addr 4', '444', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (4, true);

-- Passenger 3 (Has no active rides)
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (5, 'passenger3@test.com', 'pass', 'Charlie', 'Passenger3', 'Addr 5', '555', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (5, true);

-- Route
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km)
VALUES (1, 'Start A', 'End A', 20.0, 10.0);

-- Ride 1 (active, driver1, passenger1 as paying, passenger2 as linked)
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id,
                          actual_start_time, needs_baby_seats, needs_pet_friendly, route_id)
VALUES (1, 'ACTIVE', 500.0, 30.0, 1, 3, '2025-01-01 10:00:00', false, false, 1);
INSERT INTO active_ride_passengers (ride_id, passenger_id) VALUES (1, 4);

-- Ride 2 (scheduled, driver1, passenger2 as paying)
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id,
                          scheduled_time, needs_baby_seats, needs_pet_friendly)
VALUES (2, 'SCHEDULED', 300.0, 20.0, 1, 4, '2030-01-01 10:00:00', false, false);


-- Ride 3 (scheduled, driver3, passenger3 as paying, passenger5 as linked)
INSERT INTO active_rides (id, status, estimated_price, estimated_duration_min, driver_id, paying_passenger_id,
                          scheduled_time, needs_baby_seats, needs_pet_friendly)
VALUES (3, 'SCHEDULED', 200.0, 15.0, 6, 3, '2025-06-15 08:00:00', false, false);
INSERT INTO active_ride_passengers (ride_id, passenger_id) VALUES (3, 5);