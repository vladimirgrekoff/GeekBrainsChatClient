//Домашнее задание,уровень 2, урок 7: Владимир Греков
package com.grekoff.chat_client.models;

import com.grekoff.chat_client.controllers.ChatController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTH_OK_CMD_PREFIX = "/authOk"; // + username
    private static final String AUTH_ERR_CMD_PREFIX = "/authErr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/cMsg"; // + msg
    private static final String SERVER_ECHO_MSG_CMD_PREFIX = "/echo"; // + msg

    private static final String SERVER_MSG_CMD_PREFIX = "/sMsg"; // + msg
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

    public ChatController getController() {
        return chatController;
    }
    public void setController(ChatController controller) {
        this.chatController = controller;
    }

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

        } catch (Exception e) {
//            e.printStackTrace();
            chatController.appendMessage("СЕРВЕР НЕ ОТВЕЧАЕТ");
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
                chatController.appendMessage("СОЕДИНЕНИЕ С СЕРВЕРОМ ОТСУТСТВУЕТ");
            }
        });
        t.start();
    }

    private void parsingMessage(String serverMessage) {
        if (serverMessage != null && serverMessage.startsWith(AUTH_OK_CMD_PREFIX)) {
            String[] clientUser = serverMessage.split("\\s+");
            if (clientUser.length >= 2) {
                chatController.setUserName(clientUser[1]);
                chatController.updateScene();
                if (!chatController.getUserName().equals(clientUser[1])) {
                    chatController.updateScene();
                }
                chatController.appendMessage(serverMessage);
            }
        } else if (serverMessage != null && serverMessage.startsWith(SERVER_ECHO_MSG_CMD_PREFIX)) {
            String[] echoMessage = serverMessage.split("\\s+");
            if (echoMessage.length >= 2) {
                echoMessage[0] = "Я:";
                serverMessage = String.join(" ", echoMessage);
                chatController.appendMessage(serverMessage);
            }
        } else if (serverMessage != null){
            chatController.appendMessage(serverMessage);
        }
    }

    public void transferMessage(String message) {

        try {
            if (message != null) {
                out.writeUTF(message);
                out.flush();
            }
        } catch (IOException e) {
            chatController.appendMessage("ОШИБКА ОТПРАВКИ СООБЩЕНИЯ");
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
            chatController.appendMessage("СОЕДИНЕНИЕ ЗАКРЫТО");
        }
    }

    public void sendMessage(String message) {
        if (message.equals(CONNECT_CMD_PREFIX)) {//если сервер запущен после клиента
            try {
                connect();
                waitMessage();
                transferMessage(message);
            } catch (RuntimeException e) {
                chatController.appendMessage("НЕ УДАЛОСЬ УСТАНОВИТЬ СОЕДИНЕНИЕ С СЕРВЕРОМ");
            }
        } else if (!message.isEmpty()) {
            try {
                if (out != null) {
                    transferMessage(message);
                }
//                } else {
                if (in == null) {
                    chatController.appendMessage("ВОССТАНОВИТЕ СОЕДИНЕНИЕ С СЕРВЕРОМ: /connect");
                }
            } catch (Exception e) {
//                e.printStackTrace();
                chatController.appendMessage("ОШИБКА ОТПРАВКИ СООБЩЕНИЯ");
            }
        }
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
}





