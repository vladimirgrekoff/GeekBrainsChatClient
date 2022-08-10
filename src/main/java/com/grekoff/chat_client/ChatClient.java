//Домашнее задание,уровень 2, урок 8: Владимир Греков
package com.grekoff.chat_client;

import com.grekoff.chat_client.controllers.ChatController;
import com.grekoff.chat_client.controllers.SignController;
import com.grekoff.chat_client.models.Network;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class ChatClient extends Application {
    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;

    private SignController signController;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        network = new Network();
        network.setChatClient(this);
        network.connect();

//        openAuthDialog();
//        createChatDialog();
    }

    public void startDialog()throws IOException{;
        openAuthDialog();
        createChatDialog();
    }
    private void openAuthDialog() throws IOException {
        FXMLLoader authLoader = new FXMLLoader(ChatClient.class.getResource("auth-view.fxml"));
        authStage = new Stage();
        Scene scene = new Scene(authLoader.load(), 600, 400);

        authStage.setScene(scene);
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        authStage.setTitle("Авторизация!");
        authStage.setY(200);
        authStage.setX(600);
//        authStage.setAlwaysOnTop(true);
        authStage.show();

        SignController signController = authLoader.getController();

        signController.setNetwork(network);
        signController.setChatClient(this);
    }


    public void createChatDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClient.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Чат \"Просто чат\"! вер.3");
        primaryStage.setScene(scene);
        primaryStage.setX(600);
        primaryStage.setY(200);
//        primaryStage.show();





        chatController = fxmlLoader.getController();
        network.setController(chatController);
        chatController.setNetwork(network);
        chatController.setChatClient(this);


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                network.closeConnection();
                primaryStage.close();
            }
        });
    }

    public void openChatDialog() {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle("Чат-клиент! вер.3");

        network.waitMessage();
        chatController.setUsernameTitle(network.getUsername());
    }

    public void showErrorAlert(String title, String errorMessage, boolean action) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(errorMessage);
        if (action) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public void showInfoAlert(String title, String infoMessage, boolean action) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(infoMessage);
        if (action) {
            alert.showAndWait();
        } else {
            alert.show();
        }
    }

    public Stage getAuthStage() {
        return authStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();

    }
}
