//Домашнее задание,уровень 3, урок 3: Владимир Греков
package com.grekoff.chat_client.controllers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.models.Message;
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

    private String userName;

    private ArrayList<Message> historyMessage;



    @FXML
    void initialize() {

        historyMessage = new ArrayList<>();
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
            message = replaceServiceCharacters(message);
            if (selectedRecipient != null) {
                network.sendPrivateMessage(selectedRecipient, message);
        } else {
            if (message.contains("srv_cmd:")) {
                message = message.replace("srv_cmd:", "/");
            }
                network.sendMessage(message);
            }
        }
    }

    private String replaceServiceCharacters(String txtString) {
        // заменить символ "/" на "&#47;"
        String result;
        if (txtString.contains("/")) {
            result = txtString.replaceAll("/", "&#47;");
        } else {
            result = txtString;
        }
        return result;
    }

    private String restoreServiceCharacters(String txtString) {
        // заменить комбинацию "&#47;" на символ "/"
        String result;
        if (txtString.contains("&#47")) {
            result = txtString.replaceAll("&#47;", "/");
        } else {
            result = txtString;
        }
        return result;
    }

    public void appendServerMessage(String message) {
        listMessage.appendText(String.format("Сообщение: %s\n\n", message));
        Message msg = new Message(null, "Сообщение:", message);
        historyMessage.add(msg);
    }

    public void appendMessage(String sender, String message) {
        message = restoreServiceCharacters(message);
        String timeStamp = DateFormat.getInstance().format(new Date());

        listMessage.appendText(timeStamp + "\n");
        listMessage.appendText(String.format("%s:\n%s\n\n", sender, message));
        Message msg = new Message(timeStamp, sender, message);
        historyMessage.add(msg);
    }
    public void loadMessageFromFile() {
        String strHistory = "";

        for (Message e: historyMessage) {
            strHistory = strHistory + e.toString() + "\n";
        }
        listMessage.appendText(strHistory);
    }

    public ArrayList<Message> saveMessageToFile() {
        if (historyMessage.size() > 100) {
            int n = historyMessage.size() - 100;
            historyMessage.subList(0, n).clear();
        }

        return historyMessage;
    }

    public void updateUsernameTitle() {
        Platform.runLater(() -> {
            setUsernameTitle(getUserName());
        });
    }

    public ArrayList<Message> getHistoryMessage() {
        return historyMessage;
    }

    public void setHistoryMessage(ArrayList<Message> historyMessage) {
        this.historyMessage = historyMessage;
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