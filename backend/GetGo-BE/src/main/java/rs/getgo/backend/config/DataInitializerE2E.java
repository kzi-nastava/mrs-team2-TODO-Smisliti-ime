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
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializerE2E {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Profile("test-e2e")
    public CommandLineRunner initializeE2EData(
            DriverRepository driverRepository,
            PassengerRepository passengerRepository,
            CompletedRideRepository completedRideRepository) {

        return args -> {

            if (driverRepository.count() > 0) {
                log.info("Database not empty, skipping E2E init");
                return;
            }

            Vehicle vehicle = new Vehicle();
            vehicle.setModel("Toyota Corolla");
            vehicle.setType(VehicleType.VAN);
            vehicle.setLicensePlate("123456");
            vehicle.setNumberOfSeats(4);
            vehicle.setIsBabyFriendly(true);
            vehicle.setIsPetFriendly(false);
            vehicle.setIsAvailable(true);

            Driver driver = new Driver();
            driver.setEmail("d@gmail.com");
            driver.setPassword(passwordEncoder.encode("dddddddd"));
            driver.setName("Driver One");
            driver.setSurname("Drivone");
            driver.setAddress("Driver 123");
            driver.setPhone("0654829461");
            driver.setRole(UserRole.DRIVER);
            driver.setBlocked(false);
            driver.setActive(true);
            driver.setActivated(true);
            driver.setVehicle(vehicle);
            driver.setCurrentLatitude(45.239748);
            driver.setCurrentLongitude(19.82158);
            driver.setLastLocationUpdate(LocalDateTime.now());
            Driver savedDriver = driverRepository.save(driver);

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
            Passenger savedPassenger = passengerRepository.save(passenger);

            Route route = new Route();
            route.setStartingPoint("26, Bulevar patrijarha Pavla, Novi Sad");
            route.setEndingPoint("9, Melhiora Erdujheljija, Novi Sad");
            route.setEstDistanceKm(0.866);
            route.setEstTimeMin(2.322);
            route.setEncodedPolyline("[[19.805174,45.241346],[19.804639,45.241399]]");

            WayPoint wp1 = new WayPoint();
            wp1.setAddress("26, Bulevar patrijarha Pavla, Novi Sad");
            wp1.setLatitude(45.2414932);
            wp1.setLongitude(19.805204);
            wp1.setReachedAt(LocalDateTime.of(2026, 2, 16, 2, 23, 17));

            WayPoint wp2 = new WayPoint();
            wp2.setAddress("36, Bulevar patrijarha Pavla, Novi Sad");
            wp2.setLatitude(45.2423247);
            wp2.setLongitude(19.7986327);
            wp2.setReachedAt(LocalDateTime.of(2026, 2, 16, 2, 23, 21));

            WayPoint wp3 = new WayPoint();
            wp3.setAddress("9, Melhiora Erdujheljija, Novi Sad");
            wp3.setLatitude(45.2444768);
            wp3.setLongitude(19.7992792);
            wp3.setReachedAt(LocalDateTime.of(2026, 2, 16, 2, 23, 22));

            route.setWaypoints(List.of(wp1, wp2, wp3));

            CompletedRide ride = new CompletedRide();
            ride.setRoute(route);
            ride.setStartTime(LocalDateTime.of(2026, 2, 11, 23, 40, 0));
            ride.setEndTime(LocalDateTime.of(2026, 2, 11, 23, 44, 17));
            ride.setEstimatedPrice(260.673);
            ride.setEstDistanceKm(0.866);
            ride.setEstTime(2.322);
            ride.setVehicleType(VehicleType.VAN);
            ride.setNeedsBabySeats(true);
            ride.setNeedsPetFriendly(false);
            ride.setDriverId(savedDriver.getId());
            ride.setDriverName("Driver One");
            ride.setDriverEmail("d@gmail.com");
            ride.setPayingPassengerId(savedPassenger.getId());
            ride.setPayingPassengerName("Passenger Pass Passone");
            ride.setPayingPassengerEmail("p@gmail.com");
            ride.setCompletedNormally(true);
            ride.setPanicPressed(false);
            ride.setCancelled(false);
            ride.setStoppedEarly(false);
            completedRideRepository.save(ride);

            log.info("E2E test data initialized");
        };
    }
}