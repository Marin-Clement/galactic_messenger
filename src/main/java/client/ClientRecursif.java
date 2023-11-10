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
import java.util.Scanner;
import java.util.Stack;

public class ClientRecursif {
    private StompSession stompSession;
    Scanner scanner = new Scanner(System.in);
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private final int port = 8080;

    private Stack<Message> messageStack = new Stack<>();
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
                    messageStack.push(message);
                }
            });
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
    private void commandsInterface(){

    }

    private void help(){
        System.out.println("you just lunch help");
    }

    private void online_users(){

    }
    private void request_private_chat(String recipient){
        SendPrivateMessage(recipient, "/private_chat");
        System.out.println("Private chat request sent to " + recipient);
    }
    private void private_chat(String recipient){
        System.out.println("Private chat created with " + recipient);
        String content = scanner.nextLine();
        SendPrivateMessage(recipient, content);
        private_chat(recipient);
    }
    private void accept_decline(String sender){
        Scanner scanner = new Scanner(System.in);
        System.out.println("You recieved a private chat request from " + sender);
        System.out.println("You can accept or decline with '/accept' or '/decline'.");
        System.out.println("Private chat request accepted.");
        SendPrivateMessage(sender, "/accept");
        private_chat(sender);
        scanner.nextLine();
        String input = scanner.nextLine();
        System.out.println("blyat");
        System.out.println(input);
        if (input.equals("/accept")) {
            System.out.println("Private chat request accepted.");
            SendPrivateMessage(sender, "/accept");
            private_chat(sender);
        } else {
            System.out.println("Private chat request declined.");
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

    private void SendPrivateMessage(String recipient, String content){
        System.out.println("Content : " + content + " sent to : " + recipient);
        Message message = new Message(username, recipient, content);
        byte[] bytesMessage = serializePrivateMessage(message);
        stompSession.send("/app/send-message", bytesMessage);
    }

    private void SendGroupPrivateMessage(String groupName, String content){
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
}