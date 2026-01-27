package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Driver;
import rs.getgo.backend.model.entities.VehicleChangeRequest;
import rs.getgo.backend.model.enums.RequestStatus;

import java.util.List;

public interface VehicleChangeRequestRepository extends JpaRepository<VehicleChangeRequest, Long> {
    List<VehicleChangeRequest> findByStatus(RequestStatus status);
    boolean existsByDriverAndStatus(Driver driver, RequestStatus status);
}
