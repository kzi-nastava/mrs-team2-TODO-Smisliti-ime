-- completed ride fixture
-- Reuse passenger 2 from active-ride
INSERT INTO routes (id, starting_point, ending_point, est_time_min, est_distance_km) VALUES (7, 'CompStart','CompEnd',15.0,4.5);

INSERT INTO completed_rides (
  id, route_id, start_time, end_time, estimated_price, est_time, est_distance_km, vehicle_type, paying_passenger_id,
  actual_distance_km, actual_price, is_cancelled, is_completed_normally, is_panic_pressed, is_stopped_early, needs_baby_seats, needs_pet_friendly
)
VALUES (8, 7, '2025-12-01 08:00:00', '2025-12-01 08:20:00', 120.0, 15.0, 4.5, 'STANDARD', 2,
        4.6, 115.0, false, true, false, false, false, false);
