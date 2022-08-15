//Домашнее задание,уровень 3, урок 3: Владимир Греков
package com.grekoff.chat_client;

import com.grekoff.chat_client.controllers.ChatController;
import com.grekoff.chat_client.controllers.SignController;
import com.grekoff.chat_client.models.Message;
import com.grekoff.chat_client.models.Network;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.util.ArrayList;

public class ChatClient extends Application {
    private Network network;
    private Stage primaryStage;
    private Stage authStage;
    private ChatController chatController;

    private SignController signController;
    private String login;
    private ObjectOutputStream oos;
    private FileOutputStream fos;

    @Override
    public void start(Stage stage) {
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

    //    public void createChatDialog(Stage stage) throws IOException {
    public void createChatDialog() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClient.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("Чат \"Просто чат\"! вер.3");
        primaryStage.setScene(scene);
        primaryStage.setX(600);
        primaryStage.setY(200);
//        primaryStage.show();




//        Network network = new Network();
        chatController = fxmlLoader.getController();
        network.setController(chatController);
        chatController.setNetwork(network);
        chatController.setChatClient(this);
//        chatController.startNetwork(chatController);


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                saveMessageToFile(chatController.saveMessageToFile());
                network.closeConnection();
                primaryStage.close();
            }
        });
    }

    public void openChatDialog() throws IOException {
        authStage.close();
        primaryStage.show();
//        primaryStage.setTitle(network.getUsername());
        primaryStage.setTitle("Чат-клиент! вер.4");

        network.waitMessage(); //network.waitMessage(chatController);
        chatController.setUsernameTitle(network.getUsername());

        File path = new File("src/main/resources/lib");
        File file = new File("src/main/resources/lib/history_" + getLogin() + ".txt");

        if (notExists(path)){
            if (file.mkdirs()) {
                if (notExists(file)){
                    file.createNewFile();
                }
            }
        }


        loadMessagesFromFile(file);
        fos = new FileOutputStream(file);
    }

    public static boolean notExists(File file) {
        return !file.exists();
    }
    private void loadMessagesFromFile(File file) {

        try {
            FileInputStream is = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(is);

            ArrayList<Message> historyMessages = (ArrayList<Message>) ois.readObject();
            if (!(historyMessages == null)) {
                chatController.setHistoryMessage(historyMessages);
                chatController.loadMessageFromFile();
            }
            is.close();
            ois.close();
        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.out.println("конец файла");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveMessageToFile(ArrayList<Message> historyMessages) {

        try  {
            oos = new ObjectOutputStream(fos);
            oos.writeObject(historyMessages);

        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Network getNetwork() {
        return network;
    }

    public static void main(String[] args) {
        launch();
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
