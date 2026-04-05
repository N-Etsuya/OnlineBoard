package onlineboard.model;

import onlineboard.util.PasswordEncryptor;

public class User {
    private String email;
    private String hashedPassword;
    private String nickname;

    public User(String email, String hashedPassword, String nickname) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public boolean checkPassword(String password) {
        return PasswordEncryptor.checkPassword(password, hashedPassword);
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}