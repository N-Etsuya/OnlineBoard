package onlineboard.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import onlineboard.MainApp;
import onlineboard.util.JsonConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class CreateThreadController {
    @FXML
    private TextField titleField;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleCreate() {
        String title = titleField.getText();
        if (title.isEmpty()) {
            mainApp.showErrorAlert("エラー", "タイトルを入力してください。");
            return;
        }

        Map<String, String> request = new HashMap<>();
        request.put("command", "CREATE_THREAD");
        request.put("data", JsonConverter.toJson(new String(title.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);

            if ("OK".equals(responseMap.get("status"))) {
                Platform.runLater(() -> mainApp.showThreadList());
            } else {
                mainApp.showErrorAlert("エラー", "スレッドの作成に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "スレッドの作成中にエラーが発生しました: " + e.getMessage());
        }
    }
}