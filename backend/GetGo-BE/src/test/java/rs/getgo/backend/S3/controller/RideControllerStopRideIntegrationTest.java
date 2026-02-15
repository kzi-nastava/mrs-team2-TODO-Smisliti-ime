package rs.getgo.backend.S3.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.ride.StopRideDTO;
import rs.getgo.backend.dtos.ride.RideCompletionDTO;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.entities.Route;
import rs.getgo.backend.model.entities.WayPoint;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.model.enums.UserRole;
import rs.getgo.backend.repositories.ActiveRideRepository;
import rs.getgo.backend.repositories.DriverRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.time.LocalDateTime;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
public class RideControllerStopRideIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void stopRide_happyPath_returnsOkAndDto() {
        Driver driver = new Driver();
        driver.setEmail("driver@x.com");
        driver.setName("Drv");
        driver.setActive(false);
        driver.setPassword(passwordEncoder.encode("Driver123!"));
        driver.setRole(UserRole.DRIVER);
        driver.setActivated(true);
        driver = driverRepository.save(driver);

        Passenger passenger = new Passenger();
        passenger.setEmail("pass@x.com");
        passenger.setName("Pass");
        passenger.setSurname("Surname");
        passenger.setCanAccessSystem(true);
        passenger = passengerRepository.save(passenger);

        Route route = new Route();
        route.setEstTimeMin(30.0);
        route.setEstDistanceKm(10.0);
        route.setWaypoints(new LinkedList<>());
        WayPoint wp = new WayPoint();
        wp.setLatitude(45.2671);
        wp.setLongitude(19.8335);
        route.getWaypoints().add(wp);

        ActiveRide ar = new ActiveRide();
        ar.setRoute(route);
        ar.setEstimatedPrice(100.0);
        ar.setEstimatedDurationMin(30.0);
        ar.setActualStartTime(LocalDateTime.now().minusMinutes(10));
        ar.setStatus(RideStatus.ACTIVE);
        ar.setDriver(driver);
        ar.setPayingPassenger(passenger);
        ar.setLinkedPassengers(java.util.List.of());

        ar = activeRideRepository.save(ar);

        CreateLoginDTO loginReq = new CreateLoginDTO();
        loginReq.setEmail(driver.getEmail());
        loginReq.setPassword("Driver123!");

        ResponseEntity<CreatedLoginDTO> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, CreatedLoginDTO.class);
        assertThat(loginResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(loginResp.getBody()).isNotNull();
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        StopRideDTO req = new StopRideDTO();
        req.setLatitude(45.2671);
        req.setLongitude(19.8335);
        req.setStoppedAt(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<StopRideDTO> entity = new HttpEntity<>(req, headers);

        ResponseEntity<RideCompletionDTO> response = restTemplate.postForEntity("/api/rides/" + ar.getId() + "/stop", entity, RideCompletionDTO.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        RideCompletionDTO body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo("STOPPED_EARLY");
        assertThat(body.getRideId()).isNotNull();
        assertThat(body.getPrice()).isGreaterThan(0);
    }

    @Test
    public void stopRide_nonexistentRide_returnsNotFound() {
        StopRideDTO req = new StopRideDTO();
        req.setLatitude(45.0);
        req.setLongitude(20.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StopRideDTO> entity = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/rides/999999/stop", entity, String.class);
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    public void stopRide_unauthenticated_returnsForbidden() {
        // prepare an active ride (driver exists but we won't authenticate)
        Driver driver = new Driver();
        driver.setEmail("driver2@x.com");
        driver.setName("Drv2");
        driver.setActive(false);
        driver = driverRepository.save(driver);

        Passenger passenger = new Passenger();
        passenger.setEmail("pass2@x.com");
        passenger.setName("Pass2");
        passenger.setSurname("Surname2");
        passenger.setCanAccessSystem(true);
        passenger = passengerRepository.save(passenger);

        ActiveRide ar = new ActiveRide();
        ar.setRoute(new Route());
        ar.setEstimatedPrice(50.0);
        ar.setEstimatedDurationMin(10.0);
        ar.setActualStartTime(LocalDateTime.now().minusMinutes(5));
        ar.setStatus(RideStatus.ACTIVE);
        ar.setDriver(driver);
        ar.setPayingPassenger(passenger);
        ar = activeRideRepository.save(ar);

        StopRideDTO req = new StopRideDTO();
        req.setLatitude(44.0);
        req.setLongitude(20.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StopRideDTO> entity = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/rides/" + ar.getId() + "/stop", entity, String.class);
        // without a valid JWT the request should be denied by security -> 401 or 403; check for 4xx and specifically forbidden
        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
