package rs.getgo.backend.services;

import java.util.Optional;

public interface CoordinateMappingService {
    public Optional<double[]> call(String url, String mode);
}