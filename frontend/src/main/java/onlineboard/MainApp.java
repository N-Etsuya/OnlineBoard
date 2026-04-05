package onlineboard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import onlineboard.network.Client;
import onlineboard.controllers.*;
import onlineboard.models.Thread;

import java.io.IOException;
import java.util.Map;

public class MainApp extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;
    private Client client;
    private Alert updateAlert;

    private ThreadListController threadListController;
    private ThreadViewController threadViewController;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Online Board");

        updateAlert = new Alert(Alert.AlertType.INFORMATION);
        updateAlert.setTitle("更新通知");
        updateAlert.setHeaderText(null);

        this.client = new Client("localhost", 8000, this);
        new java.lang.Thread(client).start();

        initRootLayout();
        showLogin();

        primaryStage.setOnCloseRequest(event -> {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (IOException e) {
                    System.err.println("Error during client disconnect: " + e.getMessage());
                }
            }
        });
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();
            Scene scene = new Scene(rootLayout);
            scene.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/LoginView.fxml"));
            VBox loginView = (VBox) loader.load();
            rootLayout.setCenter(loginView);

            LoginController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showThreadList() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/ThreadListView.fxml"));
            BorderPane threadListView = (BorderPane) loader.load();

            rootLayout.setCenter(threadListView);

            threadListController = loader.getController();
            threadListController.setMainApp(this);
            threadListController.refreshThreadList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showThreadView(Thread thread) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/ThreadView.fxml"));
            BorderPane threadView = (BorderPane) loader.load();
            rootLayout.setCenter(threadView);

            threadViewController = loader.getController();
            threadViewController.setMainApp(this);
            threadViewController.setThread(thread);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/RegisterView.fxml"));
            VBox registerView = (VBox) loader.load();
            rootLayout.setCenter(registerView);

            RegisterController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showCreateThread() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("views/CreateThreadView.fxml"));
            VBox createThreadView = (VBox) loader.load();
            rootLayout.setCenter(createThreadView);

            CreateThreadController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Client getClient() {
        return client;
    }

    public void handleServerUpdate(Map<String, Object> updateMap) {
        String eventType = (String) updateMap.get("event");
        Platform.runLater(() -> {
            switch (eventType) {
                case "NEW_THREAD":
                    showUpdateNotification("新しいスレッドが追加されました。");
                    break;
                case "NEW_POST":
                    String threadTitle = (String) updateMap.get("threadTitle");
                    showUpdateNotification("スレッド「" + threadTitle + "」に新しい投稿が追加されました。");
                    break;
                case "DELETE_THREAD":
                    showUpdateNotification("スレッドが削除されました。");
                    break;
                case "DELETE_POST":
                    String deletedPostThreadTitle = (String) updateMap.get("threadTitle");
                    showUpdateNotification("スレッド「" + deletedPostThreadTitle + "」の投稿が削除されました。");
                    break;
                default:
                    System.err.println("Unknown event type: " + eventType);
            }
        });
    }

    public void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText("エラーが発生しました");
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public void showUpdateNotification(String message) {
        Platform.runLater(() -> {
            updateAlert.setContentText(message);
            updateAlert.show();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}