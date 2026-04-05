package onlineboard.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import onlineboard.MainApp;
import onlineboard.util.JsonConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            mainApp.showErrorAlert("エラー", "メールアドレスとパスワードを入力してください。");
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("command", "LOGIN");
        request.put("data", JsonConverter.toJson(Map.of("email", email, "password", password)));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);
            if ("OK".equals(responseMap.get("status"))) {
                mainApp.showThreadList();
            } else {
                mainApp.showErrorAlert("エラー", "ログインに失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "ログイン中にエラーが発生しました: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        mainApp.showRegister();
    }
}