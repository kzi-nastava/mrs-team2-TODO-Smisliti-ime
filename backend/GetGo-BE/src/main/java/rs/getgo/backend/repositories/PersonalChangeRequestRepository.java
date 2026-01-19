package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.PersonalChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;

import java.util.List;

public interface PersonalChangeRequestRepository extends JpaRepository<PersonalChangeRequest, Long> {
    List<PersonalChangeRequest> findByStatus(RequestStatus status);
    boolean existsByDriverAndStatus(Driver driver, RequestStatus status);
}
