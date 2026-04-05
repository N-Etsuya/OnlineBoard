package onlineboard.repository;

import onlineboard.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private SQLiteConnection connection;


    public UserRepository(SQLiteConnection connection) {
        this.connection = connection;
        createTable();
    }

    public synchronized void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS users ("
                + "email VARCHAR(255) PRIMARY KEY,"
                + "hashedPassword VARCHAR(255) NOT NULL,"
                + "nickname VARCHAR(255) NOT NULL"
                + ")";
        try {
            connection.connect(); 
            connection.executeUpdate(query);
        } catch (SQLException e) {
            logger.error("Error creating users table: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating users table", e);
        } finally {
            connection.disconnect();
        }
    }

    //ユーザーを作成
    public synchronized void createUser(User user) {
        logger.info("Creating user: {}", user);
        String query = "INSERT INTO users (email, hashedPassword, nickname) VALUES (?, ?, ?)";
        try {
            connection.connect();
            connection.executeUpdate(query, user.getEmail(), user.getHashedPassword(), user.getNickname());
            logger.info("User created successfully: {}", user);
        } catch (SQLException e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating user", e);
        } finally {
            connection.disconnect();
        }
    }

    //メールアドレスからユーザーを持ってくる
    public synchronized User getUserByEmail(String email) {
        logger.info("Fetching user with email {}", email);
        String query = "SELECT * FROM users WHERE email = ?";
        try {
            connection.connect();
            ResultSet resultSet = connection.executeQuery(query, email);

            if (resultSet.next()) {
                String hashedPassword = resultSet.getString("hashedPassword");
                String nickname = resultSet.getString("nickname");
                User user = new User(email, hashedPassword, nickname);
                logger.info("User fetched successfully: {}", user);
                return user;
            }

            resultSet.close();
        } catch (SQLException e) {
            logger.error("Error fetching user with email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error fetching user with email " + email, e);
        } finally {
            connection.disconnect();
        }

        logger.info("User not found for email {}", email);
        return null;
    }

}