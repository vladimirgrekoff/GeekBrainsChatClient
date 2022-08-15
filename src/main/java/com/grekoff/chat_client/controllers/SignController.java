//Домашнее задание,уровень 3, урок 2: Владимир Греков
package com.grekoff.chat_client.controllers;

import java.io.IOException;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignController {
    private static final String REG_OK_CMD_PREFIX = "/regOk"; // + registration Ok message
    private static final String REG_ERR_CMD_PREFIX = "/regErr"; // + error message
    private static final String REG_EDIT_CMD_PREFIX = "/regEdit"; // + login + password + newUsername
    private static final String REG_EDIT_OK_CMD_PREFIX = "/regEditOk"; // + newUsername
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField loginReg;

    @FXML
    private TextField passReg;

    @FXML

    private CheckBox editUserName;

    @FXML
    private TextField usernameReg;

    private Network network;

    private ChatClient chatClient;

    private ChatController chatController;


    @FXML
    void initialize() {

    }

    @FXML
    void signUp(ActionEvent event) throws IOException {
        String login = loginReg.getText().trim();
        String password = passReg.getText().trim();
        String username = usernameReg.getText().trim();
        boolean editUsername = editUserName.isSelected();
        int indexStart;
        int indexEnd;




        if (login.length() == 0 || password.length() == 0 || username.length() == 0) {
            chatClient.showErrorAlert("Ошибка ввода при регистрации", "Поля не должны быть пустыми", false);

            return;
        }

        if (login.length() > 32 || password.length() > 32 || username.length() > 32) {
            chatClient.showErrorAlert("Ошибка ввода при регистрации", "Длина логина, пароля и имени в чате должны быть не более 32 символов",false);
            return;
        }



        String answerMessage = (network.sendRegMessage(login, password, username, editUsername));

        answerMessage = answerMessage.trim();
        indexStart = answerMessage.indexOf("/");
        indexEnd = answerMessage.indexOf("/", indexStart + 1);
        String typeMessage = answerMessage.substring(0, indexEnd);

        answerMessage = answerMessage.replaceAll(typeMessage, "");
        String regResultMessage = answerMessage.replaceAll("/", "").trim();

        if (typeMessage.equalsIgnoreCase(REG_EDIT_OK_CMD_PREFIX)) {
            loginReg.clear();
            passReg.clear();
            usernameReg.clear();
            chatClient.showInfoAlert("Правка профиля", "Имя пользователя изменено. Для входа в чат перейдите на вкладку \"Вход\"", true);

        } else if (typeMessage.equalsIgnoreCase(REG_OK_CMD_PREFIX)) {
            loginReg.clear();
            passReg.clear();
            usernameReg.clear();
            chatClient.showInfoAlert("Успешная регистрация", "Регистрация прошла успешно. Для входа в чат перейдите на вкладку \"Вход\"", true);

        } else {
            chatClient.showErrorAlert("Ошибка регистрации", regResultMessage, true);
        }
    }

    @FXML
    void checkAuth(ActionEvent event) throws IOException {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            chatClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не должны быть пустыми", false);
            return;
        }

        if (login.length() > 32 || password.length() > 32) {
            chatClient.showErrorAlert("Ошибка ввода при аутентификации", "Длина логина и пароля должны быть не более 32 символов",false);
            return;
        }

        String authErrorMessage = network.sendAuthMessage(login, password);

        if (authErrorMessage == null) {
            chatClient.setLogin(login);
            chatClient.openChatDialog();
        } else {
            chatClient.showErrorAlert("Ошибка аутентификации", authErrorMessage, true);
        }

    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Network getNetwork() {
        return network;
    }

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatClient getStartClient() {
        return chatClient;
    }

    public void setController(ChatController controller) {
        this.chatController = controller;
    }

    public ChatController getController() {
        return chatController;
    }
}
