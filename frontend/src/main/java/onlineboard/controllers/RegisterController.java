package onlineboard.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import onlineboard.MainApp;
import onlineboard.util.JsonConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class RegisterController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nicknameField;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String nickname = nicknameField.getText();

        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
            mainApp.showErrorAlert("エラー", "全ての項目を入力してください。");
            return;
        }

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("password", password);
        userData.put("nickname", new String(nickname.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        Map<String, String> request = new HashMap<>();
        request.put("command", "REGISTER");
        request.put("data", JsonConverter.toJson(userData));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);

            if ("OK".equals(responseMap.get("status"))) {
                mainApp.showUpdateNotification("ユーザー登録が完了しました。\nログイン画面からログインしてください。");
                mainApp.showLogin();
            } else {
                mainApp.showErrorAlert("エラー", "ユーザー登録に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "ユーザー登録中にエラーが発生しました: " + e.getMessage());
        }
    }
}