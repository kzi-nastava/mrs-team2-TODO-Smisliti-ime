package rs.getgo.backend.exceptions;

public class RatingException extends RuntimeException {
    private final String code;

    public RatingException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
