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
            PassengerRepository passengerRepository) {

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

            // Create Passengers
            Passenger passenger1 = createPassenger1();
            passengerRepository.save(passenger1);
            log.info("Created passenger: {}", passenger1.getEmail());

            Passenger passenger2 = createPassenger2();
            passengerRepository.save(passenger2);
            log.info("Created passenger: {}", passenger2.getEmail());

            log.info("Database initialization completed successfully!");
            log.info("=".repeat(50));
            log.info("Default credentials:");
            log.info("Admin - Email: admin@getgo.com, Password: Admin123!");
            log.info("Driver 1 - Email: driver1@getgo.com, Password: Driver123!");
            log.info("Driver 2 - Email: driver2@getgo.com, Password: Driver123!");
            log.info("Passenger 1 - Email: passenger1@getgo.com, Password: Pass123!");
            log.info("Passenger 2 - Email: passenger2@getgo.com, Password: Pass123!");
            log.info("=".repeat(50));
        };
    }

    private Administrator createAdmin() {
        Administrator admin = new Administrator();
        admin.setEmail("a");
        admin.setPassword(passwordEncoder.encode("a"));
        admin.setName("Admin");
        admin.setSurname("Administrator");
        admin.setAddress("Admin Address 2324");
        admin.setPhone("86786876788");
        admin.setRole(UserRole.ADMIN);
        admin.setBlocked(false);
        return admin;
    }

    private Driver createDriver1() {
        Driver driver = new Driver();
        driver.setEmail("d1");
        driver.setPassword(passwordEncoder.encode("d1"));
        driver.setName("Driver1");
        driver.setSurname("Drivone");
        driver.setAddress("Driver1 Address 6546");
        driver.setPhone("+381642345677");
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
        driver.setEmail("d2");
        driver.setPassword(passwordEncoder.encode("d2"));
        driver.setName("Driver2");
        driver.setSurname("Drivtwo");
        driver.setAddress("Driver2 Address 298");
        driver.setPhone("+381642345678");
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

    private Vehicle createVehicle1() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("Toyota Corolla");
        vehicle.setType(VehicleType.VAN);
        vehicle.setLicensePlate("NS-123-AB");
        vehicle.setNumberOfSeats(4);
        vehicle.setIsBabyFriendly(true);
        vehicle.setIsPetFriendly(false);
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Vehicle createVehicle2() {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel("Volkswagen Passat");
        vehicle.setType(VehicleType.SUV);
        vehicle.setLicensePlate("NS-456-CD");
        vehicle.setNumberOfSeats(4);
        vehicle.setIsBabyFriendly(false);
        vehicle.setIsPetFriendly(true);
        vehicle.setIsAvailable(true);
        return vehicle;
    }

    private Passenger createPassenger1() {
        Passenger passenger = new Passenger();
        passenger.setEmail("p1");
        passenger.setPassword(passwordEncoder.encode("p1"));
        passenger.setName("Passenger1");
        passenger.setSurname("Passone");
        passenger.setAddress("Street 598");
        passenger.setPhone("+381642345676");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setCanAccessSystem(true);
        passenger.setProfilePictureUrl(null);
        return passenger;
    }

    private Passenger createPassenger2() {
        Passenger passenger = new Passenger();
        passenger.setEmail("p2");
        passenger.setPassword(passwordEncoder.encode("p2"));
        passenger.setName("Passenger2");
        passenger.setSurname("Passtwo");
        passenger.setAddress("Street 123");
        passenger.setPhone("+381642345675");
        passenger.setRole(UserRole.PASSENGER);
        passenger.setBlocked(false);
        passenger.setCanAccessSystem(true);
        passenger.setProfilePictureUrl(null);
        return passenger;
    }
}