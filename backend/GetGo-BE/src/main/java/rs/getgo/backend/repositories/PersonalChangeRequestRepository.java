package rs.getgo.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.PersonalChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;

public interface PersonalChangeRequestRepository extends JpaRepository<PersonalChangeRequest, Long> {
    Page<PersonalChangeRequest> findByStatus(RequestStatus status, Pageable pageable);
    boolean existsByDriverAndStatus(Driver driver, RequestStatus status);
}
