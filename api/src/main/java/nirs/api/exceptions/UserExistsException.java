package nirs.api.exceptions;

public class UserExistsException extends Exception {
    private String existingUsername;

    public UserExistsException(String existingUsername) {
        super("User " + existingUsername + " already exists");

        this.existingUsername = existingUsername;
    }

    public String getExistingUsername() {
        return existingUsername;
    }
}