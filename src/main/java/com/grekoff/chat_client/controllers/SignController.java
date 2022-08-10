//Домашнее задание,уровень 2, урок 8: Владимир Греков
package com.grekoff.chat_client.controllers;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignController {
    private static final String REG_OK_CMD_PREFIX = "/regOk"; // + registration Ok message
    private static final String REG_ERR_CMD_PREFIX = "/regErr"; // + error message
    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField loginReg;

    @FXML
    private TextField passReg;

    @FXML
    private TextField usernameReg;
    private Network network;
    private ChatClient chatClient;


    @FXML
    void initialize() {

    }

    @FXML
    void signUp(ActionEvent event) {
        String login = loginReg.getText().trim();
        String password = passReg.getText().trim();
        String username = usernameReg.getText().trim();

        if (login.length() == 0 || password.length() == 0 || username.length() == 0) {
            System.out.println("Ошибка ввода при регистрации");
            System.out.println();
            chatClient.showErrorAlert("Ошибка ввода при регистрации", "Поля не должны быть пустыми", false);

            return;
        }

        if (login.length() > 32 || password.length() > 32 || username.length() > 32) {
            chatClient.showErrorAlert("Ошибка ввода при регистрации", "Длина логина, пароля и имени в чате должны быть не более 32 символов",false);
            return;
        }

        String[] answerMessage = (network.sendRegMessage(login, password, username)).split("\\s+");

        String typeMessage = answerMessage[0];
        answerMessage[0]="";
        String regResultMessage = String.join(" ", answerMessage);

        if (typeMessage.equalsIgnoreCase(REG_OK_CMD_PREFIX)) {
            loginReg.clear();
            passReg.clear();
            usernameReg.clear();
            chatClient.showInfoAlert("Успешная регистрация", "Регистрация прошла успешно. Для входа в чат перейдите на вкладку \"Вход\"", true);
        } else {
            chatClient.showErrorAlert("Ошибка регистрации", regResultMessage, true);
        }
    }

    @FXML
    void checkAuth(ActionEvent event) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0 || password.length() == 0) {
            System.out.println("Ошибка ввода при аутентификации");
            System.out.println();
            chatClient.showErrorAlert("Ошибка ввода при аутентификации", "Поля не должны быть пустыми", false);

            return;
        }

        if (login.length() > 32 || password.length() > 32) {
            chatClient.showErrorAlert("Ошибка ввода при аутентификации", "Длина логина и пароля должны быть не более 32 символов",false);
            return;
        }

        String authErrorMessage = network.sendAuthMessage(login, password);

        if (authErrorMessage == null) {
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
}
