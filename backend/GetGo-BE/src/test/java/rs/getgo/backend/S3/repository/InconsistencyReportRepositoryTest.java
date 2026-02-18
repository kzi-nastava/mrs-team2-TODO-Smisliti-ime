package rs.getgo.backend.S3.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.InconsistencyReport;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.InconsistencyReportRepository;
import rs.getgo.backend.repositories.PassengerRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sql/S3/inconsistency-report-test-data.sql")
public class InconsistencyReportRepositoryTest {

    @Autowired
    private InconsistencyReportRepository reportRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    public void fixture_shouldLoadReport() {
        List<InconsistencyReport> reports = reportRepository.findAll();
        assertFalse(reports.isEmpty());
    }

    @Test
    public void findUnlinkedReportsByPassenger_returnsEmptyForNewPassenger() {
        Passenger p = new Passenger();
        p.setEmail("tmp@test.com");
        p = passengerRepository.save(p);

        List<InconsistencyReport> reports = reportRepository.findUnlinkedReportsByPassenger(p);
        assertNotNull(reports);
        assertTrue(reports.isEmpty());
    }

    @Test
    public void saveAndQuery_byCompletedRideIdHandlesNull() {
        InconsistencyReport r = new InconsistencyReport();
        r.setText("desc");
        reportRepository.save(r);
        List<InconsistencyReport> byRide = reportRepository.findByCompletedRide_IdOrderByCreatedAtDesc(null);
        assertNotNull(byRide);
    }

}
