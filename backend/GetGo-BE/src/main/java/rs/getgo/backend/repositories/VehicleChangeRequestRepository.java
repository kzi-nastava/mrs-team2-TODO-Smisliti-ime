package rs.getgo.backend.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.VehicleChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;

public interface VehicleChangeRequestRepository extends JpaRepository<VehicleChangeRequest, Long> {
    Page<VehicleChangeRequest> findByStatus(RequestStatus status, Pageable pageable);
    boolean existsByDriverAndStatus(Driver driver, RequestStatus status);
}
