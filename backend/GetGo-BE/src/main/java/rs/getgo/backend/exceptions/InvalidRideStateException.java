package rs.getgo.backend.exceptions;

public class InvalidRideStateException extends RuntimeException {
    public InvalidRideStateException(String message) {
        super(message);
    }

    public InvalidRideStateException(String message, Throwable cause) {
        super(message, cause);
    }
}

