package rs.getgo.backend.model.enums;

public enum RideStatus {
    SCHEDULED,                          // Scheduled ride with no driver yet
    DRIVER_FINISHING_PREVIOUS_RIDE,     // Driver still needs to finish previous ride
    DRIVER_READY,                       // Driver received ride but didn't start moving yet
    DRIVER_INCOMING,                    // Driver moving towards start point
    DRIVER_ARRIVED,                     // Driver waiting to start ride at start point
    ACTIVE,                             // Driver moving towards end point through waypoints
    CANCELLED,
    FINISHED,
    // TODO: add PANIC_PRESSED and potentially more to reduce attribute count from completed ride?
}
