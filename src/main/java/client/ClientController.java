package client;

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
import java.util.List;
import java.util.Scanner;

public class ClientController {
    private StompSession stompSession;
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final int port = 8080;

    private String serverUrl = "http://localhost:" + port + "/api/";

    public ClientController(){
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        try{
            stompSession = stompClient.connect("ws://localhost:" + port + "/ws", sessionHandler).get();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void performLoginOrRegister() {
        Scanner scanner = new Scanner(System.in);

        // Perform login or register based on user input
        while (true) {
            System.out.println("Login or Register? (or type 'anonymous' to continue as anonymous user)");
            String choice = scanner.nextLine();

            if ("login".equalsIgnoreCase(choice)) {
                System.out.println("Enter your username: ");
                String username = scanner.nextLine();
                System.out.println("Enter your password: ");
                String password = scanner.nextLine();
                login(username, password);
                this.username = username;
                break;
            } else if ("register".equalsIgnoreCase(choice)) {
                System.out.println("Enter a new username: ");
                String username = scanner.nextLine();
                System.out.println("Enter a password: ");
                String password = scanner.nextLine();
                register(username, password);
                this.username = username;
                login(username, password);
                break;
            } else if ("anonymous".equalsIgnoreCase(choice)) {
                this.username = "anonymous";
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private void login(String username, String password) {
        String loginUrl = serverUrl + "login?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(loginUrl, HttpMethod.POST, null, String.class);
        System.out.println(responseEntity.getBody());
    }

    private void register(String username, String password) {
        String registerUrl = serverUrl + "register?username=" + username + "&password=" + password;
        ResponseEntity<String> responseEntity = restTemplate.exchange(registerUrl, HttpMethod.POST, null, String.class);
        System.out.println(responseEntity.getBody());
    }
    private void CommandsInterface(){
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("What do you wan't to do do ? : ");
            String command = scanner.nextLine();
            String[] commandSplit = command.split(" ");
            switch (command){
                case "/help" ->help();
                case "/private_chat" -> private_chat(commandSplit[1]);
                case "/create_group" -> create_group(commandSplit[1], false);
                case "/join_group" -> join_group(commandSplit[1], false);
                case "/msg_group" -> msg_group(commandSplit[1], commandSplit[2]);
                case "/create_secure_group" -> create_group(commandSplit[1], true);
                case "/join_secure_group" -> join_group(commandSplit[1], true);
                case "/online_users" -> online_users();
            }
        }
    }

    private void ConnectAndRecieve(){
        try{
            stompSession.subscribe("/topic/messages/" + username, new StompFrameHandler(){
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class; // Specify the payload type as byte[]
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    byte[] messageBytes = (byte[]) payload;
                    Message message = deserializeMessage(messageBytes);
                    // if the message is from the user, don't print it
                    if (!message.getSender().equals(username) && message.getRecipient().equals(username)) {
                        System.out.println("Received message: " + message.getContent() + " from " + message.getSender());
                    }
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void help(){

    }

    private void online_users(){

    }

    private void private_chat(String recipient){

    }
    private void accept(){

    }
    private void decline(){

    }
    private void create_group(String group_name, boolean isSecure){
        String url;
        if(isSecure){
            url = serverUrl + "create-group?groupName=" + group_name + "&isSecure=" + isSecure;
        }else{
            url = serverUrl + "create-group?groupName=" + group_name;
        }
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, null, String.class);
    }
    private void join_group(String group_name, boolean isSecure){

    }
    private void msg_group(String group_name, String content){

    }


    private void SendPrivateMessage(String recipient, String content){
        Message message = new Message(username, recipient, content);
        byte[] bytesMessage = serializeMessage(message);
        stompSession.send("/app/send-message/" + recipient, bytesMessage);
    }

    private void SendGroupMessage(String groupName, String content){
        Message message = new Message(username, groupName, content);
        byte[] bytesMessage = serializeMessage(message);
        stompSession.send("/app/send-group-message/" + groupName, bytesMessage);
    }

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
            return new Message("", "", "");
        }
    }
    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket Server");
        }
    }
}
