package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import server.messaging.PrivateMessage;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Scanner;


public class WebSocketClient {
    String username = "Not logged in";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String serverUrl = "http://localhost:8080/api/";

    public static void main(String[] args) {
        WebSocketClient client = new WebSocketClient();
        client.connectAndSend();
    }

    public void connectAndSend() {
        performLoginOrRegister();
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        StompSession stompSession;
        try {
            stompSession = stompClient.connect("ws://localhost:8080/messages", sessionHandler).get();
            System.out.println("Connected to WebSocket Server");

            // Subscribe to the /topic/messages to receive messages from the server
            stompSession.subscribe("/topic/messages/" + username, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class; // Specify the payload type as byte[]
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    byte[] messageBytes = (byte[]) payload;
                    PrivateMessage message = deserializeMessage(messageBytes);
                    // if the message is from the user, don't print it
                    if (!message.getSender().equals(username) && message.getRecipient().equals(username)) {
                        System.out.println("Received message: " + message.getContent() + " from " + message.getSender());
                    }
                }
            });

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String recipient;
                System.out.println("Enter a message to send (or type 'exit' to quit): ");
                String input = scanner.nextLine();
                System.out.println("Enter the recipient: ");
                recipient = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                PrivateMessage message = new PrivateMessage(username, recipient, input);
                byte[] messageBytes = serializeMessage(message);
                stompSession.send("/app/send-message", messageBytes);
            }
        } catch (Exception e) {
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

    private static class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket Server");
        }
    }

    // Serialize a PrivateMessage into bytes
    private byte[] serializeMessage(PrivateMessage message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private PrivateMessage deserializeMessage(byte[] messageBytes) {
        try {
            return objectMapper.readValue(messageBytes, PrivateMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new PrivateMessage("", "", "");
        }
    }
}
