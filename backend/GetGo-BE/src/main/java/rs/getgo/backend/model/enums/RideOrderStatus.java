package rs.getgo.backend.model.enums;

public enum RideOrderStatus {
    SUCCESS,
    NO_DRIVERS_AVAILABLE,
    ALL_DRIVERS_BUSY,
    DRIVER_OVERTIME_LIMIT,
    INVALID_SCHEDULED_TIME,
    PASSENGER_NOT_FOUND,
    LINKED_PASSENGER_NOT_FOUND
}
