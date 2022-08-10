//Домашнее задание,уровень 2, урок 7: Владимир Греков
package com.grekoff.chat_client.controllers;

import com.grekoff.chat_client.models.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatController {
    @FXML
    private TextField inputField;
    @FXML
    private TextArea listMessage;
    @FXML
    private Button sendButton;

    @FXML
    private Label usernameTitle;

    private String userName;
    private Network network;



    @FXML
    void initialize() {
        listMessage.setFocusTraversable(true);
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
//                inputField.requestFocus();
//            }
//        });
        Platform.runLater(() -> {
            inputField.requestFocus();
        });

        sendButton.setOnAction(event -> sendMessage());
        inputField.setOnAction(event -> sendMessage());

    }
    public void startNetwork(ChatController chatController) throws RuntimeException {

        try {
            network.connect();
            network.waitMessage();
        } catch (RuntimeException e) {
//            e.printStackTrace();
            appendMessage("СЕРВЕР НЕ ОТВЕЧАЕТ");
        }
    }



    @FXML
    private void sendMessage() {
        String message = inputField.getText().trim();
        inputField.clear();
//        inputField.setFocusTraversable(true);///////////////////
        inputField.requestFocus();
        if (!message.isEmpty()) {
            network.sendMessage(message);
        }
    }



    public void appendMessage(String message) {
//        if (message != null && message.startsWith("/authOk")) {
//            String[] clientUser = message.split("\\s");
//            if (clientUser.length >= 2) {
//                setUserName(clientUser[1]);
//                updateScene();
//            }
//        }
        if (message.contains("\n")) {
            message = message.substring(message.length() - 2);
        }
        String dateFormatString = new SimpleDateFormat("d MMM, HH:mm:ss").format(new Date());
        listMessage.appendText(String.format("%s\t\t%s\n", message, dateFormatString));
    }

    public void updateScene() {
        Platform.runLater(() -> {
            setUsernameTitle(getUserName());
        });
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUsernameTitle(String userName) {
        this.usernameTitle.setText(userName);
    }

}