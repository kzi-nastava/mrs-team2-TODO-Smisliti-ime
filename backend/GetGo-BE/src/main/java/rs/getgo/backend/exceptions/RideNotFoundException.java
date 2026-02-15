package rs.getgo.backend.exceptions;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(String message) {
        super(message);
    }

    public RideNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

