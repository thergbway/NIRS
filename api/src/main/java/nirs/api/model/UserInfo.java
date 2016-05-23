package nirs.api.model;

import java.io.Serializable;

public class UserInfo implements Serializable{
    private final String firstName;
    private final String lastName;
    private final String username;
    private final String email;

    public UserInfo(String firstName, String lastName, String username, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
