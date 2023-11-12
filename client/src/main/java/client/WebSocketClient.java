package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import client.messaging.Message;


public class WebSocketClient {

    private StompSession stompSession;
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final int port = 8080;
    private final String serverUrl = "http://localhost:" + port + "/api/";

    private ArrayList<String> acceptedUsers = new ArrayList<>();

    private ArrayList<Message> messagesCached = new ArrayList<>();

    public static void main(String[] args) {
        WebSocketClient client = new WebSocketClient();
        client.connectAndSend();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.logout();
        }));
    }

    public void connectAndSend() {

        performLoginOrRegister();
        printLogoMessage();

        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        try {
            stompSession = stompClient.connectAsync("ws://localhost:" + port + "/ws", sessionHandler).get();

            // Subscribe to the /topic/messages to receive messages from the server
            stompSession.subscribe("/topic/messages/" + username, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class; // Specify the payload type as byte[]
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    byte[] messageBytes = (byte[]) payload;
                    Message message = deserializeMessage(messageBytes);
                    if (message.getIsPrivate()) {
                        if (message.getSender().equals(username)) {
                            printColored("You sent a private message to " + message.getRecipient() + ": " + message.getContent(), "\u001B[35m");
                        } else {
                            boolean accepted = false;
                            for (String acceptedUser : acceptedUsers) {
                                if (acceptedUser.equals(message.getSender())) {
                                    accepted = true;
                                    break;
                                }
                            }
                            if (accepted) {
                                printColored(message.getSender() + " sent a private message to you: " + message.getContent(), "\u001B[35m");
                            } else {
                                messagesCached.add(message);
                                printColored(message.getSender() + " wants to send you a private message. Type '\u001B[32m/accept " + message.getSender() + "\u001B[0m' to accept.", "\u001B[31m");
                            }
                        }
                    } else {
                        if (message.getSender().equals(username)) {
                            printColored("You sent a group message to " + message.getRecipient() + ": " + message.getContent(), "\u001B[35m");
                        } else {
                            printColored(message.getSender() + " sent a group message to " + message.getRecipient() + ": " + message.getContent(), "\u001B[31m");
                        }
                    }
                }
            });

            while (true) {
                CommandInterface();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CommandInterface() {
        Scanner scanner = new Scanner(System.in);
        printColored("Enter a command: ", "\u001B[32m");
        String command = scanner.nextLine();
        String[] commandSplit = command.split(" ");
        if (commandSplit.length == 0) {
            printColored("Invalid command (not enough arguments).", "\u001B[31m");
            return;
        }
        String commandName = commandSplit[0];
        switch (commandName) {
            case "/help" -> help();
            case "/private_chat" -> {
                if (commandSplit.length < 3) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String recipient = commandSplit[1];
                String content = command.substring(command.indexOf(recipient) + recipient.length() + 1);
                privateChat(recipient, content);
            }
            case "/send_group" -> {
                if (commandSplit.length < 3) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String groupName = commandSplit[1];
                String sendGroupContent = command.substring(command.indexOf(groupName) + groupName.length() + 1);
                sendGroup(groupName, sendGroupContent);
            }
            case "/create_group" -> {
                if (commandSplit.length < 2) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String createGroupName = commandSplit[1];
                createGroup(createGroupName);
            }
            case "/add_user_to_group" -> {
                if (commandSplit.length < 3) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String addUserGroupName = commandSplit[1];
                String addUserUsername = commandSplit[2];
                addUserToGroup(addUserGroupName, addUserUsername);
            }
            case "/remove_user_from_group" -> {
                if (commandSplit.length < 3) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String removeUserGroupName = commandSplit[1];
                String removeUserUsername = commandSplit[2];
                removeUserFromGroup(removeUserGroupName, removeUserUsername);
            }
            case "/online_users" -> onlineUsers();
            case "/accept" -> {
                if (commandSplit.length < 2) {
                    printColored("Invalid command (not enough arguments).", "\u001B[31m");
                    return;
                }
                String acceptUsername = commandSplit[1];
                acceptUser(acceptUsername);
            }
            case "/logout" -> logout();
            default -> printColored("Invalid command.", "\u001B[31m");
        }
    }

    private void help() {
        printColored("Commands:", "\u001B[32m");
        printColored("/help", "\u001B[32m");
        printColored("/private_chat <recipient> <content>", "\u001B[32m");
        printColored("/send_group <group_name> <content>", "\u001B[32m");
        printColored("/create_group <group_name>", "\u001B[32m");
        printColored("/add_user_to_group <group_name> <username>", "\u001B[32m");
        printColored("/remove_user_from_group <group_name> <username>", "\u001B[32m");
        printColored("/online_users", "\u001B[32m");
        printColored("/accept <username>", "\u001B[32m");
        printColored("/logout", "\u001B[32m");
    }

    private void acceptUser(String username) {
        acceptedUsers.add(username);
        for (Message message : messagesCached) {
            if (message.getSender().equals(username)) {
                printColored(message.getSender() + " sent a private message to you: " + message.getContent(), "\u001B[31m");
            }
        }
    }

    private void privateChat(String recipient, String content) {
        Message message = new Message(username, recipient, content, true);
        byte[] bytesMessage = serializeMessage(message);
        stompSession.send("/app/send-message", bytesMessage);
    }

    private void sendGroup(String groupName, String content) {
        Message message = new Message(username, groupName, content, false);
        byte[] bytesMessage = serializeMessage(message);
        stompSession.send("/app/send-group-message", bytesMessage);
    }

    private void createGroup(String groupName) {
        String createGroupUrl = serverUrl + "/create-group?groupName=" + groupName;
        ResponseEntity<String> responseEntity = restTemplate.exchange(createGroupUrl, HttpMethod.POST, null, String.class);
        printColored(responseEntity.getBody(), "\u001B[32m");
        addUserToGroup(groupName, username);
    }

    private void addUserToGroup(String groupName, String username) {
        String addUserToGroupUrl = serverUrl + "/add-user-to-group?groupName=" + groupName + "&username=" + username;
        ResponseEntity<String> responseEntity = restTemplate.exchange(addUserToGroupUrl, HttpMethod.POST, null, String.class);
        printColored(responseEntity.getBody(), "\u001B[32m");
    }


    private void removeUserFromGroup(String groupName, String username) {
        String removeUserFromGroupUrl = serverUrl + "/remove-user-from-group?groupName=" + groupName + "&username=" + username;
        ResponseEntity<String> responseEntity = restTemplate.exchange(removeUserFromGroupUrl, HttpMethod.POST, null, String.class);
        printColored(responseEntity.getBody(), "\u001B[32m");
    }

    private void onlineUsers() {
        String onlineUsersUrl = serverUrl + "/online-users";
        ResponseEntity<String> responseEntity = restTemplate.exchange(onlineUsersUrl, HttpMethod.POST, null, String.class);
        String[] body = responseEntity.getBody().split(" ");
        for (String user : body){
            printColored(user, "\u001B[36m");
        }
    }

    private void logout() {
        String logoutUrl = serverUrl + "/logout?username=" + username;
        ResponseEntity<String> responseEntity = restTemplate.exchange(logoutUrl, HttpMethod.POST, null, String.class);
        printColored(responseEntity.getBody(), "\u001B[32m");
        System.exit(0);
    }

    private void printLogoMessage() {
        System.out.println("\u001B[36m   _____       _            _   _        __  __                                          ");
        System.out.println("  / ____|     | |          | | (_)      |  \\/  |                                         ");
        System.out.println(" | |  __  __ _| | __ _  ___| |_ _  ___  | \\  / | ___  ___ ___  ___ _ __   __ _  ___ _ __ ");
        System.out.println(" | | |_ |/ _` | |/ _` |/ __| __| |/ __| | |\\/| |/ _ \\/ __/ __|/ _ \\ '_ \\ / _` |/ _ \\ '__|");
        System.out.println(" | |__| | (_| | | (_| | (__| |_| | (__  | |  | |  __/\\__ \\__ \\  __/ | | | (_| |  __/ |   ");
        System.out.println("  \\_____|\\__,_|_|\\__,_|\\___|\\__|_|\\___| |_|  |_|\\___||___/___/\\___|_| |_|\\__, |\\___|_|   ");
        System.out.println("                                                                          __/ |           ");
        System.out.println("                                                                         |___/            ");
        System.out.println("\u001B[33mWelcome to Galactic Messenger!\u001B[0m");
        System.out.println("Type '\u001B[32m/help\u001B[0m' for a list of commands.");
    }

    private void printColored(String message, String colorCode) {
        System.out.println(colorCode + message + "\u001B[0m");
    }

    private void performLoginOrRegister() {
        Scanner scanner = new Scanner(System.in);
        Boolean loggedIn = false;
        // Perform login or register based on user input
        printColored("Type '\u001B[32mlogin\u001B[0m' to login or '\u001B[32mregister\u001B[0m' to register.", "\u001B[33m");
        while (!loggedIn) {
            String choice = scanner.nextLine();

            if ("login".equalsIgnoreCase(choice)) {
                printColored("Enter your username: ", "\u001B[33m");
                String username = scanner.nextLine();
                printColored("Enter your password: ", "\u001B[33m");
                String password = scanner.nextLine();
                loggedIn = login(username, password);
                this.username = username;
            } else if ("register".equalsIgnoreCase(choice)) {
                printColored("Enter a username: ", "\u001B[33m");
                String username = scanner.nextLine();
                printColored("Enter a password: ", "\u001B[33m");
                String password = scanner.nextLine();
                loggedIn = register(username, password);
                this.username = username;
                if (loggedIn) {
                    loggedIn = login(username, password);
                }
            } else {
                printColored("Invalid choice.", "\u001B[31m");
            }
        }
    }

    private boolean login(String username, String password) {
        String loginUrl = serverUrl + "login?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, null, String.class);
        if (responseEntity.getBody().equals("Login successful.")) {
            printColored(responseEntity.getBody(), "\u001B[32m");
            return true;
        } else {
            printColored(responseEntity.getBody(), "\u001B[31m");
            return false;
        }
    }

    private boolean register(String username, String password) {
        String registerUrl = serverUrl + "register?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(registerUrl, HttpMethod.POST, null, String.class);
        if (responseEntity.getBody().equals("Registration successful.")) {
            printColored(responseEntity.getBody(), "\u001B[32m");
            return true;
        } else {
            printColored(responseEntity.getBody(), "\u001B[31m");
            return false;
        }
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket Server");
        }
    }

    // Serialize a PrivateMessage into bytes
    private byte[] serializeMessage(Message message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private Message deserializeMessage(byte[] messageBytes) {
        try {
            return objectMapper.readValue(messageBytes, Message.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message("", "", "", false);
        }
    }
}
