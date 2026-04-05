package onlineboard.service;

import onlineboard.model.User;
import onlineboard.repository.UserRepository;
import onlineboard.util.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized User register(String email, String password, String nickname) {
        logger.info("Registering user with email {}", email);

        if (userRepository.getUserByEmail(email) != null) {
            logger.warn("Registration failed: Email {} already exists", email);
            throw new RuntimeException("Email already exists");
        }
        if (!isValidEmail(email)) {
            logger.warn("Registration failed: Invalid email format {}", email);
            throw new RuntimeException("Invalid email format");
        }

        String hashedPassword = PasswordEncryptor.hashPassword(password);
        User user = new User(email, hashedPassword, nickname);
        userRepository.createUser(user);

        logger.info("User registered successfully: {}", user);
        return user;
    }

    public synchronized User login(String email, String password) {
        logger.info("Logging in user with email {}", email);
        User user = userRepository.getUserByEmail(email);

        if (user != null && user.checkPassword(password)) {
            logger.info("User logged in successfully: {}", user);
            return user;
        }

        logger.warn("Login failed: Invalid email or password for {}", email);
        throw new RuntimeException("Authentication failed");
    }

    public synchronized void logout(User user) {
        // ログアウト処理
        logger.info("User logged out: {}", user);
    }

    private synchronized boolean isValidEmail(String email) {
        // メールアドレスの形式を検証するロジックを実装
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}