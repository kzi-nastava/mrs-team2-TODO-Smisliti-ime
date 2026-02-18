package rs.getgo.backend.S2.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import rs.getgo.backend.GetGoBeApplication;
import rs.getgo.backend.model.entities.InconsistencyReport;
import rs.getgo.backend.repositories.InconsistencyReportRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = GetGoBeApplication.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = {"/sql/S2/inconsistency-s2.sql"})
public class InconsistencyReportRepositoryTest {

    @Autowired
    private InconsistencyReportRepository reportRepository;

    @Test
    public void fixture_shouldLoadReport() {
        List<InconsistencyReport> reports = reportRepository.findAll();
        assertThat(reports).isNotEmpty();
    }
}

