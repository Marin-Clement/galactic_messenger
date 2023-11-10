package client;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import server.messaging.Message;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Scanner;

public class ClientController {
    private StompSession stompSession;
    Scanner scanner = new Scanner(System.in);
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final int port = 8080;
    private String[] messagingStatus = {"inactive", ""};
    private String status = "";
    private String serverUrl = "http://localhost:" + port + "/api/";

    public static void main(String[] args) {
        ClientController client = new ClientController();
        client.connectAndRecieve();
    }
    public void connectAndRecieve() {
        performLoginOrRegister();
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        try {
            stompSession = stompClient.connectAsync("ws://localhost:" + port + "/ws", sessionHandler).get();

            // Subscribe to the /topic/PrivateMessages to receive PrivateMessages from the server
            stompSession.subscribe("/topic/messages/" + username, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class; // Specify the payload type as byte[]
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    byte[] MessageBytes = (byte[]) payload;
                    Message message = deserializePrivateMessage(MessageBytes);
                    System.out.println(message.getSender() + " : " + message.getContent());
                    switch (message.getContent()){
                        case "/private_chat" -> {accept_decline(message.getSender()); commandsInterface();}
                        case "/accept" -> {System.out.println(message.getSender() + " accepted your private chat request.");
                                            sendmessage(message.getSender(), "/accept");
                                            changeMessagingStatus("Active", message.getSender());}
                        case "/decline" -> System.out.println(message.getSender() + " declined your private chat request.");
                    }
                    // if the PrivateMessage is from the user, don't print it
                    if (message.getSender().equals(messagingStatus[1])) {
                        System.out.println(message.getSender() + " : " + message.getContent());
                    }
                }
            });
            
//            Scanner scanner = new Scanner(System.in);
//            while (true) {
//                String recipient;
//                System.out.println("Enter a Message to send (or type 'exit' to quit): ");
//                String input = scanner.nextLine();
//                if (input.equalsIgnoreCase("exit")) {
//                    break;
//                }
//                System.out.println("Enter the recipient: ");
//                recipient = scanner.nextLine();
//                sendmessage(recipient, input);
//            }

            commandsInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket Server");
        }
    }

    private void performLoginOrRegister() {
        // Perform login or register based on user input
        System.out.println("/Login or /Register? ");
        String choice = scanner.nextLine();

        if ("/login".equalsIgnoreCase(choice)) {
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();
            System.out.println("Enter your password: ");
            String password = scanner.nextLine();
            login(username, password);
            this.username = username;
        } else if ("/register".equalsIgnoreCase(choice)) {
            System.out.println("Enter a new username: ");
            String username = scanner.nextLine();
            System.out.println("Enter a new password: ");
            String password = scanner.nextLine();
            register(username, password);
            this.username = username;
            login(username, password);
        } else {
            System.out.println("Invalid choice.");
            performLoginOrRegister();
        }
    }

    private boolean login(String username, String password) {
        String loginUrl = serverUrl + "login?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, null, String.class);
        System.out.println(responseEntity.getBody());
        if (responseEntity.getBody().equals("Login successful.")){
            return true;
        } else{
            return false;
        }
    }

    private boolean register(String username, String password) {
        String registerUrl = serverUrl + "register?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(registerUrl, HttpMethod.POST, null, String.class);
        System.out.println(responseEntity.getBody());
        if (responseEntity.getBody().equals("Registration successful.")){
            return true;
        } else{
            boolean isLoginValid = login(username, password);
            if (isLoginValid){
                System.out.println("Login successful.");
                return true;
            } else {
                System.out.println("Login failed.");
                return false;
            }
        }
    }
    private void commandsInterface(){
        if (messagingStatus[0].equals("Active")){
            private_chat_interface(messagingStatus[1]);
            commandsInterface();
        } else {
            System.out.println("Commands Interface : ");
            String input = scanner.nextLine();
            String[] commandSplit = input.split(" ");

            String command = commandSplit[0];
            String commandArg1;
            String commandArg2;
            if (commandSplit.length > 2){
                commandArg1 = commandSplit[1];
                commandArg2 = commandSplit[2];
            } else if (commandSplit.length > 1) {
                commandArg1 = commandSplit[1];
                commandArg2 = "";
            }else {
                commandArg1 = "";
                commandArg2 = "";
            }
            switch (command){
                case "/help" ->help();
                case "/private_chat" -> {request_private_chat(commandArg1); commandsInterface();}
                case "/create_group" -> {create_group(commandArg1, false); commandsInterface();}
                case "/join_group" -> {join_group(commandArg1, false); commandsInterface();}
                case "/msg_group" -> {msg_group(commandArg1, commandArg2); commandsInterface();}
                case "/create_secure_group" -> {create_group(commandArg1, true); commandsInterface();}
                case "/join_secure_group" -> {join_group(commandArg1, true); commandsInterface();}
                case "/online_users" -> {online_users(); commandsInterface();}
                default -> {System.out.println("Invalid choice.");
                    help();
                    commandsInterface();}
            }
        }
    }

    private void help(){
        System.out.println("you just lunch help");
    }

    private void online_users(){

    }
    private void request_private_chat(String recipient){
        sendmessage(recipient, "/private_chat");
        System.out.println("Private chat request sent to " + recipient);

    }
    private void private_chat_interface(String recipient){
        String content = scanner.nextLine();
        if (content.equals("/exit") || content.equals("/quit")){
            changeMessagingStatus("", "");
        } else {
            sendmessage(recipient, content);
        }
    }
    private void accept_decline(String sender){
        Scanner scanner = new Scanner(System.in);
        System.out.println("You recieved a private chat request from " + sender);
        System.out.println("You can accept or decline with '/accept' or '/decline'.");
        String input = scanner.nextLine();
        if (input.equals("/accept")) {
            System.out.println("Private chat request accepted.");
            sendmessage(sender, "/accept");
            changeMessagingStatus("Active", sender);
        } else if (input.equals("/decline")) {
            System.out.println("Private chat request declined.");
            sendmessage(sender, "/decline");
        } else {
            System.out.println("Invalid choice. (accept / decline)");
            accept_decline(sender);
        }
    }
    private void create_group(String group_name, boolean isSecure){
        if (group_name.equals("")){
            help();
            return;
        }
        String url;
        if(isSecure){
            url = serverUrl + "create-group?groupName=" + group_name + "&isSecure=" + isSecure;
        }else{
            url = serverUrl + "create-group?groupName=" + group_name;
        }
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
        System.out.println(responseEntity.getBody());
    }
    private void join_group(String group_name, boolean isSecure){
        if (group_name.equals("")){
            help();
            return;
        }
        
    }
    private void msg_group(String group_name, String content){

    }

    private void sendmessage(String recipient, String content){
        System.out.println("Private Message methods : content : " + content + " to : " + recipient);
        Message message = new Message(username, recipient, content);
        byte[] bytesMessage = serializePrivateMessage(message);
        stompSession.send("/app/send-message", bytesMessage);
    }

    private void sendGroupPrivateMessage(String groupName, String content){
        Message PrivateMessage = new Message(username, groupName, content);
        byte[] bytesPrivateMessage = serializePrivateMessage(PrivateMessage);
        stompSession.send("/app/send-group-PrivateMessage/" + groupName, bytesPrivateMessage);
    }

    private byte[] serializePrivateMessage(Message PrivateMessage) {
        try {
            return objectMapper.writeValueAsBytes(PrivateMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private Message deserializePrivateMessage(byte[] PrivateMessageBytes) {
        try {
            return objectMapper.readValue(PrivateMessageBytes, Message.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Message("", "", "");
        }
    }
    private void changeMessagingStatus (String status, String sender){
        messagingStatus[0] = status;
        messagingStatus[1] = sender;
    }
}
