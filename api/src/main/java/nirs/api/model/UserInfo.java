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

    public static UserInfoBuilder builder() {
        return new UserInfoBuilder();
    }

    public static class UserInfoBuilder {

        private String firstName;
        private String lastName;
        private String username;
        private String email;

        public UserInfoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserInfoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserInfoBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserInfoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserInfo build() {
            return new UserInfo(firstName, lastName, username, email);
        }


    }
}
