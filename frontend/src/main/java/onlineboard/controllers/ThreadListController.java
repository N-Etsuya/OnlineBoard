package onlineboard.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; 
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import onlineboard.MainApp;
import onlineboard.models.Thread;
import onlineboard.util.JsonConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadListController {
    @FXML
    private TableView<Thread> threadTable;
    @FXML
    private TableColumn<Thread, LocalDateTime> createdAtColumn;
    @FXML
    private TableColumn<Thread, String> titleColumn;

    private MainApp mainApp;
    private final ObservableList<Thread> threadList = FXCollections.observableArrayList();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        
        createdAtColumn.setCellFactory(column -> {
            TableCell<Thread, LocalDateTime> cell = new TableCell<Thread, LocalDateTime>() {
                private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty) {
                        setText(null);
                    }
                    else {
                        setText(formatter.format(item));
                    }
                }
            };
            return cell;
        });

        threadTable.setItems(threadList);
        threadTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Thread selectedThread = threadTable.getSelectionModel().getSelectedItem();
                if (selectedThread != null) {
                    mainApp.showThreadView(selectedThread);
                }
            }
        });
    }

    @FXML
    private void handleRefresh() {
        refreshThreadList();
    }

    public void refreshThreadList() {
        Map<String, String> request = new HashMap<>();
        request.put("command", "GET_THREADS");

        try {
            String responseJson = mainApp.getClient().sendRequestAndWait(JsonConverter.toJson(request));
            Map<String, Object> responseMap = JsonConverter.jsonToMap(responseJson);

            if ("OK".equals(responseMap.get("status"))) {
                List<Thread> threads = JsonConverter.jsonToThreadList(JsonConverter.toJson(responseMap.get("threads")));
                Platform.runLater(() -> {
                    threadList.clear();
                    threadList.addAll(threads);
                });
            } else {
                mainApp.showErrorAlert("エラー", "スレッド一覧の取得に失敗しました: " + responseMap.get("message"));
            }
        } catch (IOException | InterruptedException e) {
            mainApp.showErrorAlert("エラー", "スレッド一覧の取得中にエラーが発生しました: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateThread(ActionEvent event) {
        mainApp.showCreateThread();
    }
}