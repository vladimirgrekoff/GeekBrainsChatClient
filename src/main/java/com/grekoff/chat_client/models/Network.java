//Домашнее задание,уровень 2, урок 8: Владимир Греков
package com.grekoff.chat_client.models;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.controllers.ChatController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTH_OK_CMD_PREFIX = "/authOk"; // + username
    private static final String AUTH_ERR_CMD_PREFIX = "/authErr"; // + error message
    private static final String REG_CMD_PREFIX = "/reg"; // + login + password + username
    private static final String REG_OK_CMD_PREFIX = "/regOk"; // + entry
    private static final String REG_ERR_CMD_PREFIX = "/regErr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_ECHO_MSG_CMD_PREFIX = "/echo"; // + msg

    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg

    private static final String SERVER_MSG_CMD_USERS_PREFIX = "/userList"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/pm"; // + username + msg
    private static final String CONNECT_CMD_PREFIX = "/connect"; // + login + password
    private static final String STOP_SERVER_CMD_PREFIX = "/stop";
    private static final String END_CLIENT_CMD_PREFIX = "/end";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 8888;

    private final String host;
    private final int port;

    public Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private ChatController chatController;

    private String username;

    private ChatClient chatClient;




    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Network() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void connect() {
        try {
            socket = new Socket(host, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


            String answer = in.readUTF();
            if (answer.startsWith(CONNECT_CMD_PREFIX)){
                chatClient.showInfoAlert("Сообщение сервера", answer.split("\\s+", 2)[1], true);
                chatClient.startDialog();
            }

        } catch (Exception e) {
//            chatController.appendMessage("СЕРВЕР НЕ ОТВЕЧАЕТ");
            chatClient.showErrorAlert("Ошибка подключения","СОЕДИНЕНИЕ НЕ УСТАНОВЛЕНО",true);

            closeConnection();


        }
    }


    public void waitMessage() {

        Thread t = new Thread(() -> {
            String strFromServer;
            try {
                while (true) {
                    if (in == null) {
                        break;
                    }
                    strFromServer = in.readUTF();
                    if (strFromServer.equalsIgnoreCase(END_CLIENT_CMD_PREFIX)) {
                        break;
                    }
                    if (strFromServer != null || !strFromServer.isEmpty()) {
                        parsingMessage(strFromServer);
                    }
                }
            } catch (IOException e) {
                chatController.appendServerMessage("СОЕДИНЕНИЕ С СЕРВЕРОМ ОТСУТСТВУЕТ");
            }
        });
        t.start();
    }

    private void parsingMessage(String incomingMessage) {

        String typeMessage = incomingMessage.split("\\s+")[0];

        if (typeMessage.equalsIgnoreCase(SERVER_MSG_CMD_USERS_PREFIX)) {
            String[] clientUser = incomingMessage.split("\\s+");
            List<String> clients;
            if (clientUser.length >= 2) {
                clientUser[0] = "";
                for (int i=0; i < clientUser.length; i++){
                    if (clientUser[i].equals(this.username)){
                        clientUser[i]="";
                    }
                }
                clients = Arrays.asList((String.join(" ", clientUser).trim()).split("\\s+"));
                getController().setUsersList(clients);
            }
        } else if (typeMessage.equalsIgnoreCase(SERVER_ECHO_MSG_CMD_PREFIX)) {
            String[] echoMessage = incomingMessage.split("\\s+");
            if (echoMessage.length >= 2) {
                String sender = "Я";
                echoMessage[0] = "";
                String message = String.join(" ", echoMessage).trim();
                chatController.appendMessage(sender, message);
            }
        } else if (typeMessage.equalsIgnoreCase(CLIENT_MSG_CMD_PREFIX)) {
            String[] parts = incomingMessage.split("\\s+", 3);
            String sender = parts[1];
            String messageFromSender = parts[2];
            chatController.appendMessage(sender, messageFromSender);

        } else if (typeMessage.equalsIgnoreCase(PRIVATE_MSG_CMD_PREFIX)) {
            String[] parts = incomingMessage.split("\\s+", 3);
            String sender = parts[1];
            String messageFromSender = parts[2];
            chatController.appendMessage("Вам пишет " + sender, messageFromSender);

        } else if (typeMessage.equalsIgnoreCase(SERVER_MSG_CMD_PREFIX)) {
            String[] parts = incomingMessage.split("\\s+", 2);
            String serverMessage = parts[1];
            chatController.appendServerMessage(serverMessage);
        }
    }

    public void transferMessage(String message) {

        try {
            if (message != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            chatClient.showErrorAlert("Ошибка отправки сообщения","ОШИБКА ОТПРАВКИ СООБЩЕНИЯ",false);
        }
    }
    public void closeConnection() {
        try {
            if (out != null) {
                out.writeUTF("/end");
                out.flush();
            }

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {
            chatClient.showInfoAlert("Завершение сеанса","СОЕДИНЕНИЕ ЗАКРЫТО",true);
        }
    }

    public void sendMessage(String message) {
        if (message.equals(CONNECT_CMD_PREFIX)) {//если сервер запущен после клиента
            try {
                connect();
                waitMessage();
                transferMessage(message);
            } catch (RuntimeException e) {
                chatClient.showErrorAlert("Ошибка подключения","НЕ УДАЛОСЬ УСТАНОВИТЬ СОЕДИНЕНИЕ С СЕРВЕРОМ",false);
            }
        } else if (!message.isEmpty()) {
            try {
                if (out != null) {
                    transferMessage(message);
                }

                if (in == null) {
                    chatClient.showErrorAlert("Ошибка подключения","ВОССТАНОВИТЕ СОЕДИНЕНИЕ С СЕРВЕРОМ: /connect", false);
                }
            } catch (Exception e) {
                chatClient.showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
            }
        }
    }
    public void sendPrivateMessage(String recipient, String message) {
        try {
            out.writeUTF(String.format("%s %s %s", PRIVATE_MSG_CMD_PREFIX, recipient, message));
        } catch (IOException e) {
            chatClient.showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
        }
    }
    public String sendAuthMessage(String login, String password) {
        String result = "";
        try {
            out.writeUTF(String.format("%s %s %s", AUTH_CMD_PREFIX, login, password));
            String answer;
            do {
                answer = in.readUTF();
                if (answer.startsWith(AUTH_OK_CMD_PREFIX)) {
                    this.username = answer.split("\\s+", 2)[1];
                    chatController.setUserName(getUsername());
                    chatController.updateUsernameTitle();
                    result = null;
                } else if (!answer.startsWith(SERVER_ECHO_MSG_CMD_PREFIX)) {
                    result = answer.split("\\s+", 2)[1];
                }
            } while (answer.startsWith(SERVER_ECHO_MSG_CMD_PREFIX));
        } catch (IOException e) {
//            e.printStackTrace();
            getChatClient().showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
//            return e.getMessage();
        }
        return result;
    }
    public String sendRegMessage(String login, String password, String username) {
        String answer = "";
        try {
            out.writeUTF(String.format("%s %s %s %s", REG_CMD_PREFIX, login, password, username));
            do {
                answer = in.readUTF();
                if (answer.startsWith(REG_OK_CMD_PREFIX) || answer.startsWith(REG_ERR_CMD_PREFIX)){
                    break;
                }
            } while (answer.startsWith(SERVER_ECHO_MSG_CMD_PREFIX));
        } catch (IOException e) {
//            e.printStackTrace();
            getChatClient().showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
//            return e.getMessage();
        }
        return answer;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void setIn(DataInputStream in) {
        this.in = in;
    }

    public String getUsername() {
        return username;
    }

    public void setController(ChatController controller) {
        this.chatController = controller;
    }

    public ChatController getController() {
        return chatController;
    }

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatClient getChatClient() {
        return chatClient;
    }


}






