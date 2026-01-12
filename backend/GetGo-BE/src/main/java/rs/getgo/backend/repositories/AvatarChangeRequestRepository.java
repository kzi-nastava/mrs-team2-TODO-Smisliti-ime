package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.AvatarChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;

import java.util.List;

public interface AvatarChangeRequestRepository extends JpaRepository<AvatarChangeRequest, Long> {
    List<AvatarChangeRequest> findByStatus(RequestStatus status);
}
