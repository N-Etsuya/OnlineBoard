module onlineboard {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.net.http;
    requires com.google.gson;

    opens onlineboard to javafx.fxml, javafx.graphics;
    opens onlineboard.controllers to javafx.fxml;
    opens onlineboard.models to com.google.gson;

    exports onlineboard;
    exports onlineboard.controllers;
    exports onlineboard.models;
    exports onlineboard.util;
    exports onlineboard.network;
}