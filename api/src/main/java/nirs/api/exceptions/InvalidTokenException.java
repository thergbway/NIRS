package nirs.api.exceptions;

public class InvalidTokenException extends Exception {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
