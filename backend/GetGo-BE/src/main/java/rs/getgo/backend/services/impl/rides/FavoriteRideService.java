package rs.getgo.backend.services.impl.rides;

import org.springframework.stereotype.Service;
import rs.getgo.backend.dtos.favorite.CreatedFavoriteRideDTO;
import rs.getgo.backend.dtos.favorite.GetFavoriteRideDTO;
import rs.getgo.backend.model.entities.*;
import rs.getgo.backend.repositories.CompletedRideRepository;
import rs.getgo.backend.repositories.FavoriteRideRepository;
import rs.getgo.backend.repositories.PassengerRepository;
import rs.getgo.backend.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteRideService {

    private final FavoriteRideRepository favoriteRideRepository;
    private final UserRepository userRepository;
    private final CompletedRideRepository completedRideRepository;
    private final PassengerRepository passengerRepository;

    public FavoriteRideService(
            FavoriteRideRepository favoriteRideRepository,
            UserRepository userRepository,
            CompletedRideRepository completedRideRepository,
            PassengerRepository passengerRepository
    ) {
        this.favoriteRideRepository = favoriteRideRepository;
        this.userRepository = userRepository;
        this.completedRideRepository = completedRideRepository;
        this.passengerRepository = passengerRepository;
    }

    public CreatedFavoriteRideDTO favoriteRide(String userEmail, Long completedRideId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        CompletedRide completedRide = completedRideRepository.findById(completedRideId)
                .orElseThrow(() -> new RuntimeException("Completed ride not found with id: " + completedRideId));

        // Verify user was part of ride
        boolean isPayingPassenger = completedRide.getPayingPassengerId().equals(user.getId());
        boolean isLinkedPassenger = completedRide.getLinkedPassengerIds() != null &&
                completedRide.getLinkedPassengerIds().contains(user.getId());
        if (!isPayingPassenger && !isLinkedPassenger) {
            throw new RuntimeException("Unauthorized: You were not part of this ride");
        }

        // Check if already favorite
        if (favoriteRideRepository.existsByCompletedRideIdAndUserEmail(completedRideId, userEmail)) {
            throw new RuntimeException("You have already favorited this ride");
        }

        FavoriteRide favoriteRide = new FavoriteRide();
        favoriteRide.setUser(user);
        favoriteRide.setCompletedRideId(completedRideId);
        favoriteRide.setVehicleType(completedRide.getVehicleType());
        favoriteRide.setNeedsBabySeats(completedRide.isNeedsBabySeats());
        favoriteRide.setNeedsPetFriendly(completedRide.isNeedsPetFriendly());

        List<FavoriteWaypoint> waypoints = buildFavoriteWaypoints(completedRide);
        favoriteRide.setWaypoints(waypoints);

        List<String> linkedPassengerEmails = new ArrayList<>();
        if (completedRide.getLinkedPassengerIds() != null && !completedRide.getLinkedPassengerIds().isEmpty()) {
            List<Passenger> linkedPassengers = passengerRepository.findAllById(completedRide.getLinkedPassengerIds());
            linkedPassengerEmails = linkedPassengers.stream()
                    .map(Passenger::getEmail)
                    .collect(Collectors.toList());
        }
        favoriteRide.setLinkedPassengerEmails(linkedPassengerEmails);

        FavoriteRide saved = favoriteRideRepository.save(favoriteRide);

        CreatedFavoriteRideDTO response = new CreatedFavoriteRideDTO();
        response.setFavoriteRideId(saved.getId());
        response.setSuccess(true);

        return response;
    }

    private static List<FavoriteWaypoint> buildFavoriteWaypoints(CompletedRide completedRide) {
        List<FavoriteWaypoint> waypoints = new ArrayList<>();
        Route route = completedRide.getRoute();

        if (route != null && route.getWaypoints() != null) {
            for (WayPoint wp : route.getWaypoints()) {
                FavoriteWaypoint favoriteWp = new FavoriteWaypoint();
                favoriteWp.setAddress(wp.getAddress());
                favoriteWp.setLatitude(wp.getLatitude());
                favoriteWp.setLongitude(wp.getLongitude());
                waypoints.add(favoriteWp);
            }
        }
        return waypoints;
    }

    public List<GetFavoriteRideDTO> getFavoriteUserRides(String userEmail) {
        List<FavoriteRide> favoriteRides = favoriteRideRepository.findByUserEmail(userEmail);

        return favoriteRides.stream()
                .map(this::buildGetFavoriteRideDTO)
                .collect(Collectors.toList());
    }

    public GetFavoriteRideDTO getFavoriteUserRide(Long id, String userEmail) {
        FavoriteRide favoriteRide = favoriteRideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite ride not found with id: " + id));

        if (!favoriteRide.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Favorite ride does not belong to user");
        }

        return buildGetFavoriteRideDTO(favoriteRide);
    }

    public void unfavoriteRide(Long completedRideId, String userEmail) {
        FavoriteRide favoriteRide = favoriteRideRepository.
                findByCompletedRideId(completedRideId)
                .orElseThrow(() -> new RuntimeException(
                        "Favorite ride not found that binds to completed ride with id:" + completedRideId));

        if (!favoriteRide.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Favorite ride does not belong to user");
        }

        favoriteRideRepository.delete(favoriteRide);
    }

    private GetFavoriteRideDTO buildGetFavoriteRideDTO(FavoriteRide favoriteRide) {
        GetFavoriteRideDTO dto = new GetFavoriteRideDTO();
        dto.setId(favoriteRide.getId());
        dto.setVehicleType(favoriteRide.getVehicleType() != null
                ? favoriteRide.getVehicleType().toString()
                : "ANY");
        dto.setNeedsBabySeats(favoriteRide.isNeedsBabySeats());
        dto.setNeedsPetFriendly(favoriteRide.isNeedsPetFriendly());

        List<String> addresses = favoriteRide.getWaypoints().stream()
                .map(FavoriteWaypoint::getAddress)
                .collect(Collectors.toList());
        dto.setAddresses(addresses);

        List<Double> latitudes = favoriteRide.getWaypoints().stream()
                .map(FavoriteWaypoint::getLatitude)
                .collect(Collectors.toList());
        dto.setLatitudes(latitudes);

        List<Double> longitudes = favoriteRide.getWaypoints().stream()
                .map(FavoriteWaypoint::getLongitude)
                .collect(Collectors.toList());
        dto.setLongitudes(longitudes);

        dto.setLinkedPassengerEmails(
                favoriteRide.getLinkedPassengerEmails() != null
                        ? favoriteRide.getLinkedPassengerEmails()
                        : new ArrayList<>()
        );

        return dto;
    }
}