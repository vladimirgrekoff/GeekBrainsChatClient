//Домашнее задание,уровень 3, урок 2: Владимир Греков
package com.grekoff.chat_client.models;

import com.grekoff.chat_client.ChatClient;
import com.grekoff.chat_client.controllers.ChatController;
import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Network {

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + password
    private static final String AUTH_OK_CMD_PREFIX = "/authOk"; // + username
    private static final String AUTH_ERR_CMD_PREFIX = "/authErr"; // + error message
    private static final String REG_CMD_PREFIX = "/reg"; // + login + password + username
    private static final String REG_OK_CMD_PREFIX = "/regOk"; // + entry
    private static final String REG_ERR_CMD_PREFIX = "/regErr"; // + error message
    private static final String REG_EDIT_CMD_PREFIX = "/regEdit"; // + login + password + newUsername
    private static final String REG_EDIT_OK_CMD_PREFIX = "/regEditOk"; // + newUsername
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
    public Timer timer;


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
            if (answer.startsWith(CONNECT_CMD_PREFIX)) {
                answer = answer.replaceAll(CONNECT_CMD_PREFIX, "");
                answer = answer.replaceAll("/", "").trim();
                StartTimer();
                chatClient.showInfoAlert("Сообщение сервера", answer, true);
                chatClient.startDialog();
            }

//            chatClient.startDialog();


        } catch (Exception e) {
//            e.printStackTrace();
//            chatController.appendMessage("СЕРВЕР НЕ ОТВЕЧАЕТ");
            chatClient.showErrorAlert("Ошибка подключения", "СОЕДИНЕНИЕ НЕ УСТАНОВЛЕНО", true);
            closeConnection();
        }
    }


    public void StartTimer() {
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Platform.runLater(() -> {
                    chatClient.showErrorAlert("Окончание времени до начала сеанса", "Время для выполнения входа в чат\nзакончилось. Соединение с сервером закрыто.", true);
                    closeConnection();
                    chatClient.getAuthStage().close();;
                    chatClient.getPrimaryStage().close();
                });
                timer.cancel();
            }
        }, 120 * 1000);
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
        int indexStart;
        int indexEnd;

        indexStart = incomingMessage.indexOf("/",0);
        indexEnd = incomingMessage.indexOf("/",indexStart+1);
        String typeMessage = incomingMessage.substring(0,indexEnd).trim();

        if (typeMessage.equalsIgnoreCase(SERVER_MSG_CMD_USERS_PREFIX)) {
            incomingMessage = incomingMessage.replaceAll(SERVER_MSG_CMD_USERS_PREFIX, "").trim() ;
            incomingMessage = incomingMessage.substring(1);
            String[] clientUser = incomingMessage.split("/");
            List<String> clients;
            String usersNames = "";
            if (clientUser.length >= 2) {
                for (int i=0; i < clientUser.length; i++){
                    if (clientUser[i].equals(this.username)){
                        clientUser[i] = "";
                    }
                }
                for (String s : clientUser) {
                    if (!s.equals("")) {
                        usersNames = usersNames + s + "/";
                    }
                }
                clients = Arrays.asList(usersNames.trim().split("/"));
                getController().setUsersList(clients);
            }

        } else if (typeMessage.equalsIgnoreCase(SERVER_ECHO_MSG_CMD_PREFIX)) {
            incomingMessage = incomingMessage.replaceAll(SERVER_ECHO_MSG_CMD_PREFIX, "").trim() ;
            incomingMessage = incomingMessage.substring(1);
            String[] echoMessage = incomingMessage.split("/");

            if (echoMessage.length >= 2) {
                String sender = "Я";
                if (echoMessage[0].contains("cMsg")) {
                    echoMessage[0] = "";
                    sender = sender + " в чат";
                } else if (echoMessage[0].contains("pm")) {
                    echoMessage[0] = "";
                    sender = sender + " для: " + echoMessage[1];
                    echoMessage[1] = "";
                }
                String message = (String.join(" ", echoMessage)).trim();
                chatController.appendMessage(sender, message);
            }

        } else if (typeMessage.equalsIgnoreCase(SERVER_MSG_CMD_PREFIX)) {
            incomingMessage = incomingMessage.replaceAll(SERVER_MSG_CMD_PREFIX, "").trim() ;
            incomingMessage = incomingMessage.substring(1);
            if (incomingMessage.contains("/")){
                incomingMessage = incomingMessage.replaceAll("/", " ");
            }
            String serverMessage = incomingMessage;
            chatController.appendServerMessage(serverMessage);

        } else if (typeMessage.equalsIgnoreCase(CLIENT_MSG_CMD_PREFIX)) {
            incomingMessage = incomingMessage.replaceAll(CLIENT_MSG_CMD_PREFIX, "").trim() ;
            incomingMessage = incomingMessage.substring(1);
            String[] parts = incomingMessage.split("/");

            String sender = parts[0] + " в чат";
            parts[0] = "";
            String clientMessage = (String.join(" ", parts)).trim();
            chatController.appendMessage(sender, clientMessage);

        } else if (typeMessage.equalsIgnoreCase(PRIVATE_MSG_CMD_PREFIX)) {
            incomingMessage = incomingMessage.replaceAll(PRIVATE_MSG_CMD_PREFIX, "").trim() ;
            incomingMessage = incomingMessage.substring(1);
            String[] parts = incomingMessage.split("/");

            String sender = "Вам пишет " + parts[0];
            parts[0] = "";

            String privateMessage = (String.join(" ", parts)).trim();
            chatController.appendMessage(sender, privateMessage);
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
                    transferMessage(String.format("%s/%s", CLIENT_MSG_CMD_PREFIX, message));
//                    transferMessage(message);
                }

                if (in == null) {
                    chatClient.showErrorAlert("Ошибка подключения","ВОССТАНОВИТЕ СОЕДИНЕНИЕ С СЕРВЕРОМ: /connect", false);
                }
            } catch (Exception e) {
//                e.printStackTrace();
                chatClient.showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
            }
        }
    }
    public void sendPrivateMessage(String recipient, String message) {
        try {
            out.writeUTF(String.format("%s/%s/%s", PRIVATE_MSG_CMD_PREFIX, recipient, message));
        } catch (IOException e) {
//            e.printStackTrace();
            chatClient.showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
        }
    }
    public String sendAuthMessage(String login, String password) {
        String result = "";
        int indexStart;
        int indexEnd;
        String answer;
        String typeMessage;
        if (timer != null) {
            timer.cancel();
        }
        try {
            out.writeUTF(String.format("%s/%s/%s", AUTH_CMD_PREFIX, login, password));
            do {
                answer = in.readUTF();
                indexStart = answer.indexOf("/",0);
                indexEnd = answer.indexOf("/",indexStart+1);
                typeMessage = answer.substring(0,indexEnd).trim();
                if (typeMessage.equalsIgnoreCase(AUTH_OK_CMD_PREFIX)) {
                    answer = answer.replaceAll(AUTH_OK_CMD_PREFIX, "").trim() ;
                    this.username = answer.substring(1);
                    chatController.setUserName(getUsername());
                    chatController.updateUsernameTitle();
                    result = null;
                } else if (!typeMessage.equalsIgnoreCase(SERVER_ECHO_MSG_CMD_PREFIX)) {
                    answer = answer.replaceAll(typeMessage, "").trim() ;
                    result = answer.substring(1);
                }
            } while (typeMessage.equalsIgnoreCase(SERVER_ECHO_MSG_CMD_PREFIX));
        } catch (IOException e) {
//            e.printStackTrace();
            getChatClient().showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
        }
        return result;
    }
    public String sendRegMessage(String login, String password, String username, boolean editUserName) {
        String answer = "";
        String typeMessage;
        if (timer != null) {
            timer.cancel();
        }
        try {
            if (editUserName) {
                typeMessage = REG_EDIT_CMD_PREFIX;
            } else {
                typeMessage = REG_CMD_PREFIX;
            }
            out.writeUTF(String.format("%s/%s/%s/%s", typeMessage, login, password, username));
            do {
                answer = in.readUTF();

                if (answer.startsWith(REG_OK_CMD_PREFIX) || answer.startsWith(REG_EDIT_OK_CMD_PREFIX) || answer.startsWith(REG_ERR_CMD_PREFIX)) {
                    break;
                }
//            } while (!answer.startsWith(REG_OK_CMD_PREFIX) || !answer.startsWith(REG_ERR_CMD_PREFIX));
            } while (answer.startsWith(SERVER_ECHO_MSG_CMD_PREFIX));
        } catch (IOException e) {
//            e.printStackTrace();
            getChatClient().showErrorAlert("Ошибка отправки сообщения","ОШИБКА ПРИ ОТПРАВКЕ СООБЩЕНИЯ", false);
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






