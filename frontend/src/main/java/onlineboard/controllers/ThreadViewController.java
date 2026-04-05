package onlineboard.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import onlineboard.MainApp;
import onlineboard.models.Post;
import onlineboard.models.Thread;
import onlineboard.util.JsonConverter;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class ThreadViewController {
    @FXML
    private Label threadTitleLabel;
    @FXML
    private VBox postsContainer;
    @FXML
    private TextArea messageArea;

    private MainApp mainApp;
    private Thread thread;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
        threadTitleLabel.setText(thread.getTitle());
        fetchAndDisplayPosts();
    }

    @FXML
    private void handleRefresh() {
        fetchAndDisplayPosts();
    }

    @FXML
    private void handleBack() {
        mainApp.showThreadList();
    }

    public void fetchAndDisplayPosts() {
        Map<String, String> request = new HashMap<>();
        request.put("command", "GET_POSTS");
        request.put("data", JsonConverter.toJson(thread.getThreadId()));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);

            if ("OK".equals(responseMap.get("status"))) {
                List<Post> posts = JsonConverter.jsonToPostList(JsonConverter.toJson(responseMap.get("posts")));
                Platform.runLater(() -> {
                    postsContainer.getChildren().clear();
                    for (Post post : posts) {
                        postsContainer.getChildren().add(createPostView(post));
                    }
                });
            } else {
                mainApp.showErrorAlert("エラー", "投稿の取得に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "投稿の取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    private VBox createPostView(Post post) {
        VBox postBox = new VBox(5);
        postBox.getStyleClass().add("post-box");

        HBox headerBox = new HBox(10);
        Label nicknameLabel = new Label(post.getNickname());
        nicknameLabel.getStyleClass().add("post-nickname");
        Label dateLabel = new Label(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dateLabel.getStyleClass().add("post-date");
        headerBox.getChildren().addAll(nicknameLabel, dateLabel);

        TextFlow contentFlow = new TextFlow(new Text(post.getContent()));
        contentFlow.getStyleClass().add("post-content");

        Button deleteButton = new Button("削除");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> handleDeletePost(post));

        postBox.getChildren().addAll(headerBox, contentFlow, deleteButton);
        VBox.setVgrow(contentFlow, Priority.ALWAYS);

        return postBox;
    }

    @FXML
    private void handleSend() {
        String content = messageArea.getText();
        if (content.isEmpty()) {
            mainApp.showErrorAlert("エラー", "メッセージを入力してください。");
            return;
        }

        Map<String, String> postData = new HashMap<>();
        postData.put("threadId", thread.getThreadId());
        postData.put("content", new String(content.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));

        Map<String, String> request = new HashMap<>();
        request.put("command", "CREATE_POST");
        request.put("data", JsonConverter.toJson(postData));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);
            if ("OK".equals(responseMap.get("status"))) {
                messageArea.clear();
                fetchAndDisplayPosts();
            } else {
                mainApp.showErrorAlert("エラー", "投稿に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "投稿中にエラーが発生しました: " + e.getMessage());
        }
    }

    private void handleDeletePost(Post post) {
        Map<String, String> request = new HashMap<>();
        request.put("command", "DELETE_POST");
        request.put("data", post.getPostId());

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);

            if ("OK".equals(responseMap.get("status"))) {
                fetchAndDisplayPosts();
            } else {
                mainApp.showErrorAlert("エラー", "投稿の削除に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "投稿の削除中にエラーが発生しました: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteThread() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("スレッド削除の確認");
        alert.setHeaderText("スレッド「" + thread.getTitle() + "」を削除します");
        alert.setContentText("このスレッドを削除してもよろしいですか？この操作は取り消せません。");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteThread();
            }
        });
    }

    private void deleteThread() {
        Map<String, String> request = new HashMap<>();
        request.put("command", "DELETE_THREAD");
        request.put("data", JsonConverter.toJson(thread.getThreadId()));

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);
            if ("OK".equals(responseMap.get("status"))) {
                Platform.runLater(() -> mainApp.showThreadList());
            } else {
                mainApp.showErrorAlert("エラー", "スレッドの削除に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "スレッドの削除中にエラーが発生しました: " + e.getMessage());
        }
    }
}