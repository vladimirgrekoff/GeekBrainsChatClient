//Домашнее задание,уровень 3, урок 3: Владимир Греков
package com.grekoff.chat_client.models;

import java.io.*;

public class Message implements Serializable {
    private String time;
    private String sender;
    private String message;

    public Message(String time, String sender, String message) {
        this.time = time;
        this.sender = sender;
        this.message = message;
    }

    public String toString() {
        String returnString = "";

        if (time != null) {
            returnString = time + "\n";
        }

        returnString = returnString + sender + "\n" + message + "\n";

        return returnString;
    }


}
