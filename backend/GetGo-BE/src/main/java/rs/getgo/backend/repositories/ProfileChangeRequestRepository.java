package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.ProfileChangeRequest;

public interface ProfileChangeRequestRepository extends JpaRepository<ProfileChangeRequest, Long> {
}
