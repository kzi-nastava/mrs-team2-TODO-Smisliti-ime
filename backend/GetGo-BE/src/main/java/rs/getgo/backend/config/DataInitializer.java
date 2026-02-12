package rs.getgo.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.model.enums.VehicleType;
import rs.getgo.backend.repositories.*;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("dev") // Only runs in 'dev' profile
    public CommandLineRunner initializeData(
            UserRepository userRepository,
            AdministratorRepository administratorRepository,
            DriverRepository driverRepository,
            PassengerRepository passengerRepository,
            RidePriceRepository ridePriceRepository) {

        return args -> {
            // Check if data already exists
            if (userRepository.count() > 0) {
                log.info("Database already contains data. Skipping initialization.");
                return;
            }

            log.info("Initializing database with default users...");

            // Create Admin
            Administrator admin = createAdmin();
            administratorRepository.save(admin);
            log.info("Created admin: {}", admin.getEmail());

            // Create Drivers with Vehicles
            Driver driver1 = createDriver1();
            Vehicle vehicle1 = createVehicle1();
            driver1.setVehicle(vehicle1);
            driverRepository.save(driver1);
            log.info("Created driver: {}", driver1.getEmail());

            Driver driver2 = createDriver2();
            Vehicle vehicle2 = createVehicle2();
            driver2.setVehicle(vehicle2);
            driverRepository.save(driver2);
            log.info("Created driver: {}", driver2.getEmail());

            Driver driver3 = createDriver3();
            Vehicle vehicle3 = createVehicle3();
            driver3.setVehicle(vehicle3);
            driverRepository.save(driver3);
            log.info("Created driver: {}", driver3.getEmail());

            // Create Passengers
            Passenger passenger1 = createPassenger1();
            passengerRepository.save(passenger1);
            log.info("Created passenger: {}", passenger1.getEmail());

            Passenger passenger2 = createPassenger2();
            passengerRepository.save(passenger2);
            log.info("Created passenger: {}", passenger2.getEmail());

            for (VehicleType type : VehicleType.values()) {
                if (ridePriceRepository.findByVehicleType(type).isEmpty()) {
                    RidePrice price = new RidePrice();
                    price.setVehicleType(type);
                    price.setPricePerKm(getDefaultPricePerKm(type));
                    price.setStartPrice(getDefaultStartPrice(type));
                    ridePriceRepository.save(price);
                    log.info("Created ride price for: {}", type);
                }
            }

            log.info("Database initialization completed successfully!");
        };
    }

    private Administrator createAdmin() {
        Administrator admin = new Administrator();
        admin.setEmail("a@gmail.com");
        admin.setPassword(passwordEncoder.encode("aaaaaaaa"));
        admin.setName("Admin Only");
        admin.setSurname("Administrator");
        admin.setAddress("Admin Address 2324");
        admin.setPhone("0603849872");
        admin.setRole(UserRole.ADMIN);
        admin.setBlocked(false);
        return admin;
    }

    private Driver createDriver1() {
        Driver driver = new Driver();
        driver.setEmail("d@gmail.com");
        driver.setPassword(passwordEncoder.encode("dddddddd"));
        driver.setName("Driver One");
        driver.setSurname("Drivone");
        driver.setAddress("Driver1 Address 6546");
        driver.setPhone("0654829461");
        driver.setRole(UserRole.DRIVER);
        driver.setBlocked(false);
        driver.setActive(false);
        driver.setActivated(true);
        driver.setProfilePictureUrl(null);
        driver.setCurrentLatitude(45.2671);
        driver.setCurrentLongitude(19.8335);
        driver.setLastLocationUpdate(LocalDateTime.now());
        return driver;
    }

    private Driver createDriver2() {
        Driver driver = new Driver();
        driver.setEmail("f@gmail.com");
        driver.setPassword(passwordEncoder.encode("ffffffff"));
        driver.setName("Driver Two");
        driver.setSurname("Drivtwo");
        driver.setAddress("Driver2 Address 298");
        driver.setPhone("0654342365");
        driver.setRole(UserRole.DRIVER);
        driver.setBlocked(false);
        driver.setActive(false);
        driver.setActivated(true);
        driver.setProfilePictureUrl(null);
        driver.setCurrentLatitude(45.2550);
        driver.setCurrentLongitude(19.8450);
        driver.setLastLocationUpdate(LocalDateTime.now());
        return driver;
    }

    private Driver createDriver3() {
        Driver driver = new Driver();
        driver.setEmail("g@gmail.com");
        driver.setPassword(passwordEncoder.encode("gggggggg"));
        driver.setName("Driver Three");
        driver.setSurname("Drivthree");
        driver.setAddress("Driver3 Address 456");
        driver.setPhone("0654987321");
        driver.setRole(UserRole.DRIVER);
        driver.setBlocked(false);
        driver.setActive(false);
        driver.setActivated(true);
        driver.setProfilePictureUrl(null);
        driver.setCurrentLatitude(45.2400);
        driver.setCurrentLongitude(19.8200);
        driver.setLastLocationUpdate(LocalDateTime.now());
        return driver;
    }

    private Vehicle createVehicle1() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("Toyota Corolla");
        vehicle.setType(VehicleType.VAN);
        vehicle.setLicensePlate("123456");
        vehicle.setNumberOfSeats(4);
        vehicle.setIsBabyFriendly(true);
        vehicle.setIsPetFriendly(false);
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Vehicle createVehicle2() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("Volkswagen Passat");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setLicensePlate("456789");
        vehicle.setNumberOfSeats(4);
        vehicle.setIsBabyFriendly(false);
        vehicle.setIsPetFriendly(true);
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Vehicle createVehicle3() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("BMW X5");
        vehicle.setType(VehicleType.SEDAN);
        vehicle.setLicensePlate("789012");
        vehicle.setNumberOfSeats(5);
        vehicle.setIsBabyFriendly(true);
        vehicle.setIsPetFriendly(true);
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Passenger createPassenger1() {
        Passenger passenger = new Passenger();
        passenger.setEmail("p@gmail.com");
        passenger.setPassword(passwordEncoder.encode("pppppppp"));
        passenger.setName("Passenger Pass");
        passenger.setSurname("Passone");
        passenger.setAddress("Street 598");
        passenger.setPhone("0658473923");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setCanAccessSystem(true);
        passenger.setProfilePictureUrl(null);
        return passenger;
    }

    private Passenger createPassenger2() {
        Passenger passenger = new Passenger();
        passenger.setEmail("o@gmail.com");
        passenger.setPassword(passwordEncoder.encode("oooooooo"));
        passenger.setName("Passenger Yes");
        passenger.setSurname("Passtwo");
        passenger.setAddress("Street 123");
        passenger.setPhone("0653452348");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setCanAccessSystem(true);
        passenger.setProfilePictureUrl(null);
        return passenger;
    }

    private Double getDefaultPricePerKm(VehicleType type) {
        return switch (type) {
            case STANDARD -> 50.0;
            case SEDAN -> 60.0;
            case VAN -> 70.0;
            case SUV -> 80.0;
            case LUXURY -> 120.0;
        };
    }

    private Double getDefaultStartPrice(VehicleType type) {
        return switch (type) {
            case STANDARD -> 150.0;
            case SEDAN -> 180.0;
            case VAN -> 200.0;
            case SUV -> 220.0;
            case LUXURY -> 300.0;
        };
    }

}