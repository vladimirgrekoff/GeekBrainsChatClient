//Домашнее задание,уровень 2, урок 7: Владимир Греков
package com.grekoff.chat_client;

import com.grekoff.chat_client.controllers.ChatController;
import com.grekoff.chat_client.models.Network;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatClient extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClient.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Чат \"Просто чат\"! вер.2");
        stage.setScene(scene);
        stage.setX(800);
        stage.setY(200);
        stage.show();


        Network network = new Network();

        ChatController chatController = fxmlLoader.getController();

        network.setController(chatController);

        chatController.setNetwork(network);

        chatController.startNetwork(chatController);


        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                network.closeConnection();
                stage.close();
            }
        });
    }


    public static void main(String[] args) {
        launch();

    }
}
