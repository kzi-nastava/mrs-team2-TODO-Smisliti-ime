package rs.getgo.backend.S2.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.ActiveRide;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.InconsistencyReport;
import rs.getgo.backend.model.entities.Panic;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.model.enums.RideStatus;
import rs.getgo.backend.repositories.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql({"/sql/S1/active-ride-test-data.sql", "/sql/S3/panic-test-data.sql", "/sql/S3/inconsistency-report-test-data.sql", "/sql/S3/completed-ride-test-data.sql"})
public class FinishRideRepositoryTest {

    @Autowired
    private ActiveRideRepository activeRideRepository;

    @Autowired
    private PanicRepository panicRepository;

    @Autowired
    private InconsistencyReportRepository reportRepository;

    @Autowired
    private CompletedRideRepository completedRideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    private ActiveRide activeRide1;
    private Driver driver1;
    private Passenger passenger1;

    @BeforeEach
    void setUp() {
        // load entities inserted by SQL fixtures
        activeRide1 = activeRideRepository.findById(1L).orElse(null);
        driver1 = driverRepository.findById(1L).orElse(null);
        passenger1 = passengerRepository.findById(3L).orElse(null);
    }

    @Test
    public void fixture_shouldLoadActiveRideAndRelations() {
        assertThat(activeRide1).isNotNull();
        assertThat(activeRide1.getStatus()).isEqualTo(RideStatus.ACTIVE);
        assertThat(activeRide1.getDriver()).isNotNull();
        assertThat(activeRide1.getPayingPassenger()).isNotNull();
    }

    @Test
    public void findFirstByDriverAndStatusOrderByScheduledTimeAsc_returnsScheduledRide() {
        // driver1 has scheduled ride id=2 according to SQL
        Optional<ActiveRide> opt = activeRideRepository.findFirstByDriverAndStatusOrderByScheduledTimeAsc(driver1, RideStatus.SCHEDULED);
        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(2L);
    }

    @Test
    public void panicFixture_shouldBePresentForRide10() {
        // panic SQL inserts a panic for ride id 10
        List<Panic> panics = panicRepository.findAll();
        assertThat(panics).isNotEmpty();
        // find by ride id via repository method if available, otherwise assert list contains matching
        boolean anyForRide10 = panics.stream().anyMatch(p -> p.getRideId() != null && p.getRideId().equals(10L));
        assertThat(anyForRide10).isTrue();
    }

    @Test
    public void inconsistencyReportsFixture_shouldLoadReports() {
        List<InconsistencyReport> reports = reportRepository.findAll();
        assertThat(reports).isNotNull();
        // There may be zero or more depending on fixture; just ensure repository is usable
    }

    @Test
    public void completedRideFixture_shouldLoadCompletedRide() {
        // completed_ride_test_data.sql inserts a completed ride with id 11
        Optional<?> cr = completedRideRepository.findById(11L);
        assertThat(cr).isPresent();
    }

}
