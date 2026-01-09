package rs.getgo.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.getgo.backend.model.entities.Administrator;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}
