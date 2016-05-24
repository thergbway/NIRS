package nirs.dao;

import nirs.api.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import sun.misc.BASE64Encoder;

import java.security.SecureRandom;
import java.sql.ResultSet;

@Service
public class UserService {
    @Autowired
    private Sql2o sql2o;

    public String createToken(String username) {
        SecureRandom generator = new SecureRandom();
        byte randomBytes[] = new byte[32];
        generator.nextBytes(randomBytes);

        BASE64Encoder encoder = new BASE64Encoder();

        String token = encoder.encode(randomBytes);

        try (Connection con = sql2o.open()) {
            con
                .createQuery("INSERT INTO tokens(value, username) VALUES (:value, :username)")
                .addParameter("value", token)
                .addParameter("username", username)
                .executeUpdate();

            return token;
        }
    }

    public boolean isValidCredentials(String username, String password) {
        try (Connection con = sql2o.open()) {
            long count = con
                .createQuery("SELECT count(*) AS count FROM users u WHERE u.username = :username AND u.password = :password")
                .addParameter("username", username)
                .addParameter("password", password)
                .executeScalar(Long.class);

            return count == 1L;
        }
    }

    public boolean isValidToken(String token) {
        try (Connection con = sql2o.open()) {
            long count = con
                .createQuery("SELECT count(*) AS count FROM tokens t WHERE t.value = :value")
                .addParameter("value", token)
                .executeScalar(Long.class);

            return count == 1;
        }
    }

    public UserInfo getUserInfo(String token) {
        try (Connection con = sql2o.open()) {
            String username = getUsername(token);

            UserInfo userInfo = con
                .createQuery("SELECT first_name , last_name, username, email FROM users " +
                    "WHERE username = :username")
                .addParameter("username", username)
                .executeAndFetchFirst((ResultSet resultSet) -> {
                    return UserInfo.builder()
                        .firstName(resultSet.getString("first_name"))
                        .lastName(resultSet.getString("last_name"))
                        .username(resultSet.getString("username"))
                        .email(resultSet.getString("email"))
                        .build();
                });

            return userInfo;
        }
    }

    public boolean isUsernamePresented(String username) {
        try (Connection con = sql2o.open()) {
            long count = con
                .createQuery("SELECT count(*) AS count FROM users u WHERE u.username = :username")
                .addParameter("username", username)
                .executeScalar(Long.class);

            return count == 1;
        }
    }

    public boolean isEmailPresented(String email) {
        try (Connection con = sql2o.open()) {
            long count = con
                .createQuery("SELECT count(*) AS count FROM users u WHERE u.email = :email")
                .addParameter("email", email)
                .executeScalar(Long.class);

            return count == 1;
        }
    }

    public void createUser(String username, String password, String firstName, String lastName, String email) {
        try (Connection con = sql2o.open()) {
            con
                .createQuery("INSERT INTO users(username, password, first_name, last_name, email) VALUES " +
                    "(:username, :password, :firstName, :lastName, :email)")
                .addParameter("username", username)
                .addParameter("password", password)
                .addParameter("firstName", firstName)
                .addParameter("lastName", lastName)
                .addParameter("email", email)
                .executeUpdate();
        }
    }

    public String getUsername(String token) {
        try (Connection con = sql2o.open()) {
            return con
                .createQuery("SELECT u.username AS username FROM users u RIGHT JOIN tokens t " +
                    "ON u.username = t.username WHERE t.value = :value")
                .addParameter("value", token)
                .executeAndFetchFirst((ResultSet resultSet) -> {
                    return resultSet.getString("username");
                });
        }
    }
}
