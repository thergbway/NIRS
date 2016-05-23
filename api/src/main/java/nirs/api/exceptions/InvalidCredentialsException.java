package nirs.api.exceptions;

public class InvalidCredentialsException extends Exception{
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
