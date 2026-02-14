-- Completed ride 1: driver id 1
INSERT INTO completed_rides (id, driver_id, driver_name, driver_email, paying_passenger_id,
                             paying_passenger_name, paying_passenger_email, start_time, end_time,
                             estimated_price, actual_price, est_distance_km, actual_distance_km,
                             est_time, needs_baby_seats, needs_pet_friendly,
                             is_completed_normally, is_panic_pressed, is_cancelled, is_stopped_early)
VALUES (1, 1, 'John', 'driver1@test.com', 3, 'Bob', 'passenger1@test.com',
        '2025-03-01 09:00:00', '2025-03-01 09:30:00',
        500.0, 480.0, 10.0, 9.5, 30.0, false, false, true, false, false, false);

-- Completed ride 2: driver id 1
INSERT INTO completed_rides (id, driver_id, driver_name, driver_email, paying_passenger_id,
                             paying_passenger_name, paying_passenger_email, start_time, end_time,
                             estimated_price, actual_price, est_distance_km, actual_distance_km,
                             est_time, needs_baby_seats, needs_pet_friendly,
                             is_completed_normally, is_panic_pressed, is_cancelled, is_stopped_early)
VALUES (2, 1, 'John', 'driver1@test.com', 4, 'Alice', 'passenger2@test.com',
        '2025-06-01 14:00:00', '2025-06-01 14:45:00',
        300.0, 310.0, 8.0, 8.2, 20.0, false, false, true, false, false, false);

-- Completed ride 3: driver id 2
INSERT INTO completed_rides (id, driver_id, driver_name, driver_email, paying_passenger_id,
                             paying_passenger_name, paying_passenger_email, start_time, end_time,
                             estimated_price, actual_price, est_distance_km, actual_distance_km,
                             est_time, needs_baby_seats, needs_pet_friendly,
                             is_completed_normally, is_panic_pressed, is_cancelled, is_stopped_early)
VALUES (3, 2, 'Jane', 'driver2@test.com', 3, 'Bob', 'passenger1@test.com',
        '2025-04-15 11:00:00', '2025-04-15 11:20:00',
        200.0, 195.0, 5.0, 4.8, 15.0, false, false, true, false, false, false);