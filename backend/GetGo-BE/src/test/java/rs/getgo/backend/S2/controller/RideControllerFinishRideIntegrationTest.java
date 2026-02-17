package rs.getgo.backend.S2.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import rs.getgo.backend.dtos.login.CreateLoginDTO;
import rs.getgo.backend.dtos.login.CreatedLoginDTO;
import rs.getgo.backend.dtos.ride.UpdateRideDTO;
import rs.getgo.backend.dtos.ride.UpdatedRideDTO;
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
public class RideControllerFinishRideIntegrationTest {

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
    public void finishRide_happyPath_returnsOkAndDto() {
        // prepare driver with password and role
        Driver driver = new Driver();
        driver.setEmail("driver@gmail.com");
        driver.setName("Drv");
        driver.setActive(false);
        driver.setPassword(passwordEncoder.encode("Driver123!"));
        driver.setRole(UserRole.DRIVER);
        driver.setActivated(true);
        driver = driverRepository.save(driver);

        // prepare passenger
        Passenger passenger = new Passenger();
        passenger.setEmail("pass@gmail.com");
        passenger.setName("Pass");
        passenger.setSurname("Surname");
        passenger.setCanAccessSystem(true);
        passenger = passengerRepository.save(passenger);

        // prepare route and active ride
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

        // login to obtain JWT
        CreateLoginDTO loginReq = new CreateLoginDTO();
        loginReq.setEmail(driver.getEmail());
        loginReq.setPassword("Driver123!");

        ResponseEntity<CreatedLoginDTO> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, CreatedLoginDTO.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        // build request
        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<UpdateRideDTO> request = new HttpEntity<>(req, headers);

        ResponseEntity<UpdatedRideDTO> response = restTemplate.exchange("/api/rides/" + ar.getId() + "/finish", HttpMethod.PUT, request, UpdatedRideDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // assert concrete enum-backed status
        assertThat(response.getBody().getStatus()).isEqualTo(RideStatus.FINISHED.name());
    }

    @Test
    public void finishRide_unauthenticated_returnsForbidden() {
        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateRideDTO> request = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/rides/1/finish", HttpMethod.PUT, request, String.class);
        // without auth should be 403 Forbidden (Authorization denied)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void finishRide_invalidRequestBody_returns400() {
        // prepare driver with password and role
        Driver driver = new Driver();
        driver.setEmail("driver_invalid@gmail.com");
        driver.setName("DrvInv");
        driver.setActive(false);
        driver.setPassword(passwordEncoder.encode("Driver123!"));
        driver.setRole(UserRole.DRIVER);
        driver.setActivated(true);
        driver = driverRepository.save(driver);

        // prepare active ride
        Passenger passenger = new Passenger();
        passenger.setEmail("pass_inv@gmail.com");
        passenger.setName("PassInv");
        passenger.setSurname("Surname");
        passenger.setCanAccessSystem(true);
        passenger = passengerRepository.save(passenger);

        ActiveRide ar = new ActiveRide();
        ar.setRoute(new Route());
        ar.setEstimatedPrice(100.0);
        ar.setEstimatedDurationMin(30.0);
        ar.setActualStartTime(LocalDateTime.now().minusMinutes(10));
        ar.setStatus(RideStatus.ACTIVE);
        ar.setDriver(driver);
        ar.setPayingPassenger(passenger);
        ar = activeRideRepository.save(ar);

        // login
        CreateLoginDTO loginReq = new CreateLoginDTO();
        loginReq.setEmail(driver.getEmail());
        loginReq.setPassword("Driver123!");

        ResponseEntity<CreatedLoginDTO> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, CreatedLoginDTO.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        // invalid body: blank status
        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<UpdateRideDTO> request = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/rides/" + ar.getId() + "/finish", HttpMethod.PUT, request, String.class);
        // should be validation error -> 400
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void finishRide_cannotFinishInCurrentState_returnsBadRequest() {
        // driver + ride in FINISHED state
        Driver driver = new Driver();
        driver.setEmail("driver_state@gmail.com");
        driver.setName("DrvState");
        driver.setActive(false);
        driver.setPassword(passwordEncoder.encode("Driver123!"));
        driver.setRole(UserRole.DRIVER);
        driver.setActivated(true);
        driver = driverRepository.save(driver);

        Passenger passenger = new Passenger();
        passenger.setEmail("pass_state@gmail.com");
        passenger.setName("PassState");
        passenger.setSurname("Surname");
        passenger.setCanAccessSystem(true);
        passenger = passengerRepository.save(passenger);

        ActiveRide ar = new ActiveRide();
        ar.setRoute(new Route());
        ar.setEstimatedPrice(100.0);
        ar.setEstimatedDurationMin(30.0);
        ar.setActualStartTime(LocalDateTime.now().minusMinutes(10));
        ar.setStatus(RideStatus.FINISHED); // already finished
        ar.setDriver(driver);
        ar.setPayingPassenger(passenger);
        ar = activeRideRepository.save(ar);

        // login and attempt finish
        CreateLoginDTO loginReq = new CreateLoginDTO();
        loginReq.setEmail(driver.getEmail());
        loginReq.setPassword("Driver123!");

        ResponseEntity<CreatedLoginDTO> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, CreatedLoginDTO.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<UpdateRideDTO> request = new HttpEntity<>(req, headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/rides/" + ar.getId() + "/finish", HttpMethod.PUT, request, String.class);
        // service should reject -> 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void finishRide_nonexistentRide_returnsBadRequest() {
        // create driver and login
        Driver driver = new Driver();
        driver.setEmail("driver_missing@gmail.com");
        driver.setName("DrvMiss");
        driver.setActive(false);
        driver.setPassword(passwordEncoder.encode("Driver123!"));
        driver.setRole(UserRole.DRIVER);
        driver.setActivated(true);
        driver = driverRepository.save(driver);

        CreateLoginDTO loginReq = new CreateLoginDTO();
        loginReq.setEmail(driver.getEmail());
        loginReq.setPassword("Driver123!");

        ResponseEntity<CreatedLoginDTO> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, CreatedLoginDTO.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        UpdateRideDTO req = new UpdateRideDTO();
        req.setStatus("FINISHED");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<UpdateRideDTO> request = new HttpEntity<>(req, headers);

        // choose an ID that very likely doesn't exist
        ResponseEntity<String> response = restTemplate.exchange("/api/rides/99999999/finish", HttpMethod.PUT, request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
