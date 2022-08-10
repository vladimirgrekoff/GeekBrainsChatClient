//Домашнее задание,уровень 3, урок 2: Владимир Греков
package com.grekoff.chat_client.controllers;

import java.text.DateFormat;
import java.util.List;
import java.util.ResourceBundle;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.models.Network;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import java.util.Date;

public class ChatController {

    @FXML
    private ListView<String> usersList;

    @FXML
    private Label usernameTitle;

    @FXML
    private TextArea listMessage;

    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;

    private Network network;

    @FXML
    private String selectedRecipient;

    private ChatClient chatClient;

    @FXML
    private ResourceBundle resources;


    private String userName;



    @FXML
    void initialize() {

        listMessage.setFocusTraversable(true);
        listMessage.setVisible(true);
        Platform.runLater(() -> {
            inputField.requestFocus();
        });

        sendButton.setOnAction(event -> sendMessage());
        inputField.setOnAction(event -> sendMessage());

        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }


    @FXML
    private void sendMessage() {
        String message = inputField.getText().trim();
        inputField.clear();
        inputField.requestFocus();
        if (!message.isEmpty()) {
            if (selectedRecipient != null) {
                network.sendPrivateMessage(selectedRecipient, message);
            } else {
                network.sendMessage(message);
            }
        }
    }

    public void appendServerMessage(String message) {
        listMessage.appendText(String.format("Сообщение: %s\n\n", message));
    }

    public void appendMessage(String sender, String message) {
        String timeStamp = DateFormat.getInstance().format(new Date());

        listMessage.appendText(timeStamp + "\n");
        listMessage.appendText(String.format("%s:\n%s\n\n", sender, message));
    }

    public void updateUsernameTitle() {
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

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    public ChatClient getChatClient() {
        return chatClient;
    }

    public void setUsersList(List<String> strUserList) {
        updateUsersList(strUserList);
    }
    public void updateUsersList(List<String> strUserList) {
        Platform.runLater(() -> {
            this.usersList.refresh();
            this.usersList.setItems(FXCollections.observableArrayList(strUserList));
            usersList.setFocusTraversable(true);
        });
    }
}