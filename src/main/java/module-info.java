module com.grekoff.chat_client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.grekoff.chat_client to javafx.fxml;
    exports com.grekoff.chat_client;
    exports com.grekoff.chat_client.controllers;
    opens com.grekoff.chat_client.controllers to javafx.fxml;
    exports com.grekoff.chat_client.models;
    opens com.grekoff.chat_client.models to javafx.fxml;
}