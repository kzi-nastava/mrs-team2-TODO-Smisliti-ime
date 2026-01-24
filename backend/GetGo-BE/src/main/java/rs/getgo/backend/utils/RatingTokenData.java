package rs.getgo.backend.utils;

public class RatingTokenData {
    private Long rideId;
    private Long passengerId;

    public RatingTokenData(Long rideId, Long passengerId) {
        this.rideId = rideId;
        this.passengerId = passengerId;
    }

    public Long getRideId() { return rideId; }
    public Long getPassengerId() { return passengerId; }
}
