-- Driver test data
INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked) VALUES (40, 'dtest@test.com','pass','Drv','Test','AddrD','404',0,false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude) VALUES (40, true, true, 44.0, 20.0);

INSERT INTO users (id, email, password, name, surname, address, phone, role, is_blocked) VALUES (41, 'dtest2@test.com','pass','Drv2','Test2','AddrD2','405',0,false);
INSERT INTO drivers (id, is_active, is_activated, current_latitude, current_longitude) VALUES (41, false, true, 44.1, 20.1);

