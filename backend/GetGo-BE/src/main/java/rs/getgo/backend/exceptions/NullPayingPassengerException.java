package rs.getgo.backend.exceptions;

public class NullPayingPassengerException extends RuntimeException {
    public NullPayingPassengerException(String message) {
        super(message);
    }

    public NullPayingPassengerException(String message, Throwable cause) {
        super(message, cause);
    }
}

