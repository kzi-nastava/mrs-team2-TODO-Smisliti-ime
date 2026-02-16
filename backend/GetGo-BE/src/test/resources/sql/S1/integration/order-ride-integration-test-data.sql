-- Free passenger
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (100, 'p@gmail.com', 'pass', 'Free', 'Passenger', 'Addr 1', '111', 2, false);
INSERT INTO passengers (id, can_access_system) VALUES (100, true);

-- Free driver
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked)
VALUES (200, 'd@gmail.com', 'pass', 'Available', 'Driver', 'Addr 2', '222', 0, false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude)
VALUES (200, true, true, 45.25, 19.84);
INSERT INTO vehicles (id, model, type, license_plate, number_of_seats, is_baby_friendly, is_pet_friendly, is_available)
VALUES (200, 'Toyota Camry', 'STANDARD', 'NS-123-AB', 4, false, false, true);
UPDATE drivers SET vehicle_id = 200 WHERE id = 200;